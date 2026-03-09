package com.example.Sentinel.controller;

import com.example.Sentinel.dto.RiskAssessmentDto;
import com.example.Sentinel.entity.RiskAssessment;
import com.example.Sentinel.repo.RiskAssessmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private RiskAssessmentRepo riskAssessmentRepo;

    @GetMapping("/history")
    public ResponseEntity<List<RiskAssessmentDto>> getHistory() {
        List<RiskAssessment> all = riskAssessmentRepo.findAllByOrderByIdDesc();
        List<RiskAssessmentDto> dtos = new ArrayList<>();

        for (RiskAssessment risk : all) {
            RiskAssessmentDto dto = new RiskAssessmentDto();
            dto.setId(risk.getId());
            dto.setTransactionId(risk.getTransaction().getTransactionId());
            dto.setUserId(risk.getTransaction().getUsers().getUserId());
            dto.setAmount(risk.getTransaction().getAmount());
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
            dtos.add(dto);
        }

        return ResponseEntity.ok(dtos);
    }
}
