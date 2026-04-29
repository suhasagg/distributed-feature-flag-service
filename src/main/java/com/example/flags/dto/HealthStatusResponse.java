package com.example.flags.dto;

import java.time.Instant;
import java.util.Map;

public record HealthStatusResponse(String status, Instant checkedAt, Map<String, String> dependencies) {}
