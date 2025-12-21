package com.gazi.ParkUs.controller;

import com.gazi.ParkUs.dto.BookingResponseDto;
import com.gazi.ParkUs.dto.ParkingSpotResponseDto;
import com.gazi.ParkUs.dto.SpotAvailabilityResponseDto;
import com.gazi.ParkUs.dto.UserResponseDto;
import com.gazi.ParkUs.entities.Booking;
import com.gazi.ParkUs.entities.ParkingSpot;
import com.gazi.ParkUs.entities.SpotAvailability;
import com.gazi.ParkUs.repositories.BookingRepository;
import com.gazi.ParkUs.repositories.ParkingSpotRepository;
import com.gazi.ParkUs.repositories.SpotAvailabilityRepository;
import com.gazi.ParkUs.repositories.UserRepository;
import com.gazi.ParkUs.services.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

	private final UserRepository userRepo;
	private final BookingRepository bookingRepo;
	private final ParkingSpotRepository spotRepo;
	private final SpotAvailabilityRepository availabilityRepo;
	private final BookingService bookingService;

	public AdminController(
			UserRepository userRepo,
			BookingRepository bookingRepo,
			ParkingSpotRepository spotRepo,
			SpotAvailabilityRepository availabilityRepo,
			BookingService bookingService
	) {
		this.userRepo = userRepo;
		this.bookingRepo = bookingRepo;
		this.spotRepo = spotRepo;
		this.availabilityRepo = availabilityRepo;
		this.bookingService = bookingService;
	}

	@GetMapping("/users")
		    public ResponseEntity<Page<UserResponseDto>> listUsers(
			    @PageableDefault(size = 20, sort = "registrationDate", direction = Sort.Direction.DESC) Pageable pageable
		    ) {
			Page<UserResponseDto> users = userRepo.findAll(pageable)
				.map(u -> new UserResponseDto(
					u.getFirstName(),
					u.getLastName(),
					u.getEmail(),
					u.getRole(),
					u.getRegistrationDate()));
			return ResponseEntity.ok(users);
	}

	@GetMapping("/bookings")
		    public ResponseEntity<Page<BookingResponseDto>> listBookings(
			    @PageableDefault(size = 20, sort = "bookedAt", direction = Sort.Direction.DESC) Pageable pageable
		    ) {
			Page<BookingResponseDto> bookings = bookingRepo.findAll(pageable)
				.map(this::toBookingDto);
			return ResponseEntity.ok(bookings);
	}

	@PatchMapping("/bookings/{id}/status")
	public ResponseEntity<Void> adminUpdateStatus(
			@PathVariable Long id,
			@RequestParam String status
	) {
		bookingService.updateStatus(id, status);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/bookings/{id}")
	public ResponseEntity<Void> adminCancelBooking(@PathVariable Long id) {
		bookingService.deleteBooking(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/spots")
		    public ResponseEntity<Page<ParkingSpotResponseDto>> listSpots(
			    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
		    ) {
			Page<ParkingSpotResponseDto> spots = spotRepo.findAll(pageable)
				.map(this::toSpotDto);
			return ResponseEntity.ok(spots);
	}

	@GetMapping("/availability")
		    public ResponseEntity<Page<SpotAvailabilityResponseDto>> listAvailability(
			    @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable
		    ) {
			Page<SpotAvailabilityResponseDto> availabilities = availabilityRepo.findAll(pageable)
				.map(this::toAvailabilityDto);
			return ResponseEntity.ok(availabilities);
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
}
