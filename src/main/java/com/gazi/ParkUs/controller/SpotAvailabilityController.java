package com.gazi.ParkUs.controller;

import com.gazi.ParkUs.dto.SpotAvailabilityRequestDto;
import com.gazi.ParkUs.dto.SpotAvailabilityResponseDto;
import com.gazi.ParkUs.services.SpotAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class SpotAvailabilityController {

    private final SpotAvailabilityService availabilityService;

    public SpotAvailabilityController(SpotAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<SpotAvailabilityResponseDto> createAvailability(
            @Valid @RequestBody SpotAvailabilityRequestDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityService.createAvailability(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpotAvailabilityResponseDto> getAvailabilityById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(availabilityService.getAvailabilityById(id));
    }

    @GetMapping("/spot/{spotId}")
    public ResponseEntity<List<SpotAvailabilityResponseDto>> getAvailabilitiesBySpot(
            @PathVariable Long spotId
    ) {
        return ResponseEntity.ok(availabilityService.getAvailabilitiesBySpot(spotId));
    }

    @GetMapping("/spot/{spotId}/available")
    public ResponseEntity<List<SpotAvailabilityResponseDto>> getAvailableSlotsBySpot(
            @PathVariable Long spotId
    ) {
        return ResponseEntity.ok(availabilityService.getAvailableSlotsBySpot(spotId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.noContent().build();
    }
}
