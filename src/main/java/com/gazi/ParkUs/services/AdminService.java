package com.gazi.ParkUs.services;

import com.gazi.ParkUs.User.UserRole;
import com.gazi.ParkUs.dto.*;
import com.gazi.ParkUs.entities.*;
import com.gazi.ParkUs.exceptions.InvalidRequestException;
import com.gazi.ParkUs.exceptions.ResourceNotFoundException;
import com.gazi.ParkUs.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepo;
    private final BookingRepository bookingRepo;
    private final ParkingSpotRepository spotRepo;
    private final SpotAvailabilityRepository availabilityRepo;
    private final BookingLogRepository bookingLogRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepo,
            BookingRepository bookingRepo,
            ParkingSpotRepository spotRepo,
            SpotAvailabilityRepository availabilityRepo,
            BookingLogRepository bookingLogRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepo = userRepo;
        this.bookingRepo = bookingRepo;
        this.spotRepo = spotRepo;
        this.availabilityRepo = availabilityRepo;
        this.bookingLogRepo = bookingLogRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ============ USER MANAGEMENT ============

    public UserResponseDto getUserById(Long userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserDto(user);
    }

    public UserResponseDto updateUser(Long userId, RegisterUserDto dto) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepo.save(user);
        return toUserDto(user);
    }

    public void deleteUser(Long userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepo.delete(user);
    }

    public void changeUserRole(Long userId, String roleStr) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid role: " + roleStr);
        }

        user.setRole(role);
        userRepo.save(user);
    }

    // ============ BOOKING MANAGEMENT ============

    public BookingResponseDto updateBooking(Long bookingId, BookingRequestDto dto) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Update availability if changed
        if (!booking.getAvailability().getAvailabilityId().equals(dto.getAvailabilityId())) {
            SpotAvailability newAvailability = availabilityRepo.findById(dto.getAvailabilityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

            if (Boolean.TRUE.equals(newAvailability.getIsBooked())) {
                throw new InvalidRequestException("New availability slot is already booked");
            }

            // Unbook old availability
            booking.getAvailability().setIsBooked(false);

            // Book new availability
            newAvailability.setIsBooked(true);
            booking.setAvailability(newAvailability);
        }

        // Update renter if changed
        if (!booking.getRenter().getUserId().equals(dto.getRenterId())) {
            UserEntity newRenter = userRepo.findById(dto.getRenterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Renter not found"));
            booking.setRenter(newRenter);
        }

        // Recalculate total amount
        long hours = ChronoUnit.HOURS.between(
                booking.getAvailability().getStartTime(),
                booking.getAvailability().getEndTime()
        );
        BigDecimal total = booking.getSpot().getPricePerHour().multiply(BigDecimal.valueOf(hours));
        booking.setTotalAmount(total);

        bookingRepo.save(booking);
        logBooking(booking);

        return toBookingDto(booking);
    }

    // ============ PARKING SPOT MANAGEMENT ============

    public ParkingSpotResponseDto createParkingSpot(ParkingSpotRequestDto dto) {
        UserEntity owner = userRepo.findById(dto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        if (!(owner instanceof RegularUser)) {
            throw new InvalidRequestException("Owner must be a regular user");
        }

        ParkingSpot spot = new ParkingSpot();
        spot.setOwner((RegularUser) owner);
        spot.setTitle(dto.getTitle());
        spot.setDescription(dto.getDescription());
        spot.setLocation(dto.getLocation());
        spot.setLatitude(dto.getLatitude());
        spot.setLongitude(dto.getLongitude());
        spot.setSlotType(dto.getSlotType());
        spot.setPricePerHour(dto.getPricePerHour());

        spotRepo.save(spot);
        return toSpotDto(spot);
    }

    public ParkingSpotResponseDto adminUpdateSpot(Long spotId, ParkingSpotRequestDto dto) {
        ParkingSpot spot = spotRepo.findById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking spot not found"));

        spot.setTitle(dto.getTitle());
        spot.setDescription(dto.getDescription());
        spot.setLocation(dto.getLocation());
        spot.setLatitude(dto.getLatitude());
        spot.setLongitude(dto.getLongitude());
        spot.setSlotType(dto.getSlotType());
        spot.setPricePerHour(dto.getPricePerHour());

        // Admin can change owner
        if (dto.getOwnerId() != null && !dto.getOwnerId().equals(spot.getOwner().getUserId())) {
            UserEntity newOwner = userRepo.findById(dto.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
            if (!(newOwner instanceof RegularUser)) {
                throw new InvalidRequestException("Owner must be a regular user");
            }
            spot.setOwner((RegularUser) newOwner);
        }

        spotRepo.save(spot);
        return toSpotDto(spot);
    }

    public void adminDeleteSpot(Long spotId) {
        ParkingSpot spot = spotRepo.findById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking spot not found"));
        spotRepo.delete(spot);
    }

    // ============ AVAILABILITY MANAGEMENT ============

    public SpotAvailabilityResponseDto createAvailability(SpotAvailabilityRequestDto dto) {
        ParkingSpot spot = spotRepo.findById(dto.getSpotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking spot not found"));

        SpotAvailability availability = new SpotAvailability();
        availability.setSpot(spot);
        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());
        availability.setIsBooked(false);

        availabilityRepo.save(availability);
        return toAvailabilityDto(availability);
    }

    public SpotAvailabilityResponseDto updateAvailability(Long availabilityId, SpotAvailabilityRequestDto dto) {
        SpotAvailability availability = availabilityRepo.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());

        availabilityRepo.save(availability);
        return toAvailabilityDto(availability);
    }

    public void deleteAvailability(Long availabilityId) {
        SpotAvailability availability = availabilityRepo.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        if (Boolean.TRUE.equals(availability.getIsBooked())) {
            throw new InvalidRequestException("Cannot delete booked availability. Cancel the booking first.");
        }

        availabilityRepo.delete(availability);
    }

    // ============ HELPER METHODS ============

    private UserResponseDto toUserDto(UserEntity user) {
        return new UserResponseDto(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getRegistrationDate()
        );
    }

    private BookingResponseDto toBookingDto(Booking booking) {
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

    private ParkingSpotResponseDto toSpotDto(ParkingSpot spot) {
        ParkingSpotResponseDto dto = new ParkingSpotResponseDto();
        dto.setSpotId(spot.getSpotId());
        dto.setTitle(spot.getTitle());
        dto.setDescription(spot.getDescription());
        dto.setLocation(spot.getLocation());
        dto.setLatitude(spot.getLatitude());
        dto.setLongitude(spot.getLongitude());
        dto.setSlotType(spot.getSlotType());
        dto.setPricePerHour(spot.getPricePerHour());
        dto.setOwnerId(spot.getOwner().getUserId());
        return dto;
    }

    private SpotAvailabilityResponseDto toAvailabilityDto(SpotAvailability availability) {
        SpotAvailabilityResponseDto dto = new SpotAvailabilityResponseDto();
        dto.setAvailabilityId(availability.getAvailabilityId());
        dto.setSpotId(availability.getSpot().getSpotId());
        dto.setStartTime(availability.getStartTime());
        dto.setEndTime(availability.getEndTime());
        dto.setIsBooked(availability.getIsBooked());
        return dto;
    }

    private void logBooking(Booking booking) {
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
