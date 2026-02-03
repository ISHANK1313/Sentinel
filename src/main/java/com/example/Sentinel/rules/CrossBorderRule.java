package com.example.Sentinel.rules;

public class CrossBorderRule {
    public Long calculateScore(boolean crossBorder){
        if(!crossBorder){
            return 0L;
        }
        return 10L;
    }
}
