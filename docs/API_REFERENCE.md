# URL Shortener Service - API Reference

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080/api`  
**Documentation:** [Swagger UI](http://localhost:8080/api/swagger-ui.html)

---

## Table of Contents

1. [Authentication](#authentication)
2. [Base URL & Headers](#base-url--headers)
3. [Endpoints](#endpoints)
   - [Create URL](#create-url)
   - [Bulk Create URLs](#bulk-create-urls)
   - [Get All URLs](#get-all-urls)
   - [Get URL Details](#get-url-details)
   - [Redirect & Track](#redirect--track)
   - [Update URL](#update-url)
   - [Delete URL](#delete-url)
   - [Search URLs](#search-urls)
   - [Get Top Clicked](#get-top-clicked-urls)
4. [Response Codes](#response-codes)
5. [Error Handling](#error-handling)
6. [Rate Limits](#rate-limits)

---

## Authentication

**Current Version:** No authentication required (MVP)

All endpoints are publicly accessible. No API key or token needed.

**Future Versions:** Bearer token + role-based access control planned for Phase 2.

---

## Base URL & Headers

### Base URL
```
http://localhost:8080/api
```

### Required Headers
All POST/PUT requests require:
```
Content-Type: application/json
```

### Optional Headers
```
Accept: application/json
```

---

## Endpoints

### Create URL

**Endpoint:** `POST /urls`

**Description:** Create a single shortened URL

**Request Body:**
```json
{
  "originalUrl": "https://www.example.com/very/long/url",
  "customAlias": "my-alias",
  "description": "Optional description"
}
```

**Required Fields:**
- `originalUrl` (string, max 2048 chars): The URL to shorten

**Optional Fields:**
- `customAlias` (string): User-friendly alias (auto-generated if omitted)
- `description` (string, max 500 chars): Metadata

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://github.com/spring-projects/spring-boot",
    "customAlias": "spring-boot-repo",
    "description": "Spring Boot Official Repository"
  }'
```

**PowerShell Example:**
```powershell
$body = @{
    originalUrl = "https://github.com/spring-projects/spring-boot"
    customAlias = "spring-boot-repo"
    description = "Spring Boot Official Repository"
} | ConvertTo-Json

curl -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -Body $body
```

**Response (201 Created):**
```json
{
  "id": 1,
  "shortCode": "abc123XYZ",
  "originalUrl": "https://github.com/spring-projects/spring-boot",
  "customAlias": "spring-boot-repo",
  "clickCount": 0,
  "description": "Spring Boot Official Repository",
  "isActive": true,
  "createdAt": "2026-07-24T12:00:00",
  "updatedAt": "2026-07-24T12:00:00",
  "shortenedUrl": "http://localhost:8080/api/s/abc123XYZ"
}
```

---

### Bulk Create URLs

**Endpoint:** `POST /urls/bulk`

**Description:** Create multiple shortened URLs at once (5 recommended)

**Request Body:**
```json
[
  {
    "originalUrl": "https://github.com/spring-projects/spring-boot",
    "customAlias": "spring-boot-repo",
    "description": "Spring Boot Repository"
  },
  {
    "originalUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "customAlias": "youtube-tutorial",
    "description": "YouTube Tutorial"
  }
]
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/urls/bulk \
  -H "Content-Type: application/json" \
  -d '[
    {"originalUrl":"https://github.com/spring-projects/spring-boot","customAlias":"spring-boot-repo","description":"Spring Boot Repository"},
    {"originalUrl":"https://www.youtube.com/watch?v=dQw4w9WgXcQ","customAlias":"youtube-tutorial","description":"YouTube Tutorial"}
  ]'
```

**Response (201 Created):**
```json
[
  {
    "id": 1,
    "shortCode": "abc123XYZ",
    "originalUrl": "https://github.com/spring-projects/spring-boot",
    "customAlias": "spring-boot-repo",
    "clickCount": 0,
    "description": "Spring Boot Repository",
    "isActive": true,
    "createdAt": "2026-07-24T12:00:00",
    "updatedAt": "2026-07-24T12:00:00",
    "shortenedUrl": "http://localhost:8080/api/s/abc123XYZ"
  },
  {
    "id": 2,
    "shortCode": "def456UVW",
    "originalUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "customAlias": "youtube-tutorial",
    "clickCount": 0,
    "description": "YouTube Tutorial",
    "isActive": true,
    "createdAt": "2026-07-24T12:00:01",
    "updatedAt": "2026-07-24T12:00:01",
    "shortenedUrl": "http://localhost:8080/api/s/def456UVW"
  }
]
```

---

### Get All URLs

**Endpoint:** `GET /urls`

**Description:** Retrieve all shortened URLs (paginated in future)

**Query Parameters:** None (for MVP)

**cURL Example:**
```bash
curl http://localhost:8080/api/urls
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "shortCode": "abc123XYZ",
    "originalUrl": "https://github.com/spring-projects/spring-boot",
    "customAlias": "spring-boot-repo",
    "clickCount": 5,
    "description": "Spring Boot Repository",
    "isActive": true,
    "createdAt": "2026-07-24T12:00:00",
    "updatedAt": "2026-07-24T12:05:00",
    "shortenedUrl": "http://localhost:8080/api/s/abc123XYZ"
  },
  {
    "id": 2,
    "shortCode": "def456UVW",
    "originalUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "customAlias": "youtube-tutorial",
    "clickCount": 2,
    "description": "YouTube Tutorial",
    "isActive": true,
    "createdAt": "2026-07-24T12:00:01",
    "updatedAt": "2026-07-24T12:02:00",
    "shortenedUrl": "http://localhost:8080/api/s/def456UVW"
  }
]
```

---

### Get URL Details

**Endpoint:** `GET /urls/{shortCode}`

**Description:** Retrieve metadata for a specific short URL by code or alias

**Path Parameters:**
- `shortCode` (string): Short code or custom alias

**cURL Examples:**
```bash
# By short code
curl http://localhost:8080/api/urls/abc123XYZ

# By custom alias
curl http://localhost:8080/api/urls/spring-boot-repo
```

**Response (200 OK):**
```json
{
  "id": 1,
  "shortCode": "abc123XYZ",
  "originalUrl": "https://github.com/spring-projects/spring-boot",
  "customAlias": "spring-boot-repo",
  "clickCount": 5,
  "description": "Spring Boot Repository",
  "isActive": true,
  "createdAt": "2026-07-24T12:00:00",
  "updatedAt": "2026-07-24T12:05:00",
  "shortenedUrl": "http://localhost:8080/api/s/abc123XYZ"
}
```

**Error Responses:**
- `404 Not Found`: URL not found with given code/alias

---

### Redirect & Track

**Endpoint:** `GET /urls/redirect/{shortCode}`

**Description:** Redirect to original URL and increment click counter

**Path Parameters:**
- `shortCode` (string): Short code or custom alias

**cURL Example:**
```bash
# Follow redirect chain (-L)
curl -L http://localhost:8080/api/urls/redirect/spring-boot-repo
```

**Response (302 Found):**
```
Location: https://github.com/spring-projects/spring-boot
```

**Side Effects:**
- Click count incremented by 1
- `updatedAt` timestamp refreshed

---

### Update URL

**Endpoint:** `PUT /urls/{id}`

**Description:** Update originalUrl or description for existing entry

**Path Parameters:**
- `id` (number): Database ID

**Request Body:**
```json
{
  "originalUrl": "https://github.com/spring-projects/spring-boot/releases",
  "description": "Spring Boot Releases Page"
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/api/urls/1 \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://github.com/spring-projects/spring-boot/releases",
    "description": "Spring Boot Releases Page"
  }'
```

**Response (200 OK):**
```json
{
  "id": 1,
  "shortCode": "abc123XYZ",
  "originalUrl": "https://github.com/spring-projects/spring-boot/releases",
  "customAlias": "spring-boot-repo",
  "clickCount": 5,
  "description": "Spring Boot Releases Page",
  "isActive": true,
  "createdAt": "2026-07-24T12:00:00",
  "updatedAt": "2026-07-24T12:10:00",
  "shortenedUrl": "http://localhost:8080/api/s/abc123XYZ"
}
```

---

### Delete URL

**Endpoint:** `DELETE /urls/{id}`

**Description:** Permanently delete a shortened URL

**Path Parameters:**
- `id` (number): Database ID

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/urls/1
```

**Response (204 No Content):**
```
(empty body)
```

---

### Search URLs

**Endpoint:** `GET /urls/search`

**Description:** Search for URLs by keyword

**Query Parameters:**
- `term` (string, required): Search term (matches alias, description)

**cURL Examples:**
```bash
# Search by alias or description
curl "http://localhost:8080/api/urls/search?term=spring"
curl "http://localhost:8080/api/urls/search?term=youtube"
curl "http://localhost:8080/api/urls/search?term=tutorial"
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "shortCode": "abc123XYZ",
    "originalUrl": "https://github.com/spring-projects/spring-boot",
    "customAlias": "spring-boot-repo",
    "clickCount": 5,
    "description": "Spring Boot Repository",
    "isActive": true,
    "createdAt": "2026-07-24T12:00:00",
    "updatedAt": "2026-07-24T12:05:00",
    "shortenedUrl": "http://localhost:8080/api/s/abc123XYZ"
  }
]
```

---

### Get Top Clicked URLs

**Endpoint:** `GET /urls/analytics/top`

**Description:** Retrieve most-clicked URLs for analytics

**Query Parameters:**
- `limit` (number, optional, default 10): Number of top URLs to return

**cURL Examples:**
```bash
# Get top 10 (default)
curl http://localhost:8080/api/urls/analytics/top

# Get top 5
curl "http://localhost:8080/api/urls/analytics/top?limit=5"

# Get top 20
curl "http://localhost:8080/api/urls/analytics/top?limit=20"
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "shortCode": "abc123XYZ",
    "originalUrl": "https://github.com/spring-projects/spring-boot",
    "customAlias": "spring-boot-repo",
    "clickCount": 150,
    "description": "Spring Boot Repository",
    "isActive": true,
    "createdAt": "2026-07-24T12:00:00",
    "updatedAt": "2026-07-24T13:00:00",
    "shortenedUrl": "http://localhost:8080/api/s/abc123XYZ"
  },
  {
    "id": 2,
    "shortCode": "def456UVW",
    "originalUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "customAlias": "youtube-tutorial",
    "clickCount": 87,
    "description": "YouTube Tutorial",
    "isActive": true,
    "createdAt": "2026-07-24T12:00:01",
    "updatedAt": "2026-07-24T13:00:00",
    "shortenedUrl": "http://localhost:8080/api/s/def456UVW"
  }
]
```

---

## Response Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Successful GET request |
| 201 | Created | Successful POST request (resource created) |
| 204 | No Content | Successful DELETE request |
| 302 | Found | Redirect (GET /urls/redirect/{code}) |
| 400 | Bad Request | Invalid input (missing required fields, validation failed) |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate alias exists |
| 500 | Internal Server Error | Unexpected server error |

---

## Error Handling

### Error Response Format

All errors return standardized JSON:

```json
{
  "timestamp": "2026-07-24T12:00:00",
  "status": 400,
  "message": "Validation failed",
  "path": "/api/urls",
  "errors": {
    "originalUrl": "Original URL cannot be blank"
  }
}
```

### Common Errors

#### 400 - Bad Request

**Missing required field:**
```json
{
  "timestamp": "2026-07-24T12:00:00",
  "status": 400,
  "message": "Validation failed",
  "path": "/api/urls",
  "errors": {
    "originalUrl": "Original URL cannot be blank"
  }
}
```

#### 409 - Conflict

**Duplicate alias:**
```json
{
  "timestamp": "2026-07-24T12:00:00",
  "status": 409,
  "message": "Custom alias 'my-alias' is already in use",
  "path": "/api/urls"
}
```

#### 404 - Not Found

**URL not found:**
```json
{
  "timestamp": "2026-07-24T12:00:00",
  "status": 404,
  "message": "Short URL not found",
  "path": "/api/urls/invalid"
}
```

---

## Rate Limits

**Current MVP:** No rate limiting

**Future (Phase 2):**
- 1000 requests per minute per IP
- 100 requests per minute per API key

Contact DevOps for enterprise rate limit adjustments.

---

## Postman Collection

**Import URL:**
```
[Postman Collection Link - TBD]
```

**Alternative:** Import Swagger JSON:
```
http://localhost:8080/api/openapi.json
```

**Steps:**
1. Open Postman
2. Click "Import"
3. Paste `http://localhost:8080/api/openapi.json`
4. Click Import

---

## Testing Tools

### Postman (GUI)
- Download: https://www.postman.com/downloads/
- Import OpenAPI spec (see above)
- Test all endpoints with pre-filled examples

### curl (Command Line)
- Built-in on macOS/Linux
- Install on Windows: `choco install curl`
- See examples above for each endpoint

### Swagger UI (Browser)
- URL: http://localhost:8080/api/swagger-ui.html
- Interactive testing without external tools
- Try it out button for each endpoint

---

## Best Practices

1. **Check health first:**
   ```bash
   curl http://localhost:8080/api/health
   ```

2. **Use meaningful aliases:**
   - Good: `github-repo`, `youtube-tutorial`
   - Bad: `abc`, `xyz123`

3. **Always provide description:**
   - Helps with searching and analytics

4. **Handle redirects properly:**
   - Use `curl -L` to follow 302 redirects
   - Most browsers handle automatically

5. **Batch operations:**
   - Use `/urls/bulk` for 5+ URLs
   - Faster than individual POST requests

---

## Support

**Questions?** See [INDEX.md](./INDEX.md) for documentation by role.

---


