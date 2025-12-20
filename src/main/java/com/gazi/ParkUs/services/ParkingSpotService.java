package com.gazi.ParkUs.services;


import com.gazi.ParkUs.dto.ParkingSpotRequestDto;
import com.gazi.ParkUs.dto.ParkingSpotResponseDto;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ParkingSpotService {

    ParkingSpotResponseDto createSpot(ParkingSpotRequestDto dto) throws AccessDeniedException;

    ParkingSpotResponseDto getSpotById(Long id);

    List<ParkingSpotResponseDto> getAllSpots();

    List<ParkingSpotResponseDto> getSpotsByOwner(Long ownerId);

    ParkingSpotResponseDto updateSpot(Long spotId, ParkingSpotRequestDto dto) throws AccessDeniedException;

    void deleteSpot(Long spotId) throws AccessDeniedException;
}