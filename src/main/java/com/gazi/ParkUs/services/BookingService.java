package com.gazi.ParkUs.services;

import com.gazi.ParkUs.entities.Booking;
import com.gazi.ParkUs.entities.BookingLog;
import com.gazi.ParkUs.entities.ParkingSpot;
import com.gazi.ParkUs.entities.SpotAvailability;
import com.gazi.ParkUs.repositories.*;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BookingService {

    private final SpotAvailabilityRepository availabilityRepo;
    private final ParkingSpotRepository parkingSpotRepository;
    private final UserRepository userRepo;
    private final BookingRepository bookingRepo;
    private final BookingLogRepository bookingLogRepo;

    public BookingService(SpotAvailabilityRepository spotAvailabilityRepository, ParkingSpotRepository parkingSpotRepository, UserRepository userRepository, BookingRepository bookingRepo, BookingLogRepository bookingLogRepo) {
        this.availabilityRepo = spotAvailabilityRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.userRepo = userRepository;

        this.bookingRepo = bookingRepo;
        this.bookingLogRepo = bookingLogRepo;
    }


    @Transactional
    public Booking createBooking (Long availabilityId, Long renterId){

        SpotAvailability availability = availabilityRepo.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (availability.getIsBooked()) {
            throw new RuntimeException("Already booked");
        }

        ParkingSpot spot = availability.getSpot();

        long hours = ChronoUnit.HOURS.between(
                availability.getStartTime(),
                availability.getEndTime()
        );

        if (hours <= 0) {
            throw new RuntimeException("Invalid time window");
        }

        BigDecimal total = spot.getPricePerHour()
                .multiply(BigDecimal.valueOf(hours));

        Booking booking = new Booking();
        booking.setSpot(spot);
        booking.setRenter(userRepo.getReferenceById(renterId));
        booking.setAvailability(availability);
        booking.setStatus("confirmed");
        booking.setTotalAmount(total);

        availability.setIsBooked(true);

        bookingRepo.save(booking);
        availabilityRepo.save(availability);

        log(booking);

        return booking;
    }
    public Booking getBooking(Long bookingId) {
        return bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
    public List<Booking> getBookingsByRenter(Long renterId) {
        return bookingRepo.findByRenter_UserId(renterId);
    }
    public List<Booking> getBookingsForOwner(Long ownerId) {
        return bookingRepo.findBySpot_Owner_UserId(ownerId);
    }
    @Transactional
    public Booking updateStatus(Long bookingId, String newStatus) {

        Booking booking = getBooking(bookingId);

        if ("completed".equals(booking.getStatus())) {
            throw new RuntimeException("Completed booking cannot be modified");
        }

        booking.setStatus(newStatus);
        bookingRepo.save(booking);

        log(booking);

        return booking;
    }
    @Transactional
    public void cancelBooking(Long bookingId) {

        Booking booking = getBooking(bookingId);

        if ("completed".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot cancel completed booking");
        }

        booking.setStatus("cancelled");

        SpotAvailability availability = booking.getAvailability();
        availability.setIsBooked(false);

        bookingRepo.save(booking);
        availabilityRepo.save(availability);

        log(booking);
    }
    @Transactional
    public void completeBooking(Long bookingId) {

        Booking booking = getBooking(bookingId);

        if (!"confirmed".equals(booking.getStatus())) {
            throw new RuntimeException("Only confirmed bookings can be completed");
        }

        booking.setStatus("completed");
        bookingRepo.save(booking);

        log(booking);
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
