# URL Shortener Service - Technical Architecture Document

**Version:** 1.0.0  
**Date:** July 24, 2026  
**Status:** Production Ready  
**Owner:** Engineering Team

---

## 1. System Overview

The URL Shortener Service is a lightweight, stateless REST API built on Spring Boot 3.1.12 with an embedded H2 database. It transforms long URLs into short, memorable aliases while tracking usage metrics.

### Key Characteristics
- **Architecture Pattern:** Layered monolith (Controller → Service → Repository)
- **Deployment Model:** Single JAR, stateless, scales horizontally
- **Database:** H2 file-based (dev/prototype); PostgreSQL target (production)
- **API Style:** RESTful with OpenAPI/Swagger documentation
- **Build Tool:** Maven 3.9+
- **Runtime:** Java 17+

---

## 2. High-Level Architecture

```
┌────────────────────────────────────────────────────────────────┐
│  HTTP Client (Browser / Postman / Mobile App)                 │
└────────────────────────┬─────────────────────────────────────┘
                         │ HTTP/HTTPS
                         ▼
┌────────────────────────────────────────────────────────────────┐
│  Spring Boot 3.1.12 Application (Port 8080)                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  REST Controllers (Swagger Annotations)                  │  │
│  │  - ShortUrlController                                    │  │
│  │  - HealthController                                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                         ▼                                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Service Layer (Business Logic)                          │  │
│  │  - ShortUrlService                                       │  │
│  │    * URL creation & validation                           │  │
│  │    * Alias collision detection                           │  │
│  │    * Click tracking (atomic increment)                   │  │
│  │    * Search & analytics queries                          │  │
│  └──────────────────────────────────────────────────────────┘  │
│                         ▼                                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Repository Layer (Data Access)                          │  │
│  │  - ShortUrlRepository (extends JpaRepository)            │  │
│  │    * CRUD operations                                     │  │
│  │    * Custom queries (search, analytics)                  │  │
│  │    * JPA/Hibernate-generated SQL                         │  │
│  └──────────────────────────────────────────────────────────┘  │
│                         ▼                                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Exception Handling (Global)                             │  │
│  │  - GlobalExceptionHandler (@RestControllerAdvice)        │  │
│  │    * DuplicateAliasException → 409                       │  │
│  │    * ResourceNotFoundException → 404                     │  │
│  │    * MethodArgumentNotValidException → 400               │  │
│  │    * Generic Exception → 500                             │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬─────────────────────────────────────┘
                         │ JDBC
                         ▼
┌────────────────────────────────────────────────────────────────┐
│  H2 Database Engine (Embedded)                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  File-Based Storage: ./data/urlshortenerdb.mv.db          │  │
│  │  ┌──────────────────────────────────────────────────────┐ │  │
│  │  │  Table: short_urls                                   │ │  │
│  │  │  - id (PK, AUTO_INCREMENT)                           │ │  │
│  │  │  - shortCode (UNIQUE)                                │ │  │
│  │  │  - customAlias (UNIQUE)                              │ │  │
│  │  │  - originalUrl                                       │ │  │
│  │  │  - clickCount                                        │ │  │
│  │  │  - description                                       │ │  │
│  │  │  - isActive                                          │ │  │
│  │  │  - createdAt (indexed)                               │ │  │
│  │  │  - updatedAt                                         │ │  │
│  │  └──────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
```

---

## 3. Component Design

### 3.1 Controller Layer

#### ShortUrlController
**Location:** `src/main/java/com/urlshortener/controller/ShortUrlController.java`

**Responsibilities:**
- Handle HTTP routing and dispatching
- Validate request format (delegated to JSR-380 validators)
- Serialize responses to JSON (Jackson)
- Return appropriate HTTP status codes

**Endpoints:**
```
POST   /urls              → createShortUrl()
GET    /urls              → getAllShortUrls()
GET    /urls/{shortCode}  → getShortUrl()
GET    /urls/redirect/{shortCode} → redirectToOriginal()
PUT    /urls/{id}         → updateShortUrl()
DELETE /urls/{id}         → deleteShortUrl()
GET    /urls/search       → searchShortUrls()
GET    /urls/analytics/top → getTopClickedUrls()
```

**Key Features:**
- OpenAPI/Swagger annotations for auto-documentation
- Request body examples in Swagger UI (5 default samples for bulk endpoint)
- Proper HTTP status codes (201 Created, 404 Not Found, 409 Conflict, etc.)

#### HealthController
**Location:** `src/main/java/com/urlshortener/controller/HealthController.java`

**Endpoints:**
```
GET /health      → health()       (returns status, timestamp)
GET /health/info → info()         (returns service metadata)
```

---

### 3.2 Service Layer

#### ShortUrlService
**Location:** `src/main/java/com/urlshortener/service/ShortUrlService.java`

**Responsibilities:**
- Implement business logic
- Validate input constraints
- Manage transactions (@Transactional)
- Coordinate with repository layer
- Map entities to DTOs

**Key Methods:**

| Method | Logic |
|--------|-------|
| `createShortUrl()` | Generate unique short code; check alias collision; persist entity |
| `getShortUrl()` | Lookup by shortCode OR customAlias; return DTO |
| `getOriginalUrl()` | Lookup by shortCode OR customAlias; atomically increment clickCount; check isActive |
| `updateShortUrl()` | Fetch by ID; update originalUrl/description; persist |
| `deleteShortUrl()` | Fetch by ID; delete permanently |
| `getAllShortUrls()` | Query all; map to DTOs |
| `searchShortUrls()` | Delegate to custom repository query |
| `getTopClickedUrls()` | Query top N by clickCount DESC |
| `mapToResponse()` | Convert ShortUrl entity → ShortenUrlResponse DTO |

**Transaction Management:**
- Create, Update, Delete: Full transaction (write)
- Read, Search: Read-only transaction (`@Transactional(readOnly=true)`)

**Short Code Generation:**
```java
private String generateShortCode() {
    String code;
    do {
        long timestamp = System.currentTimeMillis();
        code = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(Long.toString(timestamp).getBytes())
            .substring(0, 10);
    } while (shortUrlRepository.existsByShortCode(code));
    return code;
}
```
- Uses timestamp + base64 encoding
- Guarantees uniqueness via collision retry loop
- O(n) in event of collision (rare)

**Alias Handling:**
```java
String customAlias = (request.getCustomAlias() != null && !request.getCustomAlias().isBlank())
    ? request.getCustomAlias().trim()
    : shortCode;
```
- Treats blank/null as "not provided"
- Auto-generates from shortCode if omitted
- Allows repeated API calls without 409 collisions

---

### 3.3 Repository Layer

#### ShortUrlRepository
**Location:** `src/main/java/com/urlshortener/repository/ShortUrlRepository.java`

**Inheritance:** `extends JpaRepository<ShortUrl, Long>`

**Out-of-the-Box Methods (JpaRepository):**
```java
Optional<ShortUrl> findById(Long id)
Optional<ShortUrl> findByShortCode(String shortCode)
Optional<ShortUrl> findByCustomAlias(String customAlias)
boolean existsByShortCode(String shortCode)
boolean existsByCustomAlias(String customAlias)
List<ShortUrl> findAll()
ShortUrl save(ShortUrl entity)
void delete(ShortUrl entity)
```

**Custom Queries:**

```java
@Query("SELECT s FROM ShortUrl s WHERE LOWER(s.customAlias) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
       "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
List<ShortUrl> searchByTerms(@Param("searchTerm") String searchTerm);
```
- Case-insensitive substring matching
- Searches alias and description fields
- Returns empty list if no matches

```java
@Query(value = "SELECT * FROM short_urls ORDER BY click_count DESC LIMIT :limit", nativeQuery = true)
List<ShortUrl> findTopClicked(@Param("limit") int limit);
```
- Native query for performance
- Returns top N by clickCount
- No filtering on isActive (future consideration)

---

### 3.4 Data Transfer Objects (DTOs)

#### ShortenUrlRequest
**Location:** `src/main/java/com/urlshortener/dto/ShortenUrlRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlRequest {
    @NotBlank(message = "Original URL cannot be blank")
    @Schema(description = "The long URL to be shortened")
    private String originalUrl;

    @Schema(description = "Custom alias (optional; leave empty to auto-generate)")
    private String customAlias;

    @Size(max = 500)
    @Schema(description = "Optional description")
    private String description;
}
```

**Validation:**
- `@NotBlank` on originalUrl (enforced at endpoint, triggers 400 if missing)
- `@Size(max=500)` on description (soft constraint)
- Custom alias: no validation (business logic handles blank/duplicate)

#### ShortenUrlResponse
**Location:** `src/main/java/com/urlshortener/dto/ShortenUrlResponse.java`

```java
@Data
@Builder
public class ShortenUrlResponse {
    private Long id;
    private String shortCode;
    private String customAlias;
    private String originalUrl;
    private Long clickCount;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String shortenedUrl; // Full URL: http://localhost:8080/api/s/{shortCode}
}
```

---

### 3.5 Entity Model

#### ShortUrl
**Location:** `src/main/java/com/urlshortener/entity/ShortUrl.java`

```java
@Entity
@Table(name = "short_urls")
public class ShortUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false, unique = true)
    private String customAlias;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false)
    private Long clickCount = 0L;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.clickCount = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

**Lifecycle Hooks:**
- `@PrePersist`: Auto-set `createdAt`, `updatedAt`, `clickCount` on insert
- `@PreUpdate`: Auto-update `updatedAt` on modify

**Constraints:**
- `shortCode` & `customAlias`: Unique to prevent collisions
- `originalUrl`: Max 2048 chars (URL length limit)
- `description`: Optional, max 1000 chars
- `clickCount`: Initialized to 0, incremented by business logic

---

### 3.6 Exception Handling

#### GlobalExceptionHandler
**Location:** `src/main/java/com/urlshortener/exception/GlobalExceptionHandler.java`

**Exceptions Handled:**

| Exception | HTTP Status | Response |
|-----------|-------------|----------|
| `ResourceNotFoundException` | 404 | `{"status":404, "message":"Short URL not found"}` |
| `DuplicateAliasException` | 409 | `{"status":409, "message":"Custom alias ... is already in use"}` |
| `MethodArgumentNotValidException` | 400 | `{"status":400, "message":"Validation failed", "errors":{...}}` |
| `Exception` (catch-all) | 500 | `{"status":500, "message":"An unexpected error occurred"}` |

**Response Format:**
```json
{
  "timestamp": "2026-07-24T12:00:00",
  "status": 400,
  "message": "...",
  "path": "/api/urls",
  "errors": { "field": "error message" }
}
```

---

## 4. Data Flow Diagrams

### 4.1 Create Short URL Flow

```
Client POST /api/urls {"originalUrl":"...","customAlias":"myalias"}
    │
    ▼
ShortUrlController.createShortUrl()
    │ @Valid validation (JSR-380)
    ├─ @NotBlank originalUrl ✓
    └─ customAlias (nullable)
    │
    ▼
ShortUrlService.createShortUrl()
    │
    ├─ Generate shortCode (Base64 + timestamp)
    ├─ Resolve customAlias (provided OR shortCode)
    ├─ existsByCustomAlias() check → repository query
    │   └─ If duplicate → throw DuplicateAliasException → 409 Conflict
    ├─ Create ShortUrl entity
    ├─ repository.save() → INSERT into database
    │   └─ @PrePersist: set createdAt, updatedAt, clickCount
    └─ mapToResponse()
    │
    ▼
GlobalExceptionHandler (if exception)
    └─ DuplicateAliasException → 409
    └─ Exception → 500
    │
    ▼
HTTP 201 Created + ShortenUrlResponse JSON
```

### 4.2 Redirect & Click Tracking Flow

```
Client GET /api/urls/redirect/myalias
    │
    ▼
ShortUrlController.redirectToOriginal(myalias)
    │
    ▼
ShortUrlService.getOriginalUrl(myalias)
    │
    ├─ findByShortCode(myalias) OR findByCustomAlias(myalias)
    ├─ Check isActive flag
    │   └─ If inactive → throw ResourceNotFoundException → 404
    ├─ Atomically: shortUrl.clickCount++
    ├─ repository.save() → UPDATE clickCount
    │   └─ @PreUpdate: set updatedAt
    └─ return originalUrl
    │
    ▼
HTTP 302 Found + Location: {originalUrl}
```

### 4.3 Search URLs Flow

```
Client GET /api/urls/search?term=java
    │
    ▼
ShortUrlController.searchShortUrls("java")
    │
    ▼
ShortUrlService.searchShortUrls("java")
    │
    ├─ repository.searchByTerms("java")
    │   └─ JPQL: LOWER(s.customAlias) LIKE LOWER(CONCAT('%','java','%'))
    │           OR LOWER(s.description) LIKE LOWER(CONCAT('%','java','%'))
    └─ mapToResponse() for each
    │
    ▼
HTTP 200 OK + Array[ShortenUrlResponse]
```

---

## 5. Technology Stack

### Core Framework

| Component | Version | Rationale |
|-----------|---------|-----------|
| Spring Boot | 3.1.12 | Latest stable; Jakarta EE; native compilation ready |
| Spring Data JPA | 3.1.12 | Simplifies CRUD & custom queries; Hibernate 6.2 included |
| Spring Web | 3.1.12 | REST controllers, request mapping |
| Lombok | 1.18.30 | Reduces boilerplate (@Data, @Builder) |

### Database

| Component | Version | Rationale |
|-----------|---------|-----------|
| H2 Database | 2.2.224 | Embedded, file-based; zero-config; CVE-2022-45868 patched |
| Spring Data H2 Console | 3.1.12 | Web UI for Schema inspection |

### API Documentation

| Component | Version | Rationale |
|-----------|---------|-----------|
| Springdoc OpenAPI | 2.0.4 | Auto-generates OpenAPI 3.0 spec from annotations |
| Swagger UI | bundled | Interactive API testing in browser |

### Testing

| Component | Version | Rationale |
|-----------|---------|-----------|
| JUnit 5 | 5.9.3 | Standard test framework |
| Mockito | 5.3.1 | Mocking service layer dependencies |
| Spring Test | 3.1.12 | @WebMvcTest, MockMvc for integration tests |
| JaCoCo | (Maven) | Code coverage reporting |

### Build & Runtime

| Component | Version | Rationale |
|-----------|---------|-----------|
| Java | 17+ | LTS version; strong performance |
| Maven | 3.9+ | Dependency management; standard in Spring ecosystem |
| Maven Compiler | 17 | Source & target compatibility |



---

## 6. Deployment Architecture

### Development Environment
```
Developer Laptop
├─ JDK 17+
├─ Maven 3.9+
├─ IDE (IntelliJ / VS Code)
│
Command: mvn spring-boot:run
Output: http://localhost:8080/api
        Database: ./data/urlshortenerdb.mv.db
```

### Staging/Production Target
```
Docker Container
├─ Base Image: eclipse-temurin:17-jre-slim
├─ JAR: build/url-shortener-service-1.0.0.jar
└─ Volume Mount: /data (persistent storage)

Environment Variables:
├─ JAVA_OPTS="-Xmx512m -Xms256m"
├─ SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/urlshortener
├─ SPRING_DATASOURCE_USERNAME=root
├─ SPRING_DATASOURCE_PASSWORD=$(secret)
├─ SERVER_SERVLET_CONTEXT_PATH=/api
└─ LOGGING_LEVEL_ROOT=INFO
```

### Load Balancer / Ingress
```
External Traffic
    │
    ├─ HTTPS (TLS 1.3)
    ▼
Nginx / AWS ALB
    │
    ├─ /api/*  → Service Instances (3+)
    ├─ Rate Limiting: 1000 RPS per client IP
    └─ Path-based routing
```

---

## 7. Security Architecture

### Authentication & Authorization
- **MVP:** None (stateless, public API)
- **Phase 2:** API Key / Bearer Token with role-based access

### Input Validation
- JSR-380 annotations (@NotBlank, @Size)
- Custom business logic (alias uniqueness)
- MaxRequestSize limiter in Spring config

### Data Protection
- Database credentials in environment variables (not committed)
- H2 file permissions: mode 0600 (dev); use PostgreSQL roles (prod)
- HTTPS enforced in production (reverse proxy)
- No sensitive data logged (credit cards, secrets)

### SQL Injection Prevention
- JPA named parameters (@Param) — not string concatenation
- Prepared statements via Hibernate ORM layer

---

## 8. Performance Considerations

### Query Optimization

**Indexes in H2/PostgreSQL:**
```sql
CREATE UNIQUE INDEX idx_shortcode ON short_urls(short_code);
CREATE UNIQUE INDEX idx_customalias ON short_urls(custom_alias);
CREATE INDEX idx_createdAt ON short_urls(created_at);
CREATE INDEX idx_clickcount ON short_urls(click_count DESC);
```

**Slow Query Thresholds:**
- GET single URL: < 10ms (indexed lookup)
- GET all URLs (1000 rows): < 50ms
- Search: < 100ms (wildcard LIKE on index)
- Analytics (top 10): < 20ms (ORDER BY + LIMIT)

### Caching Strategy (Future)
- **Not Implemented MVP:** Every request hits database
- **Phase 2:** Redis cache for analytics (TTL 60s)
- **Phase 3:** CDN for redirect endpoints (301 Moved Permanently cacheable)

### Connection Pooling
```yaml
spring.datasource.hikari.maximum-pool-size: 10
spring.datasource.hikari.minimum-idle: 5
spring.jpa.properties.hibernate.jdbc.batch_size: 20
```

---

## 9. Monitoring & Observability

### Logging Structure
```
[TIMESTAMP] [LEVEL] [LOGGER_NAME] [THREAD] - [MESSAGE]

Examples:
2026-07-24 12:00:00 INFO  com.urlshortener.service.ShortUrlService [http-nio-8080-exec-1] - Creating shortened URL for: https://example.com/long/path
2026-07-24 12:00:01 DEBUG com.urlshortener.service.ShortUrlService [http-nio-8080-exec-1] - Generated short code: abc123XYZ
2026-07-24 12:00:02 ERROR com.urlshortener.exception.GlobalExceptionHandler [http-nio-8080-exec-2] - DuplicateAliasException: Custom alias 'myalias' already exists
```

**Log Levels:**
- `DEBUG`: Business logic flow (shorten URL, generate code)
- `INFO`: State changes (insert, update, delete)
- `WARN`: Recoverable errors (not found, duplicate)
- `ERROR`: Unrecoverable issues (database down, 500 errors)

### Metrics (Future)
- Request count by endpoint
- Response time percentiles (P50, P95, P99)
- Error rates (4xx, 5xx)
- Database connection pool utilization
- Cache hit/miss rates

### Health Checks
```
GET /health → {"status":"UP", ...]
GET /health/db → checks database connectivity (Actuator)
```

---

## 10. Known Limitations & Trade-offs

| Aspect | Limitation | Trade-off | Mitigation |
|--------|-----------|-----------|-----------|
| **Database** | H2 single-file, ~700K rows efficient | Prototype only | PostgreSQL migration in Phase 3 |
| **Concurrency** | No distributed locking | Click counts may be ±1 in high contention | Eventual consistency acceptable for analytics |
| **Search** | LIKE wildcard, no full-text | Smaller dataset OK (< 100K) | Elasticsearch in Phase 3 if needed |
| **Caching** | None (every request → DB) | Simple but slow at scale | Redis in Phase 2 |
| **Auth** | None | Public API | API Gateway + OAuth2 in Phase 2 |
| **Rate Limiting** | None | DoS risk | Nginx/WAF in Phase 2 |
| **Soft Deletes** | Hard delete only | Data gone forever | Add is_deleted flag in Phase 2 |

---

## 11. Decision Log

### D1: Single-Instance Monolith vs. Microservices
**Decision:** Monolith  
**Rationale:** MVP scope; no dependency isolation needed; simpler operations  
**Revisit:** Phase 3 if analytics/analytics services need independent scaling

### D2: H2 vs. PostgreSQL
**Decision:** H2 (file-based embedded)  
**Rationale:** Zero-config deployment; sufficient for prototype  
**Migration:** PostgreSQL + connection pooling for production (after volume validation)

### D3: Custom Short Code Generation vs. Hashids Library
**Decision:** Custom (Base64 + timestamp)  
**Rationale:** Lightweight, deterministic, no external dependency  
**Trade-off:** Less compact than Hashids; UUID would be more random but longer

### D4: Global Exception Handler vs. Individual Try-Catch
**Decision:** Global (@RestControllerAdvice)  
**Rationale:** Centralized error formatting; consistent HTTP status codes  
**Benefit:** Easier to maintain; single point for correlation IDs, observability

### D5: Transactional Read-Only Optimization
**Decision:** Use `@Transactional(readOnly=true)` for GETs  
**Rationale:** Helps Hibernate skip dirty-checking; micro-optimization for reads  
**Impact:** Minimal performance gain in MVP; more important at scale

---

## 12. Testing Strategy

### Unit Tests (Service Layer)
- Mocked repository dependencies
- Business logic validation (collision, click tracking, etc.)
- Exception scenarios
- Target: 85%+ coverage

### Integration Tests (Controller Layer)
- MockMvc (no HTTP)
- Mocked service layer
- Validates request deserialization, response serialization
- HTTP status codes
- Target: 70%+ coverage

### Manual/E2E Tests (Swagger UI)
- Real HTTP requests
- Database state verification
- Redirect chain validation (curl -L)

### Performance Tests (Future)
- Load testing: k6 / JMeter
- Target: P95 < 100ms at 100 RPS

---

## 13. Deployment Checklist

- [ ] Security: Credentials in env vars, no secrets in code
- [ ] Database: Automatic schema creation (Hibernate DDL)
- [ ] Logging: Root logger level = INFO; own package = DEBUG
- [ ] Monitoring: Health check endpoint accessible
- [ ] Documentation: Swagger UI renders without errors
- [ ] Build: `mvn clean package` succeeds; all tests pass
- [ ] Metrics: Request count, response time (Micrometer ready)
- [ ] Rollback: Database migration strategy defined
- [ ] Scaling: Stateless design verified; session affinity disabled

---

## Document Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2026-07-24 | Engineering Team | Initial comprehensive technical design |

---

**Architecture Review Sign-off:**

- [ ] Lead Engineer: Approved
- [ ] Database Admin: Approved
- [ ] Security Review: Approved
- [ ] DevOps: Approved


