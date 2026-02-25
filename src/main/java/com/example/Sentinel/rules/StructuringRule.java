package com.example.Sentinel.rules;

import com.example.Sentinel.entity.Transaction;

import java.util.List;

import static java.time.LocalDateTime.now;

public class StructuringRule {
    public Long calculateScore(List<Transaction> last30DaysTxn){
        double threshold=100000.0;
        List<Transaction> nearThreshold = last30DaysTxn.stream()
                .filter(t -> t.getAmount() >= 70000 && t.getAmount() < threshold)
                .filter(t -> t.getTimeOfTransaction().isAfter(now().minusHours(48)))
                .toList();

        double sum = nearThreshold.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
        Long score=0L;
        if(nearThreshold.size()>=5){
            score+=30;
        }
         if(sum>50L){
             score+=50;
         }
         return score;

    }

}
