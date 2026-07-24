# URL Shortener Service - Deployment Guide

**Version:** 1.0.0  
**Date:** July 24, 2026  
**Audience:** DevOps, Deployment Engineers

---

## Pre-Deployment Checklist

- [ ] All tests passing: `./mvnw test`
- [ ] No compilation errors: `./mvnw clean compile`
- [ ] Security scan passed
- [ ] Code review approved
- [ ] Change log updated
- [ ] Rollback plan documented

---

## Build Artifacts

### Local Build

```bash
# Create JAR package
./mvnw clean package -DskipTests

# Output: target/url-shortener-service-1.0.0.jar
# Size: ~50MB (including embedded Tomcat)
```

### Docker Image

```bash
# Build image
docker build -t url-shortener:1.0.0 .

# Tag for registry
docker tag url-shortener:1.0.0 registry.company.com/url-shortener:1.0.0

# Push to registry
docker push registry.company.com/url-shortener:1.0.0
```

---

## Development Deployment

### Local Development

```bash
# Clone repository
git clone <repo-url>
cd IntelliJSourceCode

# Run application
./mvnw spring-boot:run

# Access
# API: http://localhost:8080/api
# Swagger: http://localhost:8080/api/swagger-ui.html
```

### Database Setup

H2 file-based database creates automatically:
- File: `./data/urlshortenerdb.mv.db`
- Auto-creates schema on first run
- No migration scripts needed for MVP

---

## Staging Deployment

### Environment Setup

```yaml
# .env file
SPRING_PROFILES_ACTIVE=staging
SPRING_DATASOURCE_URL=jdbc:postgresql://staging-db:5432/urlshortener
SPRING_DATASOURCE_USERNAME=urlshortener_user
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}  # From secrets manager
SERVER_PORT=8080
LOGGING_LEVEL_ROOT=INFO
```

### Docker Compose (Staging)

```yaml
# docker-compose.yml
version: '3.8'

services:
  url-shortener:
    image: url-shortener:1.0.0
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/urlshortener
      SPRING_DATASOURCE_USERNAME: urlshortener_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JAVA_OPTS: -Xmx512m -Xms256m
    depends_on:
      - postgres

  postgres:
    image: postgres:14-alpine
    environment:
      POSTGRES_DB: urlshortener
      POSTGRES_USER: urlshortener_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - db_data:/var/lib/postgresql/data

volumes:
  db_data:
```

### Deploy Staging

```bash
# Build and push
./mvnw clean package -DskipTests
docker build -t url-shortener:${VERSION} .
docker tag url-shortener:${VERSION} registry.company.com/url-shortener:${VERSION}
docker push registry.company.com/url-shortener:${VERSION}

# Deploy via docker-compose
docker-compose -f docker-compose.staging.yml up -d

# Verify deployment
curl http://staging-url-shortener:8080/api/health
```

---

## Production Deployment

### Kubernetes Deployment

```yaml
# k8s/deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: url-shortener
  labels:
    app: url-shortener
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: url-shortener
  template:
    metadata:
      labels:
        app: url-shortener
    spec:
      containers:
      - name: url-shortener
        image: registry.company.com/url-shortener:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-config
              key: url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-config
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-config
              key: password
        - name: JAVA_OPTS
          value: "-Xmx1024m -Xms512m -XX:+UseG1GC"
        
        livenessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        
        readinessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 15
          periodSeconds: 5

        resources:
          requests:
            cpu: 250m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 1Gi

---
apiVersion: v1
kind: Service
metadata:
  name: url-shortener-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
  selector:
    app: url-shortener
```

### Deploy to Kubernetes

```bash
# Create secrets
kubectl create secret generic db-config \
  --from-literal=url=jdbc:postgresql://prod-db:5432/urlshortener \
  --from-literal=username=urlshortener_user \
  --from-literal=password=${PROD_DB_PASSWORD}

# Apply deployment
kubectl apply -f k8s/deployment.yml

# Verify deployment
kubectl get pods -l app=url-shortener
kubectl logs -l app=url-shortener --all-containers=true

# Check service
kubectl get svc url-shortener-service
```

---

## Database Migration (H2 → PostgreSQL)

### Step 1: Backup H2 Data

```bash
# Export H2 database
java -cp h2-*.jar org.h2.tools.Shell
CALL CSVWRITE('shortcut.csv', 'SELECT * FROM short_urls');
```

### Step 2: Update Configuration

```yaml
# application-production.yml
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/urlshortener
    driverClassName: org.postgresql.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate  # Don't auto-create in prod
```

### Step 3: Add PostgreSQL Driver

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
    <scope>runtime</scope>
</dependency>
```

### Step 4: Create PostgreSQL Schema

```sql
CREATE TABLE short_urls (
  id BIGSERIAL PRIMARY KEY,
  short_code VARCHAR(10) UNIQUE NOT NULL,
  custom_alias VARCHAR(255) UNIQUE NOT NULL,
  original_url VARCHAR(2048) NOT NULL,
  click_count BIGINT NOT NULL DEFAULT 0,
  description VARCHAR(500),
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_shortcode ON short_urls(short_code);
CREATE INDEX idx_customalias ON short_urls(custom_alias);
CREATE INDEX idx_createdAt ON short_urls(created_at);
CREATE INDEX idx_clickcount ON short_urls(click_count DESC);
```

### Step 5: Migrate Data

```bash
# Import CSV
\COPY short_urls FROM 'shortcut.csv' WITH CSV HEADER;

# Verify
SELECT COUNT(*) FROM short_urls;
```

---

## Production Configuration

### application-production.yml

```yaml
spring:
  application:
    name: url-shortener-service
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    database-platform: org.hibernate.dialect.PostgreSQLDialect
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

server:
  port: 8080
  servlet:
    context-path: /api

logging:
  level:
    root: WARN
    com.urlshortener: INFO
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 30

springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /openapi.json
```

---

## Health Check Setup

### Corporate Firewall Health Check

```bash
# Endpoint
GET http://api.company.com/api/health

# Expected response (200 OK)
{
  "status": "UP",
  "service": "URL Shortener Service",
  "version": "1.0.0"
}
```

### Load Balancer Configuration

```
Health Check Path: /api/health
Port: 8080
Protocol: HTTP
Interval: 10 seconds
Timeout: 5 seconds
Healthy Threshold: 2
Unhealthy Threshold: 3
```

---

## Monitoring & Alerts

### Prometheus Metrics

```yaml
# Scrape config
- job_name: 'url-shortener'
  static_configs:
  - targets: ['localhost:8080']
  metrics_path: '/api/actuator/prometheus'
```

### Alert Rules

```yaml
groups:
- name: url-shortener
  rules:
  - alert: HighErrorRate
    expr: rate(http_requests_total{service="url-shortener",status=~"5.."}[5m]) > 0.05
    for: 5m
    annotations:
      summary: "High error rate detected"
  
  - alert: ServiceDown
    expr: up{service="url-shortener"} == 0
    for: 1m
    annotations:
      summary: "Service is down"
```

---

## Rollback Plan

### Rollback Steps

1. **Identify Issue:**
   - High error rate
   - Service unavailable
   - Database corruption

2. **Emergency Stop:**
   ```bash
   # Stop current deployment
   docker stop url-shortener
   # OR
   kubectl scale deployment url-shortener --replicas=0
   ```

3. **Restore Previous Version:**
   ```bash
   # Run last known good version
   docker run url-shortener:0.9.9
   # OR
   kubectl set image deployment/url-shortener \
     url-shortener=registry.company.com/url-shortener:0.9.9
   ```

4. **Verify Rollback:**
   ```bash
   curl http://api.company.com/api/health
   ```

5. **Investigate:**
   - Check logs
   - Review change log
   - Analyze metrics

---

## Post-Deployment Verification

- [ ] Health check passes
- [ ] Swagger UI accessible
- [ ] Can create short URL
- [ ] Can redirect to URL
- [ ] Database connectivity verified
- [ ] Monitoring alerts active
- [ ] Performance metrics normal (<100ms p95)
- [ ] No error logs in first hour
- [ ] Smoke tests pass
- [ ] Production endpoints responding

---

## Deployment Checklist

**Before Deployment:**
- [ ] Code review approved
- [ ] Tests passing (85%+ coverage)
- [ ] Security scan clean
- [ ] Database backup taken
- [ ] Rollback plan reviewed

**During Deployment:**
- [ ] Deploy to staging first
- [ ] Run smoke tests
- [ ] Monitor error logs
- [ ] Check performance metrics

**After Deployment:**
- [ ] Verify all endpoints
- [ ] Check database
- [ ] Monitor for 24 hours
- [ ] Confirm with product
- [ ] Update deployment log

---

## Support Contacts

| Role | Escalation |
|------|-----------|
| Deployment Issue | DevOps Lead |
| Database Issue | Database Admin |
| Performance Issue | Performance Team |
| Security Incident | Security Team |
| Business Impact | Product Manager |

---


