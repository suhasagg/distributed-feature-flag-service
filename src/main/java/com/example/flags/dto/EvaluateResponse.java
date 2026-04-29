package com.example.flags.dto;

public record EvaluateResponse(
    String flagKey,
    boolean enabled,
    String reason,
    long version
) {}
