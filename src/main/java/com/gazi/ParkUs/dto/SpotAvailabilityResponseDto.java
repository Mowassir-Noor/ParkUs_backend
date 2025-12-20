package com.gazi.ParkUs.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpotAvailabilityResponseDto {
    private Long availabilityId;
    private Long spotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isBooked;
}
