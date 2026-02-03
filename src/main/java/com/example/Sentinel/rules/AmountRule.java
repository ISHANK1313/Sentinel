package com.example.Sentinel.rules;

import com.example.Sentinel.entity.Transaction;


import java.util.List;

public class AmountRule {
      public Long calculateScore(List<Transaction>transactions,Double currentAmount){
          if(transactions==null||transactions.size()<5) {
              return 5L;
          }
          Double z= score(transactions, currentAmount);
    if(z<1.0){
        return 0L;
    }
    else if(z<2.0){
        return 10L;
    }
    else if(z<3.0){
        return 25L;
    }

    return 40L;
      }

    private Double score(List<Transaction> lastTransactions, Double currAmount){
          Double sum=0.0;
          Integer freq=0;
          for(int i=0;i< lastTransactions.size();i++){
              sum+= lastTransactions.get(i).getAmount();
              freq++;
          }
          Long stdSum=0L;
          Long avg=(long)(sum/freq);
          for(int i=0;i<lastTransactions.size();i++){
              stdSum+=(long)((lastTransactions.get(i).getAmount()-avg)*(lastTransactions.get(i).getAmount()-avg));

          }
          Long std=stdSum/freq;
          return (avg-currAmount)/std;
      }

}
