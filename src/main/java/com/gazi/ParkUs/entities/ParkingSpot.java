package com.gazi.ParkUs.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//import org.springframework.data.annotation.Id;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parkingspot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spotId;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private RegularUser owner;

    private String title;
    private String description;
    private String location;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private String slotType;
    private BigDecimal pricePerHour;

    private LocalDateTime createdAt = LocalDateTime.now();
}
