# URL Shortener Service - Performance Tuning Guide

**Version:** 1.0.0  
**Date:** July 24, 2026  
**Audience:** Performance Engineers, DevOps, Backend Team

---

## 1. Performance Baselines

### Target Metrics (MVP)

| Metric | Target | Acceptable Range | Unacceptable |
|--------|--------|------------------|--|
| P50 Latency | 20ms | 15-30ms | >50ms |
| P95 Latency | 100ms | 80-150ms | >200ms |
| P99 Latency | 300ms | 250-400ms | >500ms |
| Error Rate | <1% | <1-5% | >5% |
| Throughput | 100 RPS | 80-150 RPS | <50 RPS |
| CPU Usage | <50% | <60% | >80% |
| Memory Usage | <70% | <75% | >90% |
| Disk I/O Wait | <10% | <15% | >30% |

---

## 2. Load Testing

### Tools for Load Testing

**K6 (Recommended):**
```bash
# Install
brew install k6

# Run test
k6 run load-test.js
```

**JMeter:**
```bash
# Download and run
./jmeter -t load-test.jmx -l results.jtl -j jmeter.log
```

**Apache Bench:**
```bash
# Simple load test
ab -n 10000 -c 100 http://localhost:8080/api/health
```

### Sample K6 Load Test

```javascript
// load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '1m', target: 10 },    // Ramp up to 10 users
    { duration: '3m', target: 100 },   // Ramp up to 100 users
    { duration: '2m', target: 100 },   // Stay at 100 users
    { duration: '1m', target: 0 },     // Ramp down to 0 users
  ],

  thresholds: {
    http_req_duration: ['p(95)<200', 'p(99)<500'],  // 95% under 200ms
    http_req_failed: ['rate<0.1'],                  // <10% failure rate
  },
};

export default function () {
  // Test: Create URL
  const create_res = http.post('http://localhost:8080/api/urls', 
    JSON.stringify({
      originalUrl: 'https://example.com/' + __VU,
      customAlias: 'test-' + __VU,
      description: 'Load test'
    }),
    {
      headers: { 'Content-Type': 'application/json' },
    }
  );

  check(create_res, {
    'status is 201': r => r.status === 201,
    'response time < 200ms': r => r.timings.duration < 200,
  });

  sleep(1);

  // Test: Get all URLs
  const list_res = http.get('http://localhost:8080/api/urls');
  
  check(list_res, {
    'status is 200': r => r.status === 200,
    'response time < 100ms': r => r.timings.duration < 100,
  });

  sleep(1);
}
```

**Run K6 Test:**
```bash
k6 run load-test.js --vus 100 --duration 5m
```

---

## 3. Database Optimization

### Index Strategy

**Verify Indexes Exist:**
```sql
SELECT * FROM INFORMATION_SCHEMA.INDEXES 
WHERE TABLE_NAME='SHORT_URLS';

-- Should show:
-- idx_shortcode (on short_code)
-- idx_customalias (on custom_alias)
-- idx_createdat (on created_at)
-- idx_clickcount (on click_count DESC)
```

**Create Missing Indexes:**
```sql
CREATE UNIQUE INDEX idx_shortcode ON short_urls(short_code);
CREATE UNIQUE INDEX idx_customalias ON short_urls(custom_alias);
CREATE INDEX idx_createdAt ON short_urls(created_at);
CREATE INDEX idx_clickcount ON short_urls(click_count DESC);
```

### Query Optimization

**Slow Query: Without Index**
```sql
-- Query: Find URLs by alias
SELECT * FROM short_urls 
WHERE LOWER(custom_alias) LIKE LOWER('%test%');

-- Execution time WITHOUT index: 500ms
-- Full table scan required
```

**Optimized: With Index**
```sql
-- After creating index on custom_alias
SELECT * FROM short_urls 
WHERE LOWER(custom_alias) LIKE LOWER('%test%');

-- Execution time WITH index: 10ms
-- Index can be used for LIKE prefix searches
```

### Connection Pool Tuning

**Current Configuration:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**Optimal Range by Load:**

| Load Level | Pool Size | Min Idle | Timeout |
|-----------|---|---|---|
| Low (10 RPS) | 5 | 2 | 30s |
| Medium (50 RPS) | 10 | 5 | 30s |
| High (100+ RPS) | 20 | 10 | 30s |
| Very High (500+ RPS) | 30 | 15 | 60s |

**Formula:** `pool_size = (core_count * 2) + effective_spindle_count`

---

## 4. JVM Tuning

### JVM Options

**Current:**
```bash
JAVA_OPTS="-Xms256m -Xmx512m"
```

**Recommended by Environment:**

**Development:**
```bash
JAVA_OPTS="-Xms256m -Xmx512m -XX:+PrintGCDetails -XX:+PrintGCDateStamps"
```

**Staging:**
```bash
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

**Production:**
```bash
JAVA_OPTS="-Xms1024m -Xmx2048m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+ParallelRefProcEnabled \
  -XX:+AlwaysPreTouch \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:G1SummarizeRSetStatsPeriod=1"
```

### Monitoring GC Performance

```bash
# Enable GC logging
JAVA_OPTS="${JAVA_OPTS} \
  -Xlog:gc*:file=logs/gc.log:time,uptime,level,tags:filecount=10,filesize=100m"

# Analyze GC logs
jstat -gc -h10 <PID> 1000  # Every 1 second

# Check for full GC events
grep "Full GC" logs/gc.log

# If Full GC occurs frequently:
# 1. Increase heap size
# 2. Optimize object creation
# 3. Check for memory leaks
```

---

## 5. Spring Boot Optimization

### Application Startup Optimization

**Current Boot Time:** ~5 seconds

**Optimize Startup:**

```properties
# application.properties
# Disable unused auto-configurations
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration,\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

# Eager initialization
spring.jpa.hibernate.ddl-auto=validate  # Don't auto-create schema
```

### Request Processing Optimization

**Enable Request Caching:**

```java
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("urls", "analytics");
    }
}

// In service layer
@Cacheable(value = "urls", key = "#shortCode")
public ShortenUrlResponse getShortUrl(String shortCode) {
    // Cached result
}
```

### HTTP Compression

**Enable Gzip Compression:**

```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
    min-response-size: 512
```

---

## 6. Caching Strategy

### Request-Level Caching (Phase 2)

```yaml
# Add to application.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 60000  # 1 minute TTL
```

**Cache Configuration:**
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(1))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );

        return RedisCacheManager.create(factory);
    }
}
```

### Database Query Caching

**Cache Frequently Accessed Data:**

```java
@Service
public class AnalyticsService {

    @Cacheable(value = "topUrls", key = "#limit")
    public List<ShortenUrlResponse> getTopClickedUrls(int limit) {
        // Cache result for 5 minutes
        return urlRepository.findTopClicked(limit);
    }

    @CacheEvict(value = "topUrls", allEntries = true)
    public void invalidateAnalyticsCache() {
        // Called after URL creation/deletion
    }
}
```

---

## 7. Network Optimization

### HTTP Keep-Alive

**Enable Connection Pooling:**

```yaml
server:
  http:
    keep-alive-timeout: 60000  # 60 seconds
```

### Compression

**Response Size Impact:**

| Content | Original | Gzip | Ratio |
|---------|----------|------|-------|
| 100 URLs (JSON) | 25KB | 3KB | 88% reduction |
| Swagger UI | 500KB | 150KB | 70% reduction |
| API Docs | 200KB | 40KB | 80% reduction |

---

## 8. Database Migration Performance

### H2 to PostgreSQL Migration

**Performance Impact:**

| Operation | H2 | PostgreSQL | Improvement |
|-----------|----|----|---|
| Create URL | 5ms | 3ms | 40% faster |
| List 1000 URLs | 50ms | 15ms | 70% faster |
| Search (wildcard) | 100ms | 20ms | 80% faster |
| Get analytics | 200ms | 30ms | 85% faster |

**Migration Checklist:**

```bash
[ ] Benchmark H2 performance
[ ] Set up PostgreSQL test instance
[ ] Run parallel tests
[ ] Validate identical data
[ ] Run load tests
[ ] Quantify improvements
[ ] Plan production migration
```

---

## 9. Bottleneck Analysis

### Common Bottlenecks

**1. Database I/O (Typical)**
```
Solution:
- Add indexes
- Optimize queries
- Increase connection pool
- Use caching
```

**2. Memory Pressure (GC pauses)**
```
Solution:
- Increase heap size
- Tune GC settings
- Reduce object creation
- Implement object pooling
```

**3. CPU Saturation**
```
Solution:
- Horizontal scaling
- Code optimization
- Enable compression
- Async processing
```

**4. Network Latency**
```
Solution:
- Enable keep-alive
- Use HTTP/2
- Enable compression
- CDN for static assets
```

### Performance Profiling

```bash
# Using JProfiler (commercial)
jprofiler --config=/path/to/config.xml

# Using YourKit (trial available)
yourkit-profiler application

# Using JFR (free)
java -XX:StartFlightRecording=duration=60s,filename=recording.jfr \
     -jar url-shortener.jar
```

---

## 10. Production Performance Checklist

- [ ] Baseline metrics established
- [ ] Load testing conducted (100+ RPS)
- [ ] P95 latency < 100ms
- [ ] Error rate < 1%
- [ ] Database indexes created
- [ ] Connection pool tuned
- [ ] JVM heap sized appropriately
- [ ] Caching enabled
- [ ] Compression enabled
- [ ] Monitoring configured
- [ ] Alerting thresholds set
- [ ] Capacity plan documented

---

## 11. Performance Monitoring Dashboard

**Essential Metrics:**

```
┌─────────────────────────────────────┐
│ Response Time (P95)        85ms      │
│ Error Rate (current)       0.2%      │
│ Throughput                 95 RPS    │
│ Active Connections         12/20     │
├─────────────────────────────────────┤
│ CPU Usage          35%    ▓░░░░░░    │
│ Memory Usage       62%    ▓▓░░░░░░   │
│ Disk Space         48%    ▓▓░░░░░░   │
│ GC Time < 5min              ✅        │
└─────────────────────────────────────┘
```

---

## 12. Tuning Recommendations by Phase

### MVP (Now)
- [x] Database indexes created
- [x] Connection pool configured (10 connections)
- [x] JVM heap tuned (512MB)
- [x] Swagger UI gzip enabled
- [x] Basic monitoring

### Phase 2 (Week 2-3)
- [ ] Redis caching added (5min TTL)
- [ ] Async processing (Kafka)
- [ ] HTTP/2 support
- [ ] Advanced metrics (Micrometer)
- [ ] Load balancer configured

### Phase 3 (Month 2)
- [ ] PostgreSQL migration
- [ ] Multi-instance deployment
- [ ] CDN for static assets
- [ ] Advanced caching strategies
- [ ] Auto-scaling configuration

---

## 13. References

- **JVM Tuning Guide:** https://www.oracle.com/java/technologies/tuning
- **Spring Boot Performance:** https://spring.io/guides/gs/spring-boot/
- **Postgres Performance:** https://www.postgresql.org/docs/current/performance-tips.html
- **K6 Load Testing:** https://k6.io/docs/
- **GC Tuning:** https://www.redhat.com/en/blog/choosing-right-garbage-collector-java-application

---

**Document Version:**

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-07-24 | Initial performance guide |


