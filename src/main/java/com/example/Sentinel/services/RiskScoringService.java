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
// True max = 32.25
        double scaledScore = (weighted / 32.25) * 100.0;

// Penalty bonus: if 3+ rules fired with non-zero score, add a multi-rule boost
        long nonZeroRules = countNonZeroRules(risk);
        if (nonZeroRules >= 5) {
            scaledScore = Math.min(scaledScore * 1.35, 100.0);
        } else if (nonZeroRules >= 3) {
            scaledScore = Math.min(scaledScore * 1.20, 100.0);
        }

        double overall = Math.min(scaledScore, 100.0);
        risk.setOverallScore(overall);
    }

    private long countNonZeroRules(RiskAssessment risk) {
        long count = 0;
        if (risk.getAmountScore()            != null && risk.getAmountScore()            > 0) count++;
        if (risk.getVelocityScore()          != null && risk.getVelocityScore()          > 0) count++;
        if (risk.getLocationScore()          != null && risk.getLocationScore()          > 0) count++;
        if (risk.getMerchantCategoryScore()  != null && risk.getMerchantCategoryScore()  > 0) count++;
        if (risk.getTimeScore()              != null && risk.getTimeScore()              > 0) count++;
        if (risk.getCrossBorderScore()       != null && risk.getCrossBorderScore()       > 0) count++;
        if (risk.getDeviceFingerPrintScore() != null && risk.getDeviceFingerPrintScore() > 0) count++;
        if (risk.getStructuringScore()       != null && risk.getStructuringScore()       > 0) count++;
        if (risk.getBeneficiaryScore()       != null && risk.getBeneficiaryScore()       > 0) count++;
        return count;
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
