package com.gazi.ParkUs.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingLogDTO {
    private Long bookingId;
    private Long spotId;
    private Long renterId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime loggedAt;
}
