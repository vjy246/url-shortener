# URL Shortener Service - Troubleshooting Guide

**Version:** 1.0.0  
**Last Updated:** July 24, 2026  
**Audience:** Support, Operations, Developers

---

## Quick Diagnostics

### Is the Service Running?

```bash
# Check health
curl http://localhost:8080/api/health

# Expected response:
# {"status":"UP","service":"URL Shortener Service",...}
```

### Can I Access the Database?

```bash
# Visit H2 Console
http://localhost:8080/api/h2-console

# Run test query:
SELECT COUNT(*) FROM short_urls;
```

### Are Tests Passing?

```bash
./mvnw test

# Should see: BUILD SUCCESS
```

---

## Common Issues & Solutions

### 1. Application Won't Start

#### Error: "Port 8080 already in use"

**Cause:** Another process is using port 8080

**Solution:**
```bash
# Find process using port
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows

# OR use different port
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

#### Error: "Java not found"

**Cause:** Java 17+ not installed or not in PATH

**Solution:**
```bash
# Check Java version
java -version

# If not installed, install Java 17
# See SETUP_INSTRUCTIONS.md Step 1.1
```

#### Error: "Spring Boot application failed to start"

**Cause:** Database connectivity or configuration issue

**Solution:**
```bash
# Check logs (last 50 lines)
# Look for "ERROR" or "Exception" messages

# Clear database and restart
rm -rf ./data/urlshortenerdb*
./mvnw clean spring-boot:run
```

---

### 2. Database Issues

#### Error: "Database file locked"

**Cause:** Another process has database connection

**Solution:**
```bash
# Stop running application (Ctrl+C)
# Check for other Java processes
jps  # List all Java processes

# Delete lock file
rm -rf ./data/urlshortenerdb.mv.db.lock

# Restart application
./mvnw spring-boot:run
```

#### Error: "H2 Console shows 'Permission denied'"

**Cause:** File permissions issue

**Solution:**
```bash
# Fix file permissions (macOS/Linux)
chmod 644 ./data/urlshortenerdb*

# OR delete and recreate
rm -rf ./data/urlshortenerdb*
./mvnw spring-boot:run
```

#### No data persists after restart

**Cause:** Using in-memory H2 database (old configuration)

**Solution:**
```bash
# Verify application.yml has:
spring:
  datasource:
    url: jdbc:h2:file:./data/urlshortenerdb;MODE=MySQL

# NOT: jdbc:h2:mem:...
```

---

### 3. API Errors

#### Error: "404 Not Found" when accessing URL

**Cause:** 
- Incorrect endpoint path
- Short code doesn't exist
- Application not running

**Solution:**
```bash
# Verify app is running
curl http://localhost:8080/api/health

# Check correct path (with /api)
curl http://localhost:8080/api/urls  # Correct

# NOT
curl http://localhost:8080/urls  # Wrong - missing /api

# Verify short code exists
curl http://localhost:8080/api/urls
```

#### Error: "409 Conflict - Duplicate alias"

**Cause:** Custom alias already exists

**Solution:**
```bash
# Option 1: Use different alias
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"...","customAlias":"new-alias-name"}'

# Option 2: Omit customAlias (auto-generate)
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"..."}'

# Option 3: Delete existing URL and recreate
curl -X DELETE http://localhost:8080/api/urls/1
```

#### Error: "400 Bad Request - originalUrl cannot be blank"

**Cause:** Missing required field in request

**Solution:**
```bash
# Verify JSON has originalUrl field
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://example.com",
    "customAlias": "my-alias"
  }'
```

#### Error: "500 Internal Server Error"

**Cause:** Unexpected exception in application

**Solution:**
```bash
# Check application logs for error details
# Look for full stack trace

# Common causes:
# 1. Database connection lost
# 2. JSON parsing error
# 3. Null pointer exception

# Restart application
./mvnw spring-boot:run

# If persists, check OPERATIONS_RUNBOOK.md
```

---

### 4. Test Failures

#### Error: "Tests fail with 'Cannot find @interface method'"

**Cause:** Annotation processor not recognizing Lombok annotations

**Solution:**
```bash
# Clean and rebuild
./mvnw clean compile

# OR in IDE:
# Build → Rebuild Project

# If still failing:
# 1. Delete target/ folder
# 2. Invalidate IDE cache
# 3. Rebuild project
```

#### Error: "Test hangs or times out"

**Cause:** Test blocked on I/O or lock

**Solution:**
```bash
# Run with timeout
timeout 30 ./mvnw test

# OR increase timeout in pom.xml
mvn test -DargLine="-Xmx512m"

# Stop hung process (Ctrl+C)
```

#### Error: "Test cannot connect to database"

**Cause:** H2 database locked by other process

**Solution:**
```bash
# Kill other Java processes
pkill -f java

# Delete database
rm -rf ./data/urlshortenerdb*

# Retry tests
./mvnw test
```

---

### 5. Performance Issues

#### API Responses Slow (> 1 second)

**Cause:**
- Large dataset
- Missing database indexes
- High CPU/disk utilization

**Solution:**
```bash
# Check database size
SELECT COUNT(*) FROM short_urls;

# Check slow queries
# Enable query logging in application.yml:
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

# Verify indexes exist
SELECT * FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME='SHORT_URLS';

# If missing, recreate them
# See TECHNICAL_ARCHITECTURE.md Section 8
```

#### High Memory Usage

**Cause:** Memory leak or large result sets

**Solution:**
```bash
# Increase JVM heap
./mvnw spring-boot:run -Dspring-boot.run.arguments="--jvm-args=-Xmx1024m"

# OR set in .mvn/maven.config
-Xmx1024m

# Monitor memory usage
jstat -gc <PID> 1000  # Every 1 second
```

#### Network/Timeout Issues

**Cause:**
- Database connection timeout
- Slow network
- Firewall blocking

**Solution:**
```bash
# Test database connectivity
curl http://localhost:8080/api/health/db

# Check firewall
# Ensure port 8080 is open

# Increase connection timeout
spring:
  datasource:
    hikari:
      connection-timeout: 30000  # 30 seconds
```

---

## Debugging Tips

### 1. Enable Debug Logging

```yaml
# application.yml
logging:
  level:
    root: DEBUG
    com.urlshortener: DEBUG
    org.springframework.web: DEBUG
    org.hibernate: DEBUG
```

### 2. Inspect HTTP Requests

```bash
# Using curl with verbose flag
curl -v http://localhost:8080/api/urls

# Using curl with headers only
curl -i http://localhost:8080/api/urls
```

### 3. Monitor Database Queries

```bash
# Via H2 Console
http://localhost:8080/api/h2-console

# View last queries:
SELECT * FROM INFORMATION_SCHEMA.RECENT_QUERIES;
```

### 4. Check Application Logs

```bash
# View recent logs
tail -f logs/application.log

# Search for errors
grep ERROR logs/application.log

# Search by timestamp
grep "2026-07-24 12:" logs/application.log
```

### 5. Profile with IDE Debugger

**IntelliJ IDEA:**
1. Set breakpoint (click line number)
2. Run → Debug (Shift+F9)
3. Step through code
4. Inspect variables in Debug panel

---

## System Health Check Checklist

Before declaring "healthy", verify:

- [ ] Health endpoint responds: `curl http://localhost:8080/api/health`
- [ ] Database accessible: `curl http://localhost:8080/api/h2-console`
- [ ] Swagger UI loads: http://localhost:8080/api/swagger-ui.html
- [ ] Can create URL: `POST /api/urls` successful
- [ ] Can retrieve URL: `GET /api/urls/{code}` returns data
- [ ] Can search: `GET /api/urls/search?term=test` returns results
- [ ] Can redirect: `GET /api/urls/redirect/{code}` returns 302
- [ ] Tests pass: `./mvnw test` shows BUILD SUCCESS
- [ ] No errors in logs: `grep ERROR logs/application.log`
- [ ] Database has tables: `SELECT COUNT(*) FROM short_urls;`

---

## Getting Help

| Issue | Check | Reference |
|-------|-------|-----------|
| Setup problem | SETUP_INSTRUCTIONS.md | Developer onboarding |
| API question | API_REFERENCE.md | Endpoint documentation |
| Database issue | TECHNICAL_ARCHITECTURE.md Section 3 | Entity/schema design |
| Deployment issue | DEPLOYMENT_GUIDE.md | Production setup |
| Performance issue | TECHNICAL_ARCHITECTURE.md Section 8 | Performance tuning |
| Security issue | SECURITY_GUIDE.md | Security practices |
| Operational issue | OPERATIONS_RUNBOOK.md | Monitoring & alerts |

---

## Emergency Contacts

| Issue | Contact |
|-------|---------|
| Database down | Database Admin |
| Memory leak | Performance Team |
| Security incident | Security Team |
| Deployment failed | DevOps Lead |
| API error rate high | Backend Lead |

---

**Still not resolved?** Create GitHub issue or contact engineering team.


