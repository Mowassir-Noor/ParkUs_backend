package com.gazi.ParkUs.services;

import com.gazi.ParkUs.dto.SpotAvailabilityRequestDto;
import com.gazi.ParkUs.dto.SpotAvailabilityResponseDto;
import com.gazi.ParkUs.entities.ParkingSpot;
import com.gazi.ParkUs.entities.SpotAvailability;
import com.gazi.ParkUs.entities.UserEntity;
import com.gazi.ParkUs.exceptions.InvalidRequestException;
import com.gazi.ParkUs.exceptions.ResourceNotFoundException;
import com.gazi.ParkUs.exceptions.UnauthorizedException;
import com.gazi.ParkUs.repositories.ParkingSpotRepository;
import com.gazi.ParkUs.repositories.SpotAvailabilityRepository;
import com.gazi.ParkUs.repositories.UserRepository;
import com.gazi.ParkUs.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SpotAvailabilityServiceImpl implements SpotAvailabilityService {

    private final SpotAvailabilityRepository availabilityRepo;
    private final ParkingSpotRepository spotRepo;
    private final UserRepository userRepo;

    public SpotAvailabilityServiceImpl(
            SpotAvailabilityRepository availabilityRepo,
            ParkingSpotRepository spotRepo,
            UserRepository userRepo
    ) {
        this.availabilityRepo = availabilityRepo;
        this.spotRepo = spotRepo;
        this.userRepo = userRepo;
    }

    @Override
    public SpotAvailabilityResponseDto createAvailability(SpotAvailabilityRequestDto dto) {
        // Get authenticated user
        String currentUserEmail = SecurityUtils.currentUserEmail();
        UserEntity authenticatedUser = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        // Get parking spot
        ParkingSpot spot = spotRepo.findById(dto.getSpotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking spot not found"));

        // Verify user owns the spot
        if (!spot.getOwner().getUserId().equals(authenticatedUser.getUserId())) {
            throw new UnauthorizedException("You can only create availability for your own spots");
        }

        // Validate time range
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new InvalidRequestException("End time must be after start time");
        }

        if (dto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("Start time must be in the future");
        }

        // Check for overlapping availability
        List<SpotAvailability> overlapping = availabilityRepo.findBySpot_SpotId(dto.getSpotId())
                .stream()
                .filter(a -> isOverlapping(a, dto.getStartTime(), dto.getEndTime()))
                .toList();

        if (!overlapping.isEmpty()) {
            throw new InvalidRequestException("This time slot overlaps with existing availability");
        }

        // Create availability
        SpotAvailability availability = new SpotAvailability();
        availability.setSpot(spot);
        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());
        availability.setIsBooked(false);

        availabilityRepo.save(availability);

        return toDto(availability);
    }

    @Override
    public SpotAvailabilityResponseDto getAvailabilityById(Long id) {
        SpotAvailability availability = availabilityRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));
        return toDto(availability);
    }

    @Override
    public List<SpotAvailabilityResponseDto> getAvailabilitiesBySpot(Long spotId) {
        if (!spotRepo.existsById(spotId)) {
            throw new ResourceNotFoundException("Parking spot not found");
        }

        return availabilityRepo.findBySpot_SpotId(spotId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<SpotAvailabilityResponseDto> getAvailableSlotsBySpot(Long spotId) {
        if (!spotRepo.existsById(spotId)) {
            throw new ResourceNotFoundException("Parking spot not found");
        }

        return availabilityRepo.findBySpot_SpotId(spotId)
                .stream()
                .filter(a -> Boolean.FALSE.equals(a.getIsBooked()))
                .filter(a -> a.getStartTime().isAfter(LocalDateTime.now()))
                .map(this::toDto)
                .toList();
    }

    @Override
    public void deleteAvailability(Long id) {
        SpotAvailability availability = availabilityRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        // Get authenticated user
        String currentUserEmail = SecurityUtils.currentUserEmail();
        UserEntity authenticatedUser = userRepo.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        // Verify user owns the spot
        if (!availability.getSpot().getOwner().getUserId().equals(authenticatedUser.getUserId())) {
            throw new UnauthorizedException("You can only delete availability for your own spots");
        }

        // Don't allow deletion if already booked
        if (Boolean.TRUE.equals(availability.getIsBooked())) {
            throw new InvalidRequestException("Cannot delete availability that is already booked");
        }

        availabilityRepo.delete(availability);
    }

    // Helper methods
    private boolean isOverlapping(SpotAvailability existing, LocalDateTime newStart, LocalDateTime newEnd) {
        // Check if time ranges overlap
        return !(newEnd.isBefore(existing.getStartTime()) || newStart.isAfter(existing.getEndTime()));
    }

    private SpotAvailabilityResponseDto toDto(SpotAvailability availability) {
        SpotAvailabilityResponseDto dto = new SpotAvailabilityResponseDto();
        dto.setAvailabilityId(availability.getAvailabilityId());
        dto.setSpotId(availability.getSpot().getSpotId());
        dto.setStartTime(availability.getStartTime());
        dto.setEndTime(availability.getEndTime());
        dto.setIsBooked(availability.getIsBooked());
        return dto;
    }
}
