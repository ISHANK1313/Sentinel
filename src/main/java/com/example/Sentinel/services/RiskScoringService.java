package com.example.Sentinel.services;


import com.example.Sentinel.config.MccRegistry;
import com.example.Sentinel.entity.RiskAssessment;
import com.example.Sentinel.entity.Transaction;
import com.example.Sentinel.repo.RiskAssessmentRepo;
import com.example.Sentinel.rules.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskScoringService {
    @Autowired
    private RiskAssessmentRepo riskAssessmentRepo;
    @Autowired
    private MccRegistry mccRegistry;
    public void RiskEngine(List<Transaction> previousTransaction, Transaction currentTransaction){
        RiskAssessment riskAssessment= new RiskAssessment();
        riskAssessment.setTransaction(currentTransaction);
        setAllScores(riskAssessment,previousTransaction,currentTransaction);
        setFraudPossibility(riskAssessment);
        setTriggeredRules(riskAssessment);
        setFlag(riskAssessment,currentTransaction);
        riskAssessmentRepo.save(riskAssessment);

    }

    private void setAllScores(RiskAssessment risk, List<Transaction> transactions, Transaction curr){
        AmountRule amountRule= new AmountRule();
        CrossBorderRule crossBorderRule= new CrossBorderRule();
        DeviceFingerPrintRule deviceFingerPrintRule= new DeviceFingerPrintRule();
        MerchantCategoryCodeRule merchantCategoryCodeRule= new MerchantCategoryCodeRule();
        TimeOfTransactionRule timeOfTransactionRule= new TimeOfTransactionRule();
        UserLocationRule userLocationRule= new UserLocationRule();
        double overall=0.0;
        risk.setAmountScore(amountRule.calculateScore(transactions, curr.getAmount()));
        overall+= (double)risk.getAmountScore()*0.25;
        risk.setLocationScore(userLocationRule.calculateScore(transactions,curr.getUserLocation()));
        overall+=(double) risk.getLocationScore()*0.20;
        risk.setMerchantScore(merchantCategoryCodeRule
                .calculateScore(transactions, curr
                        .getMerchantCategoryCode(), mccRegistry));
        overall+=(double) risk.getMerchantScore()*0.15;
        risk.setTimeScore(timeOfTransactionRule.calculateScore(transactions,curr
                .getTimeOfTransaction()));
        overall+=(double)risk.getTimeScore()*0.15;
        risk.setCrossBorderScore(crossBorderRule.calculateScore(curr.isCrossBorder()));
        overall+=(double)risk.getCrossBorderScore()*0.05;
        risk.setDeviceFingerPrintScore(deviceFingerPrintRule
                .calculateScore(transactions, curr.getDeviceFingerPrint()));
        overall+=(double) risk.getDeviceFingerPrintScore()*0.20;
        risk.setVelocityScore(0L);
        overall+=(double) risk.getVelocityScore();
        risk.setOverallScore(overall);

    }
    private void setFraudPossibility(RiskAssessment risk){
        double d=risk.getOverallScore();
        if(d<=30.0){
            risk.setFraudPossibility("LOW");
        }
        else if(d<=60.0){
            risk.setFraudPossibility("MEDIUM");
        }
        else{
            risk.setFraudPossibility("HIGH");
        }
    }

    private void setTriggeredRules(RiskAssessment risk){
        List<String> stringList = new ArrayList<>();
        if(risk.getTimeScore()>=15){
            stringList.add("Time Rule");
        }
        if(risk.getMerchantScore()>=15){
            stringList.add("Merchant Code Rule");
        }
        if(risk.getLocationScore()>=5){
            stringList.add("Location Rule");
        }
        if(risk.getVelocityScore()>=10){
            stringList.add("Velocity Rule");
        }
        if(risk.getDeviceFingerPrintScore()>=5){
            stringList.add("Device Finger Print Rule");
        }
        if (risk.getCrossBorderScore()>=10){
            stringList.add("Cross Border Rule");
        }
        if(risk.getAmountScore()>=10){
            stringList.add("Amount Rule");
        }
        risk.setTriggeredRules(stringList);

    }

    private void setFlag(RiskAssessment riskAssessment,Transaction currTransaction){
        if(riskAssessment.getOverallScore()>60){
            currTransaction.setStatus("FLAGGED");
        }
    }


}
