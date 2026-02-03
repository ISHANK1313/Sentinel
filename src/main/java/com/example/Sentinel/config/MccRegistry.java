package com.example.Sentinel.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MccRegistry {

     private final Map<Integer, String> mccMap = new HashMap<>();


     @PostConstruct
     public void initializeMccCodes() {

         mccMap.put(7995, "HIGH");
         mccMap.put(6010, "HIGH");
         mccMap.put(6011, "HIGH");
         mccMap.put(6051, "HIGH");
         mccMap.put(4829, "HIGH");


         mccMap.put(5732, "MEDIUM");
         mccMap.put(5944, "MEDIUM");
         mccMap.put(4722, "MEDIUM");
         mccMap.put(7011, "MEDIUM");


         mccMap.put(5411, "LOW");
         mccMap.put(5912, "LOW");
         mccMap.put(5814, "LOW");
         mccMap.put(4111, "LOW");
     }

        // Helper method to safely get data
        public String getRiskLevel(Integer mcc) {
            return mccMap.getOrDefault(mcc, "UNKNOWN"); // Default if MCC not found
        }

}
