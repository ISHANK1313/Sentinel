package com.example.Sentinel.rules;

import com.example.Sentinel.entity.Transaction;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeOfTransactionRule {

    public Long calculateScore(List<Transaction> previousTransaction, LocalDateTime currentTime){
      if(previousTransaction==null||previousTransaction.size()<5){
          return 0L;
      }
      int percentage=score(previousTransaction, currentTime);
        if (percentage >= 10) {
            return 0L;
        } else if (percentage > 0) {
            return 5L;
        } else if (currentTime.getHour() >= 2 && currentTime.getHour() <= 5) {
            return  25L;
        } else {
            return 15L;
        }

    }
    private int score(List<Transaction> transactions,LocalDateTime currTime){
        Map<Integer, Integer> hourlyCount = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            hourlyCount.put(i, 0);
        }
        int totalTransactions = 0;
        for (Transaction t : transactions) {
            int hour = t.getTimeOfTransaction().getHour();
            hourlyCount.put(hour, hourlyCount.get(hour) + 1);
            totalTransactions++;
        }
        int currentHour = currTime.getHour();
        int currentHourCount = hourlyCount.get(currentHour);
        return (currentHourCount*100)/totalTransactions;
    }
}
