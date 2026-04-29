package com.example.flags.dto;

import java.time.Instant;

public record FlagResponse(
    String id,
    String tenantId,
    String environment,
    String flagKey,
    String description,
    boolean enabled,
    int rolloutPercentage,
    String rulesJson,
    long version,
    Instant createdAt,
    Instant updatedAt
) {}
