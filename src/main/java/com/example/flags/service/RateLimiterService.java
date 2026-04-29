package com.example.flags.service;

import com.example.flags.exception.RateLimitExceededException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {
  private final StringRedisTemplate redis;
  private final int limit;
  public RateLimiterService(StringRedisTemplate redis, @Value("${app.rate-limit.per-minute}") int limit){this.redis=redis;this.limit=limit;}
  public void check(String tenantId, String operation){
    try {
      String key = "rl:" + operation + ":" + tenantId + ":" + (System.currentTimeMillis()/60000);
      Long count = redis.opsForValue().increment(key);
      if (count != null && count == 1) redis.expire(key, Duration.ofMinutes(2));
      if (count != null && count > limit) throw new RateLimitExceededException("rate limit exceeded for tenant " + tenantId);
    } catch (RateLimitExceededException e) { throw e; }
    catch (Exception ignored) { }
  }
}
