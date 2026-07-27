# Required Scenarios

## Greenfield
Build initial creation API. AI helped compare API and architecture options. Engineer selected modular monolith, PostgreSQL, random Base62 codes, and DB uniqueness. Validate 201, persistence, aliases, and negative inputs.

## Brownfield
Add expiration. Impacted API, entity, migration, redirect, errors, analytics response, tests, and docs. Engineer injected `Clock`, used nullable UTC `Instant`, defined equality as expired, and returned 410.

## Ambiguous
"Make redirects more reliable." Clarification: a valid redirect should succeed even when analytics cannot be recorded. AI compared synchronous, best-effort, and event-driven approaches. Engineer chose best-effort for prototype and asynchronous events as production evolution.
