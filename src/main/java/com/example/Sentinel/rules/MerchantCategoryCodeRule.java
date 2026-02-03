package com.example.Sentinel.rules;

import com.example.Sentinel.config.MccRegistry;
import com.example.Sentinel.entity.Transaction;

import java.util.List;

public class MerchantCategoryCodeRule {
    public Long calculateScore(List<Transaction> transactionList, Integer currentMerchantCode, MccRegistry registry){
        if(transactionList==null||transactionList.size()<5){
            return 0L;
        }
        int percentage=score(transactionList,currentMerchantCode);
         if(percentage>90){
             return 0L;
         }
         else if(percentage>60){
             return 5L;
         }
         else if(registry.getRiskLevel(currentMerchantCode).equalsIgnoreCase("LOW")){
             return 5L;
         }
         else if(registry.getRiskLevel(currentMerchantCode).equalsIgnoreCase("MEDIUM")){
             return 15L;
         }
         else if(registry.getRiskLevel(currentMerchantCode).equalsIgnoreCase("HIGH")){
             return 30L;
         }
         return 10L;

    }
    private int score(List<Transaction> previousTransaction, Integer  currMerchCode){
        int freq=0;
        for(int i=0;i< previousTransaction.size();i++){
            if(currMerchCode==previousTransaction.get(i).getMerchantCategoryCode()){
                freq++;
            }
        }
        return (int)Math.round((freq*100.0)/ previousTransaction.size());
    }

}
