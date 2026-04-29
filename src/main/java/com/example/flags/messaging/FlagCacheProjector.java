package com.example.flags.messaging;

import com.example.flags.service.CacheService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FlagCacheProjector {
  private final CacheService cache;
  public FlagCacheProjector(CacheService cache){this.cache=cache;}
  @KafkaListener(topics = "${app.topics.flag-events}", groupId = "flag-cache-projectors")
  public void onEvent(FlagChangeEvent event){
    cache.evict(event.tenantId(), event.environment(), event.flagKey());
  }
}
