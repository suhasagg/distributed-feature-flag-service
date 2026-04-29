package com.example.flags.controller;

import com.example.flags.dto.*;
import com.example.flags.service.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flags")
public class FeatureFlagController {
  private final FlagService flags;
  private final RateLimiterService limiter;
  public FeatureFlagController(FlagService flags, RateLimiterService limiter){this.flags=flags;this.limiter=limiter;}

  @PostMapping
  public FlagResponse create(@RequestHeader("X-Tenant-ID") String tenantId, @RequestHeader(value="X-Actor", required=false) String actor, @RequestBody CreateFlagRequest request){
    limiter.check(tenantId, "write"); return flags.create(tenantId, actor, request);
  }

  @GetMapping("/{environment}")
  public List<FlagResponse> list(@RequestHeader("X-Tenant-ID") String tenantId, @PathVariable("environment") String environment){
    limiter.check(tenantId, "read"); return flags.list(tenantId, environment);
  }

  @GetMapping("/{environment}/{flagKey}")
  public FlagResponse get(@RequestHeader("X-Tenant-ID") String tenantId, @PathVariable("environment") String environment, @PathVariable("flagKey") String flagKey){
    limiter.check(tenantId, "read"); return flags.get(tenantId, environment, flagKey);
  }

  @PutMapping("/{environment}/{flagKey}")
  public FlagResponse update(@RequestHeader("X-Tenant-ID") String tenantId, @RequestHeader(value="X-Actor", required=false) String actor, @PathVariable("environment") String environment, @PathVariable("flagKey") String flagKey, @RequestBody UpdateFlagRequest request){
    limiter.check(tenantId, "write"); return flags.update(tenantId, environment, flagKey, actor, request);
  }

  @DeleteMapping("/{environment}/{flagKey}")
  public void delete(@RequestHeader("X-Tenant-ID") String tenantId, @RequestHeader(value="X-Actor", required=false) String actor, @PathVariable("environment") String environment, @PathVariable("flagKey") String flagKey){
    limiter.check(tenantId, "write"); flags.delete(tenantId, environment, flagKey, actor);
  }

  @GetMapping("/{environment}/{flagKey}/audit")
  public List<AuditResponse> audit(@RequestHeader("X-Tenant-ID") String tenantId, @PathVariable("flagKey") String flagKey){
    return flags.auditTrail(tenantId, flagKey);
  }
}
