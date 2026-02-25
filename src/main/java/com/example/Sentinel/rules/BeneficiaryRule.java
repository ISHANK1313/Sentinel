package com.example.Sentinel.rules;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Set;

public class BeneficiaryRule {
    public Long calculateScore(Long userId, Long merchantId, RedisTemplate<String,String> redisTemplate){
        String key = "beneficiary:user:" + userId;
        long now = System.currentTimeMillis();
        long last24Hr = now - Duration.ofDays(1).toMillis();
        long last90Days = now - Duration.ofDays(90).toMillis();

        Set<String> merchantIds24h = redisTemplate.opsForZSet().rangeByScore(key, last24Hr, now);
        Set<String> merchantIds90Days = redisTemplate.opsForZSet().rangeByScore(key, last90Days, now);

        long count24h = merchantIds24h != null ? merchantIds24h.size() : 0;
        long count90Days = merchantIds90Days != null ? merchantIds90Days.size() : 0;

        Double existingScore = redisTemplate.opsForZSet().score(key, merchantId.toString());
        boolean isNewMerchant = (existingScore == null);

        // AGGREGATED SCORING (SUM all patterns, max 30)
        Long score = 0L;

        // Pattern 1: High velocity in 24h (0-15 points)
        if (count24h > 7) {
            score += 15L; // Extreme velocity
        }
        else if (count24h >= 5) {
            score += 10L; // High velocity
        }
        else if (count24h >= 3) {
            score += 5L; // Moderate velocity
        }

        // Pattern 2: High volume in 90 days (0-10 points)
        if (count90Days > 30) {
            score += 10L; // Professional mule
        }
        else if (count90Days > 20) {
            score += 7L; // Suspicious spread
        }
        else if (count90Days > 10) {
            score += 3L; // Moderate spread
        }

        // Pattern 3: First-time beneficiary (0-5 points)
        if (isNewMerchant) {
            score += 5L; // New relationship risk
        }

        // Cap at 30
        return score;
    }
}
