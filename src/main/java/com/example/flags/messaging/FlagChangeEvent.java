package com.example.flags.messaging;

public record FlagChangeEvent(
    String eventId,
    String tenantId,
    String environment,
    String flagKey,
    String action,
    long version,
    long occurredAtEpochMs
) {}
