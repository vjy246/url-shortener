# Architecture Decisions (ADR Summary)

This document captures key architecture decisions and their rationale.

## ADR-001: Spring Boot Monolith for Assignment Velocity
- **Decision:** Use a modular Spring Boot service with layered architecture.
- **Why:** Optimizes delivery speed while preserving clean boundaries.
- **Trade-off:** Not independently deployable components.

## ADR-002: H2 File-Based Database for Local Persistence
- **Decision:** Use embedded H2 with file persistence.
- **Why:** Zero external dependencies for local runs and demos.
- **Trade-off:** Limited production parity vs managed relational databases.

## ADR-003: Alias + Short Code Lookup Support
- **Decision:** Support both custom aliases and generated short codes for retrieval/redirect.
- **Why:** Improves usability and aligns with ambiguous user expectations.
- **Trade-off:** Slightly more lookup logic and collision checks.

## ADR-004: Global Exception Handling Contract
- **Decision:** Centralize API error responses with `GlobalExceptionHandler`.
- **Why:** Consistent client behavior and simpler controller code.
- **Trade-off:** Requires careful maintenance of exception mappings.

## ADR-005: Test-First Validation for Risky Changes
- **Decision:** Use controller/service tests as a quality gate for behavior updates.
- **Why:** Reduces regressions and improves confidence in AI-assisted edits.
- **Trade-off:** Additional time investment in test maintenance.

