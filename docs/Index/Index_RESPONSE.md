# Legacy Path

This document has moved to `docs/project/PROJECT_RESPONSE.md`.

Use the project version for the latest content.

## 1. Problem Statement
Build a URL shortener service and demonstrate AI-assisted software engineering with human ownership, validation, and reviewable deliverables.

## 2. Features
- Create shortened URLs with optional custom aliases
- Redirect to original URLs and track clicks
- Get, list, update, delete, search, and analytics APIs
- Validation, exception handling, health checks, and documentation

## 3. Architecture
- Layered Spring Boot architecture: `controller` -> `service` -> `repository` -> `entity`
- DTOs isolate API payloads from persistence models
- Centralized error handling via `GlobalExceptionHandler`
- Local H2 persistence with operational recovery guidance

## 4. Technology Stack
- Java 17
- Spring Boot 3.1.12
- Spring Web / Spring Data JPA
- H2 database
- Bean Validation
- Lombok
- JUnit / MockMvc for tests
- Swagger/OpenAPI for API documentation

## 5. Prerequisites
- Java 17 installed
- IntelliJ IDEA project configured to JDK 17
- Local access to the repository
- Optional: Maven CLI if you want terminal-based execution

## 6. Local Setup
1. Open the project in IntelliJ.
2. Select JDK 17 in Project Structure.
3. Run `com.urlshortener.UrlShortenerServiceApplication`.
4. If the local H2 file is stale/incompatible, reset it with `reset-local-db.ps1`.

```powershell
.\reset-local-db.ps1 -Force
```

## 7. Running with Docker
Docker is **not currently implemented** in this repo because there is no `Dockerfile` yet.

What is documented instead:
- Local run via IntelliJ or Maven
- Future improvement path: add a Dockerfile and compose profile for packaging and deployment

## 8. API Examples
The API examples are documented in:
- `docs/API_REFERENCE.md`
- `docs/sample-data/SAMPLE_DATA_QUICK_COMMANDS.md`

Example base URL:

```text
http://localhost:8080/api
```

## 9. Testing
- Controller tests validate status codes and response payloads
- Service tests validate business rules and error handling
- Validation tests cover request input failures
- Sample-data docs provide command-based smoke tests

## 10. Three Engineering Scenarios

### Scenario A - Greenfield
Implemented the core URL shortener workflow from scratch with persistence, redirect handling, and analytics.

### Scenario B - Brownfield
Improved the existing system with alias-aware lookup, validation fixes, and recovery tooling.

### Scenario C - Ambiguous Requirement
Resolved the lookup ambiguity by supporting both short code and custom alias for retrieval and redirect flows.

## 11. AI-Assisted Development Approach
- AI was used as an accelerator for decomposition, debugging, documentation, and review prep.
- Human engineering ownership remained with explicit review and validation gates.
- Accepted outputs were validated against tests, runtime behavior, and documentation.

## 12. Security
- Input validation via `@Valid`, `@NotBlank`, and `@Size`
- Centralized exception handling for predictable failure responses
- No secrets committed in docs or config
- Security roadmap items are documented for future hardening

## 13. Observability
Observability is covered through:
- `HealthController` (`/api/health` and `/api/health/info`)
- Loggable service operations and structured error handling
- `docs/OPERATIONS_RUNBOOK.md` for incident response and daily checks
- `docs/TECHNICAL_ARCHITECTURE.md` for monitoring and observability design

Current observability state:
- Health endpoint exists
- Logging is enabled at application level
- Full Prometheus/Grafana stack is documented as an operational direction, not fully wired in this MVP

## 14. Assumptions
- The assignment accepts a local embedded H2 implementation for the prototype
- API consumers will use the documented base path `/api`
- Custom alias behavior is expected to be user-friendly and deterministic

## 15. Limitations
- No Dockerfile is currently provided
- No authentication/authorization layer is implemented in this MVP
- No production-grade monitoring stack is integrated yet
- H2 file persistence can require local reset when file format versions change

## 16. Trade-offs
- H2 was chosen for speed and simplicity instead of external database setup
- A layered monolith was chosen over distributed services to keep the assignment focused
- Documentation depth was prioritized so reviewers can trace decisions clearly
- Some enterprise features are documented rather than implemented to preserve scope

## 17. Future Improvements
1. Add Docker support with a `Dockerfile` and compose file.
2. Add Spring Boot Actuator metrics and readiness/liveness endpoints.
3. Add authentication/authorization for protected operations.
4. Add stronger analytics, rate limiting, and abuse prevention.
5. Add CI/CD and production deployment automation.

## Validation and Risk Notes
- Tests under `src/test/java/com/urlshortener` provide regression coverage.
- `reset-local-db.ps1` gives a repeatable recovery path for local H2 issues.
- AI-generated suggestions were not trusted blindly; each was validated or rewritten when needed.

