package com.gazi.ParkUs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequestDto {
    @NotNull(message = "Availability ID is required")
    private Long availabilityId;

    @NotNull(message = "Renter ID is required")
    private Long renterId;
}
