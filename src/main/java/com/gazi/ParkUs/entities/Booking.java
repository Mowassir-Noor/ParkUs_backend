package com.gazi.ParkUs.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "spot_id")
    private ParkingSpot spot;

    @ManyToOne
    @JoinColumn(name = "renter_id")
    private UserEntity renter;

    @ManyToOne
    @JoinColumn(name = "availability_id")
    private SpotAvailability availability;

    private LocalDateTime bookedAt = LocalDateTime.now();

    private String status;

    private BigDecimal totalAmount;
}
