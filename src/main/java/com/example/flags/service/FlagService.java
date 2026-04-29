package com.example.flags.service;

import com.example.flags.domain.*;
import com.example.flags.dto.*;
import com.example.flags.exception.*;
import com.example.flags.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlagService {
  private final FeatureFlagRepository flags;
  private final AuditEventRepository audits;
  private final FlagEventPublisher publisher;
  private final CacheService cache;

  public FlagService(FeatureFlagRepository flags, AuditEventRepository audits, FlagEventPublisher publisher, CacheService cache){
    this.flags=flags; this.audits=audits; this.publisher=publisher; this.cache=cache;
  }

  @Transactional
  public FlagResponse create(String tenantId, String actor, CreateFlagRequest request){
    validate(request.environment(), request.flagKey(), request.rolloutPercentage());
    flags.findByTenantIdAndEnvironmentAndFlagKey(tenantId, request.environment(), request.flagKey()).ifPresent(f -> { throw new BadRequestException("flag already exists"); });
    var e = new FeatureFlagEntity();
    e.setTenantId(tenantId); e.setEnvironment(request.environment()); e.setFlagKey(request.flagKey());
    e.setDescription(request.description()); e.setEnabled(request.enabled()); e.setRolloutPercentage(request.rolloutPercentage()); e.setRulesJson(request.rulesJson());
    var saved = flags.save(e);
    audit(tenantId, request.flagKey(), actor, "CREATE", "created rollout=" + request.rolloutPercentage());
    var response = FlagMapper.toResponse(saved);
    cache.put(response);
    publisher.publish(tenantId, request.environment(), request.flagKey(), "UPSERT", saved.getVersion());
    return response;
  }

  @Transactional
  public FlagResponse update(String tenantId, String env, String key, String actor, UpdateFlagRequest request){
    var e = flags.findByTenantIdAndEnvironmentAndFlagKey(tenantId, env, key).orElseThrow(() -> new NotFoundException("flag not found"));
    if (request.rolloutPercentage() != null) {
      if (request.rolloutPercentage() < 0 || request.rolloutPercentage() > 100) throw new BadRequestException("rolloutPercentage must be 0..100");
      e.setRolloutPercentage(request.rolloutPercentage());
    }
    if (request.enabled() != null) e.setEnabled(request.enabled());
    if (request.description() != null) e.setDescription(request.description());
    if (request.rulesJson() != null) e.setRulesJson(request.rulesJson());
    var saved = flags.save(e);
    audit(tenantId, key, actor, "UPDATE", "updated version=" + saved.getVersion());
    cache.evict(tenantId, env, key);
    var response = FlagMapper.toResponse(saved);
    cache.put(response);
    publisher.publish(tenantId, env, key, "UPSERT", saved.getVersion());
    return response;
  }

  public FlagResponse get(String tenantId, String env, String key){
    return cache.getFlag(tenantId, env, key).orElseGet(() -> {
      var e = flags.findByTenantIdAndEnvironmentAndFlagKey(tenantId, env, key).orElseThrow(() -> new NotFoundException("flag not found"));
      var r = FlagMapper.toResponse(e); cache.put(r); return r;
    });
  }

  public List<FlagResponse> list(String tenantId, String env){ return flags.findByTenantIdAndEnvironment(tenantId, env).stream().map(FlagMapper::toResponse).toList(); }

  @Transactional
  public void delete(String tenantId, String env, String key, String actor){
    var e = flags.findByTenantIdAndEnvironmentAndFlagKey(tenantId, env, key).orElseThrow(() -> new NotFoundException("flag not found"));
    flags.delete(e); cache.evict(tenantId, env, key); audit(tenantId, key, actor, "DELETE", "deleted"); publisher.publish(tenantId, env, key, "DELETE", e.getVersion());
  }

  public List<AuditResponse> auditTrail(String tenantId, String key){ return audits.findTop50ByTenantIdAndFlagKeyOrderByCreatedAtDesc(tenantId, key).stream().map(a -> new AuditResponse(a.getActor(), a.getAction(), a.getPayloadJson(), a.getCreatedAt())).toList(); }

  private void validate(String env, String key, int pct){ if(env==null || env.isBlank()) throw new BadRequestException("environment required"); if(key==null || key.isBlank()) throw new BadRequestException("flagKey required"); if(pct<0 || pct>100) throw new BadRequestException("rolloutPercentage must be 0..100"); }
  private void audit(String tenantId, String key, String actor, String action, String payload){ var a = new AuditEventEntity(); a.setTenantId(tenantId); a.setFlagKey(key); a.setActor(actor == null ? "unknown" : actor); a.setAction(action); a.setPayloadJson(payload); audits.save(a); }
}
