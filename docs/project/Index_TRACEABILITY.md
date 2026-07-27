# Rubric Traceability Matrix - AI-Proficient SE

This matrix maps rubric categories to concrete project evidence.

| Rubric Category | Weight | Evidence in Repository | Notes |
|---|---:|---|---|
| Problem Understanding & Engineering Reasoning | 15% | `docs/project/PROJECT_RESPONSE.md`, `docs/REQUIREMENTS.md` | Requirements normalized, assumptions/trade-offs documented |
| Software Design & Architecture | 20% | `docs/TECHNICAL_ARCHITECTURE.md`, `src/main/java/com/urlshortener/*` | Layered architecture, API/data flow separation |
| AI-Assisted Development Proficiency | 20% | `docs/project/AI_EXECUTION_LOG.md` | Prompting workflow, validation loops, ownership model |
| Code Quality & Engineering Excellence | 15% | `src/main/java/com/urlshortener/service/ShortUrlService.java`, `src/main/java/com/urlshortener/controller/ShortUrlController.java` | Structured service/controller responsibilities, exception strategy |
| Testing, Validation & Reliability | 10% | `src/test/java/com/urlshortener/controller/ShortUrlControllerTest.java`, `src/test/java/com/urlshortener/service/ShortUrlServiceTest.java`, `src/test/java/com/urlshortener/service/ShortUrlServiceExtendedTest.java` | Happy path + failure path coverage |
| Security & Production Readiness | 10% | `src/main/java/com/urlshortener/dto/ShortenUrlRequest.java`, `src/main/java/com/urlshortener/exception/GlobalExceptionHandler.java`, `docs/SECURITY_GUIDE.md` | Validation + error hygiene + security guidance |
| Operational Excellence & Observability | 5% | `src/main/java/com/urlshortener/controller/HealthController.java`, `docs/OPERATIONS_RUNBOOK.md`, `reset-local-db.ps1` | Health endpoints + operational recovery script |
| Communication & Ownership | 5% | `README.md`, `docs/INDEX.md`, `docs/project/*` | Clear runbook, setup guidance, rationale and limits |

## Scoring Readiness Commentary
- The project provides reviewable artifacts for each rubric category.
- AI usage is documented as assistive, with human approval and quality gates.
- Remaining work for top scores is mostly around advanced production hardening (auth, CI/CD gates, deep observability).


