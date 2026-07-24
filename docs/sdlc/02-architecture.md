# Architecture

A modular monolith was selected over microservices to maximize delivery quality and testability within the assignment timebox.

```text
REST API
  -> URL creation
  -> Redirect
  -> Analytics
  -> Observability
  -> PostgreSQL
```

Key choices:
- PostgreSQL unique constraint is authoritative for aliases.
- DTOs remain separate from JPA entities.
- Random Base62 codes use `SecureRandom`.
- HTTP 302 avoids permanent client caching.
- Analytics is best effort; production evolution is asynchronous events.
- Module boundaries allow later extraction into services.

## Collision-safe write path

`UrlService` validates input and chooses either the custom alias or an alias from `AliasGenerator`. Persistence is delegated to `UrlMappingWriter`, which performs each insert in a `REQUIRES_NEW` transaction.

```text
UrlService
  -> AliasGenerator
  -> UrlMappingWriter (REQUIRES_NEW)
       -> UrlMappingRepository
            -> PostgreSQL UNIQUE(short_code)
```

A unique-constraint violation for a custom alias becomes `DuplicateAliasException`. For a generated alias, the service obtains another value and starts a fresh insert transaction. This avoids retrying inside a transaction already marked rollback-only.
