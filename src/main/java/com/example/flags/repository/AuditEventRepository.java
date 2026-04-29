package com.example.flags.repository;

import com.example.flags.domain.AuditEventEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, String> {
  List<AuditEventEntity> findTop50ByTenantIdAndFlagKeyOrderByCreatedAtDesc(String tenantId, String flagKey);
}
