package com.gazi.ParkUs.controller;


import com.gazi.ParkUs.dto.ParkingSpotRequestDto;
import com.gazi.ParkUs.dto.ParkingSpotResponseDto;
import com.gazi.ParkUs.services.ParkingSpotService;
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
    public ParkingSpotResponseDto create(@RequestBody ParkingSpotRequestDto dto) throws AccessDeniedException {
        return spotService.createSpot(dto);
    }

    @GetMapping("/{id}")
    public ParkingSpotResponseDto getById(@PathVariable Long id) {
        return spotService.getSpotById(id);
    }

    @GetMapping
    public List<ParkingSpotResponseDto> getAll() {
        return spotService.getAllSpots();
    }

    @GetMapping("/owner/{ownerId}")
    public List<ParkingSpotResponseDto> getByOwner(@PathVariable Long ownerId) {
        return spotService.getSpotsByOwner(ownerId);
    }

    @PutMapping("/{id}")
    public ParkingSpotResponseDto update(
            @PathVariable Long id,
            @RequestBody ParkingSpotRequestDto dto
    ) throws AccessDeniedException {
        return spotService.updateSpot(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws AccessDeniedException {
        spotService.deleteSpot(id);
    }
}
