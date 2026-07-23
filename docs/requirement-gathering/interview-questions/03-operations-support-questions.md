# URL Shortener Service - Operations/Support Interview Questions

## Overview
This document contains preliminary interview questions for Operations and Support teams. These questions focus on SLAs, support model, incident management, reliability, and operational scalability.

---

## 1. Service Level Agreements (SLAs) & Reliability

### 1.1 SLA Requirements
- **Q1.1.1**: What's the required uptime SLA? (99%, 99.5%, 99.9%, 99.99%)
- **Q1.1.2**: What's the definition of "downtime"? (partial outage vs. full outage)
- **Q1.1.3**: Expected incident response time? (15 min, 30 min, 1 hour, 4 hours)
- **Q1.1.4**: Expected incident resolution time for critical issues? (1 hour, 4 hours, 24 hours)
- **Q1.1.5**: Grace period for SLA calculations? (excludes scheduled maintenance, etc.)
- **Q1.1.6**: SLA credits/refunds policy if breached? (% credit, terms)

### 1.2 Performance Targets (SLIs)
- **Q1.2.1**: P50, P95, P99 latency targets for URL creation? (ms)
- **Q1.2.2**: P50, P95, P99 latency targets for redirects/clicks? (ms)
- **Q1.2.3**: Error rate targets? (< 0.1%, < 1%, etc.)
- **Q1.2.4**: Throughput targets? (requests/sec at peak)
- **Q1.2.5**: Database/cache availability targets? (99.9%, 99.99%)
- **Q1.2.6**: API availability targets? (separate from infrastructure)

### 1.3 Disaster Recovery
- **Q1.3.1**: RPO (Recovery Point Objective)? (how much data loss is acceptable - hours, minutes)
- **Q1.3.2**: RTO (Recovery Time Objective)? (how long to restore service - minutes, hours)
- **Q1.3.3**: Backup frequency? (hourly, daily, continuous)
- **Q1.3.4**: Backup retention? (7 days, 30 days, 1 year)
- **Q1.3.5**: Geographic redundancy requirements? (multi-region, multi-AZ, multi-cloud)
- **Q1.3.6**: Failover strategy? (automatic, manual, time to failover)
- **Q1.3.7**: Data replication lag tolerance?

---

## 2. Support & Customer Success Model

### 2.1 Support Tiers
- **Q2.1.1**: Support tiers needed? (basic/free, standard, premium, 24/7 premium)
- **Q2.1.2**: Support channels? (email, chat, phone, documentation, community forum)
- **Q2.1.3**: Expected response time by tier?
  - P1 (critical): 15 min, 30 min, 1 hour?
  - P2 (high): 1 hour, 4 hours, 8 hours?
  - P3 (medium): 4 hours, 24 hours, 48 hours?
  - P4 (low): 24 hours, 48 hours, 1 week?
- **Q2.1.4**: Expected resolution time by tier?
- **Q2.1.5**: Staffing model? (in-house, outsourced, hybrid)
- **Q2.1.6**: Support languages needed? (English only, multilingual)
- **Q2.1.7**: Support hours? (9-5, 24/5, 24/7)

### 2.2 Incident Management
- **Q2.2.1**: Incident severity classification? (P1, P2, P3, P4 definitions)
- **Q2.2.2**: Escalation procedures? (who, when, how)
- **Q2.2.3**: War room/incident command structure?
- **Q2.2.4**: Incident commander responsibilities?
- **Q2.2.5**: Communication protocol during incidents?
- **Q2.2.6**: Postmortem/RCA (Root Cause Analysis) process? (when required, timeline)
- **Q2.2.7**: Public status page requirements? (transparency to customers)

### 2.3 Customer Communication
- **Q2.3.1**: How often should we update customers during incidents? (every 15 min, 30 min, 1 hour)
- **Q2.3.2**: Post-incident communication required? (what happened, impact, prevention)
- **Q2.3.3**: Proactive monitoring and notifications needed?
- **Q2.3.4**: Alert thresholds? (what triggers customer notification vs. internal-only)
- **Q2.3.5**: Scheduled maintenance communication? (advance notice period, preferred windows)
- **Q2.3.6**: Customer incident notification template/format?

---

## 3. Operational Observability

### 3.1 Monitoring & Alerting
- **Q3.1.1**: Monitoring stack? (Datadog, New Relic, Prometheus, CloudWatch, custom)
- **Q3.1.2**: Key metrics to monitor?
  - System: CPU, memory, disk, network, connections
  - Application: request rate, error rate, latency, throughput
  - Database: query latency, connection pool, replication lag
  - Business: URLs created, redirects, API calls, active users
- **Q3.1.3**: Alert thresholds? (when to notify ops team)
- **Q3.1.4**: Alert escalation rules? (who gets notified when)
- **Q3.1.5**: Alert fatigue management? (how to reduce false positives)
- **Q3.1.6**: Alerting channels? (email, Slack, PagerDuty, SMS)

### 3.2 Logging & Tracing
- **Q3.2.1**: Logging requirements? (what to log, verbosity levels)
- **Q3.2.2**: Log retention period? (7 days, 30 days, 1 year)
- **Q3.2.3**: Structured logging? (JSON, key-value pairs, or unstructured)
- **Q3.2.4**: Log aggregation tool? (ELK, Splunk, Datadog, CloudWatch)
- **Q3.2.5**: Distributed tracing? (Jaeger, Zipkin, Datadog APM - trace requests across services)
- **Q3.2.6**: Audit logging? (compliance requirements - who accessed what, when)
- **Q3.2.7**: PII/sensitive data in logs? (masking, redaction strategy)

### 3.3 Dashboards & Reporting
- **Q3.3.1**: Real-time dashboard requirements?
- **Q3.3.2**: Operational dashboards needed? (for ops team - health, alerts, incidents)
- **Q3.3.3**: Business dashboards needed? (for executives - traffic, revenue, users)
- **Q3.3.4**: Customer-facing dashboards? (status page, health indicators)
- **Q3.3.5**: Reporting cadence? (daily, weekly, monthly reports)
- **Q3.3.6**: Post-mortem reporting format? (template, what to include)

---

## 4. Capacity Planning & Scalability

### 4.1 Capacity Management
- **Q4.1.1**: Current baseline capacity? (baseline traffic, QPS estimate)
- **Q4.1.2**: Growth projections? (traffic doubling, 10x growth timelines)
- **Q4.1.3**: Capacity headroom targets? (% over baseline - 20%, 30%, 50%)
- **Q4.1.4**: Auto-scaling policies? (when to scale up/down, min/max limits)
- **Q4.1.5**: Scaling limits? (max capacity constraints, bottlenecks)
- **Q4.1.6**: Cost implications of scaling?
- **Q4.1.7**: Capacity planning cycle? (quarterly, annual reviews)

### 4.2 Load Testing
- **Q4.2.1**: Load testing frequency? (before launch, quarterly, after major changes)
- **Q4.2.2**: Load testing scenarios?
  - Baseline load test (expected normal traffic)
  - 2x load test
  - 10x load test
  - Spike test (sudden traffic spike)
  - Sustained load test (24+ hour duration)
- **Q4.2.3**: Peak traffic expectations? (events that cause spikes)
- **Q4.2.4**: Performance targets under load? (maintain SLA targets)
- **Q4.2.5**: Failure scenario testing? (database down, cache down, region down)

---

## 5. Maintenance & Operational Changes

### 5.1 Maintenance Windows
- **Q5.1.1**: Planned maintenance allowed? (when, frequency, duration limits)
- **Q5.1.2**: Maintenance window preferences? (day/time, weekday vs. weekend)
- **Q5.1.3**: Maximum maintenance duration? (30 min, 1 hour, 4 hours)
- **Q5.1.4**: Maintenance communication? (advance notice - 24 hours, 1 week, etc.)
- **Q5.1.5**: Maintenance mode/graceful degradation? (read-only mode, rate limiting)
- **Q5.1.6**: Rollback procedures? (if maintenance causes issues)
- **Q5.1.7**: Zero-downtime deployment capability?

### 5.2 Deployment & Changes
- **Q5.2.1**: Deployment frequency? (daily, weekly, on-demand)
- **Q5.2.2**: Canary deployment? (rollout percentage - 5%, 10%, 25%)
- **Q5.2.3**: Feature flags? (for gradual rollouts, A/B testing)
- **Q5.2.4**: Change management process? (approval, review, testing requirements)
- **Q5.2.5**: Emergency patching process? (security vulnerabilities, critical bugs)
- **Q5.2.6**: Rollback procedures? (time to rollback, automation level)
- **Q5.2.7**: Deployment notifications? (who gets notified when)

---

## 6. Data Management & Compliance

### 6.1 Data Storage & Retention
- **Q6.1.1**: Data retention requirements? (analytics, logs, backups, user data)
- **Q6.1.2**: Data archival strategy? (cold storage after X days)
- **Q6.1.3**: Data deletion process? (GDPR right-to-be-forgotten, other requirements)
- **Q6.1.4**: Data encryption? (at rest, in transit, algorithm)
- **Q6.1.5**: Database replication strategy? (synchronous, asynchronous)
- **Q6.1.6**: Backup verification? (regular restore tests)

### 6.2 Compliance & Security
- **Q6.2.1**: Compliance requirements? (GDPR, CCPA, SOC 2, ISO 27001, HIPAA, PCI-DSS)
- **Q6.2.2**: Security audit cadence? (annual, quarterly, continuous)
- **Q6.2.3**: Vulnerability management process? (scanning, remediation SLA)
- **Q6.2.4**: Incident response playbook needed?
- **Q6.2.5**: Third-party security assessments? (penetration testing, bug bounty)
- **Q6.2.6**: Access control requirements? (role-based, attribute-based)
- **Q6.2.7**: Security incident notification requirements?

---

## 7. Operational Costs

### 7.1 Infrastructure Costs
- **Q7.1.1**: Infrastructure budget? (cloud provider, hosting, % of revenue)
- **Q7.1.2**: Cost optimization targets? (% reduction, specific metrics)
- **Q7.1.3**: Cost monitoring tools? (FinOps, cloud cost management)
- **Q7.1.4**: Vendor lock-in concerns? (multi-cloud strategy)
- **Q7.1.5**: Reserved capacity vs. on-demand? (cost vs. flexibility)

### 7.2 Operational Staffing
- **Q7.2.1**: On-call rotation model? (who, when, on-call compensation)
- **Q7.2.2**: On-call expectations? (response time, escalation process)
- **Q7.2.3**: Runbook requirements? (playbooks for common issues)
- **Q7.2.4**: Knowledge sharing/documentation needs?
- **Q7.2.5**: Training requirements for ops team?
- **Q7.2.6**: Hiring timeline? (when to add team members)

---

## 8. Operational Metrics & Reporting

### 8.1 Key Operational Metrics
- **Q8.1.1**: MTBF (Mean Time Between Failures) - target?
- **Q8.1.2**: MTTR (Mean Time To Recovery) - target?
- **Q8.1.3**: MTTA (Mean Time To Acknowledge) - target?
- **Q8.1.4**: Error budget? (allowed downtime per month/quarter/year)
- **Q8.1.5**: Cost per transaction? (cost efficiency)
- **Q8.1.6**: Infrastructure cost per active user?

### 8.2 Reporting Requirements
- **Q8.2.1**: Uptime reports? (monthly, quarterly, annual)
- **Q8.2.2**: Performance reports? (latency, throughput, error rates)
- **Q8.2.3**: Incident summaries? (frequency, resolution time, root causes)
- **Q8.2.4**: Stakeholder reporting cadence? (weekly, monthly)
- **Q8.2.5**: Public transparency? (publish SLA compliance publicly)

---

## Notes
- Define clear SLA/SLO targets early and get stakeholder buy-in
- Implement comprehensive monitoring and observability before launch
- Plan disaster recovery and test regularly
- Automate runbooks and incident response procedures
- Balance cost optimization with reliability requirements
