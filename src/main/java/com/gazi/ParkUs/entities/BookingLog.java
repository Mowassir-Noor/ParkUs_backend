package com.gazi.ParkUs.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookinglog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private Long bookingId;
    private Long spotId;
    private Long ownerId;
    private Long renterId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private BigDecimal durationHours;
    private BigDecimal totalAmount;

    private String bookingStatus;

    private LocalDateTime loggedAt = LocalDateTime.now();
}
