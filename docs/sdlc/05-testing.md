# Testing

Unit coverage targets URL policy, short-code generation, and exact expiration boundaries.

Recommended integration coverage with PostgreSQL/Testcontainers:
- create and redirect;
- duplicate alias -> 409;
- expired URL -> 410;
- analytics increment;
- migration/JPA compatibility;
- analytics failure does not block redirect.

Quality gate:

```bash
mvn clean verify
```

Remaining gaps: load, chaos, multi-instance concurrency harness, and browser E2E tests.

## Regression: read-only redirect transaction

A Testcontainers integration test reproduces and prevents the transaction-boundary defect in which analytics attempted to update PostgreSQL inside the redirect method's read-only transaction. The test verifies both HTTP 302 and the incremented click counter.

## Alias and validation coverage

The test suite includes:

- duplicate custom alias -> HTTP 409;
- creation without `customAlias` -> generated Base62 code;
- deterministic generated-code collision -> retry and successful insert;
- reserved alias rejection;
- non-HTTP/HTTPS URL rejection;
- past expiration rejection;
- health and OpenAPI endpoint smoke tests;
- domain tests for expiration boundary and alias rules.

`RandomAliasCollisionIntegrationTest` replaces the production generator with a deterministic test generator. It first returns an occupied code and then a free code, proving that the retry occurs successfully after a database uniqueness violation.
