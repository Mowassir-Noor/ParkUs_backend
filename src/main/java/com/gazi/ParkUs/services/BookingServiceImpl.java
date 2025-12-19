package com.gazi.ParkUs.services;

import com.gazi.ParkUs.User.User;
import com.gazi.ParkUs.dto.BookingRequestDto;
import com.gazi.ParkUs.dto.BookingResponseDto;
import com.gazi.ParkUs.entities.*;
import com.gazi.ParkUs.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

        SpotAvailability availability = availabilityRepo.findById(dto.getAvailabilityId())
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (availability.getIsBooked()) {
            throw new RuntimeException("Already booked");
        }

        UserEntity renter = userRepo.findById(dto.getRenterId())
                .orElseThrow(() -> new RuntimeException("Renter not found"));

        long hours = ChronoUnit.HOURS.between(
                availability.getStartTime(),
                availability.getEndTime()
        );

        if (hours <= 0) {
            throw new RuntimeException("Invalid time window");
        }

        BigDecimal total = availability.getSpot()
                .getPricePerHour()
                .multiply(BigDecimal.valueOf(hours));

        Booking booking = new Booking();
        booking.setSpot(availability.getSpot());
        booking.setAvailability(availability);
        booking.setRenter(renter);
        booking.setStatus("confirmed");
        booking.setTotalAmount(total);

        availability.setIsBooked(true);

        bookingRepo.save(booking);
        availabilityRepo.save(availability);

        log(booking);

        return toDto(booking);
    }

    @Override
    public BookingResponseDto getBookingById(Long id) {
        return bookingRepo.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Override
    public List<BookingResponseDto> getBookingsByRenter(Long renterId) {
        return bookingRepo.findByRenter_UserId(renterId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long ownerId) {
        return bookingRepo.findBySpot_Owner_UserId(ownerId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void updateStatus(Long bookingId, String status) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(status);
        bookingRepo.save(booking);

        log(booking);
    }

    @Override
    public void deleteBooking(Long bookingId) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

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
