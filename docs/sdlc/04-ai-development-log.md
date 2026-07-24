# AI Development Log

## AI-001 Requirement normalization
Intent: identify requirements, ambiguities, risks, and MVP before coding.
Decision: modified. AI proposed Redis, Kafka, auth, and rate limiting. These were documented as production evolution, not prototype scope.
Validation: checked against assignment and rubric.

## AI-002 Architecture alternatives
Prompt: compare modular monolith, microservices, and serverless by delivery risk, scalability, testability, and operational complexity.
Decision: rejected AI preference for microservices. Modular monolith better fits the timebox while preserving boundaries.

## AI-003 Duplicate alias concurrency
Prompt: review duplicate alias handling across multiple instances.
AI suggestion: `existsByShortCode()` before insert.
Decision: rejected as authoritative control because concurrent requests can both pass. Final solution uses a PostgreSQL unique constraint and maps the violation to HTTP 409.

## AI-004 URL security review
AI suggestion: fetch the destination to verify availability.
Decision: rejected because it creates SSRF and latency risk. Final solution validates URI syntax, schemes, host, credentials, and length without fetching.

## AI-005 Analytics reliability
AI suggestion: synchronous exact counting in the redirect transaction.
Decision: modified. Redirect availability has higher priority. Prototype uses best-effort recording; production evolution is asynchronous events.

## AI-006 Test discovery
Decision: modified. Removed live-network tests and added expiration boundary and DB constraint cases.

## AI-007 Staff review
Accepted findings: inject `Clock`, keep DTO/entity separation, add metrics, and document technical debt.

## Secure AI usage
No secrets, credentials, customer data, or production data were sent to AI. AI output was treated as untrusted and required engineer review and validation.

## AI-009 - Transaction boundary defect

### Intent

Investigate why a valid redirect returned HTTP 500 while recording analytics.

### Observed failure

PostgreSQL rejected the analytics update because it was executed inside a read-only transaction:

`cannot execute UPDATE in a read-only transaction`

The caught persistence exception also marked the outer transaction as rollback-only, producing `UnexpectedRollbackException`.

### Root cause

The redirect method used `@Transactional(readOnly = true)`. The analytics method declared `REQUIRES_NEW`, but it was called from another method inside the same Spring bean. Because Spring transaction management is proxy-based, self-invocation bypassed the proxy and no new transaction was created.

### Engineer decision

Move analytics writing into a separate Spring bean and call it through the Spring proxy.

### Validation

Added an integration regression test that creates a short URL, invokes redirect, verifies HTTP 302, and confirms that the click counter was updated.

### Outcome

Redirect works and analytics is recorded in an independent writable transaction.

## AI-010 — Redirect test behavior

**Intent:** Verify the original HTTP redirect response and analytics update.

**Observed behavior:** `TestRestTemplate` followed the redirect and exposed the destination response instead of the service's original `302` response.

**Engineer decision:** Use `MockMvc` for redirect assertions while keeping a real PostgreSQL Testcontainer for persistence integration.

**Validation:** The test verifies `302 Found`, the exact `Location` header, and an incremented analytics counter. All integration tests run with zero skipped tests.

## AI-011 — Production-readiness improvements

**Intent:** Improve API discoverability, standardized errors, observability, and CI evidence.

**AI-assisted suggestions reviewed:** Add springdoc OpenAPI, use Spring `ProblemDetail`, add business metrics, and build a Docker image in CI.

**Engineer decisions:**

- Adopted `springdoc-openapi-starter-webmvc-ui` for generated OpenAPI documentation.
- Replaced the custom error DTO with RFC 9457-compatible `ProblemDetail` responses.
- Added counters for URL creation and errors, retaining redirect latency and analytics failure metrics.
- Extended GitHub Actions to run verification before building the image.

**Validation:** Run `mvn clean verify`, open `/swagger-ui.html`, inspect `/v3/api-docs`, and verify custom metrics under `/actuator/prometheus`.
