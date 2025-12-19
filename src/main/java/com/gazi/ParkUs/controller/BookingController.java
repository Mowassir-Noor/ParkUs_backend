package com.gazi.ParkUs.controller;

import com.gazi.ParkUs.dto.BookingRequestDto;
import com.gazi.ParkUs.dto.BookingResponseDto;
import com.gazi.ParkUs.services.BookingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingServiceImpl bookingServiceImpl;

    // CREATE booking
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestBody BookingRequestDto dto
    ) {
        return ResponseEntity.ok(bookingServiceImpl.createBooking(dto));
    }

    // GET booking by id
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(bookingServiceImpl.getBookingById(id));
    }

    // GET bookings by renter
    @GetMapping("/renter/{renterId}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByRenter(
            @PathVariable Long renterId
    ) {
        return ResponseEntity.ok(
                bookingServiceImpl.getBookingsByRenter(renterId)
        );
    }

    // GET bookings for owner (all bookings on owner’s spots)
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByOwner(
            @PathVariable Long ownerId
    ) {
        return ResponseEntity.ok(
                bookingServiceImpl.getBookingsByOwner(ownerId)
        );
    }

    // UPDATE booking status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        bookingServiceImpl.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    // DELETE booking
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long id
    ) {
        bookingServiceImpl.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
