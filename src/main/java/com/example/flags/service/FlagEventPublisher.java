package com.example.flags.service;

import com.example.flags.messaging.FlagChangeEvent;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FlagEventPublisher {
  private final KafkaTemplate<String, Object> kafka;
  private final String topic;
  public FlagEventPublisher(KafkaTemplate<String, Object> kafka, @Value("${app.topics.flag-events}") String topic){this.kafka=kafka;this.topic=topic;}
  public void publish(String tenantId, String environment, String flagKey, String action, long version){
    var event = new FlagChangeEvent(UUID.randomUUID().toString(), tenantId, environment, flagKey, action, version, System.currentTimeMillis());
    kafka.send(topic, tenantId + ":" + environment + ":" + flagKey, event);
  }
}
