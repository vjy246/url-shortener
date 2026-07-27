# URL Shortener Service - Security Guide

**Version:** 1.0.0  
**Date:** July 24, 2026  
**Audience:** Security Engineers, DevOps, Backend Team

---

## 1. Security Overview

### Threat Model

| Threat | Risk Level | Impact | Mitigation |
|--------|-----------|--------|-----------|
| **Unauthorized API Access** | HIGH | Data manipulation, service abuse | API authentication (Phase 2), rate limiting |
| **SQL Injection** | MEDIUM | Database compromise | Parameterized queries (JPA), input validation |
| **XSS via URLs** | MEDIUM | Malicious redirect | URL validation, no client-side rendering |
| **DDoS Attacks** | HIGH | Service unavailability | Rate limiting, WAF, CDN |
| **Data Breach** | HIGH | URL/click data exposure | Encryption at rest, HTTPS, access controls |
| **Credential Exposure** | HIGH | Authentication bypass | Environment variables for secrets, no hardcoding |
| **Insecure Deserialization** | MEDIUM | Code execution | Jackson safe defaults, no custom deserializers |

---

## 2. Input Validation & Sanitization

### URL Validation

**What We Validate:**
```java
// In ShortenUrlRequest.java
@NotBlank(message = "Original URL cannot be blank")
private String originalUrl;  // Max 2048 chars

@Size(max = 500)
private String description;
```

**What We DON'T Validate (Risk):**
- ❌ URL reachability (we don't ping the URL)
- ❌ Malicious content detection (phishing, malware)
- ❌ XSS payloads (assumed safe)
- ❌ Reserved domains (e.g., bit.ly)

**Mitigation for Phase 2:**
```
[ ] Add URL scanning via VirusTotal API
[ ] Implement phishing detection
[ ] Maintain blocklist of dangerous domains
[ ] Log suspicious URLs for review
```

### Custom Alias Validation

**Current Approach:**
```java
String customAlias = (request.getCustomAlias() != null && !request.getCustomAlias().isBlank())
    ? request.getCustomAlias().trim()
    : shortCode;  // Auto-generate if blank
```

**Not Validated:**
- ❌ Special characters/symbols
- ❌ Reserved words (e.g., "admin", "config")
- ❌ Profanity/offensive content
- ❌ SQL keywords

**Recommendation for Phase 2:**
```yaml
# Add reserved words blocklist
reserved_aliases:
  - admin
  - api
  - config
  - health
  - swagger
  - actuator
  - internal
```

---

## 3. Authentication & Authorization

### Current (MVP) - PUBLIC API

**No authentication implemented:**
- All endpoints publicly accessible
- No API keys required
- No user accounts

**Risk:** Anyone can create, read, update, delete URLs

**Mitigation:** Deploy behind corporate firewall or API gateway

---

### Phase 2 - API Key Authentication

**Planned Implementation:**

```yaml
# Planned: application-secured.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.company.com
          jwk-set-uri: https://auth.company.com/jwks

# API Key header
X-API-Key: sk_live_abc123...
```

**Permission Levels (Phase 2):**
```
VIEWER     → GET endpoints only
EDITOR     → GET, POST, PUT endpoints
ADMIN      → All endpoints + delete
SUPERUSER  → System configuration
```

---

## 4. Data Protection

### Encryption at Rest (MVP)

**Current:** No encryption at rest
- H2 file-based database
- Stored as plain files in `./data/`

**Recommendation for Phase 2:**
```bash
# Enable H2 encryption
jdbc:h2:file:./data/urlshortenerdb;CIPHER=AES;FILE_PASSWORD=MyPassword

# OR use PostgreSQL with pgcrypto
# Enable transparent data encryption
```

### Encryption in Transit

**MVP:** HTTPS assumed to be handled by reverse proxy

**Production Checklist:**
- [ ] TLS 1.3 minimum
- [ ] Strong cipher suites only
- [ ] HSTS header enabled
- [ ] Certificate from trusted CA
- [ ] Certificate auto-renewal configured

**Nginx Config Example:**
```nginx
server {
    listen 443 ssl http2;
    server_name api.company.com;
    
    ssl_certificate /etc/ssl/cert.pem;
    ssl_certificate_key /etc/ssl/key.pem;
    ssl_protocols TLSv1.3 TLSv1.2;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
}
```

### Credential Management

**Current Practice:**

```yaml
# ✅ CORRECT - Use environment variables
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

**What NOT to do:**

```yaml
# ❌ WRONG - Hardcoded credentials
spring:
  datasource:
    url: jdbc:h2:file:./data/urlshortenerdb
    username: sa
    password: hardcoded-password  # NEVER!
```

**Secret Management Solutions:**
- HashiCorp Vault
- AWS Secrets Manager
- Azure Key Vault
- Kubernetes Secrets (with encryption at rest)

---

## 5. SQL Injection Prevention

### Current Approach (Using JPA)

**Safe - Uses Parameterized Queries:**

```java
// ✅ SAFE: JPA generates parameterized queries
@Query("SELECT s FROM ShortUrl s WHERE LOWER(s.customAlias) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
List<ShortUrl> searchByTerms(@Param("searchTerm") String searchTerm);
```

**Compiled Query:**
```sql
SELECT * FROM short_urls 
WHERE LOWER(custom_alias) LIKE LOWER(CONCAT('%', ?, '%'))
-- Parameter binding happens at database driver level
```

**Dangerous Pattern (DON'T USE):**

```java
// ❌ UNSAFE: Never do string concatenation!
String query = "SELECT * FROM short_urls WHERE custom_alias = '" + userInput + "'";
```

### SQL Injection Test Case

```bash
# Attempt SQL injection
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://example.com",
    "customAlias": "test\'; DROP TABLE short_urls; --"
  }'

# Result: Should return error about alias validation, NOT execute DROP TABLE
```

---

## 6. API Security

### CORS Configuration

**Current (MVP):** No CORS restriction

**Phase 2 - Restrict Origins:**

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("https://app.company.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("Content-Type", "Authorization")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

### Rate Limiting

**Current (MVP):** No rate limiting

**Phase 2 - Implement Rate Limits:**

```yaml
spring:
  cloud:
    circuitbreaker:
      resilience4j:
        configs:
          default:
            registerHealthIndicator: true
            slidingWindowSize: 100
            failureRateThreshold: 50
            waitDurationInOpenState: 10000

# Rate limiting per IP
1000 requests per minute per IP
100 requests per minute per API key
```

---

## 7. Security Headers

### Recommended Headers

```nginx
# X-Frame-Options: Prevent clickjacking
add_header X-Frame-Options "SAMEORIGIN" always;

# X-Content-Type-Options: Prevent MIME sniffing
add_header X-Content-Type-Options "nosniff" always;

# X-XSS-Protection: Enable XSS filter
add_header X-XSS-Protection "1; mode=block" always;

# Content-Security-Policy: Restrict resource loading
add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'" always;

# Referrer-Policy: Control referrer information
add_header Referrer-Policy "no-referrer-when-downgrade" always;
```

---

## 8. Logging & Audit Trail

### What We Log

**✅ Log These:**
```
[TIMESTAMP] [LEVEL] [USER] - Action: CREATE URL, Status: SUCCESS, ID: 123
[TIMESTAMP] [LEVEL] [USER] - Action: DELETE URL, Status: SUCCESS, ID: 456
[TIMESTAMP] [LEVEL] [SYSTEM] - Database connection established
[TIMESTAMP] [LEVEL] [SYSTEM] - Configuration loaded from: application.yml
```

**❌ DON'T Log These:**
```
- User IP addresses (privacy risk)
- Full request bodies (might contain secrets)
- Database credentials (environment vars only)
- Personal information (emails, phone numbers)
- Session tokens (could be replayed)
```

### Log Configuration

```yaml
# application.yml
logging:
  level:
    root: WARN
    com.urlshortener: INFO
    org.springframework.security: DEBUG
  
  file:
    name: logs/security.log
    max-size: 10MB
    max-history: 30  # Keep 30 days of logs
  
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    console: "%d{yyyy-MM-dd HH:mm:ss} [%-5level] %logger{0} - %msg%n"
```

---

## 9. Dependency Security

### CVE Scanning

**Check Dependencies:**
```bash
# Using Maven
./mvnw org.owasp:dependency-check-maven:check

# Using Snyk
npm install -g snyk
snyk test
```

**Current Dependencies Status:** ✅ No known CVEs
- Spring Boot 3.1.12
- H2 Database 2.2.224 (CVE-2022-45868 patched)
- Lombok 1.18.30
- All other dependencies checked and safe

### Dependency Update Policy

**Update Frequency:**
- Security patches: Immediately
- Minor updates: Within 1 week
- Major updates: After testing (within 1 month)

---

## 10. Deployment Security Checklist

**Before Production Release:**

- [ ] All dependencies scanned for CVEs
- [ ] No hardcoded secrets in code
- [ ] Database credentials in environment variables
- [ ] HTTPS enforced (TLS 1.3)
- [ ] HSTS header configured
- [ ] Security headers in place
- [ ] Rate limiting enabled
- [ ] Logging for all API access
- [ ] Backup strategy documented
- [ ] Audit logging enabled
- [ ] Access control reviewed
- [ ] Input validation tested
- [ ] Penetration testing completed
- [ ] Security headers verified
- [ ] CORS properly configured

---

## 11. Incident Response Plan

### If Data Breach Occurs

**Step 1: Immediate Response (0-1 hour)**
```
1. Isolate affected systems
2. Preserve evidence (logs, memory dump)
3. Notify security team
4. Begin investigation
```

**Step 2: Notification (1-24 hours)**
```
1. Assess scope of breach
2. Notify affected users/stakeholders
3. Update status page
4. Document incident timeline
```

**Step 3: Recovery (24+ hours)**
```
1. Deploy patched version
2. Restore from clean backup
3. Change all credentials
4. Monitor for suspicious activity
```

### If Service Compromise Detected

```bash
# Kill all connections
pkill -f "java.*urlshortener"

# Restore from backup
restore-database --backup=latest

# Redeploy application
docker pull registry.company.com/url-shortener:latest
docker run -d url-shortener:latest
```

---

## 12. Security Best Practices

### For Developers

1. **Principle of Least Privilege**
   - Grant minimum necessary permissions
   - Use separate accounts for dev/test/prod

2. **Input Validation**
   - Validate all user inputs
   - Use allowlist approach (whitelist safe inputs)
   - Reject suspicious patterns

3. **Error Handling**
   - Don't expose stack traces to users
   - Log errors internally
   - Return generic error messages

4. **Code Review**
   - All code merged via pull request
   - Require 2 approvals before merge
   - Security-focused review checklist

### For Operations

1. **Access Control**
   - Use SSH keys (no passwords)
   - Restrict database access to app servers only
   - Audit all production access

2. **Monitoring**
   - Alert on failed login attempts
   - Monitor CPU/memory for attacks
   - Track error rates (DoS indicator)

3. **Patching**
   - Weekly security updates
   - Monthly patching cycles
   - Emergency patches for CVEs

---

## 13. Compliance & Regulations

### GDPR Compliance (Phase 2)

**Current Gaps:**
- No user accounts/personal data
- No right-to-be-forgotten
- No data retention limit

**Phase 2 Additions:**
```
[ ] Implement data deletion on request
[ ] Add data retention policies
[ ] Document data processing
[ ] Implement privacy by design
```

### SOC2 Type II (Phase 3)

**Required Controls:**
- Logging & monitoring
- Access control
- Change management
- Incident response
- Disaster recovery

---

## 14. Security Contacts

| Issue | Contact | Phone | Email |
|-------|---------|-------|-------|
| Security Vulnerability | Security Lead | ext. 1234 | security@company.com |
| Data Breach | CISO | ext. 5000 | ciso@company.com |
| Incident Response | SOC Team | ext. 2000 | soc@company.com |
| Compliance | Compliance Officer | ext. 3000 | compliance@company.com |

---

## 15. References

- **OWASP Top 10:** https://owasp.org/www-project-top-ten/
- **Spring Security Docs:** https://spring.io/projects/spring-security
- **NIST Cybersecurity Framework:** https://www.nist.gov/cyberframework
- **CIS Benchmarks:** https://www.cisecurity.org/cis-benchmarks/

---

**Document Version History:**

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-07-24 | Initial security guide |

---

**Security Review Sign-off:**

- [ ] Security Lead: Reviewed
- [ ] Compliance Officer: Approved
- [ ] Engineering Lead: Acknowledged


