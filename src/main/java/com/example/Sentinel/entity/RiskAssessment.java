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
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    @JsonIgnore
    private Transaction transaction;


    private Long locationScore;
    private Long amountScore;
    private Long timeScore;
    private Long velocityScore;
    private Long sequenceScore;
    private Long merchantCategoryScore;
    private Long crossBorderScore;
    private Long deviceFingerPrintScore;
    private Long structuringScore;
    private Long beneficiaryScore;

    private Double overallScore;

    private Double mlScore;

    private String fraudPossibility;

    @ElementCollection
    private List<String> triggeredRules;

    // Getters and Setters
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

    public Double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    public Double getMlScore() {
        return mlScore;
    }

    public void setMlScore(Double mlScore) {
        this.mlScore = mlScore;
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
