package com.example.flags.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events", indexes = @Index(name = "idx_audit_tenant_flag", columnList = "tenantId,flagKey,createdAt"))
public class AuditEventEntity {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private String id;
  private String tenantId;
  private String flagKey;
  private String actor;
  private String action;
  @Column(length = 4000)
  private String payloadJson;
  private Instant createdAt;
  @PrePersist void onCreate(){ createdAt = Instant.now(); }
  public String getId(){return id;} public void setId(String id){this.id=id;}
  public String getTenantId(){return tenantId;} public void setTenantId(String tenantId){this.tenantId=tenantId;}
  public String getFlagKey(){return flagKey;} public void setFlagKey(String flagKey){this.flagKey=flagKey;}
  public String getActor(){return actor;} public void setActor(String actor){this.actor=actor;}
  public String getAction(){return action;} public void setAction(String action){this.action=action;}
  public String getPayloadJson(){return payloadJson;} public void setPayloadJson(String payloadJson){this.payloadJson=payloadJson;}
  public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant createdAt){this.createdAt=createdAt;}
}
