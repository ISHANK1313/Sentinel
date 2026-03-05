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
        /*setFraudPossibility(riskAssessment);
        setTriggeredRules(riskAssessment);
        setFlag(riskAssessment,currentTransaction);
        riskAssessmentRepo.save(riskAssessment);
        return setAndReturnRiskDto(riskAssessment);
         */
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

        double overall = 0.0;

        // Calculate each rule score with weighted contribution
        risk.setAmountScore(amountRule.calculateScore(transactions, curr.getAmount()));
        overall += (double) risk.getAmountScore() * 0.20;

        risk.setLocationScore(userLocationRule.calculateScore(transactions, curr.getUserLocation()));
        overall += (double) risk.getLocationScore() * 0.10;

        risk.setMerchantCategoryScore(merchantCategoryCodeRule.calculateScore(
                transactions, curr.getMerchantCategoryCode(), mccRegistry));
        overall += (double) risk.getMerchantCategoryScore() * 0.15;

        risk.setTimeScore(timeOfTransactionRule.calculateScore(transactions, curr.getTimeOfTransaction()));
        overall += (double) risk.getTimeScore() * 0.10;

        risk.setCrossBorderScore(crossBorderRule.calculateScore(curr.isCrossBorder()));
        overall += (double) risk.getCrossBorderScore() * 0.05;

        risk.setDeviceFingerPrintScore(deviceFingerPrintRule.calculateScore(
                transactions, curr.getDeviceFingerPrint()));
        overall += (double) risk.getDeviceFingerPrintScore() * 0.10;

        risk.setVelocityScore(velocityRule.calculateScore(redisTemplate, curr.getUsers().getUserId()));
        overall += (double) risk.getVelocityScore() * 0.10;

        risk.setSequenceScore(0L);
        overall += (double) risk.getSequenceScore();

        risk.setStructuringScore(structuringRule.calculateScore(transactions));
        overall += (double) risk.getStructuringScore() * 0.10;

        risk.setBeneficiaryScore(beneficiaryRule.calculateScore(
                curr.getUsers().getUserId(), curr.getMerchantId(), redisTemplate));
        overall += (double) risk.getBeneficiaryScore() * 0.10;

        risk.setOverallScore(overall);
    }

    private RiskAssessmentDto convertToDto(RiskAssessment risk, String requestId) {
        RiskAssessmentDto dto = new RiskAssessmentDto();
        dto.setRequestId(requestId);
        dto.setTransactionId(risk.getTransaction().getTransactionId());
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
