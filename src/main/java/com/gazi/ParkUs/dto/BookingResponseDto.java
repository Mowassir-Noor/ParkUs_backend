package com.gazi.ParkUs.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingResponseDto {

    private Long bookingId;
    private Long spotId;

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setRenterId(Long renterId) {
        this.renterId = renterId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    private Long renterId;
    private Long ownerId;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getSpotId() {
        return spotId;
    }

    public Long getRenterId() {
        return renterId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;
    private BigDecimal totalAmount;

    // getters & setters
}
