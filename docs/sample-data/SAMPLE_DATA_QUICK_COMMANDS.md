# URL Shortener - Sample Data Quick Commands

**Copy and paste these curl commands directly into PowerShell to test the API with sample data.**

---

## 🚀 Quick Start (One-Liner Setup)

Import all 12 sample URLs at once:

```powershell
.\import-sample-data.ps1
```

Or manually with curl loop:

```powershell
$data = @(
  '{"originalUrl":"https://github.com/spring-projects/spring-boot","customAlias":"spring-boot-repo","description":"Spring Boot Repository"}',
  '{"originalUrl":"https://www.python.org/downloads","customAlias":"python-downloads","description":"Python Downloads"}',
  '{"originalUrl":"https://docs.oracle.com/javase/17/docs/api","customAlias":"java-17-docs","description":"Java 17 API"}',
  '{"originalUrl":"https://developer.mozilla.org/en-US/docs/Web/JavaScript","customAlias":"mdn-javascript","description":"MDN JavaScript"}',
  '{"originalUrl":"https://www.linkedin.com/in/jane-developer-123456","customAlias":"linkedin-jane","description":"LinkedIn Profile"}',
  '{"originalUrl":"https://twitter.com/techgirl_2024","customAlias":"twitter-techgirl","description":"Twitter Account"}',
  '{"originalUrl":"https://medium.com/@author/how-to-build-microservices-2024","customAlias":"microservices-guide","description":"Microservices Guide"}',
  '{"originalUrl":"https://www.amazon.com/ASUS-Gaming-Laptop-RTX4060-Backlit/dp/B0CX5KN8NC","customAlias":"asus-gaming-laptop","description":"Gaming Laptop"}',
  '{"originalUrl":"https://www.udemy.com/course/the-complete-java-development-bootcamp","customAlias":"java-bootcamp-udemy","description":"Java Bootcamp"}',
  '{"originalUrl":"https://www.techcrunch.com/2024/01/15/ai-startup-raises-100-million","customAlias":"ai-startup-funding","description":"AI Startup Funding"}',
  '{"originalUrl":"https://www.youtube.com/watch?v=dQw4w9WgXcQ","customAlias":"youtube-tutorial","description":"YouTube Tutorial"}',
  '{"originalUrl":"https://stackoverflow.com/questions/12345678/how-to-parse-json-in-java","customAlias":"so-json-parsing","description":"Stack Overflow"}'
)

foreach ($item in $data) {
  curl -X POST http://localhost:8080/urls -H "Content-Type: application/json" -d $item
  Write-Host ""
}
```

---

## 📝 CREATE - Sample URLs

### 1. Spring Boot Repository
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://github.com/spring-projects/spring-boot","customAlias":"spring-boot-repo","description":"Spring Boot Repository"}'
```

### 2. Python Downloads
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.python.org/downloads","customAlias":"python-downloads","description":"Python Downloads"}'
```

### 3. Java 17 API Docs
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://docs.oracle.com/javase/17/docs/api","customAlias":"java-17-docs","description":"Java 17 API"}'
```

### 4. MDN JavaScript
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://developer.mozilla.org/en-US/docs/Web/JavaScript","customAlias":"mdn-javascript","description":"MDN JavaScript"}'
```

### 5. LinkedIn Profile
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.linkedin.com/in/jane-developer-123456","customAlias":"linkedin-jane","description":"LinkedIn Profile"}'
```

### 6. Twitter Account
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://twitter.com/techgirl_2024","customAlias":"twitter-techgirl","description":"Twitter Account"}'
```

### 7. Microservices Guide
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://medium.com/@author/how-to-build-microservices-2024","customAlias":"microservices-guide","description":"Microservices Guide"}'
```

### 8. Gaming Laptop
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.amazon.com/ASUS-Gaming-Laptop-RTX4060-Backlit/dp/B0CX5KN8NC","customAlias":"asus-gaming-laptop","description":"Gaming Laptop"}'
```

### 9. Java Bootcamp
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.udemy.com/course/the-complete-java-development-bootcamp","customAlias":"java-bootcamp-udemy","description":"Java Bootcamp"}'
```

### 10. AI Startup News
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.techcrunch.com/2024/01/15/ai-startup-raises-100-million","customAlias":"ai-startup-funding","description":"AI Startup Funding"}'
```

### 11. YouTube Tutorial
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.youtube.com/watch?v=dQw4w9WgXcQ","customAlias":"youtube-tutorial","description":"YouTube Tutorial"}'
```

### 12. Stack Overflow
```powershell
curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://stackoverflow.com/questions/12345678/how-to-parse-json-in-java","customAlias":"so-json-parsing","description":"Stack Overflow"}'
```

---

## 🔍 GET - Retrieve URLs

```powershell
# Get Spring Boot repo
curl http://localhost:8080/urls/spring-boot-repo

# Get Python downloads
curl http://localhost:8080/urls/python-downloads

# Get Java docs
curl http://localhost:8080/urls/java-17-docs

# Get all URLs
curl http://localhost:8080/urls
```

---

## 🔗 REDIRECT - Track Clicks

```powershell
# Redirect to Spring Boot repo (increments click count)
curl -L http://localhost:8080/urls/redirect/spring-boot-repo

# Redirect to Python downloads
curl -L http://localhost:8080/urls/redirect/python-downloads

# Redirect to YouTube tutorial
curl -L http://localhost:8080/urls/redirect/youtube-tutorial

# Test multiple redirects (tracking clicks)
for ($i=0; $i -lt 5; $i++) {
  curl -L http://localhost:8080/urls/redirect/spring-boot-repo
}
```

---

## 🔎 SEARCH - Find URLs

```powershell
# Search for Java-related URLs
curl "http://localhost:8080/urls/search?term=java"

# Search for Python
curl "http://localhost:8080/urls/search?term=python"

# Search for tutorial
curl "http://localhost:8080/urls/search?term=tutorial"

# Search for media
curl "http://localhost:8080/urls/search?term=media"

# Search for guide
curl "http://localhost:8080/urls/search?term=guide"
```

---

## 📊 ANALYTICS - View Statistics

```powershell
# Get top 5 clicked URLs
curl "http://localhost:8080/urls/analytics/top?limit=5"

# Get top 10 clicked URLs
curl "http://localhost:8080/urls/analytics/top?limit=10"

# Get all URLs sorted by clicks
curl "http://localhost:8080/urls/analytics/top?limit=100"
```

---

## ✏️ UPDATE - Modify URLs

```powershell
# Update ID 1 (note: you'll need the actual ID from creation response)
curl -X PUT http://localhost:8080/urls/1 `
  -H "Content-Type: application/json" `
  -d '{"description":"Updated: Spring Boot Framework - Latest Stable Release"}'

# Update URL and description
curl -X PUT http://localhost:8080/urls/2 `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.python.org/downloads/","description":"Python 3.12 Downloads"}'
```

---

## ❌ DELETE - Remove URLs

```powershell
# Delete URL with ID 1
curl -X DELETE http://localhost:8080/urls/1

# Verify it's gone
curl http://localhost:8080/urls/1  # Should return 404
```

---

## 💚 HEALTH - Verify Service

```powershell
# Check if service is running
curl http://localhost:8080/health
```

---

## 📋 Complete Test Flow

**Step 1: Start the application**
```powershell
mvnw spring-boot:run
```

**Step 2: Import sample data (one of these)**
```powershell
# Option A: Run PowerShell script
.\import-sample-data.ps1

# Option B: Run curl loop (paste the code from Quick Start section above)
```

**Step 3: Verify import**
```powershell
curl http://localhost:8080/urls
```

**Step 4: Test individual URLs**
```powershell
curl http://localhost:8080/urls/spring-boot-repo
curl http://localhost:8080/urls/python-downloads
```

**Step 5: Test search**
```powershell
curl "http://localhost:8080/urls/search?term=java"
```

**Step 6: Test analytics**
```powershell
curl "http://localhost:8080/urls/analytics/top?limit=5"
```

**Step 7: Test redirect (trigger clicks)**
```powershell
curl -L http://localhost:8080/urls/redirect/youtube-tutorial
```

**Step 8: Run automated tests**
```powershell
mvnw test
```

---

## 🎯 Quick Test Scenarios

### Scenario: Find Java-related URLs and click them
```powershell
# Search
$responses = curl "http://localhost:8080/urls/search?term=java" | ConvertFrom-Json

# Redirect to each
$responses | ForEach-Object {
  Write-Host "Redirecting to $($_.customAlias)..."
  curl -L "http://localhost:8080/urls/redirect/$($_.customAlias)"
}

# View analytics
curl "http://localhost:8080/urls/analytics/top?limit=10"
```

### Scenario: Create and test a new URL
```powershell
# Create new
$response = curl -X POST http://localhost:8080/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://example.com/test","customAlias":"test-url"}' | ConvertFrom-Json

$id = $response.id

# Get it
curl http://localhost:8080/urls/test-url

# Redirect to track click
curl -L "http://localhost:8080/urls/redirect/test-url"

# Update it
curl -X PUT "http://localhost:8080/urls/$id" `
  -H "Content-Type: application/json" `
  -d '{"description":"Updated test URL"}'

# Delete it
curl -X DELETE "http://localhost:8080/urls/$id"
```

---

## 📌 Sample Data Summary

| Alias | Category | URL |
|-------|----------|-----|
| spring-boot-repo | Programming | github.com/spring-projects/spring-boot |
| python-downloads | Programming | python.org/downloads |
| java-17-docs | Programming | docs.oracle.com/javase/17/docs/api |
| mdn-javascript | Programming | developer.mozilla.org/docs/Web/JavaScript |
| linkedin-jane | Social Media | linkedin.com/in/jane-developer-123456 |
| twitter-techgirl | Social Media | twitter.com/techgirl_2024 |
| microservices-guide | Articles | medium.com/@author/how-to-build-microservices-2024 |
| asus-gaming-laptop | E-commerce | amazon.com/ASUS-Gaming-Laptop... |
| java-bootcamp-udemy | E-commerce | udemy.com/course/java-bootcamp |
| ai-startup-funding | News | techcrunch.com/2024/01/15/ai-startup... |
| youtube-tutorial | Videos | youtube.com/watch?v=dQw4w9WgXcQ |
| so-json-parsing | Code Sharing | stackoverflow.com/questions/12345678/... |

---

## ✅ Expected Responses

### Create (201)
```json
{
  "id": 1,
  "shortCode": "abc123xyz",
  "originalUrl": "https://github.com/spring-projects/spring-boot",
  "customAlias": "spring-boot-repo",
  "shortenedUrl": "http://localhost:8080/api/s/abc123xyz",
  "clickCount": 0,
  "description": "Spring Boot Repository",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Get (200)
```json
{
  "id": 1,
  "shortCode": "abc123xyz",
  "customAlias": "spring-boot-repo",
  "originalUrl": "https://github.com/spring-projects/spring-boot",
  "clickCount": 3,
  "description": "Spring Boot Repository",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:35:00",
  "shortenedUrl": "http://localhost:8080/api/s/abc123xyz"
}
```

### List (200)
```json
[
  {
    "id": 1,
    "customAlias": "spring-boot-repo",
    "originalUrl": "https://github.com/spring-projects/spring-boot"
  },
  {
    "id": 2,
    "customAlias": "python-downloads",
    "originalUrl": "https://www.python.org/downloads"
  },
  {
    "id": 3,
    "customAlias": "java-17-docs",
    "originalUrl": "https://docs.oracle.com/javase/17/docs/api"
  }
]
```

### Search (200)
```json
[
  {
    "customAlias": "java-17-docs",
    "originalUrl": "https://docs.oracle.com/javase/17/docs/api",
    "clickCount": 5,
    "omittedFields": "..."
  },
  {
    "customAlias": "java-bootcamp-udemy",
    "originalUrl": "https://www.udemy.com/course/the-complete-java-development-bootcamp",
    "clickCount": 2,
    "omittedFields": "..."
  }
]
```

### Analytics (200)
```json
[
  {
    "customAlias": "youtube-tutorial",
    "clickCount": 150,
    "omittedFields": "..."
  },
  {
    "customAlias": "ai-startup-funding",
    "clickCount": 45,
    "omittedFields": "..."
  }
]
```

---

## 🎁 Bonus: Bulk Click Simulator

Simulate many clicks to test analytics:

```powershell
# Generate 50 clicks for spring-boot-repo
for ($i=0; $i -lt 50; $i++) {
  curl -L "http://localhost:8080/urls/redirect/spring-boot-repo" -o $null
  Write-Host "Click $($i+1)"
}

# Generate 30 clicks for python-downloads
for ($i=0; $i -lt 30; $i++) {
  curl -L "http://localhost:8080/urls/redirect/python-downloads" -o $null
  Write-Host "Click $($i+1)"
}

# View analytics
Write-Host "`nTop URLs:"
curl "http://localhost:8080/urls/analytics/top?limit=5" | ConvertFrom-Json | 
  ForEach-Object { Write-Host "$($_.customAlias): $($_.clickCount) clicks" }
```

---

**All set! Your sample data is ready to test! 🚀**
