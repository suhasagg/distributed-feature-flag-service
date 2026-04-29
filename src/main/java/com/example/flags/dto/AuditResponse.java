package com.example.flags.dto;

import java.time.Instant;

public record AuditResponse(String actor, String action, String payloadJson, Instant createdAt) {}
