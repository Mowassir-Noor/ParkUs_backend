package com.gazi.ParkUs.controller;

import com.gazi.ParkUs.dto.ParkingSpotRequestDto;
import com.gazi.ParkUs.dto.ParkingSpotResponseDto;
import com.gazi.ParkUs.services.ParkingSpotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/spots")
public class ParkingSpotController {

    private final ParkingSpotService spotService;

    public ParkingSpotController(ParkingSpotService spotService) {
        this.spotService = spotService;
    }

    @PostMapping
    public ResponseEntity<ParkingSpotResponseDto> create(@Valid @RequestBody ParkingSpotRequestDto dto) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(spotService.createSpot(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingSpotResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(spotService.getSpotById(id));
    }

    @GetMapping
    public ResponseEntity<List<ParkingSpotResponseDto>> getAll() {
        return ResponseEntity.ok(spotService.getAllSpots());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ParkingSpotResponseDto>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(spotService.getSpotsByOwner(ownerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingSpotResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ParkingSpotRequestDto dto
    ) throws AccessDeniedException {
        return ResponseEntity.ok(spotService.updateSpot(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws AccessDeniedException {
        spotService.deleteSpot(id);
        return ResponseEntity.noContent().build();
    }
}
