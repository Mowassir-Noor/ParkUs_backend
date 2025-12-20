package com.gazi.ParkUs.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;


@lombok.Getter
@lombok.Setter
public class ParkingSpotResponseDto {

    private Long spotId;
    private Long ownerId;
    private String title;
    private String description;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String slotType;
    private BigDecimal pricePerHour;
    private LocalDateTime createdAt;

    public ParkingSpotResponseDto(Long spotId, Long ownerId, String title, String description, String location, BigDecimal latitude, BigDecimal longitude, String slotType, BigDecimal pricePerHour, LocalDateTime createdAt) {
        this.spotId = spotId;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.slotType = slotType;
        this.pricePerHour = pricePerHour;
        this.createdAt = createdAt;
    }



    public ParkingSpotResponseDto() {

    }
// getters & setters
}
