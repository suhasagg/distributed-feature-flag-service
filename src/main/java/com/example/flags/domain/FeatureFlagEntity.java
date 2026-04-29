package com.example.flags.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "feature_flags", indexes = {
    @Index(name = "idx_flags_tenant_key", columnList = "tenantId,flagKey", unique = true),
    @Index(name = "idx_flags_tenant_env", columnList = "tenantId,environment")
})
public class FeatureFlagEntity {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private String id;
  private String tenantId;
  private String environment;
  private String flagKey;
  private String description;
  private boolean enabled;
  private int rolloutPercentage;
  private String rulesJson;
  private long version;
  private Instant createdAt;
  private Instant updatedAt;

  @PrePersist void onCreate(){ createdAt = Instant.now(); updatedAt = createdAt; version = 1; }
  @PreUpdate void onUpdate(){ updatedAt = Instant.now(); version++; }

  public String getId(){return id;} public void setId(String id){this.id=id;}
  public String getTenantId(){return tenantId;} public void setTenantId(String tenantId){this.tenantId=tenantId;}
  public String getEnvironment(){return environment;} public void setEnvironment(String environment){this.environment=environment;}
  public String getFlagKey(){return flagKey;} public void setFlagKey(String flagKey){this.flagKey=flagKey;}
  public String getDescription(){return description;} public void setDescription(String description){this.description=description;}
  public boolean isEnabled(){return enabled;} public void setEnabled(boolean enabled){this.enabled=enabled;}
  public int getRolloutPercentage(){return rolloutPercentage;} public void setRolloutPercentage(int rolloutPercentage){this.rolloutPercentage=rolloutPercentage;}
  public String getRulesJson(){return rulesJson;} public void setRulesJson(String rulesJson){this.rulesJson=rulesJson;}
  public long getVersion(){return version;} public void setVersion(long version){this.version=version;}
  public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant createdAt){this.createdAt=createdAt;}
  public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant updatedAt){this.updatedAt=updatedAt;}
}
