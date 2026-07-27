# Legacy Path

This document has moved to `docs/project/AI_EXECUTION_LOG.md`.

Use the project version for the latest content.

## AI Usage Model
AI was used as an engineering accelerator for decomposition, implementation drafts, debugging support, and documentation scaffolding.

Engineer-owned controls applied:

- Prompt includes intent, constraints, and acceptance criteria.
- Generated output is reviewed before merge.
- Incorrect/unclear output is rejected or revised.
- Tests and runtime checks gate acceptance.

## Prompting and Iteration Pattern
1. Define task in engineering terms (input, expected behavior, failure modes).
2. Ask AI for focused change set rather than broad rewrite.
3. Validate generated code against existing architecture and tests.
4. Refine prompt using concrete errors/log output.
5. Capture accepted/rejected decisions with rationale.

## Example Decision Records

### Record 1 - Validation behavior mismatch
- **Issue:** Invalid request test expected `400` but received `201`.
- **AI suggestion:** Verify validation wiring and dependencies.
- **Engineer decision:** Add explicit `spring-boot-starter-validation` dependency and re-verify tests.
- **Outcome:** Validation pipeline restored.

### Record 2 - Startup failure on local H2 DB
- **Issue:** `Unsupported database file version or invalid file header` startup error.
- **AI suggestion:** Reset stale/incompatible DB artifacts.
- **Engineer decision:** Implement `reset-local-db.ps1` to safely back up and reset local DB files.
- **Outcome:** Repeatable local recovery path documented.

### Record 3 - Documentation quality and rubric readiness
- **Issue:** Need explicit rubric-to-artifact defensibility.
- **AI suggestion:** Build assignment response + rubric traceability matrix.
- **Engineer decision:** Add assignment evidence pack under `docs/assignment`.
- **Outcome:** Reviewers can score criterion-by-criterion with evidence.

## Quality Gates Used
- Compile-time checks in IDE
- Unit/web tests under `src/test`
- Startup/runtime verification through Spring Boot logs
- Operational sanity check via `GET /api/health`

## AI Safety and Governance Practices
- No secrets or credentials shared in prompts.
- No direct blind merge of generated code.
- Human sign-off required for behavior-changing updates.
- Security and failure mode considerations documented before acceptance.

