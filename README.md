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
