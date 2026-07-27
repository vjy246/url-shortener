# Architecture Decisions

- Modular monolith over microservices: lower delivery and operational risk.
- PostgreSQL over in-memory storage: durability and authoritative uniqueness.
- Random Base62 over deterministic URL hash: supports multiple mappings and simple semantics.
- 302 over 301: avoids permanent client caching.
- Database uniqueness over application-only existence check: concurrency correctness.
- Best-effort analytics over synchronous exact counting: redirect availability.

## ADR-007 - Separate analytics transaction boundary

### Decision

Analytics writes are performed by a separate Spring-managed component using `REQUIRES_NEW`.

### Context

Redirect lookup is read-only, but analytics requires an update. A `REQUIRES_NEW` method inside the same service does not work because self-invocation bypasses Spring's transactional proxy.

### Consequences

- Analytics runs in an independent writable transaction.
- Analytics failure can be contained without rolling back redirect lookup.
- Transaction responsibilities are explicit.
- One additional application component is required.

## ADR-008 — RFC 9457 error responses

**Status:** Accepted

**Context:** API consumers need a consistent, machine-readable error contract without maintaining a proprietary response format.

**Decision:** Return Spring `ProblemDetail` responses with a stable application-specific `code` property and field-level validation errors where applicable.

**Consequences:** Clients receive standard fields (`type`, `title`, `status`, `detail`, `instance`) while preserving a stable code for programmatic handling.

## ADR-009 — Generated OpenAPI documentation

**Status:** Accepted

**Context:** Reviewers and API consumers need an immediately discoverable and executable API contract.

**Decision:** Generate OpenAPI 3 documentation from the Spring MVC application and expose Swagger UI.

**Consequences:** Documentation stays close to the controller contract. Endpoint descriptions still require deliberate review rather than assuming generated documentation is complete.

## ADR-010 — Business metrics alongside platform metrics

**Status:** Accepted

**Context:** JVM and HTTP metrics do not explain whether the shortening workflow itself is healthy.

**Decision:** Publish domain counters and redirect latency through Micrometer, including tagged error counts.

**Consequences:** Operators can distinguish creation, redirect, expiration, analytics, and validation behavior. Tags are restricted to bounded error codes to avoid high-cardinality metrics.

## ADR: Database uniqueness is the source of truth

**Decision:** Enforce alias uniqueness with PostgreSQL and treat pre-insert checks only as optional optimizations.

**Reason:** An `exists` check followed by an insert is vulnerable to race conditions. Two requests may observe the alias as free at the same time. The unique constraint provides the final atomic guarantee.

## ADR: Retry generated aliases in independent transactions

**Decision:** Execute each generated-alias insert through `UrlMappingWriter` with `REQUIRES_NEW`.

**Reason:** A failed flush caused by a unique constraint generally marks the current transaction rollback-only. Retrying in the same transaction is unsafe. Independent transactions make each attempt isolated and deterministic.

**Rejected alternative:** Return 409 for a random collision. A collision is an internal generation detail and should normally be retried rather than exposed as a client conflict.
