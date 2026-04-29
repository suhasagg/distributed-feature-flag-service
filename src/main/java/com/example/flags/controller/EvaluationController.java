package com.example.flags.controller;

import com.example.flags.dto.*;
import com.example.flags.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/evaluate")
public class EvaluationController {
  private final EvaluationService evaluator;
  private final RateLimiterService limiter;
  public EvaluationController(EvaluationService evaluator, RateLimiterService limiter){this.evaluator=evaluator;this.limiter=limiter;}
  @PostMapping
  public EvaluateResponse evaluate(@RequestHeader("X-Tenant-ID") String tenantId, @RequestBody EvaluateRequest request){
    limiter.check(tenantId, "eval"); return evaluator.evaluate(tenantId, request);
  }
}
