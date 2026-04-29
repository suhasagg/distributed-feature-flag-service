package com.example.flags.controller;

import com.example.flags.dto.HealthStatusResponse;
import java.time.Instant;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class HealthController {
  private final DataSource ds; private final StringRedisTemplate redis; private final KafkaTemplate<String,Object> kafka;
  public HealthController(DataSource ds, StringRedisTemplate redis, KafkaTemplate<String,Object> kafka){this.ds=ds;this.redis=redis;this.kafka=kafka;}
  @GetMapping("/health")
  public HealthStatusResponse health(){
    Map<String,String> deps = new LinkedHashMap<>();
    try(var c = ds.getConnection()){ deps.put("postgres", c.isValid(1) ? "UP" : "DOWN"); } catch(Exception e){ deps.put("postgres", "DOWN"); }
    try{ redis.opsForValue().set("health:last", Instant.now().toString()); deps.put("redis", "UP"); } catch(Exception e){ deps.put("redis", "DOWN"); }
    try{ kafka.partitionsFor("feature-flag-events"); deps.put("kafka", "UP"); } catch(Exception e){ deps.put("kafka", "DOWN"); }
    String overall = deps.values().stream().allMatch("UP"::equals) ? "UP" : "DEGRADED";
    return new HealthStatusResponse(overall, Instant.now(), deps);
  }
}
