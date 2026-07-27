# URL Shortener Service - Requirements Document

**Version:** 1.0.0  
**Date:** July 24, 2026  
**Status:** Production Ready  
**Owner:** Engineering Team

---

## 1. Executive Summary

The URL Shortener Service is a Spring Boot REST API that transforms long URLs into shorter, memorable aliases. It provides core URL management, analytics tracking, and reliable redirection with persistent storage.

**Key Objectives:**
- Convert long URLs into short, shareable codes
- Track click metrics and analytics
- Support custom aliases for branded links
- Provide RESTful APIs for CRUD operations
- Ensure data persistence and reliability

---

## 2. Functional Requirements

### 2.1 Core URL Management

#### FR-1: Create Shortened URL
- **Description:** Accept a long URL and optionally a custom alias; return a short code and metadata
- **Inputs:**
  - `originalUrl` (required, string, max 2048 chars): The long URL to shorten
  - `customAlias` (optional, string): User-provided short name (leave empty for auto-generated)
  - `description` (optional, string, max 500 chars): Metadata about the URL
- **Outputs:**
  - `id`: Unique database identifier
  - `shortCode`: Auto-generated short code (10 chars, base64)
  - `customAlias`: User alias or generated short code
  - `originalUrl`: The target URL
  - `clickCount`: Click tracking (starts at 0)
  - `isActive`: Status flag (default: true)
  - `createdAt`, `updatedAt`: Timestamps
  - `shortenedUrl`: Full shortened URL (e.g., `http://localhost:8080/api/s/abc123`)
- **Business Rules:**
  - Custom alias must be unique; reject duplicates with 409 Conflict
  - originalUrl is required; reject blank with 400 Bad Request
  - Auto-generate short code using timestmap + base64 encoding if alias not provided

#### FR-2: Retrieve URL Details
- **Description:** Fetch full metadata by short code or custom alias
- **Input:** `shortCode` or `customAlias` (path parameter)
- **Output:** Full `ShortenUrlResponse` object
- **Business Rules:**
  - Support lookup by both `shortCode` and `customAlias`
  - Return 404 if not found

#### FR-3: Redirect to Original URL
- **Description:** Redirect HTTP request to original URL and increment click count
- **Input:** `shortCode` or `customAlias` (path parameter)
- **Output:** 302 Found with Location header
- **Business Rules:**
  - Increment `clickCount` atomically
  - Respect `isActive` flag; reject inactive URLs with 404
  - Return 404 if URL not found

#### FR-4: Update URL Metadata
- **Description:** Modify `originalUrl` or `description` for an existing entry
- **Input:** 
  - `id` (path): Database ID
  - `originalUrl` (optional): New target URL
  - `description` (optional): New metadata
- **Output:** Updated `ShortenUrlResponse`
- **Business Rules:**
  - Cannot update `customAlias` (immutable)
  - Return 404 if ID not found
  - Update `updatedAt` timestamp

#### FR-5: Delete URL Entry
- **Description:** Permanently remove a shortened URL
- **Input:** `id` (path)
- **Output:** 204 No Content
- **Business Rules:**
  - Return 404 if ID not found
  - Deletion is permanent (no soft delete)

#### FR-6: List All URLs
- **Description:** Retrieve all shortened URLs in the system
- **Input:** None
- **Output:** Array of `ShortenUrlResponse` objects
- **Business Rules:**
  - Return empty array if no URLs exist
  - Support pagination in future iterations

### 2.2 Search & Analytics

#### FR-7: Search URLs
- **Description:** Find URLs by keyword (alias, description, or original URL)
- **Input:** `term` (query parameter, string)
- **Output:** Array of matching `ShortenUrlResponse` objects
- **Business Rules:**
  - Case-insensitive matching
  - Search across `customAlias` and `description`
  - Return empty array if no matches

#### FR-8: Get Top Clicked URLs
- **Description:** Retrieve URLs sorted by click count (analytics)
- **Input:** `limit` (query parameter, default 10, int)
- **Output:** Top N `ShortenUrlResponse` objects ordered by `clickCount` descending
- **Business Rules:**
  - Default limit = 10
  - Never return `clickCount < 0` (invalid state)

### 2.3 Health & Observability

#### FR-9: Health Check
- **Description:** Verify service health and readiness
- **Input:** None
- **Output:** 
  ```json
  {
    "status": "UP",
    "service": "URL Shortener Service",
    "version": "1.0.0",
    "timestamp": 1721779200000
  }
  ```
- **Business Rules:**
  - Always respond with 200 OK if database is accessible

#### FR-10: API Info
- **Description:** Return service metadata
- **Input:** None
- **Output:**
  ```json
  {
    "name": "URL Shortener Service API",
    "version": "1.0.0",
    "description": "...",
    "database": "H2 (Embedded)",
    "documentation": "/api/swagger-ui.html"
  }
  ```

---

## 3. Non-Functional Requirements

### 3.1 Performance
- **API Latency:** P95 < 100ms for GET/POST under normal load
- **Throughput:** Support 1000 RPS per instance
- **Click Tracking:** Atomic increment without race conditions

### 3.2 Reliability
- **Availability:** 99.5% uptime SLA
- **Data Persistence:** H2 file-based database (survives restarts)
- **Graceful Degradation:** Reject requests during database outage with 503

### 3.3 Security
- **HTTPS:** Enforce in production (not dev)
- **Input Validation:** Reject oversized/malformed payloads
- **Rate Limiting:** Implement per IP/user in future
- **CORS:** Configure appropriately for frontend integration
- **Secrets:** Store database credentials in environment variables

### 3.4 Maintainability
- **Code Quality:** Minimum 80% unit test coverage
- **Documentation:** OpenAPI/Swagger auto-generated
- **Logging:** DEBUG level for business logic, INFO for operations
- **Error Handling:** Standardized `ErrorResponse` format with correlation IDs

### 3.5 Scalability
- **Horizontal Scaling:** Stateless design; multiple instances behind load balancer
- **Database:** H2 embedded for prototype; migrate to PostgreSQL for production scale
- **Caching:** No cache required for MVP; add Redis in future for analytics

---

## 4. API Specifications (High-Level)

### Base URL
```
http://localhost:8080/api
```

### Endpoints Summary

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| `POST` | `/urls` | Create short URL | 201 Created |
| `POST` | `/urls/bulk` | Bulk create (5+ URLs) | 201 Created |
| `GET` | `/urls` | List all URLs | 200 OK |
| `GET` | `/urls/{shortCode}` | Get URL by code/alias | 200 OK |
| `GET` | `/urls/redirect/{shortCode}` | Redirect + track | 302 Found |
| `GET` | `/urls/search?term=...` | Search URLs | 200 OK |
| `GET` | `/urls/analytics/top?limit=10` | Top clicked | 200 OK |
| `PUT` | `/urls/{id}` | Update URL | 200 OK |
| `DELETE` | `/urls/{id}` | Delete URL | 204 No Content |
| `GET` | `/health` | Health check | 200 OK |
| `GET` | `/health/info` | API info | 200 OK |

### Error Handling

All errors return standardized response:
```json
{
  "timestamp": "2026-07-24T12:00:00",
  "status": 400,
  "message": "Validation failed",
  "path": "/api/urls",
  "errors": {
    "originalUrl": "Original URL cannot be blank"
  }
}
```

**HTTP Status Codes:**
- `200 OK`: Success
- `201 Created`: Resource created
- `204 No Content`: Success with no body
- `302 Found`: Redirect
- `400 Bad Request`: Invalid input
- `404 Not Found`: Resource not found
- `409 Conflict`: Duplicate alias
- `500 Internal Server Error`: Unexpected error

---

## 5. Data Model

### ShortUrl Entity

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | Long | PK, AUTO_INCREMENT | Database ID |
| `shortCode` | String(10) | UNIQUE, NOT NULL | Base64 encoded timestamp |
| `customAlias` | String(255) | UNIQUE, NOT NULL | User-friendly name or shortCode |
| `originalUrl` | String(2048) | NOT NULL | Target URL |
| `clickCount` | Long | NOT NULL, default 0 | Click tracking |
| `description` | String(500) | Nullable | Metadata |
| `isActive` | Boolean | NOT NULL, default true | Status flag |
| `createdAt` | LocalDateTime | NOT NULL | Insertion timestamp |
| `updatedAt` | LocalDateTime | NOT NULL | Last update timestamp |

---

## 6. Integration Points

### External Dependencies
- **H2 Database:** File-based, auto-initialized on startup
- **OpenAPI/Swagger:** Auto-generated API docs at `/swagger-ui.html`
- **Lombok:** Reduces boilerplate (annotation processing)
- **Spring Data JPA:** ORM abstraction over Hibernate

### Planned Integrations (Future)
- PostgreSQL for production storage
- Redis for analytics caching
- Kafka for async click tracking
- DataDog/Prometheus for monitoring

---

## 7. Acceptance Criteria

- [ ] All 10 functional requirements tested and passing
- [ ] API contracts match OpenAPI spec
- [ ] Unit test coverage ≥ 80%
- [ ] Integration tests for all happy paths and error cases
- [ ] Error handling returns correct HTTP status and message
- [ ] Click tracking increments atomically
- [ ] Duplicate alias detection works
- [ ] Swagger UI renders without errors
- [ ] Database persists data across restarts
- [ ] All documentation up-to-date

---

## 8. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| API Response Time (P95) | < 100ms | Application logs |
| Test Coverage | ≥ 80% | JaCoCo report |
| Uptime | ≥ 99.5% | Production monitoring |
| Data Integrity | 100% | Transaction logs |
| API Documentation | 100% | Swagger spec |

---

## 9. Assumptions

### Technical Assumptions
- **Java Runtime:** Java 17+ available on deployment environments
- **Network:** Users have stable internet connectivity (no offline mode)
- **Browser Support:** Modern browsers (Chrome, Firefox, Safari, Edge 2020+)
- **Database:** PostgreSQL available for production (after H2 MVP phase)
- **API Consumption:** Clients can handle HTTP redirects (302 status code)
- **Data Format:** All URLs are valid HTTP/HTTPS URLs (no FTP, etc.)

### Operational Assumptions
- **Deployment:** Can be deployed to Docker, Kubernetes, or traditional VMs
- **Monitoring:** Prometheus/Grafana available for production monitoring
- **Logging:** Centralized log aggregation (ELK stack or equivalent)
- **Backup:** Automated daily database backups (handled by ops team)
- **Maintenance:** Scheduled maintenance windows allowed (notify users 48 hours ahead)

### Business Assumptions
- **Users:** Internal employees and external partners are the primary users
- **Scale:** Expected 1000s of short URLs initially, 100Ks in Y2
- **Aliases:** Custom aliases are optional and user-managed (no conflicts with system)
- **Data Retention:** URLs stored indefinitely unless explicitly deleted
- **Analytics:** Click tracking sufficient for business metrics (no real-time dashboard needed)
- **Cost:** Free internal service (no monetization in MVP)

### Data & Validation Assumptions
- **URL Validity:** All provided URLs are syntactically valid (basic validation only)
- **URL Reachability:** We don't validate if URLs are actually reachable/alive
- **XSS Prevention:** Original URLs trusted (no sanitization of malicious content)
- **Click Accuracy:** Click counts acceptable with ±1 variance in concurrent scenarios
- **Character Encoding:** UTF-8 for all text fields
- **Timezone:** All timestamps in UTC (no locale-specific times)

### Performance Assumptions
- **Peak Load:** Max 100 RPS per instance (handled by load balancer)
- **Response Time:** P95 latency < 100ms acceptable for MVP
- **Database Size:** ~700K URLs fitting in single H2 file
- **Concurrent Users:** <100 simultaneous users before needing horizontal scaling
- **Storage Growth:** 1MB per 1000 URLs (estimate)

---

## 10. Out of Scope

### MVP (Current Release - v1.0)

#### NOT Included - Feature-Related
- ❌ **User Accounts & Authentication:** No login required, public API
- ❌ **Role-Based Access Control:** No user permissions or role management
- ❌ **API Key Management:** No persistent API keys (future Phase 2)
- ❌ **URL Preview/Metadata:** No Open Graph, title, description extraction
- ❌ **Vanity URLs:** Paid premium aliases (marketing feature)
- ❌ **QR Code Generation:** Not generating QR codes for short URLs
- ❌ **Deep Linking:** No mobile app integration or deep link handling
- ❌ **Custom Domains:** No support for `myshortener.com/abc` (only default domain)
- ❌ **Link Expiration:** No automatic link age/expiration
- ❌ **URL Cloaking:** No hiding original URL until click (privacy feature)

#### NOT Included - Analytics
- ❌ **Real-Time Dashboard:** No live analytics UI
- ❌ **Geographic Analytics:** No country/location of clickers
- ❌ **Device Analytics:** No mobile vs desktop breakdown
- ❌ **Referrer Tracking:** No source/referrer information
- ❌ **Bot Detection:** No filtering of bot clicks
- ❌ **Predictive Analytics:** No ML-based click forecasts

#### NOT Included - Integration
- ❌ **Social Media Integration:** No Twitter/LinkedIn auto-posting
- ❌ **Webhook Notifications:** No event webhooks for clicks
- ❌ **Third-Party OAuth:** No login via Google/GitHub
- ❌ **Browser Extensions:** No Chrome/Firefox extension
- ❌ **Slack/Teams Bots:** No chat platform integration
- ❌ **IFTTT/Zapier Support:** No workflow automation platforms

#### NOT Included - Operational
- ❌ **Multi-Tenancy:** Single tenant only (one organization)
- ❌ **Multi-Region Deployment:** Single region (production location)
- ❌ **High Availability:** No active-active failover (manual recovery)
- ❌ **Disaster Recovery Plan:** Basic backups only, no documented RTO/RPO
- ❌ **Load Balancing:** Assuming external LB (not included)
- ❌ **CDN Integration:** No edge caching

#### NOT Included - Security
- ❌ **SSL/TLS Management:** Assuming reverse proxy handles HTTPS
- ❌ **DDoS Protection:** No rate limiting or bot protection (Phase 2)
- ❌ **URL Filtering:** No malicious URL detection (Phase 3)
- ❌ **Access Logs Audit:** No compliance audit trail (Phase 2)
- ❌ **Encryption at Rest:** Plain database (assume ops team secures)
- ❌ **PII Scanning:** No automatic detection of passwords/secrets in URLs

#### NOT Included - Compliance
- ❌ **GDPR Compliance:** No right-to-be-forgotten implementation
- ❌ **SOC2 Certification:** No formal compliance audit (planned Y2)
- ❌ **Data Residency:** No geographic data residency requirements
- ❌ **Encryption Standards:** No FIPS compliance
- ❌ **Backup Retention Laws:** Basic retention only

#### NOT Included - UI/UX
- ❌ **Web Dashboard:** No UI for managing URLs (API only in MVP)
- ❌ **Mobile App:** No iOS/Android application
- ❌ **Browser Extensions:** No quick-shorten bookmarklet
- ❌ **Admin Panel:** No admin interface for operations
- ❌ **Batch Operations UI:** No file upload for bulk URL shortening

---

## 11. Constraints

### Architectural Constraints
- **Database:** H2 file-based for MVP (single instance, no clustering)
- **Deployment:** Single Java process (no microservices)
- **Language:** Java/Spring Boot only (no polyglot support)
- **Framework:** Spring Boot 3.1.x only

### Technical Constraints
- **Concurrent Users:** Single instance handles <100 concurrent users
- **Data Volume:** H2 efficiently handles ~700K URLs (exceeding requires PostgreSQL)
- **Message Queues:** No async processing (synchronous only)
- **Caching:** No cache layer (every request hits database)
- **Search:** LIKE wildcard matching only (no Elasticsearch)

### Operational Constraints
- **Availability:** No SLA in MVP (best effort)
- **Backup Frequency:** Manual backups (no automated backup job)
- **Recovery:** Manual restore process (no automated failover)
- **Monitoring:** Basic health checks only (no advanced metrics)
- **Maintenance Window:** Requires service restart (no rolling upgrades)

### Business Constraints
- **Cost:** Budget-friendly deployment (no premium services)
- **Licensing:** Open-source compatible (Apache 2.0)
- **Support Model:** Community support only (no paid SLA)
- **Usage Limits:** No enforced quotas per user
- **Pricing:** Free tier only (no paid tiers)

---

## 12. Rollout Plan

### Phase 1: MVP (Current)
- Core CRUD operations
- Basic analytics (click tracking)
- File-based persistence
- Swagger documentation

### Phase 2: Hardening (Week 2-3)
- Input validation enhancements
- Bulk operations API
- Rate limiting
- Comprehensive logging
- Error tracking (Sentry)

### Phase 3: Scale (Month 2)
- PostgreSQL migration
- Redis caching
- Async processing (Kafka)
- Monitoring dashboards

---

## 13. Document Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2026-07-24 | Engineering Team | Initial version |

---

**Approval Sign-off:**

- [ ] Product: Approved
- [ ] Engineering: Approved
- [ ] Operations: Approved


