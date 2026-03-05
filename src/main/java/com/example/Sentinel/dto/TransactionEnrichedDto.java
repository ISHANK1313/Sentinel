package com.example.Sentinel.dto;

import java.time.LocalDateTime;

public class TransactionEnrichedDto {
    private String requestId;
    private Long transactionId;
    private Long userId;
    private Long merchantId;
    private Double amount;
    private String locationOfUser;
    private LocalDateTime timeOfPayment;
    private Integer merchantCategoryCode;
    private Boolean crossBorder;
    private String deviceFingerPrint;

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
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

    public Integer getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public void setMerchantCategoryCode(Integer merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public Boolean getCrossBorder() {
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
}
