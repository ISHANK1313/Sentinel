package com.example.Sentinel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class MoneyTransferDto {
    @NotNull(message="amount can not be null")
    @NotBlank(message ="amount can not be blank")
    @Positive(message="amount must be positive value")
    private Double amount;
    @NotNull(message="Location can not be null")
    @NotBlank(message ="Location can not be blank")
    private String locationOfUser;
    @NotNull(message="time can not be null")
    @NotBlank(message ="time can not be blank")
    @PastOrPresent(message = "Time cannot be future")
    private LocalDateTime timeOfPayment;
    @NotNull(message="merchant_ID can not be null")
    @NotBlank(message ="merchant_ID can not be blank")
    private Long merchantId;
    @NotNull(message="user_ID can not be null")
    @NotBlank(message ="user_ID can not be blank")
    private Long userId;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getLocationOfUser() {
        return locationOfUser;
    }

    public void setLocationOfUser(String locationOfUser) {
        this.locationOfUser = locationOfUser;
    }

    public LocalDateTime getTimeOfPayment() {
        return timeOfPayment;
    }

    public void setTimeOfPayment(LocalDateTime timeOfPayment) {
        this.timeOfPayment = timeOfPayment;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
