Distributed Feature Flag and Experimentation Service

A multi-tenant, low-latency feature flag and experimentation platform designed to demonstrate production-oriented architecture for large-scale backend systems.

This project implements:

PostgreSQL as source of truth
Redis for low-latency evaluation cache
Kafka for flag-change event propagation
Spring Boot for API layer
Multi-tenant flag isolation
Audit trail for flag changes
Percentage rollout evaluation

## 1. Problem Statement

Modern SaaS companies need a safe way to release features gradually.

The system should support:

Create and update feature flags
Enable or disable features per environment
Evaluate flags with low latency
Support percentage-based rollout
Support multi-tenant isolation
Track audit history of flag changes
Propagate flag updates asynchronously
Scale reads much more than writes

Typical use cases:

Gradual rollout: 5%, 25%, 50%, 100%
Kill switch for risky features
A/B testing
Tenant-specific feature enablement
Environment-specific config: dev, staging, prod

## 2. High-Level Architecture
                         +----------------------+
                         |       Clients        |
                         |  Web / Mobile / SDK  |
                         +----------+-----------+
                                    |
                                    v
                         +----------+-----------+
                         |    Spring Boot API   |
                         | Flags + Evaluation   |
                         +----+------------+----+
                              |            |
                    Write path|            |Read path
                              |            |
                              v            v
                       +------+---+   +----+------+
                       |PostgreSQL|   |   Redis   |
                       |Source of |   | Eval Cache|
                       |Truth     |   | Hot Flags |
                       +------+---+   +----+------+
                              |
                              v
                       +------+----------------+
                       | Kafka                 |
                       | flag-change-events    |
                       +------+----------------+
                              |
                              v
                    +---------+-------------+
                    | Flag Change Consumer  |
                    | Cache invalidation    |
                    | Propagation hook      |
                    +-----------------------+

## 3. Core Components

### 3.1 Spring Boot API

Responsibilities:

Exposes REST APIs
Validates tenant headers
Handles flag CRUD
Handles flag evaluation
Writes audit logs
Publishes Kafka events
Uses Redis cache for fast reads

### 3.2 PostgreSQL

PostgreSQL is the source of truth.

Stores:

Feature flags
Tenant ID
Environment
Flag key
Enabled status
Rollout percentage
Rules JSON
Audit trail

Why PostgreSQL:

Strong consistency for writes
Transactional updates
Reliable audit history
Simple operational model

### 3.3 Redis

Redis is used for:

Fast flag evaluation
Caching hot flag definitions
Reducing PostgreSQL read load
Future rate limiting support

Example cache key:

flag:{tenantId}:{environment}:{flagKey}

Example:

flag:tenant-a:prod:new-checkout



Kafka is used for asynchronous propagation of flag updates.

Topic:

flag-change-events

Events:

{
  "tenantId": "tenant-a",
  "environment": "prod",
  "flagKey": "new-checkout",
  "action": "UPDATE"
}

Why Kafka:

Decouples write path from propagation
Allows future SDK sync workers
Enables audit/event pipelines
Supports cache invalidation
Supports replay of flag changes

## 4. Data Model

### 4.1 Feature Flag Table
feature_flags

Fields:

id
tenant_id
environment
flag_key
description
enabled
rollout_percentage
rules_json
created_at
updated_at

Unique constraint:

tenant_id + environment + flag_key

This prevents duplicate flags inside one tenant/environment.

### 4.2 Audit Table
flag_audit_events

Fields:

id
tenant_id
environment
flag_key
actor
action
old_value
new_value
created_at

Used for:

Compliance
Debugging
Rollback analysis
Interview discussion on enterprise readiness

## 5. Consistency Model

The system uses a mixed consistency model.

Strong consistency

Used for:

Creating flags
Updating flags
Disabling flags
Audit trail writes

PostgreSQL remains canonical.

Eventual consistency

Used for:

Redis cache update
Kafka propagation
Future SDK/client cache refresh

Why this trade-off works:

Flag writes are relatively low volume
Flag reads are very high volume
Evaluation path must be fast
Cache can be invalidated after writes
PostgreSQL remains source of truth


## 6. Feature Flag Evaluation Logic

Evaluation flow:

## 1. Receive tenant, environment, flagKey, userId
## 2. Check Redis cache
## 3. If cache miss, load flag from PostgreSQL
## 4. If flag missing, return false
## 5. If flag disabled, return false
## 6. If rollout = 100, return true
## 7. If rollout = 0, return false
## 8. Hash userId + flagKey
## 9. Convert hash to bucket 0-99
## 10. Enable if bucket < rolloutPercentage

Example:

user-123 + new-checkout -> hash bucket 17
rolloutPercentage = 25
17 < 25 => enabled

This gives deterministic rollout.

Same user gets the same result consistently.

## 7. API List

### 7.1 Health Check
curl http://localhost:8080/health

Response:

{
  "status": "UP"
}

### 7.2 Create Feature Flag
curl -X POST http://localhost:8080/flags \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-a" \
  -H "X-Actor: suhas@example.com" \
  -d '{
    "environment": "prod",
    "flagKey": "new-checkout",
    "description": "Gradual rollout of new checkout flow",
    "enabled": true,
    "rolloutPercentage": 25,
    "rulesJson": "{\"country\":\"IN\"}"
  }'

Response:

{
  "id": 1,
  "tenantId": "tenant-a",
  "environment": "prod",
  "flagKey": "new-checkout",
  "description": "Gradual rollout of new checkout flow",
  "enabled": true,
  "rolloutPercentage": 25,
  "rulesJson": "{\"country\":\"IN\"}"
}



### 7.3 List Flags
curl http://localhost:8080/flags/prod \
  -H "X-Tenant-ID: tenant-a"

Response:

[
  {
    "id": 1,
    "tenantId": "tenant-a",
    "environment": "prod",
    "flagKey": "new-checkout",
    "enabled": true,
    "rolloutPercentage": 25
  }
]


### 7.4 Get Single Flag
curl http://localhost:8080/flags/prod/new-checkout \
  -H "X-Tenant-ID: tenant-a"

Response:

{
  "id": 1,
  "tenantId": "tenant-a",
  "environment": "prod",
  "flagKey": "new-checkout",
  "enabled": true,
  "rolloutPercentage": 25
}


### 7.5 Evaluate Flag
curl -X POST http://localhost:8080/evaluate \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-a" \
  -d '{
    "environment": "prod",
    "flagKey": "new-checkout",
    "userId": "user-123",
    "attributes": {
      "country": "IN",
      "plan": "premium"
    }
  }'

Response:

{
  "flagKey": "new-checkout",
  "enabled": true,
  "reason": "ROLLOUT_MATCH"
}

Possible reasons:

FLAG_NOT_FOUND
FLAG_DISABLED
ROLLOUT_MATCH
ROLLOUT_MISS


### 7.6 Update Flag

curl -X PUT http://localhost:8080/flags/prod/new-checkout \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-a" \
  -H "X-Actor: suhas@example.com" \
  -d '{
    "rolloutPercentage": 50
  }'

Response:

{
  "id": 1,
  "tenantId": "tenant-a",
  "environment": "prod",
  "flagKey": "new-checkout",
  "enabled": true,
  "rolloutPercentage": 50
}


### 7.7 Audit Trail

curl http://localhost:8080/flags/prod/new-checkout/audit \
  -H "X-Tenant-ID: tenant-a"

Response:

[
  {
    "actor": "suhas@example.com",
    "action": "CREATE",
    "flagKey": "new-checkout"
  },
  {
    "actor": "suhas@example.com",
    "action": "UPDATE",
    "flagKey": "new-checkout"
  }
]

## 8. Multi-Tenancy Strategy

Tenant is identified using:

X-Tenant-ID

Tenant isolation applies to:

Create flag
List flags
Get flag
Update flag
Evaluate flag
Audit trail
Redis cache key
Kafka event payload

Example:

tenant-a:prod:new-checkout
tenant-b:prod:new-checkout

Both tenants can have the same flag key safely.

## 9. Production Readiness Analysis
### 9.1 Scalability
API layer

The API is stateless.

Scale horizontally using:

multiple Spring Boot instances behind load balancer

Scale signals:

CPU usage
request latency
p95/p99 latency
request rate
Redis hit ratio
Read scalability

Evaluation APIs are read-heavy.

Optimization:

Redis cache for hot flags
Local in-memory cache inside SDK
CDN/edge config snapshots for public client SDKs
Batch evaluation API
Streaming flag updates to SDKs

Target:

p95 evaluation latency < 10 ms from server cache
p95 SDK local evaluation < 1 ms
Write scalability

Writes are low volume compared to reads.

Still scalable with:

PostgreSQL primary
Kafka event propagation
Async cache invalidation
Audit trail partitioning by tenant/date

### 9.2 Resilience
PostgreSQL failure

Impact:

Flag writes fail
Cache-backed evaluations may continue temporarily

Mitigation:

Read replicas
automated failover
backups
connection pool limits
circuit breaker
Redis failure

Impact:

Evaluation falls back to PostgreSQL
Latency increases

Mitigation:

Redis cluster
short DB fallback path
local cache fallback
circuit breaker to avoid Redis timeout storms
Kafka failure

Impact:

Flag update succeeds in DB
Propagation events may be delayed

Mitigation:

transactional outbox pattern
retry publisher
dead-letter topic
idempotent consumers

Recommended production improvement:

Write DB transaction:
## 1. update feature_flags
## 2. insert outbox_event

Separate worker:
## 1. read outbox
## 2. publish to Kafka
## 3. mark event published

This prevents losing Kafka events after DB commit.

### 9.3 Security

Production system should add:

JWT authentication
tenant authorization
RBAC
admin-only flag writes
read-only SDK tokens
audit logs for every mutation
encryption in transit
encryption at rest
secrets in Vault/AWS Secrets Manager
request validation
rate limiting
IP allowlisting for admin APIs

Example roles:

FLAG_ADMIN
FLAG_VIEWER
SDK_CLIENT
ORG_ADMIN

### 9.4 Observability

Add metrics:

flag_evaluation_requests_total
flag_evaluation_latency_ms
flag_cache_hit_ratio
flag_update_events_total
kafka_publish_failures_total
kafka_consumer_lag
postgres_query_latency
redis_latency
audit_write_failures

Dashboards:

API latency
Redis cache hit ratio
Kafka lag
PostgreSQL connections
flag update rate
evaluation QPS
error rate by tenant

Tracing:

API request -> Redis -> PostgreSQL -> Kafka

Use:

OpenTelemetry
Prometheus
Grafana
Loki/ELK
Jaeger/Tempo


### 9.5 Performance

Key optimizations:

Redis cache for hot flags
PostgreSQL index on tenant/environment/flagKey
avoid JSON parsing on hot path
precompile rules
batch evaluation endpoint
local SDK cache
async Kafka propagation
connection pooling

Recommended DB index:

CREATE UNIQUE INDEX idx_flags_tenant_env_key
ON feature_flags (tenant_id, environment, flag_key);

Audit index:

CREATE INDEX idx_audit_tenant_env_key
ON flag_audit_events (tenant_id, environment, flag_key, created_at DESC);


### 9.6 Operations

Deployment strategy:

Docker for local
Kubernetes for production
Helm chart
readiness/liveness probes
rolling deployment
blue-green deployment
canary deployment

Health checks:

/health
/ready
/live

Rollback strategy:

keep previous app version
database migrations backward compatible
Kafka event schema versioning
feature flags for risky features


### 9.7 SLA Considerations

Target SLA:

99.95% availability

To achieve:

multi-AZ deployment
Redis cluster
PostgreSQL HA
Kafka replication
load balancer health checks
graceful degradation
local SDK fallback
cache warmup
automated rollback
alerting on SLO burn rate

Example SLOs:

99.9% evaluations < 50 ms
99.95% API availability
Kafka propagation lag < 5 seconds
Redis cache hit ratio > 90%


## 10. Cost Optimization

### 10.1 API Layer

Cost controls:

stateless autoscaling
right-size CPU/memory
use spot instances for non-critical workers
use autoscaling based on RPS/latency
separate read-heavy evaluation service from admin write service


### 10.2 PostgreSQL

Cost controls:

keep canonical data compact
archive old audit logs
partition audit table by month
use read replicas only when needed
avoid storing large JSON blobs unnecessarily
tune indexes carefully


### 10.3 Redis

Cost controls:

cache only hot flags
use TTL
avoid caching huge tenant configs forever
compress large config snapshots
use local SDK cache to reduce Redis calls
monitor memory fragmentation


### 10.4 Kafka

Cost controls:

avoid excessive partitions
compact flag-change topic if needed
tune retention period
use managed Kafka only when operationally justified
use outbox polling for smaller systems before Kafka


### 10.5 Cloud Cost Strategy

Use:

reserved instances for steady baseline
spot instances for stateless workers
autoscaling for API layer
scheduled scaling for predictable traffic
log retention policies
separate prod/staging resource sizes
cold storage for old audit logs


## 11. Advanced Production Enhancements

### 11.1 SDK Architecture

In production, clients should not call server for every evaluation.

Better model:

SDK downloads tenant flag snapshot
SDK evaluates locally
Server streams updates
SDK refreshes local cache

Benefits:

ultra-low latency
lower infra cost
works during network failure
avoids central bottleneck


### 11.2 Streaming Updates

Use:

Server-Sent Events
WebSocket
gRPC stream
Kafka-to-edge fanout service

Flow:

Admin updates flag
PostgreSQL commit
Kafka event emitted
Streaming service receives event
SDKs receive update
Local cache refreshed


### 11.3 Rule Engine

Current system supports basic JSON rules.

Production rule engine can support:

country == IN
plan == premium
appVersion >= 10.2
device == android
email endsWith @company.com
userId in allowlist

Rule evaluation should be:

deterministic
fast
precompiled
observable
testable


### 11.4 Experimentation Support

Add experiment table:

experiments
experiment_variants
experiment_assignments
experiment_metrics

Support:

A/B testing
multivariate tests
sticky assignment
exposure logging
conversion tracking
statistical significance


### 11.5 Audit and Compliance

For enterprise readiness:

immutable audit log
actor identity
before/after value
approval workflow
change reason
ticket ID
rollback support
export logs to SIEM



