package com.gazi.ParkUs.dto;

import lombok.Data;

@Data
public class BookingRequestDto {
    private Long spotId;
    private Long availabilityId;
    private Long renterId;
}
