package com.example.Sentinel.services;


import com.example.Sentinel.config.MccRegistry;
import com.example.Sentinel.dto.RiskAssessmentDto;
import com.example.Sentinel.entity.RiskAssessment;
import com.example.Sentinel.entity.Transaction;
import com.example.Sentinel.repo.RiskAssessmentRepo;
import com.example.Sentinel.rules.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskScoringService {
    @Autowired
    private RiskAssessmentRepo riskAssessmentRepo;
    @Autowired
    private MccRegistry mccRegistry;
    public RiskAssessmentDto RiskEngine(List<Transaction> previousTransaction, Transaction currentTransaction, RedisTemplate<String,String>redisTemplate){
        RiskAssessment riskAssessment= new RiskAssessment();
        riskAssessment.setTransaction(currentTransaction);
        setAllScores(riskAssessment,previousTransaction,currentTransaction,redisTemplate);
        return convertToDto(riskAssessment, currentTransaction.getRequestId());
    }

    private void setAllScores(RiskAssessment risk, List<Transaction> transactions,
                              Transaction curr, RedisTemplate<String, String> redisTemplate) {

        AmountRule amountRule = new AmountRule();
        CrossBorderRule crossBorderRule = new CrossBorderRule();
        DeviceFingerPrintRule deviceFingerPrintRule = new DeviceFingerPrintRule();
        MerchantCategoryCodeRule merchantCategoryCodeRule = new MerchantCategoryCodeRule();
        TimeOfTransactionRule timeOfTransactionRule = new TimeOfTransactionRule();
        UserLocationRule userLocationRule = new UserLocationRule();
        VelocityRule velocityRule = new VelocityRule();
        StructuringRule structuringRule = new StructuringRule();
        BeneficiaryRule beneficiaryRule = new BeneficiaryRule();

        // Compute each raw score (each rule returns 0..40 or similar range)
        risk.setAmountScore(amountRule.calculateScore(transactions, curr.getAmount()));

        risk.setLocationScore(userLocationRule.calculateScore(transactions, curr.getUserLocation()));

        risk.setMerchantCategoryScore(merchantCategoryCodeRule.calculateScore(
                transactions, curr.getMerchantCategoryCode(), mccRegistry));

        risk.setTimeScore(timeOfTransactionRule.calculateScore(transactions, curr.getTimeOfTransaction()));

        risk.setCrossBorderScore(crossBorderRule.calculateScore(curr.isCrossBorder()));

        risk.setDeviceFingerPrintScore(deviceFingerPrintRule.calculateScore(
                transactions, curr.getDeviceFingerPrint()));

        risk.setVelocityScore(velocityRule.calculateScore(redisTemplate, curr.getUsers().getUserId()));

        risk.setSequenceScore(0L);

        risk.setStructuringScore(structuringRule.calculateScore(transactions));

        risk.setBeneficiaryScore(beneficiaryRule.calculateScore(
                curr.getUsers().getUserId(), curr.getMerchantId(), redisTemplate));

        // FIX: Weighted sum that produces a 0-100 range
        // Each raw score is multiplied by its weight, then the result is scaled to 0-100
        // Weights: Amount=0.20, Velocity=0.15, Location=0.10, MCC=0.10, Time=0.10,
        //          CrossBorder=0.05, Device=0.10, Structuring=0.10, Beneficiary=0.10
        // (Sequence is placeholder at 0)
        double weighted = 0.0;
        weighted += (double) risk.getAmountScore()           * 0.20;  // max 40*0.20 = 8
        weighted += (double) risk.getVelocityScore()         * 0.15;  // max 35*0.15 = 5.25
        weighted += (double) risk.getLocationScore()         * 0.10;  // max 10*0.10 = 1
        weighted += (double) risk.getMerchantCategoryScore() * 0.10;  // max 30*0.10 = 3
        weighted += (double) risk.getTimeScore()             * 0.10;  // max 25*0.10 = 2.5
        weighted += (double) risk.getCrossBorderScore()      * 0.05;  // max 10*0.05 = 0.5
        weighted += (double) risk.getDeviceFingerPrintScore()* 0.10;  // max 10*0.10 = 1
        weighted += (double) risk.getStructuringScore()      * 0.10;  // max 80*0.10 = 8
        weighted += (double) risk.getBeneficiaryScore()      * 0.10;  // max 30*0.10 = 3
        // Theoretical max weighted ≈ 32.25

        // Scale to 0-100 range. Max weighted is ~32, so multiply by ~3.1 to get 0-100
        double overall = Math.min(weighted * 3.1, 100.0);

        risk.setOverallScore(overall);
    }

    private RiskAssessmentDto convertToDto(RiskAssessment risk, String requestId) {
        RiskAssessmentDto dto = new RiskAssessmentDto();
        dto.setRequestId(requestId);
        dto.setTransactionId(risk.getTransaction().getTransactionId());
        dto.setUserId(risk.getTransaction().getUsers().getUserId());
        dto.setAmount(risk.getTransaction().getAmount());
        dto.setAmountScore(risk.getAmountScore());
        dto.setLocationScore(risk.getLocationScore());
        dto.setMerchantCategoryScore(risk.getMerchantCategoryScore());
        dto.setTimeScore(risk.getTimeScore());
        dto.setOverallScore(risk.getOverallScore());
        dto.setCrossBorderScore(risk.getCrossBorderScore());
        dto.setDeviceFingerPrintScore(risk.getDeviceFingerPrintScore());
        dto.setVelocityScore(risk.getVelocityScore());
        dto.setSequenceScore(risk.getSequenceScore());
        dto.setStructuringScore(risk.getStructuringScore());
        dto.setBeneficiaryScore(risk.getBeneficiaryScore());

        return dto;
    }


}
