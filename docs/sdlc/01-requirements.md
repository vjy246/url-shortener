# Requirement Analysis

## Functional
- Create a shortened URL.
- Optional custom alias and expiration.
- Redirect active URLs.
- Return 404 for unknown and 410 for expired links.
- Expose click analytics.

## Non-functional
- Duplicate-alias correctness across instances.
- Fast and reliable redirects.
- Testability, maintainability, security, and observability.
- Durable storage and repeatable setup.

## Ambiguities
- Same URL: same code or multiple codes?
- Case-sensitive aliases?
- Exact or eventually consistent analytics?
- Authentication required?
- Private-network destinations allowed?

## Assumptions
- Same URL may have multiple codes.
- Aliases are case-sensitive.
- Only HTTP/HTTPS.
- Expiration uses UTC and returns 410.
- Analytics must not intentionally break redirect.
- Authentication is out of prototype scope.

## Out of scope
Users, billing, editing, custom domains, malware scanning, rate limiting, Redis, Kafka, UI, multi-region deployment.

## Alias requirements

- `customAlias` is optional.
- When omitted, the system generates a Base62 alias with a configurable length.
- Every alias is unique at the database level.
- Duplicate custom aliases return HTTP 409 with `DUPLICATE_ALIAS`.
- Random alias collisions are retried a configurable number of times.
- Aliases are 3-32 characters and may contain letters, digits, hyphens, and underscores.
- Application route names such as `api`, `actuator`, `swagger-ui`, and `v3` are reserved.

## URL and expiration requirements

- Only HTTP and HTTPS destination URLs are accepted.
- Destination URLs must contain a valid host and must not contain credentials.
- Expiration, when supplied, must be later than the current UTC instant.
- Resolving an expired link returns HTTP 410.
