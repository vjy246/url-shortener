# URL Shortener

An AI-assisted, engineer-led URL shortener service built with Spring Boot 3, PostgreSQL, and Flyway. Developed following the SDLC documented in [`docs/sdlc/`](docs/sdlc/).

## Features

- Create short URLs with optional custom aliases and expiration
- Fast HTTP 302 redirects with best-effort click analytics
- RFC 9457 `ProblemDetail` error responses
- OpenAPI 3 / Swagger UI at `/swagger-ui.html`
- Prometheus metrics at `/actuator/prometheus`
- Flyway-managed schema migrations
- Testcontainers integration tests

## Quick Start

### Prerequisites

- Java 21
- Docker (for PostgreSQL via Testcontainers in tests, or run PostgreSQL locally)

### Run tests

```bash
mvn clean verify
```

### Run locally

```bash
# Start PostgreSQL
docker run -d --name pg -e POSTGRES_DB=urlshortener -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16-alpine

# Start the application
mvn spring-boot:run
```

Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) to explore the API.

## API

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| `POST` | `/api/urls` | Create a short URL | 201, 400, 409 |
| `GET` | `/{shortCode}` | Redirect to destination | 302, 404, 410 |
| `GET` | `/api/urls/{shortCode}/analytics` | Get click count | 200, 404 |

### Create example

```bash
curl -X POST http://localhost:8080/api/urls \
  -H 'Content-Type: application/json' \
  -d '{"destinationUrl":"https://example.com","customAlias":"my-link"}'
```

## SDLC Documentation

All SDLC artefacts are in [`docs/sdlc/`](docs/sdlc/):

| File | Contents |
|------|----------|
| [01-requirements.md](docs/sdlc/01-requirements.md) | Functional & non-functional requirements, assumptions, ambiguities |
| [02-architecture.md](docs/sdlc/02-architecture.md) | Architecture overview and key design choices |
| [03-backlog.md](docs/sdlc/03-backlog.md) | Task backlog with dependencies and acceptance criteria |
| [04-ai-development-log.md](docs/sdlc/04-ai-development-log.md) | AI interaction log: accepted, modified, and rejected suggestions |
| [05-testing.md](docs/sdlc/05-testing.md) | Testing strategy and coverage targets |
| [06-security-review.md](docs/sdlc/06-security-review.md) | Security controls and known gaps |
| [07-architecture-decisions.md](docs/sdlc/07-architecture-decisions.md) | Architecture decision records (ADRs) |
| [08-scenarios.md](docs/sdlc/08-scenarios.md) | Greenfield, brownfield, and ambiguous scenarios |
| [09-final-summary.md](docs/sdlc/09-final-summary.md) | Engineering summary, trade-offs, and next steps |
| [10-prompt-pack.md](docs/sdlc/10-prompt-pack.md) | AI prompt templates used during development |
| [11-demo-script.md](docs/sdlc/11-demo-script.md) | Demo walkthrough script |

## Architecture

```
REST API (UrlController)
  ├── POST /api/urls      → UrlService.createUrl()
  │     ├── UrlPolicy     (validation)
  │     ├── AliasGenerator (Base62, SecureRandom)
  │     └── UrlMappingWriter (REQUIRES_NEW transaction)
  ├── GET /{shortCode}    → UrlService.resolveRedirect()
  │     └── AnalyticsWriter (REQUIRES_NEW, best-effort)
  └── GET /api/urls/{shortCode}/analytics → UrlService.getAnalytics()

PostgreSQL ← Flyway migrations (V1, V2)
```

Key decisions: modular monolith, PostgreSQL unique constraint for alias safety, 302 redirects, best-effort analytics in a separate transaction bean.
# URL Shortener Service

A Spring Boot REST API for creating, managing, and tracking shortened URLs with an embedded H2 database.

---

## 🌐 Application URLs

| Purpose | URL |
|---------|-----|
| **Base URL** | http://localhost:8080/api |
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html |
| **Swagger UI (index)** | http://localhost:8080/api/swagger-ui/index.html |
| **API Docs (JSON)** | http://localhost:8080/api/openapi.json |
| **H2 Database Console** | http://localhost:8080/api/h2-console |
| **Health Check** | http://localhost:8080/api/health |
| **API Info** | http://localhost:8080/api/health/info |

---

## 📦 Project Evidence Pack

- `docs/project/PROJECT_RESPONSE.md`
- `docs/project/RUBRIC_TRACEABILITY.md`
- `docs/project/AI_EXECUTION_LOG.md`

---

## 🔍 Looking Up URLs - Two Methods

### Method 1: By Custom Alias (Recommended)
```powershell
curl http://localhost:8080/api/urls/spring-boot-repo
```

### Method 2: By Short Code (Auto-Generated)
```powershell
curl http://localhost:8080/api/urls/abc123XYZ
```

**Both methods work for:**
- `GET /api/urls/{shortCode}` - Get URL details
- `GET /api/urls/redirect/{shortCode}` - Redirect to original (tracks click)

### Run the Application
```powershell
# Option A: IntelliJ
# Run com.urlshortener.UrlShortenerServiceApplication

# Option B: Maven CLI (if mvn is installed)
mvn spring-boot:run
```

### Run All Tests
```powershell
mvn test
```

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/urls` | Create a shortened URL |
| `POST` | `/api/urls/bulk` | Create multiple shortened URLs at once (5 by default) |
| `GET` | `/api/urls` | Get all shortened URLs |
| `GET` | `/api/urls/{shortCode}` | Get URL by short code OR custom alias |
| `GET` | `/api/urls/redirect/{shortCode}` | Redirect to original URL (tracks click) - accepts short code OR custom alias |
| `PUT` | `/api/urls/{id}` | Update a shortened URL |
| `DELETE` | `/api/urls/{id}` | Delete a shortened URL |
| `GET` | `/api/urls/search?term=...` | Search URLs by keyword |
| `GET` | `/api/urls/analytics/top?limit=10` | Get top clicked URLs |

---

## 📋 Sample Usage

### Create a Short URL (Examples in Swagger UI)

Visit Swagger UI at: **http://localhost:8080/api/swagger-ui/index.html**

Click on **"POST /api/urls"** → **"Try it out"** → Select example → **"Execute"**

**Manual Examples:**

**Example 1: GitHub Repository**
```powershell
curl -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://github.com/spring-projects/spring-boot","customAlias":"spring-boot-repo","description":"Spring Boot Official Repository"}'
```

**Example 2: YouTube Video**
```powershell
curl -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.youtube.com/watch?v=dQw4w9WgXcQ","customAlias":"youtube-tutorial","description":"Popular YouTube Tutorial"}'
```

**Example 3: Documentation**
```powershell
curl -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://docs.oracle.com/javase/17/docs/api","customAlias":"java-17-docs","description":"Java 17 API Documentation"}'
```

**Example 4: Auto-Generated Alias**
```powershell
curl -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.python.org/downloads","description":"Python Downloads Page"}'
```

### Create 5 Sample URLs at Once (Bulk)

```powershell
curl -X POST http://localhost:8080/api/urls/bulk `
  -H "Content-Type: application/json" `
  -d '[
    {"originalUrl":"https://github.com/spring-projects/spring-boot","customAlias":"spring-boot-repo","description":"Spring Boot Repository"},
    {"originalUrl":"https://www.youtube.com/watch?v=dQw4w9WgXcQ","customAlias":"youtube-tutorial","description":"YouTube Tutorial"},
    {"originalUrl":"https://docs.oracle.com/javase/17/docs/api","customAlias":"java-17-docs","description":"Java 17 Docs"},
    {"originalUrl":"https://www.python.org/downloads","customAlias":"python-downloads","description":"Python Downloads"},
    {"originalUrl":"https://developer.mozilla.org/en-US/docs/Web/JavaScript","customAlias":"mdn-javascript","description":"MDN JavaScript"}
  ]'
```

### Test Other Endpoints

**Search URLs**
```powershell
curl "http://localhost:8080/api/urls/search?term=java"
```

**View Analytics**
```powershell
curl "http://localhost:8080/api/urls/analytics/top?limit=5"
```

**Import Sample Data (12 pre-loaded URLs)**
```powershell
./import-sample-data.ps1
```

---

## 🗄️ H2 Database

The application uses an **embedded H2 file-based database** (data persists between restarts).

| Setting | Value |
|---------|-------|
| Console URL | http://localhost:8080/api/h2-console |
| JDBC URL | `jdbc:h2:file:./data/urlshortenerdb` |
| Data Location | `./data/urlshortenerdb.mv.db` |
| Username | `sa` |
| Password | *(empty)* |

### Data Persistence
- Data is now saved to disk in the `./data/` directory
- Database files persist between application restarts
- To reset the database, delete the `./data/` folder and restart the app
- Or use the safe reset script (backs up old DB files first):

```powershell
.\reset-local-db.ps1 -Force
```
