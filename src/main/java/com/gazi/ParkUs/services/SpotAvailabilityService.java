package com.gazi.ParkUs.services;

import com.gazi.ParkUs.dto.SpotAvailabilityRequestDto;
import com.gazi.ParkUs.dto.SpotAvailabilityResponseDto;

import java.util.List;

public interface SpotAvailabilityService {
    SpotAvailabilityResponseDto createAvailability(SpotAvailabilityRequestDto dto);
    SpotAvailabilityResponseDto getAvailabilityById(Long id);
    List<SpotAvailabilityResponseDto> getAvailabilitiesBySpot(Long spotId);
    List<SpotAvailabilityResponseDto> getAvailableSlotsBySpot(Long spotId);
    void deleteAvailability(Long id);
}
