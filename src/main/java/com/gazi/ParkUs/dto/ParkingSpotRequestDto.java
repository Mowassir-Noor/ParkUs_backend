package com.gazi.ParkUs.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Setter
@Getter
public class ParkingSpotRequestDto {

    private Long ownerId;
    private String title;
    private String description;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String slotType;
    private BigDecimal pricePerHour;

    // getters & setters
}
