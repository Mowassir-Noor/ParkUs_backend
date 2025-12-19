package com.gazi.ParkUs.dto;

import lombok.Data;

@Data
public class BookingRequestDTO {
    private Long spotId;
    private Long availabilityId;
    private Long renterId;
}
