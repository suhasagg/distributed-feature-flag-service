package com.example.flags.repository;

import com.example.flags.domain.FeatureFlagEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlagEntity, String> {
  Optional<FeatureFlagEntity> findByTenantIdAndEnvironmentAndFlagKey(String tenantId, String environment, String flagKey);
  List<FeatureFlagEntity> findByTenantIdAndEnvironment(String tenantId, String environment);
}
