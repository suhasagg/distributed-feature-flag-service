package com.example.flags.service;

import com.example.flags.domain.FeatureFlagEntity;
import com.example.flags.dto.FlagResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
  private final StringRedisTemplate redis;
  private final ObjectMapper mapper;
  private final Duration ttl;
  public CacheService(StringRedisTemplate redis, ObjectMapper mapper, @Value("${app.cache.ttl-seconds}") long seconds){this.redis=redis;this.mapper=mapper;this.ttl=Duration.ofSeconds(seconds);} 
  public String key(String tenantId, String env, String flagKey){ return "flag:" + tenantId + ":" + env + ":" + flagKey; }
  public Optional<FlagResponse> getFlag(String tenantId, String env, String flagKey){
    try { String raw = redis.opsForValue().get(key(tenantId, env, flagKey)); return raw == null ? Optional.empty() : Optional.of(mapper.readValue(raw, FlagResponse.class)); } catch(Exception e){ return Optional.empty(); }
  }
  public void put(FlagResponse response){
    try { redis.opsForValue().set(key(response.tenantId(), response.environment(), response.flagKey()), mapper.writeValueAsString(response), ttl); } catch(Exception ignored) {}
  }
  public void evict(String tenantId, String env, String flagKey){ try { redis.delete(key(tenantId, env, flagKey)); } catch(Exception ignored) {} }
}
