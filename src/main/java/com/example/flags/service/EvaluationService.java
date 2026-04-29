package com.example.flags.service;

import com.example.flags.dto.*;
import org.springframework.stereotype.Service;

@Service
public class EvaluationService {
  private final FlagService flags;
  public EvaluationService(FlagService flags){this.flags=flags;}

  public EvaluateResponse evaluate(String tenantId, EvaluateRequest request){
    var flag = flags.get(tenantId, request.environment(), request.flagKey());
    if (!flag.enabled()) return new EvaluateResponse(flag.flagKey(), false, "FLAG_DISABLED", flag.version());
    int bucket = Math.floorMod((tenantId + ":" + flag.flagKey() + ":" + request.userId()).hashCode(), 100);
    boolean enabled = bucket < flag.rolloutPercentage();
    return new EvaluateResponse(flag.flagKey(), enabled, enabled ? "ROLLOUT_MATCH" : "ROLLOUT_MISS", flag.version());
  }
}
