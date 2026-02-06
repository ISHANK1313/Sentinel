package com.example.Sentinel.dto;

import java.util.List;

public class RiskAssessmentDto {
    private Long id;
    private Long locationScore;
    private Long amountScore;
    private Long timeScore;
    private Long velocityScore;
    private Long sequenceScore;
    private Long merchantCategoryScore;
    private Long CrossBorderScore;
    private Long deviceFingerPrintScore;
    private Double overallScore;
    private String fraudPossibility;
    private List<String> triggeredRules;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLocationScore() {
        return locationScore;
    }

    public void setLocationScore(Long locationScore) {
        this.locationScore = locationScore;
    }

    public Long getAmountScore() {
        return amountScore;
    }

    public void setAmountScore(Long amountScore) {
        this.amountScore = amountScore;
    }

    public Long getTimeScore() {
        return timeScore;
    }

    public void setTimeScore(Long timeScore) {
        this.timeScore = timeScore;
    }

    public Long getVelocityScore() {
        return velocityScore;
    }

    public void setVelocityScore(Long velocityScore) {
        this.velocityScore = velocityScore;
    }

    public Long getSequenceScore() {
        return sequenceScore;
    }

    public void setSequenceScore(Long sequenceScore) {
        this.sequenceScore = sequenceScore;
    }

    public Long getMerchantCategoryScore() {
        return merchantCategoryScore;
    }

    public void setMerchantCategoryScore(Long merchantCategoryScore) {
        this.merchantCategoryScore = merchantCategoryScore;
    }

    public Long getCrossBorderScore() {
        return CrossBorderScore;
    }

    public void setCrossBorderScore(Long crossBorderScore) {
        CrossBorderScore = crossBorderScore;
    }

    public Long getDeviceFingerPrintScore() {
        return deviceFingerPrintScore;
    }

    public void setDeviceFingerPrintScore(Long deviceFingerPrintScore) {
        this.deviceFingerPrintScore = deviceFingerPrintScore;
    }

    public Double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    public String getFraudPossibility() {
        return fraudPossibility;
    }

    public void setFraudPossibility(String fraudPossibility) {
        this.fraudPossibility = fraudPossibility;
    }

    public List<String> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(List<String> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }
}
