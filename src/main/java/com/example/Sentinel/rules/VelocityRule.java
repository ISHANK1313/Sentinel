package com.example.Sentinel.rules;

import org.springframework.data.redis.core.RedisTemplate;

public class VelocityRule {
   public Long calculateScore(RedisTemplate<String,String>redisTemplate,Long userId){
       String key5Min = "users:" + userId + ":velocity:5min";
       String key1Hr = "users:" + userId + ":velocity:1hr";
       String key24Hr = "users:" + userId + ":velocity:24hr";
       String value5min=redisTemplate.opsForValue().get(key5Min);
       String value1hr=redisTemplate.opsForValue().get(key1Hr);
       String value24hr=redisTemplate.opsForValue().get(key24Hr);
       Long val5min = value5min != null ? Long.parseLong(value5min) : 0L;
       Long score5min;
       if(val5min<=4){
           score5min= 0L;
       } else if (val5min<=6) {
           score5min= 15L;
       }
       else {
           score5min=35L;
       }
       Long val1hr=value1hr != null ? Long.parseLong(value1hr) : 0L;
       Long score1hr;
       if(val1hr<=10){
           score1hr= 0L;
       } else if (val1hr<=15) {
           score1hr= 20L;
       }
       else {
           score1hr=30L;
       }
       Long val24hr=value24hr != null ? Long.parseLong(value24hr) : 0L;
       Long score24hr;
       if(val24hr<=50){
           score24hr= 0L;
       } else if (val24hr<=100) {
           score24hr= 15L;
       }
       else {
           score24hr=25L;
       }
     return Math.max(score24hr,Math.max(score1hr,score5min));
   }

}
