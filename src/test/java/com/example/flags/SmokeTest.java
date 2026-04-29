package com.example.flags;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
  "spring.datasource.url=jdbc:h2:mem:testdb",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "spring.kafka.bootstrap-servers=localhost:9092"
})
class SmokeTest {
  @Test void contextLoads() {}
}
