package com.gazi.ParkUs.services;

import com.gazi.ParkUs.dto.BookingRequestDto;
import com.gazi.ParkUs.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto dto);

    BookingResponseDto getBookingById(Long id);

    List<BookingResponseDto> getBookingsByRenter(Long renterId);

    List<BookingResponseDto> getBookingsByOwner(Long ownerId);

    void updateStatus(Long bookingId, String status);

    void deleteBooking(Long bookingId);
}

