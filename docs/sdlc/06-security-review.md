# Security Review

Implemented: HTTP/HTTPS allowlist, host requirement, credential rejection, length limits, alias allowlist, environment-based DB credentials, non-root container, generic errors, and no routine logging of destination URLs.

The current service does not fetch user URLs, so redirect itself is not a direct SSRF path. A future preview/validation feature would require egress restrictions, DNS/IP validation, private-address blocking, and redirect limits.

Production management APIs need authentication, authorization, rate limiting, abuse detection, dependency scanning, TLS, audit logging, and secret management.

## URL and route protection

- Only `http` and `https` schemes are accepted; `file`, `javascript`, `data`, and other schemes are rejected.
- URLs containing user-info credentials are rejected to reduce accidental credential exposure.
- Reserved aliases prevent user-created redirects from shadowing operational and documentation routes such as `/actuator`, `/swagger-ui`, and `/v3`.
- Alias and URL values are never used as metric tags, preventing unbounded metric cardinality.
