# URL Shortener Service - Testing Guide

## Overview

This document provides comprehensive guidance on testing the URL Shortener Service. The application can be tested at multiple levels: unit tests, integration tests, component tests, and end-to-end tests.

---

## 1. Unit Tests

Unit tests verify individual methods in isolation using mocks.

### Existing Test Files

#### `ShortUrlServiceTest.java`
- Tests basic service operations
- Uses Mockito for repository mocking
- Covers: create, get, delete operations
- Location: `src/test/java/com/urlshortener/service/ShortUrlServiceTest.java`

#### `ShortUrlServiceExtendedTest.java` (NEW)
- Extended test coverage with additional scenarios
- Tests: partial updates, click count increments, inactive URLs, search, analytics
- Exception handling and edge cases
- Location: `src/test/java/com/urlshortener/service/ShortUrlServiceExtendedTest.java`

### Running Unit Tests

```powershell
# Run all service tests
./mvnw test -Dtest=ShortUrlService*

# Run specific test class
./mvnw test -Dtest=ShortUrlServiceTest

# Run specific test method
./mvnw test -Dtest=ShortUrlServiceTest#testCreateShortUrl_Success

# Run with coverage
./mvnw test jacoco:report
```

### Test Categories

```
CREATE OPERATIONS
├── testCreateShortUrl_Success
├── testCreateShortUrl_WithoutCustomAlias
├── testCreateShortUrl_WithDescription
├── testCreateShortUrl_ActiveByDefault
└── testCreateShortUrl_DuplicateAlias

READ OPERATIONS
├── testGetShortUrl_Success
├── testGetShortUrl_ReturnsCorrectFields
└── testGetShortUrl_NotFound

REDIRECT OPERATIONS
├── testGetOriginalUrl_Success
├── testGetOriginalUrl_IncrementsClickCount
└── testGetOriginalUrl_InactiveUrl

UPDATE OPERATIONS
├── testUpdateShortUrl_Success
├── testUpdateShortUrl_PartialUpdate
└── testUpdateShortUrl_NotFound

DELETE OPERATIONS
├── testDeleteShortUrl_Success
├── testDeleteShortUrl_RemovesFromDatabase
└── testDeleteShortUrl_NotFound

LIST/SEARCH OPERATIONS
├── testGetAllShortUrls_ReturnsEmptyList
├── testGetAllShortUrls_ReturnsMultipleUrls
├── testSearchShortUrls_ReturnsMatches
└── testSearchShortUrls_NoMatches

ANALYTICS OPERATIONS
├── testGetTopClickedUrls_ReturnsTopUrls
└── testGetTopClickedUrls_WithLimit
```

---

## 2. Integration Tests (Controller Tests)

Integration tests verify the controller endpoints and HTTP responses.

### Test File

**`ShortUrlControllerTest.java`** (NEW)
- WebMvcTest for controller layer
- Tests HTTP status codes and response bodies
- Mocks service layer
- Location: `src/test/java/com/urlshortener/controller/ShortUrlControllerTest.java`

### Test Coverage

```
POST /urls
├── testCreateShortUrl_Success (201)
├── testCreateShortUrl_InvalidRequest (400)
└── testCreateShortUrl_DuplicateAlias (409)

GET /urls/{shortCode}
├── testGetShortUrl_Success (200)
└── testGetShortUrl_NotFound (404)

GET /urls/redirect/{shortCode}
├── testRedirectToOriginal_Success (302)
└── testRedirectToOriginal_NotFound (404)

PUT /urls/{id}
├── testUpdateShortUrl_Success (200)
└── testUpdateShortUrl_NotFound (404)

DELETE /urls/{id}
├── testDeleteShortUrl_Success (204)
└── testDeleteShortUrl_NotFound (404)

GET /urls
└── testGetAllShortUrls_Success (200)

GET /urls/search?term=...
└── testSearchShortUrls_Success (200)

GET /urls/analytics/top?limit=...
└── testGetTopClickedUrls_Success (200)
```

### Running Integration Tests

```powershell
# Run all controller tests
./mvnw test -Dtest=ShortUrlControllerTest

# Run specific test
./mvnw test -Dtest=ShortUrlControllerTest#testCreateShortUrl_Success

# Run all tests
./mvnw test
```

---

## 3. Manual Testing with cURL

Test the API endpoints directly using curl.

### Start the Application

```powershell
# Build and run the application
./mvnw clean spring-boot:run

# The application will start on http://localhost:8080
# Swagger UI available at http://localhost:8080/swagger-ui.html
```

### Test API Endpoints

#### 1. Create a Shortened URL

```powershell
# With custom alias
$json = @{
    originalUrl = "https://www.example.com/very/long/url/path"
    customAlias = "myalias"
    description = "My test URL"
} | ConvertTo-Json

$response = curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json

# Without custom alias (auto-generated)
$json = @{
    originalUrl = "https://www.github.com/project/repo"
} | ConvertTo-Json

curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json
```

#### 2. Get Short URL Details

```powershell
curl -X GET http://localhost:8080/urls/abc123 `
  -H "Content-Type: application/json"
```

#### 3. Redirect to Original URL (Tests click tracking)

```powershell
# This returns 302 redirect status
curl -X GET http://localhost:8080/urls/redirect/abc123 `
  -H "Content-Type: application/json" `
  -L  # Follow redirect
```

#### 4. Update a Short URL

```powershell
$json = @{
    originalUrl = "https://www.newurl.com"
    description = "Updated description"
} | ConvertTo-Json

curl -X PUT http://localhost:8080/urls/1 `
  -H "Content-Type: application/json" `
  -Body $json
```

#### 5. Delete a Short URL

```powershell
curl -X DELETE http://localhost:8080/urls/1 `
  -H "Content-Type: application/json"
```

#### 6. Get All Short URLs

```powershell
curl -X GET http://localhost:8080/urls `
  -H "Content-Type: application/json"
```

#### 7. Search Short URLs

```powershell
curl -X GET "http://localhost:8080/urls/search?term=myalias" `
  -H "Content-Type: application/json"
```

#### 8. Get Top Clicked URLs

```powershell
curl -X GET "http://localhost:8080/urls/analytics/top?limit=10" `
  -H "Content-Type: application/json"
```

#### 9. Health Check

```powershell
curl -X GET http://localhost:8080/health `
  -H "Content-Type: application/json"
```

---

## 4. Testing with Postman

### Import the Collection

Create a new Postman collection with the following requests:

**Environment Variables:**
```
base_url = http://localhost:8080
short_code = abc123
short_id = 1
```

**Collection:**

1. **Create Short URL**
   - Method: POST
   - URL: `{{base_url}}/urls`
   - Body (JSON):
     ```json
     {
       "originalUrl": "https://www.example.com/very/long/url",
       "customAlias": "myalias",
       "description": "Test URL"
     }
     ```
   - Expected: 201 Created

2. **Get Short URL**
   - Method: GET
   - URL: `{{base_url}}/urls/{{short_code}}`
   - Expected: 200 OK

3. **Redirect to Original**
   - Method: GET
   - URL: `{{base_url}}/urls/redirect/{{short_code}}`
   - Expected: 302 Found (with Location header)

4. **Update Short URL**
   - Method: PUT
   - URL: `{{base_url}}/urls/{{short_id}}`
   - Body (JSON):
     ```json
     {
       "originalUrl": "https://www.newurl.com",
       "description": "Updated description"
     }
     ```
   - Expected: 200 OK

5. **Get All URLs**
   - Method: GET
   - URL: `{{base_url}}/urls`
   - Expected: 200 OK

6. **Search URLs**
   - Method: GET
   - URL: `{{base_url}}/urls/search?term=myalias`
   - Expected: 200 OK

7. **Get Top URLs**
   - Method: GET
   - URL: `{{base_url}}/urls/analytics/top?limit=10`
   - Expected: 200 OK

8. **Delete Short URL**
   - Method: DELETE
   - URL: `{{base_url}}/urls/{{short_id}}`
   - Expected: 204 No Content

9. **Access Health**
   - Method: GET
   - URL: `{{base_url}}/health`
   - Expected: 200 OK

---

## 5. Testing Error Scenarios

### Test Case: Duplicate Custom Alias

```powershell
# First request (should succeed)
$json = @{
    originalUrl = "https://www.example.com/url1"
    customAlias = "duplicate"
} | ConvertTo-Json

curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json

# Second request with same alias (should fail with 409)
$json = @{
    originalUrl = "https://www.example.com/url2"
    customAlias = "duplicate"
} | ConvertTo-Json

curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body $json
```

### Test Case: Invalid URL Not Found

```powershell
curl -X GET http://localhost:8080/urls/nonexistent `
  -H "Content-Type: application/json"
# Expected: 404 Not Found
```

### Test Case: Click Tracking

```powershell
# Create a URL and record initial response
$createResponse = curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -Body '{"originalUrl":"https://example.com"}'

# Extract short code from response
$shortCode = $createResponse.shortCode

# Call redirect multiple times
for ($i=0; $i -lt 5; $i++) {
    curl -X GET http://localhost:8080/urls/redirect/$shortCode -L
}

# Check click count
curl -X GET http://localhost:8080/urls/$shortCode
# Expected: clickCount should be 5
```

---

## 6. Running All Tests

### Full Test Suite

```powershell
# Run all tests with detailed output
./mvnw test -X

# Run tests and skip build
./mvnw test -DskipBuild

# Run tests with profiles (if configured)
./mvnw test -P integration-tests

# Generate test report
./mvnw surefire-report:report
# Report location: target/site/surefire-report.html
```

### Test Coverage Report

```powershell
# Generate JaCoCo coverage report
./mvnw clean test jacoco:report

# Report location: target/site/jacoco/index.html

# View coverage in IDE
# Open target/site/jacoco/index.html in browser
```

---

## 7. Debugging Tests

### Enable Debug Logging

Create/update `src/test/resources/logback-test.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <root level="DEBUG">
        <appender-ref ref="STDOUT"/>
    </root>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
</configuration>
```

### Run Single Test with Debug

```powershell
./mvnw -Dtest=ShortUrlControllerTest#testCreateShortUrl_Success test -DforkMode=never -X
```

---

## 8. Best Practices

### ✅ DO

- Write tests for happy path and error scenarios
- Mock external dependencies (database, APIs)
- Use descriptive test names
- Keep tests independent and isolated
- Use parameterized tests for multiple scenarios
- Test edge cases (null values, empty strings, etc.)

### ❌ DON'T

- Test framework internals (Spring, JPA)
- Make tests dependent on test execution order
- Use real databases in unit tests
- Sleep or use timeouts in tests
- Make tests too broad (test one thing per test)

---

## 9. Test Maintenance

### Updating Tests

When adding new features:
1. Write test first (TDD)
2. Implement feature
3. Verify test passes
4. Update documentation

### Common Issues

| Issue | Solution |
|-------|----------|
| `NullPointerException` in tests | Check mock setup in @BeforeEach |
| Test failing locally but passing in CI | Check for test data pollution, use @DirtiesContext |
| Slow tests | Reduce database queries, mock external calls |
| Flakey tests | Remove time-dependent logic, use fixed time values |

---

## 10. Quick Reference

```powershell
# Build project
./mvnw clean build

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ShortUrlServiceTest

# Run with coverage
./mvnw clean test jacoco:report

# Start application
./mvnw spring-boot:run

# Generate test report
./mvnw surefire-report:report
```

---

## Summary

This URL Shortener Service has comprehensive test coverage across:

- **Unit Tests**: Service logic (82+ test methods)
- **Integration Tests**: Controller endpoints (13+ test methods)
- **Manual Tests**: Full end-to-end API validation
- **Error Scenarios**: Exception handling and edge cases

Use this guide for development, debugging, and quality assurance.


