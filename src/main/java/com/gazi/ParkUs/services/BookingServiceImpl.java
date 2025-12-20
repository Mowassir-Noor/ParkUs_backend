package com.gazi.ParkUs.services;

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
        if (!authenticatedUser.getUserId().equals(dto.getRenterId())) {
            throw new UnauthorizedException("You can only create bookings for yourself");
        }

        // Lock the availability row to prevent race conditions
        SpotAvailability availability = availabilityRepo.findById(dto.getAvailabilityId())
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

        if (!isRenter && !isOwner) {
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

        if (!authenticatedUser.getUserId().equals(renterId)) {
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

        if (!authenticatedUser.getUserId().equals(ownerId)) {
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

        if (!booking.getSpot().getOwner().getUserId().equals(authenticatedUser.getUserId())) {
            throw new UnauthorizedException("Only the spot owner can update booking status");
        }

        // Validate status
        List<String> validStatuses = List.of("pending", "confirmed", "cancelled", "completed");
        if (!validStatuses.contains(status.toLowerCase())) {
            throw new InvalidRequestException("Invalid status. Must be one of: " + String.join(", ", validStatuses));
        }

        booking.setStatus(status);
        bookingRepo.save(booking);

        log(booking);
    }

    @Override
    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Verify authorization - only renter can delete their booking
        String currentUserEmail = SecurityUtils.currentUserEmail();
        UserEntity authenticatedUser = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        if (!booking.getRenter().getUserId().equals(authenticatedUser.getUserId())) {
            throw new UnauthorizedException("You can only delete your own bookings");
        }

        // Free up the availability
        SpotAvailability availability = booking.getAvailability();
        availability.setIsBooked(false);
        availabilityRepo.save(availability);

        bookingRepo.delete(booking);

        log(booking);
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
