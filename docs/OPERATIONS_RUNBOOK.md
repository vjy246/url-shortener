# URL Shortener Service - Operations Runbook

**Version:** 1.0.0  
**Date:** July 24, 2026  
**Audience:** SREs, DevOps, Operations Team

---

## 1. Service Overview for Operators

**Service Name:** URL Shortener Service  
**Technology:** Spring Boot 3.1.12 + H2/PostgreSQL  
**Runtime:** Java 17+  
**Port:** 8080 (default, configurable)  
**Health Check:** `GET /api/health`  
**Dependencies:** H2 Database (file-based) or PostgreSQL (production)

---

## 2. Daily Operations

### Morning Health Check

**Execute Every Business Day Morning:**

```bash
#!/bin/bash
# health-check.sh

echo "URL Shortener Service - Daily Health Check"
echo "=========================================="

# 1. Check if service is running
if curl -s http://localhost:8080/api/health | grep -q "UP"; then
    echo "✅ Service is UP"
else
    echo "❌ Service is DOWN - ALERT REQUIRED"
    exit 1
fi

# 2. Check database
if curl -s http://localhost:8080/api/health/db | grep -q "UP"; then
    echo "✅ Database is UP"
else
    echo "❌ Database is DOWN - INVESTIGATE"
    exit 1
fi

# 3. Check disk space
DISK_USAGE=$(df -h /data | awk 'NR==2 {print $5}' | sed 's/%//')
if [ "$DISK_USAGE" -lt 80 ]; then
    echo "✅ Disk usage: ${DISK_USAGE}%"
else
    echo "⚠️  ALERT: Disk usage: ${DISK_USAGE}% (>80%)"
fi

# 4. Check recent errors
ERROR_COUNT=$(grep -c "ERROR" logs/application.log 2>/dev/null || echo "0")
if [ "$ERROR_COUNT" -lt 5 ]; then
    echo "✅ Error count acceptable: $ERROR_COUNT"
else
    echo "⚠️  WARNING: High error count: $ERROR_COUNT"
fi

echo "Health check completed at $(date)"
```

**Run Automated:**
```bash
# Add to crontab
0 8 * * 1-5 /opt/url-shortener/health-check.sh
```

---

### Monitoring Metrics

**Key Metrics to Watch:**

| Metric | Healthy | Warning | Critical |
|--------|---------|---------|----------|
| Response Time (p95) | <100ms | 100-500ms | >500ms |
| Error Rate (5xx) | <1% | 1-5% | >5% |
| CPU Usage | <50% | 50-80% | >80% |
| Memory Usage | <70% | 70-90% | >90% |
| Disk Usage | <70% | 70-85% | >85% |
| DB Connections | <15 | 15-19 | 20+ |

---

## 3. Monitoring & Alerting

### Prometheus Metrics

**Scrape Configuration:**

```yaml
# /etc/prometheus/prometheus.yml
global:
  scrape_interval: 15s
  scrape_timeout: 10s

scrape_configs:
  - job_name: 'url-shortener'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/api/actuator/prometheus'
```

### Alert Rules

**Create Alert Rules:**

```yaml
# /etc/prometheus/rules.yml
groups:
- name: url_shortener
  interval: 30s
  rules:
  
  # Service Down
  - alert: ServiceDown
    expr: up{job="url-shortener"} == 0
    for: 1m
    annotations:
      summary: "URL Shortener service is down"
      severity: "critical"
  
  # High Error Rate
  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
    for: 5m
    annotations:
      summary: "Error rate > 5%"
      severity: "warning"
  
  # High Response Time
  - alert: SlowResponses
    expr: histogram_quantile(0.95, http_request_duration_seconds) > 1
    for: 5m
    annotations:
      summary: "P95 response time > 1s"
      severity: "warning"
  
  # Out of Disk Space
  - alert: LowDiskSpace
    expr: (node_filesystem_avail_bytes / node_filesystem_size_bytes) * 100 < 15
    for: 5m
    annotations:
      summary: "Disk space < 15%"
      severity: "critical"
  
  # High Memory Usage
  - alert: HighMemoryUsage
    expr: (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100 > 90
    for: 5m
    annotations:
      summary: "Memory usage > 90%"
      severity: "warning"
```

### Grafana Dashboard

**Essential Panels:**

1. **Service Health**
   - Uptime percentage
   - Last health check
   - Error rate trend

2. **Performance**
   - Response time (p50, p95, p99)
   - Requests per second
   - Database queries per second

3. **Resources**
   - CPU usage %
   - Memory usage %
   - Disk usage %
   - Database connection pool

4. **Errors**
   - Error rate by status code
   - Top error messages
   - Error trend (24h)

---

## 4. Incident Response

### Service Down (Critical)

**Timeline:**
- **T+0min:** Alert fires
- **T+2min:** On-call engineer notified
- **T+5min:** Initial investigation
- **T+15min:** Status update to stakeholders

**Investigation Steps:**

```bash
# 1. Check if service process is running
ps aux | grep java | grep url-shortener

# 2. Check application logs
tail -100 logs/application.log | grep ERROR

# 3. Check system resources
free -h  # Memory
df -h    # Disk
top -b -n1 | head -20  # CPU

# 4. Check database connectivity
curl http://localhost:8080/api/health/db

# 5. Check network connectivity
netstat -tlnp | grep 8080
```

**Resolution Steps:**

```bash
# Step 1: Try graceful restart
docker restart url-shortener-service
sleep 10

# Step 2: Check health
curl http://localhost:8080/api/health

# Step 3: If still down, check logs
docker logs url-shortener-service | tail -50

# Step 4: Resource issue? Scale up
docker update --memory=2g url-shortener-service

# Step 5: Database issue? Check connectivity
curl http://localhost:8080/api/health/db

# Step 6: Last resort: Rollback
docker pull registry.company.com/url-shortener:previous-version
docker stop url-shortener-service
docker run -d --name url-shortener-service registry.company.com/url-shortener:previous-version
```

---

### High Error Rate

**When Error Rate > 5%:**

```bash
# 1. Check recent errors
grep ERROR logs/application.log | tail -20

# 2. Check for pattern
grep ERROR logs/application.log | grep -o "Exception: [^,]*" | sort | uniq -c

# 3. Check database
SELECT COUNT(*) FROM short_urls;  # Ensure DB is responsive

# 4. Check dependencies
curl -v https://external-api.com/health  # Any external APIs used?

# 5. Restart if transient error
docker restart url-shortener-service
```

---

### High Resource Usage

**When Memory > 90%:**

```bash
# 1. Check current memory
docker stats url-shortener-service

# 2. Find memory leak candidates
jmap -heap <PID>  # Heap dump analysis

# 3. Increase JVM heap (temporary)
docker update --env JAVA_OPTS="-Xmx2g" url-shortener-service

# 4. Restart service
docker restart url-shortener-service

# 5. Monitor memory trend
# If continuously increasing = memory leak
# Contact development team for investigation
```

**When Disk > 85%:**

```bash
# 1. Find large files
du -sh /data/* | sort -hr

# 2. Check log files
ls -lh logs/

# 3. Archive old logs
tar -czf logs/archive_$(date +%Y%m%d).tar.gz logs/*.log.*
rm logs/*.log.*

# 4. Check database size
du -sh /data/urlshortenerdb*

# 5. If database is large, backup and clean
# Contact database administrator
```

---

## 5. Backup & Recovery

### Automated Backup Schedule

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups/url-shortener"
BACKUP_DATE=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup database
cp /data/urlshortenerdb.mv.db $BACKUP_DIR/urlshortenerdb_$BACKUP_DATE.mv.db

# Compress
gzip $BACKUP_DIR/urlshortenerdb_$BACKUP_DATE.mv.db

# Keep only last 7 days
find $BACKUP_DIR -name "*.gz" -mtime +7 -delete

echo "Backup completed: $BACKUP_DIR/urlshortenerdb_$BACKUP_DATE.mv.db.gz"
```

**Schedule Daily at 2 AM:**
```bash
# Add to crontab
0 2 * * * /opt/url-shortener/backup.sh
```

### Manual Recovery

**If Database Corrupted:**

```bash
# 1. Stop application
docker stop url-shortener-service

# 2. Restore from backup
latest_backup=$(ls -t /backups/url-shortener/*.gz | head -1)
gunzip -c $latest_backup > /data/urlshortenerdb.mv.db

# 3. Restart application
docker start url-shortener-service

# 4. Verify
curl http://localhost:8080/api/health
```

---

## 6. Scaling Operations

### Horizontal Scaling (Adding More Instances)

**Before Scaling:**
- Ensure load balancer configured
- Database connection pool sufficient
- Monitoring upgraded

**Scale Up:**
```bash
# Using Docker Compose
docker-compose up -d --scale url-shortener=3

# Using Kubernetes
kubectl scale deployment url-shortener --replicas=5

# Load Balancer Configuration
# Add new instances to pool
# Health check endpoint: /api/health
```

**Monitor After Scaling:**
```bash
# Check all instances healthy
curl http://lb.company.com/api/health
curl http://lb.company.com/api/urls  # Test endpoint

# Monitor connection distribution
# Should be balanced across instances
```

---

## 7. Maintenance Windows

### Planned Maintenance

**Notification Schedule:**
- Announce 2 weeks before
- Remind 1 week before
- Final reminder 24 hours before
- Status updates every 15 minutes during maintenance

**Maintenance Checklist:**

```bash
#!/bin/bash
# Pre-maintenance
echo "PRE-MAINTENANCE"
- [ ] Health check all instances
- [ ] Backup database
- [ ] Notify stakeholders
- [ ] Prepare rollback plan

# Maintenance Window
echo "DURING MAINTENANCE"
- [ ] Drain connections (graceful shutdown)
- [ ] Stop application
- [ ] Apply updates/patches
- [ ] Run migrations (if any)
- [ ] Start application
- [ ] Health checks

# Post-maintenance
echo "POST-MAINTENANCE"
- [ ] Verify all endpoints working
- [ ] Run smoke tests
- [ ] Monitor error rates (30 min)
- [ ] Notify stakeholders
- [ ] Document changes
```

---

## 8. Common Operational Tasks

### Update Application Version

```bash
# 1. Backup current version
docker tag url-shortener:current url-shortener:backup-$(date +%Y%m%d)

# 2. Pull new version
docker pull registry.company.com/url-shortener:1.1.0

# 3. Perform rolling update
docker update --image registry.company.com/url-shortener:1.1.0 url-shortener-service
docker restart url-shortener-service

# 4. Verify
curl http://localhost:8080/api/health

# 5. Rollback if needed
docker restart --image url-shortener:backup-$(date +%Y%m%d) url-shortener-service
```

### View Application Logs

```bash
# Real-time logs
docker logs -f url-shortener-service

# Last 100 lines
docker logs --tail 100 url-shortener-service

# Logs since 1 hour ago
docker logs --since 1h url-shortener-service

# Logs between timestamps
docker logs url-shortener-service --until 2026-07-24T15:00:00Z
```

### Restart Service

```bash
# Graceful restart (recommended)
docker restart url-shortener-service
sleep 5
curl http://localhost:8080/api/health

# Force restart (last resort)
docker kill url-shortener-service
docker rm url-shortener-service
docker run -d --name url-shortener-service registry.company.com/url-shortener:latest
```

---

## 9. Performance Optimization

### Database Query Tuning

```sql
-- Check index usage
SELECT * FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME='SHORT_URLS';

-- Check slow queries
-- Enable query logging in application.yml:
-- spring.jpa.show-sql: true

-- Sample slow query analysis
EXPLAIN SELECT * FROM short_urls WHERE LOWER(custom_alias) LIKE LOWER('%test%');

-- Create missing indexes if needed
CREATE INDEX idx_shortcode ON short_urls(short_code);
CREATE INDEX idx_customalias ON short_urls(custom_alias);
```

### Connection Pool Tuning

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Adjust based on load
      minimum-idle: 5            # Keep connections ready
      connection-timeout: 30000  # 30 seconds
      idle-timeout: 600000       # 10 minutes
      max-lifetime: 1800000      # 30 minutes
```

---

## 10. Runbook Quick Reference

| Issue | Command/Action | Duration |
|-------|---|---|
| Check health | `curl http://localhost:8080/api/health` | 1 sec |
| View logs | `docker logs -f url-shortener-service` | 5 sec |
| Restart service | `docker restart url-shortener-service` | 10 sec |
| Manual backup | `/opt/url-shortener/backup.sh` | 30 sec |
| Restore backup | `gunzip backup.gz \| restore` | 2 min |
| Scale up | `kubectl scale deployment url-shortener --replicas=5` | 5 min |

---

## 11. Escalation Contacts

| Severity | Contact | Response Time | Phone |
|----------|---------|---|---|
| CRITICAL | On-call Engineer | 5 min | ext. 9999 |
| HIGH | Engineering Lead | 15 min | ext. 2000 |
| MEDIUM | Operations Mgr | 30 min | ext. 3000 |
| LOW | Support Team | 2 hours | ext. 4000 |

---

## 12. Knowledge Base

**Useful References:**
- TROUBLESHOOTING.md - Common issues & fixes
- DEPLOYMENT_GUIDE.md - Deployment procedures
- SECURITY_GUIDE.md - Security best practices
- PERFORMANCE_TUNING.md - Performance optimization

---

**Document Version:**

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-07-24 | Initial runbook |


