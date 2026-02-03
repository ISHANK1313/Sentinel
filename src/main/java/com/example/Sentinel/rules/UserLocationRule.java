package com.example.Sentinel.rules;

import com.example.Sentinel.entity.Transaction;

import java.util.List;

public class UserLocationRule {

    public Long calculateScore(List<Transaction> transactionList, String currentLocation){
        if(transactionList==null||transactionList.size()<5){
            return 0L;
        }
        int percentage =score(transactionList, currentLocation);

        if(percentage >90){
            return 0L;
        }
        else if(percentage >60){
            return 5L;
        }
        return 10L;
    }
    private int score(List<Transaction>transactions,String currLocation){
        int freq=0;
        for(int i=0;i< transactions.size();i++){
            if(transactions.get(i).getUserLocation().equalsIgnoreCase(currLocation)){
                freq++;
            }
        }
        return (freq*100)/ transactions.size();
    }
}
