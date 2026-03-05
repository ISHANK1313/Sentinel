package com.example.Sentinel.services;

import com.example.Sentinel.dto.MlScoreDto;
import com.example.Sentinel.dto.RiskAssessmentDto;
import com.example.Sentinel.dto.RuleScoreDto;
import com.example.Sentinel.entity.RiskAssessment;
import com.example.Sentinel.entity.Transaction;
import com.example.Sentinel.repo.RiskAssessmentRepo;
import com.example.Sentinel.repo.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class AggregatorService {

    @Autowired
    private RiskAssessmentRepo riskAssessmentRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final Map<String, RuleScoreDto> ruleScores = new ConcurrentHashMap<>();
    private final Map<String, MlScoreDto> mlScores = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @KafkaListener(topics = "rule-scores", groupId = "aggregator-group")
    public void consumeRuleScore(RuleScoreDto msg) {
        ruleScores.put(msg.getRequestId(), msg);
        tryAggregate(msg.getRequestId());
    }

    @KafkaListener(topics = "ml-scores", groupId = "aggregator-group")
    public void consumeMlScore(MlScoreDto msg) {
        mlScores.put(msg.getRequestId(), msg);
        tryAggregate(msg.getRequestId());
    }

    private synchronized void tryAggregate(String requestId) {

        RuleScoreDto rule = ruleScores.get(requestId);
        MlScoreDto ml = mlScores.get(requestId);

        if (rule != null && ml != null) {

            aggregateAndPublish(requestId, rule, ml);

        } else if (rule != null) {

            scheduler.schedule(() -> {
                if (!mlScores.containsKey(requestId)) {
                    aggregateAndPublish(requestId, rule, null);
                }
            }, 3, TimeUnit.SECONDS);

        } else if (ml != null) {

            scheduler.schedule(() -> {
                if (!ruleScores.containsKey(requestId)) {
                    aggregateAndPublish(requestId, null, ml);
                }
            }, 3, TimeUnit.SECONDS);
        }
    }

    private void aggregateAndPublish(String requestId, RuleScoreDto rule, MlScoreDto ml) {

        ruleScores.remove(requestId);
        mlScores.remove(requestId);

        Long transactionId =
                rule != null ? rule.getTransactionId() : ml.getTransactionId();

        Transaction txn = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        RiskAssessment risk = new RiskAssessment();
        risk.setTransaction(txn);

        Double ruleScore = null;
        Double mlScore = null;

        if (rule != null) {

            risk.setAmountScore(rule.getAmountScore());
            risk.setVelocityScore(rule.getVelocityScore());
            risk.setLocationScore(rule.getLocationScore());
            risk.setTimeScore(rule.getTimeScore());
            risk.setMerchantCategoryScore(rule.getMerchantCategoryScore());
            risk.setCrossBorderScore(rule.getCrossBorderScore());
            risk.setDeviceFingerPrintScore(rule.getDeviceFingerPrintScore());
            risk.setStructuringScore(rule.getStructuringScore());
            risk.setBeneficiaryScore(rule.getBeneficiaryScore());
            risk.setSequenceScore(rule.getSequenceScore());

            ruleScore = rule.getOverallScore();
        }

        if (ml != null) {
            risk.setMlScore(ml.getMlScore());
            mlScore = ml.getMlScore();
        }

        double finalScore;

        if (ruleScore != null && mlScore != null) {
            finalScore = (ruleScore * 0.6) + (mlScore * 0.4);
        } else if (ruleScore != null) {
            finalScore = ruleScore;
        } else {
            finalScore = mlScore;
        }

        risk.setOverallScore(finalScore);

        risk.setFraudPossibility(determineFraudLevel(finalScore));

        List<String> triggered = determineTriggeredRules(risk);
        risk.setTriggeredRules(triggered);

        if (finalScore > 60) {
            txn.setStatus("FLAGGED");
            transactionRepo.save(txn);
        }

        riskAssessmentRepo.save(risk);

        RiskAssessmentDto dto = convertToDto(risk, requestId);

        kafkaTemplate.send("risk-results", dto);
    }

    private String determineFraudLevel(double score) {

        if (score <= 30) return "LOW";
        if (score <= 60) return "MEDIUM";
        return "HIGH";
    }

    private List<String> determineTriggeredRules(RiskAssessment risk) {

        List<String> rules = new ArrayList<>();

        if (risk.getAmountScore() != null && risk.getAmountScore() >= 10)
            rules.add("Amount Rule");

        if (risk.getVelocityScore() != null && risk.getVelocityScore() >= 20)
            rules.add("Velocity Rule");

        if (risk.getLocationScore() != null && risk.getLocationScore() >= 5)
            rules.add("Location Rule");

        if (risk.getStructuringScore() != null && risk.getStructuringScore() >= 30)
            rules.add("Structuring Rule");

        if (risk.getBeneficiaryScore() != null && risk.getBeneficiaryScore() >= 15)
            rules.add("Beneficiary Rule");

        if (risk.getMlScore() != null && risk.getMlScore() >= 60)
            rules.add("ML Model Alert");

        return rules;
    }

    private RiskAssessmentDto convertToDto(RiskAssessment risk, String requestId) {

        RiskAssessmentDto dto = new RiskAssessmentDto();

        dto.setId(risk.getId());
        dto.setRequestId(requestId);
        dto.setTransactionId(risk.getTransaction().getTransactionId());

        dto.setAmountScore(risk.getAmountScore());
        dto.setVelocityScore(risk.getVelocityScore());
        dto.setLocationScore(risk.getLocationScore());
        dto.setTimeScore(risk.getTimeScore());
        dto.setMerchantCategoryScore(risk.getMerchantCategoryScore());
        dto.setCrossBorderScore(risk.getCrossBorderScore());
        dto.setDeviceFingerPrintScore(risk.getDeviceFingerPrintScore());
        dto.setStructuringScore(risk.getStructuringScore());
        dto.setBeneficiaryScore(risk.getBeneficiaryScore());
        dto.setSequenceScore(risk.getSequenceScore());

        dto.setOverallScore(risk.getOverallScore());
        dto.setMlScore(risk.getMlScore());

        dto.setFraudPossibility(risk.getFraudPossibility());
        dto.setTriggeredRules(risk.getTriggeredRules());

        return dto;
    }
}
