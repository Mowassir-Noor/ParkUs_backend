package com.gazi.ParkUs.services;

import com.gazi.ParkUs.User.UserRole;
import com.gazi.ParkUs.dto.BookingRequestDto;
import com.gazi.ParkUs.dto.BookingResponseDto;
import com.gazi.ParkUs.entities.*;
import com.gazi.ParkUs.exceptions.BookingConflictException;
import com.gazi.ParkUs.exceptions.InvalidRequestException;
import com.gazi.ParkUs.exceptions.ResourceNotFoundException;
import com.gazi.ParkUs.exceptions.UnauthorizedException;
import com.gazi.ParkUs.repositories.*;
import com.gazi.ParkUs.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final SpotAvailabilityRepository availabilityRepo;
    private final UserRepository userRepo;
    private final BookingRepository bookingRepo;
    private final BookingLogRepository bookingLogRepo;

    public BookingServiceImpl(
            SpotAvailabilityRepository availabilityRepo,
            UserRepository userRepo,
            BookingRepository bookingRepo,
            BookingLogRepository bookingLogRepo
    ) {
        this.availabilityRepo = availabilityRepo;
        this.userRepo = userRepo;
        this.bookingRepo = bookingRepo;
        this.bookingLogRepo = bookingLogRepo;
    }

    @Override
    public BookingResponseDto createBooking(BookingRequestDto dto) {
        // Get authenticated user
        String currentUserEmail = SecurityUtils.currentUserEmail();
        UserEntity authenticatedUser = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        // Verify the authenticated user is the renter
        if (!isAdmin(authenticatedUser) && !authenticatedUser.getUserId().equals(dto.getRenterId())) {
            throw new UnauthorizedException("You can only create bookings for yourself");
        }

        // Lock the availability row to prevent race conditions
        SpotAvailability availability = availabilityRepo.lockById(dto.getAvailabilityId())
            .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        // Check if already booked (with lock)
        if (Boolean.TRUE.equals(availability.getIsBooked())) {
            throw new BookingConflictException("This time slot is already booked");
        }

        // Validate time window
        if (availability.getStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("Cannot book a time slot in the past");
        }

        if (availability.getEndTime().isBefore(availability.getStartTime())) {
            throw new InvalidRequestException("End time must be after start time");
        }

        UserEntity renter = userRepo.findById(dto.getRenterId())
                .orElseThrow(() -> new ResourceNotFoundException("Renter not found"));

        // Calculate hours and total amount
        long hours = ChronoUnit.HOURS.between(
                availability.getStartTime(),
                availability.getEndTime()
        );

        if (hours <= 0) {
            throw new InvalidRequestException("Booking duration must be at least 1 hour");
        }

        BigDecimal total = availability.getSpot()
                .getPricePerHour()
                .multiply(BigDecimal.valueOf(hours));

        // Create booking
        Booking booking = new Booking();
        booking.setSpot(availability.getSpot());
        booking.setAvailability(availability);
        booking.setRenter(renter);
        booking.setStatus("confirmed");
        booking.setTotalAmount(total);

        // Mark availability as booked (within the same transaction)
        availability.setIsBooked(true);

        bookingRepo.save(booking);
        availabilityRepo.save(availability);

        log(booking);

        return toDto(booking);
    }

    @Override
    public BookingResponseDto getBookingById(Long id) {
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Verify authorization - user can only see their own bookings or bookings on their spots
        String currentUserEmail = SecurityUtils.currentUserEmail();
        UserEntity authenticatedUser = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        boolean isRenter = booking.getRenter().getUserId().equals(authenticatedUser.getUserId());
        boolean isOwner = booking.getSpot().getOwner().getUserId().equals(authenticatedUser.getUserId());

        if (!isAdmin(authenticatedUser) && !isRenter && !isOwner) {
            throw new UnauthorizedException("You don't have permission to view this booking");
        }

        return toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingsByRenter(Long renterId) {
        // Verify authorization
        String currentUserEmail = SecurityUtils.currentUserEmail();
        UserEntity authenticatedUser = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        if (!isAdmin(authenticatedUser) && !authenticatedUser.getUserId().equals(renterId)) {
            throw new UnauthorizedException("You can only view your own bookings");
        }

        return bookingRepo.findByRenter_UserId(renterId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long ownerId) {
        // Verify authorization
        String currentUserEmail = SecurityUtils.currentUserEmail();
        UserEntity authenticatedUser = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        if (!isAdmin(authenticatedUser) && !authenticatedUser.getUserId().equals(ownerId)) {
            throw new UnauthorizedException("You can only view bookings for your own spots");
        }

        return bookingRepo.findBySpot_Owner_UserId(ownerId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void updateStatus(Long bookingId, String status) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Verify authorization - only spot owner can update status
        String currentUserEmail = SecurityUtils.currentUserEmail();
        UserEntity authenticatedUser = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        if (!isAdmin(authenticatedUser) && !booking.getSpot().getOwner().getUserId().equals(authenticatedUser.getUserId())) {
            throw new UnauthorizedException("Only the spot owner can update booking status");
        }

        String newStatus = status.toLowerCase();
        List<String> validStatuses = List.of("pending", "confirmed", "cancelled", "completed");
        if (!validStatuses.contains(newStatus)) {
            throw new InvalidRequestException("Invalid status. Must be one of: " + String.join(", ", validStatuses));
        }

        String currentStatus = booking.getStatus().toLowerCase();
        boolean allowed = switch (currentStatus) {
            case "pending" -> List.of("confirmed", "cancelled").contains(newStatus);
            case "confirmed" -> List.of("completed", "cancelled").contains(newStatus);
            default -> false; // completed/cancelled are terminal
        };

        if (!allowed) {
            throw new InvalidRequestException("Invalid transition from " + currentStatus + " to " + newStatus);
        }

        booking.setStatus(newStatus);

        // If cancelled, free the slot
        if ("cancelled".equals(newStatus) && Boolean.TRUE.equals(booking.getAvailability().getIsBooked())) {
            booking.getAvailability().setIsBooked(false);
            availabilityRepo.save(booking.getAvailability());
        }

        bookingRepo.save(booking);
        log(booking);
    }

    @Override
    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Verify authorization - only renter can cancel their booking
        String currentUserEmail = SecurityUtils.currentUserEmail();
        UserEntity authenticatedUser = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        boolean isAdmin = isAdmin(authenticatedUser);
        if (!isAdmin && !booking.getRenter().getUserId().equals(authenticatedUser.getUserId())) {
            throw new UnauthorizedException("You can only cancel your own bookings");
        }

        if (!isAdmin) {
            LocalDateTime now = LocalDateTime.now();
            boolean hasStarted = !booking.getAvailability().getStartTime().isAfter(now);
            if (hasStarted) {
                throw new InvalidRequestException("Cannot cancel a booking that has started or finished");
            }

            String currentStatus = booking.getStatus().toLowerCase();
            if (!List.of("pending", "confirmed").contains(currentStatus)) {
                throw new InvalidRequestException("Only pending or confirmed bookings can be cancelled");
            }
        }

        booking.setStatus("cancelled");
        booking.getAvailability().setIsBooked(false);

        availabilityRepo.save(booking.getAvailability());
        bookingRepo.save(booking);
        log(booking);
    }

    private boolean isAdmin(UserEntity user) {
        return user.getRole() == UserRole.ROLE_ADMIN;
    }

    // ------------------------
    // Helpers
    // ------------------------

    private BookingResponseDto toDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(booking.getBookingId());
        dto.setSpotId(booking.getSpot().getSpotId());
        dto.setRenterId(booking.getRenter().getUserId());
        dto.setOwnerId(booking.getSpot().getOwner().getUserId());
        dto.setStatus(booking.getStatus());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStartTime(booking.getAvailability().getStartTime());
        dto.setEndTime(booking.getAvailability().getEndTime());

        return dto;
    }

    private void log(Booking booking) {
        SpotAvailability a = booking.getAvailability();

        BookingLog log = new BookingLog();
        log.setBookingId(booking.getBookingId());
        log.setSpotId(booking.getSpot().getSpotId());
        log.setOwnerId(booking.getSpot().getOwner().getUserId());
        log.setRenterId(booking.getRenter().getUserId());
        log.setStartTime(a.getStartTime());
        log.setEndTime(a.getEndTime());
        log.setDurationHours(
                BigDecimal.valueOf(
                        ChronoUnit.HOURS.between(a.getStartTime(), a.getEndTime())
                )
        );
        log.setTotalAmount(booking.getTotalAmount());
        log.setBookingStatus(booking.getStatus());

        bookingLogRepo.save(log);
    }
}
