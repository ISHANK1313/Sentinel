package com.example.Sentinel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class RiskAssessment {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="transaction_id",nullable = false,unique = true)
    @JsonIgnore
    private Transaction transaction;

    private Long locationScore;
    private Long amountScore;
    private Long timeScore;
    private Long velocityScore;
    private Long merchantScore;
    private Long sequenceScore;
    private Long merchantCategoryScore;
    private Long CrossBorderScore;
    private Long deviceFingerPrintScore;
    private Double overallScore;
    private String fraudPossibility;
    @ElementCollection
    private List<String> triggeredRules;

    public String getFraudPossibility() {
        return fraudPossibility;
    }

    public void setFraudPossibility(String fraudPossibility) {
        this.fraudPossibility = fraudPossibility;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
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

    public Long getMerchantScore() {
        return merchantScore;
    }

    public void setMerchantScore(Long merchantScore) {
        this.merchantScore = merchantScore;
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
