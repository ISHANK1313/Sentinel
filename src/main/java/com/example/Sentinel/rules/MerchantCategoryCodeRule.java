package com.example.Sentinel.rules;

import com.example.Sentinel.config.MccRegistry;
import com.example.Sentinel.entity.Transaction;

import java.util.List;

public class MerchantCategoryCodeRule {
    public Long calculateScore(List<Transaction> transactionList, Integer currentMerchantCode, MccRegistry registry){
        int fr=score(transactionList,currentMerchantCode);
         if(fr>3){
             return 0L;
         }
         else if(fr>1){
             return 5L;
         }
         else if(registry.getRiskLevel(currentMerchantCode)=="LOW"){
             return 5L;
         }
         else if(registry.getRiskLevel(currentMerchantCode)=="MEDIUM"){
             return 15L;
         }
         else if(registry.getRiskLevel(currentMerchantCode)=="HIGH"){
             return 30L;
         }
         return 10L;

    }
    public int score(List<Transaction> previousTransaction, Integer  currMerchCode){
        int freq=0;
        for(int i=0;i< previousTransaction.size();i++){
            if(currMerchCode==previousTransaction.get(i).getMerchantCategoryCode()){
                freq++;
            }
        }
        return freq;
    }

}
