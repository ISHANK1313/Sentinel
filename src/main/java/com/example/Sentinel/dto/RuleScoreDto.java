package com.example.Sentinel.dto;

import java.util.List;

public class RuleScoreDto {
    private String requestId;
    private Long transactionId;

    private Long amountScore;
    private Long velocityScore;
    private Long locationScore;
    private Long timeScore;
    private Long merchantCategoryScore;
    private Long crossBorderScore;
    private Long deviceFingerPrintScore;
    private Long structuringScore;
    private Long beneficiaryScore;
    private Long sequenceScore;

    private Double overallScore;
    private List<String> triggeredRules;

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

    public Long getAmountScore() {
        return amountScore;
    }

    public void setAmountScore(Long amountScore) {
        this.amountScore = amountScore;
    }

    public Long getVelocityScore() {
        return velocityScore;
    }

    public void setVelocityScore(Long velocityScore) {
        this.velocityScore = velocityScore;
    }

    public Long getLocationScore() {
        return locationScore;
    }

    public void setLocationScore(Long locationScore) {
        this.locationScore = locationScore;
    }

    public Long getTimeScore() {
        return timeScore;
    }

    public void setTimeScore(Long timeScore) {
        this.timeScore = timeScore;
    }

    public Long getMerchantCategoryScore() {
        return merchantCategoryScore;
    }

    public void setMerchantCategoryScore(Long merchantCategoryScore) {
        this.merchantCategoryScore = merchantCategoryScore;
    }

    public Long getCrossBorderScore() {
        return crossBorderScore;
    }

    public void setCrossBorderScore(Long crossBorderScore) {
        this.crossBorderScore = crossBorderScore;
    }

    public Long getDeviceFingerPrintScore() {
        return deviceFingerPrintScore;
    }

    public void setDeviceFingerPrintScore(Long deviceFingerPrintScore) {
        this.deviceFingerPrintScore = deviceFingerPrintScore;
    }

    public Long getStructuringScore() {
        return structuringScore;
    }

    public void setStructuringScore(Long structuringScore) {
        this.structuringScore = structuringScore;
    }

    public Long getBeneficiaryScore() {
        return beneficiaryScore;
    }

    public void setBeneficiaryScore(Long beneficiaryScore) {
        this.beneficiaryScore = beneficiaryScore;
    }

    public Long getSequenceScore() {
        return sequenceScore;
    }

    public void setSequenceScore(Long sequenceScore) {
        this.sequenceScore = sequenceScore;
    }

    public Double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    public List<String> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(List<String> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }
}
