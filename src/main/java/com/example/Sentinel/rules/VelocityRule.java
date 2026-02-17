package com.example.Sentinel.rules;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

public class VelocityRule {
   public Long calculateScore(RedisTemplate<String,String>redisTemplate,Long userId){
       String key = "users:" + userId + ":velocity";
       long now=System.currentTimeMillis();
       long cutoff5min=now- Duration.ofMinutes(5).toMillis();
       long cutoff1hr=now-Duration.ofHours(1).toMillis();
       long cutoff24hr=now-Duration.ofDays(1).toMillis();
       Long count5min=redisTemplate.opsForZSet().count(key,cutoff5min,now);
       Long count1hr=redisTemplate.opsForZSet().count(key,cutoff1hr,now);
       Long count24hr=redisTemplate.opsForZSet().count(key,cutoff24hr,now);
       count5min = count5min != null ? count5min : 0L;
       count1hr = count1hr != null ? count1hr : 0L;
       count24hr = count24hr != null ? count24hr : 0L;
       Long score5min;
       if (count5min <= 4) score5min = 0L;
       else if (count5min <= 6) score5min = 15L;
       else score5min = 35L;

       Long score1hr;
       if (count1hr <= 10) score1hr = 0L;
       else if (count1hr <= 15) score1hr = 20L;
       else score1hr = 30L;

       Long score24hr;
       if (count24hr <= 50) score24hr = 0L;
       else if (count24hr <= 100) score24hr = 15L;
       else score24hr = 25L;

     return Math.max(score24hr,Math.max(score1hr,score5min));
   }

}
