package com.example.Sentinel.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class MoneyTransferDto {
    @NotNull(message="amount can not be null")
    @Positive(message="amount must be positive value")
    private Double amount;

    @NotNull(message="Location can not be null")
    @NotBlank(message ="Location can not be blank")
    private String locationOfUser;

    @NotNull(message="time can not be null")
    @PastOrPresent(message = "Time cannot be future")
    private LocalDateTime timeOfPayment;

    @NotNull(message="merchant_ID can not be null")
    private Long merchantId;

    @NotNull(message="user_ID can not be null")
    private Long userId;

    @NotNull(message="merchantCategoryCode can not be null")
    @NotBlank(message ="merchantCategoryCode can not be blank")
    private String merchantCategoryCode;

    @NotNull(message="cross-border info required")
    private Boolean crossBorder;

    @NotNull(message="deviceFingerPrint can not be null")
    @NotBlank(message ="deviceFingerPrint can not be blank")
    private String deviceFingerPrint;

    // Getters and setters
    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public void setMerchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public Boolean isCrossBorder() {
        return crossBorder;
    }

    public void setCrossBorder(Boolean crossBorder) {
        this.crossBorder = crossBorder;
    }

    public String getDeviceFingerPrint() {
        return deviceFingerPrint;
    }

    public void setDeviceFingerPrint(String deviceFingerPrint) {
        this.deviceFingerPrint = deviceFingerPrint;
    }

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

