package com.gazi.ParkUs.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "spotavailability")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpotAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long availabilityId;

    @ManyToOne
    @JoinColumn(name = "spot_id", nullable = false)
    private ParkingSpot spot;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Boolean isBooked = false;
}
