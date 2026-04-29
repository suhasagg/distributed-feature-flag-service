package com.example.flags.dto;

public record UpdateFlagRequest(
    Boolean enabled,
    Integer rolloutPercentage,
    String description,
    String rulesJson
) {}
