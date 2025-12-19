package com.gazi.ParkUs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BookingResponseDTO {
    private Long bookingId;
    private String status;
    private BigDecimal totalAmount;
}
