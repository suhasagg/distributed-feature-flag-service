package com.example.flags.config;

import com.example.flags.messaging.FlagChangeEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaConfig {
  @Bean
  NewTopic flagEventsTopic(@Value("${app.topics.flag-events}") String topic) {
    return TopicBuilder.name(topic).partitions(6).replicas(1).build();
  }
}
