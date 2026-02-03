package com.example.Sentinel.rules;

import com.example.Sentinel.entity.Transaction;

import java.util.List;

public class UserLocationRule {

    public Long calculateScore(List<Transaction> transactionList, String currentLocation){
        int fr=score(transactionList, currentLocation);
        if(fr>5){
            return 0L;
        }
        else if(fr>2){
            return 5L;
        }
        return 10L;
    }
    public int score(List<Transaction>transactions,String currLocation){
        int freq=0;
        for(int i=0;i< transactions.size();i++){
            if(transactions.get(i).getUserLocation()==currLocation){
                freq++;
            }
        }
        return freq;
    }
}
