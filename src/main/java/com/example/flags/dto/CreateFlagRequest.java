package com.example.flags.dto;

public record CreateFlagRequest(
    String environment,
    String flagKey,
    String description,
    boolean enabled,
    int rolloutPercentage,
    String rulesJson
) {}
