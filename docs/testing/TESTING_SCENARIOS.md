# URL Shortener - Practical Testing Scenarios

This document provides ready-to-use test scenarios with curl commands you can copy and paste.

## Prerequisites

1. Application running: `./mvnw spring-boot:run`
2. Access: http://localhost:8080
3. Swagger UI: http://localhost:8080/swagger-ui.html (recommended)

---

## Scenario 1: Basic URL Shortening Workflow

**Goal:** Create a short URL, retrieve it, and track clicks

### Step 1: Create a shortened URL

```powershell
$json = @{
    originalUrl = "https://www.github.com/spring-projects/spring-boot"
    customAlias = "springboot-repo"
    description = "Spring Boot Repository"
} | ConvertTo-Json

$response = curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json

Write-Host $response
# Expected Response (201 Created):
# {
#   "id": 1,
#   "shortCode": "abc123xyz",
#   "originalUrl": "https://www.github.com/spring-projects/spring-boot",
#   "customAlias": "springboot-repo",
#   "clickCount": 0,
#   "description": "Spring Boot Repository",
#   "isActive": true,
#   "shortenedUrl": "http://localhost:8080/api/s/abc123xyz"
# }
```

### Step 2: Retrieve URL details

```powershell
curl -X GET http://localhost:8080/urls/abc123xyz `
  -H "Content-Type: application/json"

# Expected Response (200 OK):
# Same as above response
```

### Step 3: Simulate clicks by redirecting

```powershell
# Click #1
curl -X GET http://localhost:8080/urls/redirect/abc123xyz -L

# Click #2
curl -X GET http://localhost:8080/urls/redirect/abc123xyz -L

# Click #3
curl -X GET http://localhost:8080/urls/redirect/abc123xyz -L
```

### Step 4: Verify click count increased

```powershell
curl -X GET http://localhost:8080/urls/abc123xyz `
  -H "Content-Type: application/json"

# Expected: "clickCount": 3
```

---

## Scenario 2: Managing Multiple URLs

**Goal:** Create multiple URLs and organize them

### Step 1: Create URL #1

```powershell
$json = @{
    originalUrl = "https://docs.microsoft.com/dotnet"
    customAlias = "dotnet-docs"
    description = ".NET Documentation"
} | ConvertTo-Json

curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json
```

### Step 2: Create URL #2

```powershell
$json = @{
    originalUrl = "https://www.python.org"
    customAlias = "python-official"
    description = "Python Official Website"
} | ConvertTo-Json

curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json
```

### Step 3: Create URL #3 (no custom alias)

```powershell
$json = @{
    originalUrl = "https://www.rust-lang.org"
    description = "Rust Programming Language"
} | ConvertTo-Json

curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json
```

### Step 4: List all URLs

```powershell
curl -X GET http://localhost:8080/urls `
  -H "Content-Type: application/json"

# Expected Response (200 OK):
# [
#   { ... URL #1 ... },
#   { ... URL #2 ... },
#   { ... URL #3 ... }
# ]
```

### Step 5: Search for specific URL

```powershell
# Search by alias
curl -X GET "http://localhost:8080/urls/search?term=python" `
  -H "Content-Type: application/json"

# Expected: Returns URL #2 with python-official alias
```

---

## Scenario 3: Error Handling

**Goal:** Test error cases and edge conditions

### Test Case 3.1: Duplicate Custom Alias

```powershell
# This succeeds
$json = @{
    originalUrl = "https://example1.com"
    customAlias = "conflict"
} | ConvertTo-Json

curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json

# This fails (409 Conflict)
$json = @{
    originalUrl = "https://example2.com"
    customAlias = "conflict"
} | ConvertTo-Json

curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json

# Expected Response (409):
# {
#   "timestamp": "2024-01-15T10:30:00.000+00:00",
#   "status": 409,
#   "error": "Conflict",
#   "message": "Custom alias 'conflict' is already in use"
# }
```

### Test Case 3.2: URL Not Found

```powershell
curl -X GET http://localhost:8080/urls/nonexistent123 `
  -H "Content-Type: application/json"

# Expected Response (404):
# {
#   "timestamp": "2024-01-15T10:30:00.000+00:00",
#   "status": 404,
#   "error": "Not Found",
#   "message": "Short URL not found"
# }
```

### Test Case 3.3: Invalid Request (Missing Required Field)

```powershell
$json = @{
    customAlias = "incomplete"
    # Missing originalUrl!
} | ConvertTo-Json

curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json

# Expected Response (400):
# {
#   "timestamp": "2024-01-15T10:30:00.000+00:00",
#   "status": 400,
#   "error": "Bad Request",
#   "message": "Original URL cannot be blank"
# }
```

### Test Case 3.4: Description Too Long

```powershell
$json = @{
    originalUrl = "https://example.com"
    description = "$('x' * 501)"  # 501 characters (max is 500)
} | ConvertTo-Json

curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json

# Expected Response (400): Max length violation
```

---

## Scenario 4: URL Updates

**Goal:** Modify existing URLs

### Step 1: Create a URL

```powershell
$json = @{
    originalUrl = "https://old-url.com/path"
    customAlias = "to-update"
    description = "Original description"
} | ConvertTo-Json

$response = curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json

# Note the ID (let's say it's 1)
```

### Step 2: Update the URL

```powershell
$json = @{
    originalUrl = "https://new-url.com/updated-path"
    description = "Updated description"
} | ConvertTo-Json

curl -X PUT http://localhost:8080/urls/1 `
  -H "Content-Type: application/json" `
  -Body $json

# Expected Response (200): Updated URL details
```

### Step 3: Verify changes

```powershell
curl -X GET http://localhost:8080/urls/to-update `
  -H "Content-Type: application/json"

# Expected: New description and URL should be reflected
```

---

## Scenario 5: Analytics

**Goal:** Track most popular URLs

### Step 1: Create and click URLs

```powershell
# Create URL A
$json = @{ originalUrl = "https://example.com/a"; customAlias = "url-a" } | ConvertTo-Json
curl -X POST http://localhost:8080/urls -H "Content-Type: application/json" -Body $json

# Create URL B
$json = @{ originalUrl = "https://example.com/b"; customAlias = "url-b" } | ConvertTo-Json
curl -X POST http://localhost:8080/urls -H "Content-Type: application/json" -Body $json

# Click URL A (5 times)
for ($i=0; $i -lt 5; $i++) {
    curl -X GET http://localhost:8080/urls/redirect/url-a -L
}

# Click URL B (3 times)
for ($i=0; $i -lt 3; $i++) {
    curl -X GET http://localhost:8080/urls/redirect/url-b -L
}
```

### Step 2: Get top clicked URLs

```powershell
curl -X GET "http://localhost:8080/urls/analytics/top?limit=5" `
  -H "Content-Type: application/json"

# Expected Response (200):
# [
#   { "customAlias": "url-a", "clickCount": 5, ... },
#   { "customAlias": "url-b", "clickCount": 3, ... }
# ]
```

---

## Scenario 6: URL Deletion

**Goal:** Remove URLs from the system

### Step 1: Create a URL to delete

```powershell
$json = @{
    originalUrl = "https://temporary.com"
    customAlias = "temp-url"
} | ConvertTo-Json

$response = curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json

# Note the ID (let's say it's 5)
```

### Step 2: Delete the URL

```powershell
curl -X DELETE http://localhost:8080/urls/5 `
  -H "Content-Type: application/json"

# Expected Response (204): No Content
```

### Step 3: Verify deletion

```powershell
curl -X GET http://localhost:8080/urls/temp-url `
  -H "Content-Type: application/json"

# Expected Response (404): Not Found
```

---

## Scenario 7: Health Check

**Goal:** Verify application is running

```powershell
curl -X GET http://localhost:8080/health `
  -H "Content-Type: application/json"

# Expected Response (200):
# {
#   "status": "UP"
# }
```

---

## Automating Tests with PowerShell Script

Save this as `test-urls.ps1`:

```powershell
# URL Shortener API Test Script

$BaseUrl = "http://localhost:8080"

function Test-CreateUrl {
    param(
        [string]$OriginalUrl,
        [string]$Alias,
        [string]$Description
    )
    
    $json = @{
        originalUrl = $OriginalUrl
        customAlias = $Alias
        description = $Description
    } | ConvertTo-Json
    
    $response = curl -X POST "$BaseUrl/urls" `
        -H "Content-Type: application/json" `
        -Body $json
    
    Write-Host "✓ Created URL: $Alias"
    return $response
}

function Test-GetUrl {
    param([string]$ShortCode)
    
    $response = curl -X GET "$BaseUrl/urls/$ShortCode" `
        -H "Content-Type: application/json"
    
    Write-Host "✓ Retrieved URL: $ShortCode"
    return $response
}

function Test-ListUrls {
    $response = curl -X GET "$BaseUrl/urls" `
        -H "Content-Type: application/json"
    
    Write-Host "✓ Listed all URLs"
    return $response
}

# Run tests
Write-Host "Starting URL Shortener API Tests...`n"

Test-CreateUrl "https://github.com" "github-link" "GitHub"
Test-CreateUrl "https://google.com" "google-link" "Google"
Test-ListUrls

Write-Host "`n✓ All tests completed!"
```

Run it:
```powershell
.\test-urls.ps1
```

---

## Using Postman

### Import Collection

1. Open Postman
2. Click "Import"
3. Paste this collection JSON:

```json
{
  "info": {
    "name": "URL Shortener API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create URL",
      "request": {
        "method": "POST",
        "header": [
          {"key": "Content-Type", "value": "application/json"}
        ],
        "url": {"raw": "{{base_url}}/urls"},
        "body": {
          "mode": "raw",
          "raw": "{\"originalUrl\": \"https://example.com\", \"customAlias\": \"test\"}"
        }
      }
    },
    {
      "name": "Get All URLs",
      "request": {
        "method": "GET",
        "url": {"raw": "{{base_url}}/urls"}
      }
    }
  ]
}
```

---

## Performance Testing

### Load Test with Apache Bench

```powershell
# Install Apache Bench (if not installed)
# On Windows: choco install apache-bench

# Test with 100 requests, 10 concurrent
ab -n 100 -c 10 http://localhost:8080/urls

# Expected output shows response times and throughput
```

### Load Test with curl

```powershell
# Simulate 50 requests
for ($i=0; $i -lt 50; $i++) {
    curl -X GET http://localhost:8080/urls `
        -H "Content-Type: application/json" `
        | Out-Null
    Write-Host "Request $($i+1) completed"
}
```

---

## Troubleshooting

### Issue: "Connection refused"
**Solution:** Ensure application is running on port 8080
```powershell
./mvnw spring-boot:run
```

### Issue: "Port 8080 already in use"
**Solution:** Kill the process using the port
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Issue: "JSON parsing error"
**Solution:** Ensure JSON is properly formatted
```powershell
$json = @{ field = "value" } | ConvertTo-Json
Write-Host $json  # Verify before sending
```

### Issue: "415 Unsupported Media Type"
**Solution:** Always include Content-Type header
```powershell
curl -X POST ... -H "Content-Type: application/json" ...
```

---

## Summary

You now have:
- ✅ Ready-to-use curl commands
- ✅ Complete test scenarios
- ✅ Error case examples
- ✅ PowerShell automation script
- ✅ Postman collection
- ✅ Troubleshooting guide

Start with Scenario 1 and work your way through for a complete understanding of the API!


