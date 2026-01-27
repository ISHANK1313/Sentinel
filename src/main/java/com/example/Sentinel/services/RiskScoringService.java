package com.example.Sentinel.services;


import com.example.Sentinel.repo.RiskAssessmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {
    @Autowired
    private RiskAssessmentRepo riskAssessmentRepo;

}
