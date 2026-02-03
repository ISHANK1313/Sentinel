package com.example.Sentinel.rules;

import com.example.Sentinel.entity.Transaction;

import java.util.List;

public class DeviceFingerPrintRule {
    public Long calculateScore(List<Transaction> transactionList, String currentDevice){
        if(transactionList==null||transactionList.size()<5){
            return 0L;
        }
        if(score(transactionList,currentDevice)>95){
            return 0L;
        }
        else if(score(transactionList, currentDevice)>60){
            return 5L;
        }
        return 10L;
    }
    private int score(List<Transaction> previousTransaction, String currDevice){
        int freq=0;
        for(int i=0;i< previousTransaction.size();i++){
            if(previousTransaction.get(i).getDeviceFingerPrint().equalsIgnoreCase(currDevice)){
                freq++;
            }
        }
        return (int)Math.round((freq*100.0)/ previousTransaction.size());
    }
}
