package com.example.flags.dto;

import java.util.Map;

public record EvaluateRequest(
    String environment,
    String flagKey,
    String userId,
    Map<String, String> attributes
) {}
