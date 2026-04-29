package com.example.flags.service;

import com.example.flags.domain.FeatureFlagEntity;
import com.example.flags.dto.FlagResponse;

public class FlagMapper {
  public static FlagResponse toResponse(FeatureFlagEntity e) {
    return new FlagResponse(e.getId(), e.getTenantId(), e.getEnvironment(), e.getFlagKey(), e.getDescription(), e.isEnabled(), e.getRolloutPercentage(), e.getRulesJson(), e.getVersion(), e.getCreatedAt(), e.getUpdatedAt());
  }
}
