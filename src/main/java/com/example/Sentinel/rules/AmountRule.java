package com.example.Sentinel.rules;

import com.example.Sentinel.entity.Transaction;


import java.util.List;

public class AmountRule {
      public Long calculateScore(List<Transaction> lastTransactions){
          Double sum=0.0;
          Integer freq=0;
          for(int i=0;i< lastTransactions.size();i++){
             sum+= lastTransactions.get(i).getAmount();
             freq++;
          }

       return (long)(sum/freq);
      }

}
