package com.gazi.ParkUs.controller;

import com.gazi.ParkUs.dto.*;
import com.gazi.ParkUs.entities.Booking;
import com.gazi.ParkUs.entities.ParkingSpot;
import com.gazi.ParkUs.entities.SpotAvailability;
import com.gazi.ParkUs.entities.UserEntity;
import com.gazi.ParkUs.repositories.BookingRepository;
import com.gazi.ParkUs.repositories.ParkingSpotRepository;
import com.gazi.ParkUs.repositories.SpotAvailabilityRepository;
import com.gazi.ParkUs.repositories.UserRepository;
import com.gazi.ParkUs.services.*;
import jakarta.validation.Valid;
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
	private final ParkingSpotService parkingSpotService;
	private final SpotAvailabilityService availabilityService;
	private final AdminService adminService;

	public AdminController(
			UserRepository userRepo,
			BookingRepository bookingRepo,
			ParkingSpotRepository spotRepo,
			SpotAvailabilityRepository availabilityRepo,
			BookingService bookingService,
			ParkingSpotService parkingSpotService,
			SpotAvailabilityService availabilityService,
			AdminService adminService
	) {
		this.userRepo = userRepo;
		this.bookingRepo = bookingRepo;
		this.spotRepo = spotRepo;
		this.availabilityRepo = availabilityRepo;
		this.bookingService = bookingService;
		this.parkingSpotService = parkingSpotService;
		this.availabilityService = availabilityService;
		this.adminService = adminService;
	}

	// ============ USER MANAGEMENT ============
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

	@GetMapping("/users/{id}")
	public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
		return ResponseEntity.ok(adminService.getUserById(id));
	}

	@PatchMapping("/users/{id}")
	public ResponseEntity<UserResponseDto> updateUser(
			@PathVariable Long id,
			@Valid @RequestBody RegisterUserDto dto
	) {
		return ResponseEntity.ok(adminService.updateUser(id, dto));
	}

	@DeleteMapping("/users/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		adminService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/users/{id}/role")
	public ResponseEntity<Void> changeUserRole(
			@PathVariable Long id,
			@RequestParam String role
	) {
		adminService.changeUserRole(id, role);
		return ResponseEntity.noContent().build();
	}

	// ============ BOOKING MANAGEMENT ============
	@GetMapping("/bookings")
	public ResponseEntity<Page<BookingResponseDto>> listBookings(
			@PageableDefault(size = 20, sort = "bookedAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<BookingResponseDto> bookings = bookingRepo.findAll(pageable)
				.map(this::toBookingDto);
		return ResponseEntity.ok(bookings);
	}

	@GetMapping("/bookings/{id}")
	public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable Long id) {
		return ResponseEntity.ok(bookingService.getBookingById(id));
	}

	@PostMapping("/bookings")
	public ResponseEntity<BookingResponseDto> createBookingAsAdmin(
			@Valid @RequestBody BookingRequestDto dto
	) {
		return ResponseEntity.ok(bookingService.createBooking(dto));
	}

	@PatchMapping("/bookings/{id}")
	public ResponseEntity<BookingResponseDto> updateBooking(
			@PathVariable Long id,
			@Valid @RequestBody BookingRequestDto dto
	) {
		return ResponseEntity.ok(adminService.updateBooking(id, dto));
	}

	@PatchMapping("/bookings/{id}/status")
	public ResponseEntity<Void> updateBookingStatus(
			@PathVariable Long id,
			@RequestParam String status
	) {
		bookingService.updateStatus(id, status);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/bookings/{id}")
	public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
		bookingService.deleteBooking(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/bookings/status/{status}")
	public ResponseEntity<List<BookingResponseDto>> getBookingsByStatus(@PathVariable String status) {
		return ResponseEntity.ok(
				bookingRepo.findByStatus(status)
						.stream()
						.map(this::toBookingDto)
						.toList()
		);
	}

	// ============ PARKING SPOT MANAGEMENT ============
	@GetMapping("/spots")
	public ResponseEntity<Page<ParkingSpotResponseDto>> listSpots(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<ParkingSpotResponseDto> spots = spotRepo.findAll(pageable)
				.map(this::toSpotDto);
		return ResponseEntity.ok(spots);
	}

	@GetMapping("/spots/{id}")
	public ResponseEntity<ParkingSpotResponseDto> getSpotById(@PathVariable Long id) {
		return ResponseEntity.ok(parkingSpotService.getSpotById(id));
	}

	@PostMapping("/spots")
	public ResponseEntity<ParkingSpotResponseDto> createSpotAsAdmin(
			@Valid @RequestBody ParkingSpotRequestDto dto
	) {
		return ResponseEntity.ok(adminService.createParkingSpot(dto));
	}

	@PutMapping("/spots/{id}")
	public ResponseEntity<ParkingSpotResponseDto> updateSpot(
			@PathVariable Long id,
			@Valid @RequestBody ParkingSpotRequestDto dto
	) {
		return ResponseEntity.ok(adminService.adminUpdateSpot(id, dto));
	}

	@DeleteMapping("/spots/{id}")
	public ResponseEntity<Void> deleteSpot(@PathVariable Long id) {
		adminService.adminDeleteSpot(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/spots/owner/{ownerId}")
	public ResponseEntity<List<ParkingSpotResponseDto>> getSpotsByOwner(@PathVariable Long ownerId) {
		return ResponseEntity.ok(parkingSpotService.getSpotsByOwner(ownerId));
	}

	// ============ AVAILABILITY MANAGEMENT ============
	@GetMapping("/availability")
	public ResponseEntity<Page<SpotAvailabilityResponseDto>> listAvailability(
			@PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable
	) {
		Page<SpotAvailabilityResponseDto> availabilities = availabilityRepo.findAll(pageable)
				.map(this::toAvailabilityDto);
		return ResponseEntity.ok(availabilities);
	}

	@GetMapping("/availability/{id}")
	public ResponseEntity<SpotAvailabilityResponseDto> getAvailabilityById(@PathVariable Long id) {
		return ResponseEntity.ok(availabilityService.getAvailabilityById(id));
	}

	@PostMapping("/availability")
	public ResponseEntity<SpotAvailabilityResponseDto> createAvailabilityAsAdmin(
			@Valid @RequestBody SpotAvailabilityRequestDto dto
	) {
		return ResponseEntity.ok(adminService.createAvailability(dto));
	}

	@PutMapping("/availability/{id}")
	public ResponseEntity<SpotAvailabilityResponseDto> updateAvailability(
			@PathVariable Long id,
			@Valid @RequestBody SpotAvailabilityRequestDto dto
	) {
		return ResponseEntity.ok(adminService.updateAvailability(id, dto));
	}

	@DeleteMapping("/availability/{id}")
	public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
		adminService.deleteAvailability(id);
		return ResponseEntity.noContent().build();
	}

	// ============ HELPER METHODS ============

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
