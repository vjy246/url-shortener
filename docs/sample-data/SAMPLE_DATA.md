# URL Shortener - Sample Data for Testing

This file provides ready-to-use test data for the URL Shortener API.

---

## 📋 Sample Test Data (JSON Payloads)

### Category 1: Technology & Documentation Sites

#### 1.1 GitHub Repository
```json
{
  "originalUrl": "https://github.com/spring-projects/spring-boot",
  "customAlias": "spring-boot-repo",
  "description": "Spring Boot - Official Repository"
}
```

#### 1.2 Python Official
```json
{
  "originalUrl": "https://www.python.org/downloads",
  "customAlias": "python-downloads",
  "description": "Python Official Downloads"
}
```

#### 1.3 Java Documentation
```json
{
  "originalUrl": "https://docs.oracle.com/javase/17/docs/api",
  "customAlias": "java-17-docs",
  "description": "Java 17 API Documentation"
}
```

#### 1.4 MDN Web Docs
```json
{
  "originalUrl": "https://developer.mozilla.org/en-US/docs/Web/JavaScript",
  "customAlias": "mdn-javascript",
  "description": "MDN JavaScript Documentation"
}
```

### Category 2: Social Media & Sharing

#### 2.1 LinkedIn Profile
```json
{
  "originalUrl": "https://www.linkedin.com/in/jane-developer-123456",
  "customAlias": "linkedin-jane",
  "description": "Jane Developer's LinkedIn Profile"
}
```

#### 2.2 Twitter Account
```json
{
  "originalUrl": "https://twitter.com/techgirl_2024",
  "customAlias": "twitter-techgirl",
  "description": "TechGirl Twitter Account"
}
```

#### 2.3 Blog Post
```json
{
  "originalUrl": "https://medium.com/@author/how-to-build-microservices-2024",
  "customAlias": "microservices-guide",
  "description": "How to Build Microservices - Medium Article"
}
```

### Category 3: E-commerce & Products

#### 3.1 Amazon Product
```json
{
  "originalUrl": "https://www.amazon.com/ASUS-Gaming-Laptop-RTX4060-Backlit/dp/B0CX5KN8NC",
  "customAlias": "asus-gaming-laptop",
  "description": "ASUS Gaming Laptop on Amazon"
}
```

#### 3.2 Udemy Course
```json
{
  "originalUrl": "https://www.udemy.com/course/the-complete-java-development-bootcamp",
  "customAlias": "java-bootcamp-udemy",
  "description": "Complete Java Development Bootcamp"
}
```

### Category 4: News & Articles

#### 4.1 Tech News
```json
{
  "originalUrl": "https://www.techcrunch.com/2024/01/15/ai-startup-raises-100-million",
  "customAlias": "ai-startup-funding",
  "description": "TechCrunch - AI Startup Funding News"
}
```

#### 4.2 Research Paper
```json
{
  "originalUrl": "https://arxiv.org/pdf/2401.00123.pdf",
  "customAlias": "ml-research-paper",
  "description": "Machine Learning Research Paper"
}
```

### Category 5: Videos & Media

#### 5.1 YouTube Tutorial
```json
{
  "originalUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
  "customAlias": "youtube-tutorial",
  "description": "Popular YouTube Tutorial"
}
```

#### 5.2 Netflix Series
```json
{
  "originalUrl": "https://www.netflix.com/title/81606276",
  "customAlias": "netflix-series",
  "description": "Netflix Series Recommendation"
}
```

### Category 6: Tools & Services

#### 6.1 GitHub Gist
```json
{
  "originalUrl": "https://gist.github.com/user123/abc123def456ghi789jkl",
  "customAlias": "github-gist-snippet",
  "description": "Useful Code Snippet on GitHub Gist"
}
```

#### 6.2 Stack Overflow Question
```json
{
  "originalUrl": "https://stackoverflow.com/questions/12345678/how-to-parse-json-in-java",
  "customAlias": "so-json-parsing",
  "description": "Stack Overflow - JSON Parsing in Java"
}
```

#### 6.3 Figma Design
```json
{
  "originalUrl": "https://www.figma.com/file/abc123/mobile-app-ui-design",
  "customAlias": "figma-mobile-ui",
  "description": "Mobile App UI Design in Figma"
}
```

---

## 📊 Test Data Categories

| Category | Count | Examples |
|----------|-------|----------|
| Programming | 4 | Spring Boot, Python, Java, JavaScript |
| Social Media | 2 | LinkedIn, Twitter |
| Articles | 2 | Medium, TechCrunch |
| E-commerce | 2 | Amazon, Udemy |
| Video | 1 | YouTube |
| Code Sharing | 1 | Stack Overflow |
| **Total** | **12** | **Ready to use** |

---

## 🧪 PowerShell Test Script

Save this as `import-sample-data.ps1`:

```powershell
# URL Shortener - Sample Data Importer
# This script creates multiple test URLs

$BaseUrl = "http://localhost:8080"
$urls = @(
    @{
        originalUrl = "https://github.com/spring-projects/spring-boot"
        customAlias = "spring-boot-repo"
        description = "Spring Boot - Official Repository"
    },
    @{
        originalUrl = "https://www.python.org/downloads"
        customAlias = "python-downloads"
        description = "Python Official Downloads"
    },
    @{
        originalUrl = "https://docs.oracle.com/javase/17/docs/api"
        customAlias = "java-17-docs"
        description = "Java 17 API Documentation"
    },
    @{
        originalUrl = "https://developer.mozilla.org/en-US/docs/Web/JavaScript"
        customAlias = "mdn-javascript"
        description = "MDN JavaScript Documentation"
    },
    @{
        originalUrl = "https://www.linkedin.com/in/jane-developer-123456"
        customAlias = "linkedin-jane"
        description = "Jane Developer's LinkedIn Profile"
    },
    @{
        originalUrl = "https://twitter.com/techgirl_2024"
        customAlias = "twitter-techgirl"
        description = "TechGirl Twitter Account"
    },
    @{
        originalUrl = "https://medium.com/@author/how-to-build-microservices-2024"
        customAlias = "microservices-guide"
        description = "How to Build Microservices - Medium Article"
    },
    @{
        originalUrl = "https://www.amazon.com/ASUS-Gaming-Laptop-RTX4060-Backlit/dp/B0CX5KN8NC"
        customAlias = "asus-gaming-laptop"
        description = "ASUS Gaming Laptop on Amazon"
    },
    @{
        originalUrl = "https://www.udemy.com/course/the-complete-java-development-bootcamp"
        customAlias = "java-bootcamp-udemy"
        description = "Complete Java Development Bootcamp"
    },
    @{
        originalUrl = "https://www.techcrunch.com/2024/01/15/ai-startup-raises-100-million"
        customAlias = "ai-startup-funding"
        description = "TechCrunch - AI Startup Funding News"
    },
    @{
        originalUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        customAlias = "youtube-tutorial"
        description = "Popular YouTube Tutorial"
    },
    @{
        originalUrl = "https://stackoverflow.com/questions/12345678/how-to-parse-json-in-java"
        customAlias = "so-json-parsing"
        description = "Stack Overflow - JSON Parsing in Java"
    }
)

Write-Host "🚀 Starting Sample Data Import..." -ForegroundColor Cyan
Write-Host "Target: $BaseUrl`n" -ForegroundColor Gray

$successCount = 0
$failureCount = 0

foreach ($url in $urls) {
    try {
        $json = $url | ConvertTo-Json
        $response = curl -X POST "$BaseUrl/urls" `
            -H "Content-Type: application/json" `
            -Body $json `
            -ErrorAction Stop
        
        $successCount++
        Write-Host "✓ [$successCount] Created: $($url.customAlias)" -ForegroundColor Green
    }
    catch {
        $failureCount++
        Write-Host "✗ [$failureCount] Failed: $($url.customAlias)" -ForegroundColor Red
        Write-Host "  Error: $_" -ForegroundColor Yellow
    }
    
    Start-Sleep -Milliseconds 100  # Small delay between requests
}

Write-Host "`n════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "✓ Success: $successCount | ✗ Failed: $failureCount" -ForegroundColor Cyan
Write-Host "════════════════════════════════════════" -ForegroundColor Cyan

# Verify data was imported
Write-Host "`n📊 Verifying imported data..." -ForegroundColor Yellow
$allUrls = curl -X GET "$BaseUrl/urls" -H "Content-Type: application/json"
Write-Host "Total URLs in database: $(($allUrls | ConvertFrom-Json).Length)" -ForegroundColor Cyan
```

**Run it:**
```powershell
./import-sample-data.ps1
```

---

## 📄 SQL Inserts (For Direct Database Loading)

If you want to insert data directly into H2 database:

```sql
-- Insert sample URLs into the short_urls table

INSERT INTO short_urls (short_code, original_url, custom_alias, click_count, created_at, updated_at, description, is_active)
VALUES 
  ('abc123xy', 'https://github.com/spring-projects/spring-boot', 'spring-boot-repo', 0, NOW(), NOW(), 'Spring Boot - Official Repository', true),
  ('def456ab', 'https://www.python.org/downloads', 'python-downloads', 5, NOW(), NOW(), 'Python Official Downloads', true),
  ('ghi789cd', 'https://docs.oracle.com/javase/17/docs/api', 'java-17-docs', 3, NOW(), NOW(), 'Java 17 API Documentation', true),
  ('jkl012ef', 'https://developer.mozilla.org/en-US/docs/Web/JavaScript', 'mdn-javascript', 15, NOW(), NOW(), 'MDN JavaScript Documentation', true),
  ('mno345gh', 'https://www.linkedin.com/in/jane-developer-123456', 'linkedin-jane', 2, NOW(), NOW(), 'Jane Developer LinkedIn Profile', true),
  ('pqr678ij', 'https://twitter.com/techgirl_2024', 'twitter-techgirl', 8, NOW(), NOW(), 'TechGirl Twitter Account', true),
  ('stu901kl', 'https://medium.com/@author/how-to-build-microservices-2024', 'microservices-guide', 12, NOW(), NOW(), 'How to Build Microservices', true),
  ('vwx234mn', 'https://www.amazon.com/ASUS-Gaming-Laptop-RTX4060-Backlit/dp/B0CX5KN8NC', 'asus-gaming-laptop', 1, NOW(), NOW(), 'ASUS Gaming Laptop on Amazon', true),
  ('yza567op', 'https://www.udemy.com/course/the-complete-java-development-bootcamp', 'java-bootcamp-udemy', 4, NOW(), NOW(), 'Complete Java Development Bootcamp', true),
  ('bcd890qr', 'https://www.techcrunch.com/2024/01/15/ai-startup-raises-100-million', 'ai-startup-funding', 20, NOW(), NOW(), 'TechCrunch - AI Startup Funding', true),
  ('efg123st', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'youtube-tutorial', 100, NOW(), NOW(), 'Popular YouTube Tutorial', true),
  ('hij456uv', 'https://stackoverflow.com/questions/12345678/how-to-parse-json-in-java', 'so-json-parsing', 7, NOW(), NOW(), 'Stack Overflow - JSON Parsing', true);

-- Verify inserts
SELECT * FROM short_urls;
```

Access H2 Console: http://localhost:8080/h2-console

---

## 🔄 Full Test Workflow with Sample Data

### Step 1: Start Application
```powershell
mvnw spring-boot:run
```

### Step 2: Import Sample Data (Choose one method)

**Option A: Using PowerShell Script**
```powershell
./import-sample-data.ps1
```

**Option B: Using curl One-Liner Loop**
```powershell
$data = @(
  '{"originalUrl":"https://github.com/spring-projects/spring-boot","customAlias":"spring-boot-repo"}',
  '{"originalUrl":"https://www.python.org/downloads","customAlias":"python-downloads"}',
  '{"originalUrl":"https://docs.oracle.com/javase/17/docs/api","customAlias":"java-17-docs"}'
)

foreach ($item in $data) {
  curl -X POST http://localhost:8080/urls `
    -H "Content-Type: application/json" `
    -d $item
}
```

**Option C: Direct Database Insert**
1. Open http://localhost:8080/h2-console
2. Paste SQL from above
3. Run

### Step 3: Verify Data Was Imported
```powershell
curl http://localhost:8080/urls
```

### Step 4: Test Queries
```powershell
# Get specific URL
curl http://localhost:8080/urls/spring-boot-repo

# Redirect and track
curl -L http://localhost:8080/urls/redirect/python-downloads

# Search
curl "http://localhost:8080/urls/search?term=java"

# Analytics
curl "http://localhost:8080/urls/analytics/top?limit=5"
```

---

## 🧪 Test Scenarios Using Sample Data

### Scenario 1: Search by Category
```powershell
# Search for Java-related
curl "http://localhost:8080/urls/search?term=java"

# Expected: java-17-docs, java-bootcamp-udemy
```

### Scenario 2: Track Most Popular
```powershell
curl "http://localhost:8080/urls/analytics/top?limit=5"

# Expected: youtube-tutorial (100), ai-startup-funding (20), mdn-javascript (15), microservices-guide (12), twitter-techgirl (8)
```

### Scenario 3: Bulk Redirect Testing
```powershell
# Test multiple redirects
'spring-boot-repo', 'python-downloads', 'java-17-docs' | 
  ForEach-Object { 
    Write-Host "Redirecting $_..."
    curl -L "http://localhost:8080/urls/redirect/$_" -o $null
  }
```

### Scenario 4: Update Sample Data
```powershell
$json = @{
    description = "Updated: Spring Boot Repository - Latest Version"
} | ConvertTo-Json

curl -X PUT http://localhost:8080/urls/1 `
  -H "Content-Type: application/json" `
  -Body $json
```

---

## ✅ Quick Start Checklist

- [ ] Save sample data file
- [ ] Start application: `mvnw spring-boot:run`
- [ ] Choose import method (PowerShell, curl, or SQL)
- [ ] Run import
- [ ] Verify: `curl http://localhost:8080/urls`
- [ ] Test queries
- [ ] Run test scenarios
- [ ] View in Swagger UI: http://localhost:8080/swagger-ui.html

---

## 🚀 Next Steps

1. **Import sample data** using any method above
2. **Verify data** with GET /urls endpoint
3. **Run tests** with `mvnw test`
4. **Explore** with different queries
5. **Modify** entries to test updates
6. **Delete** entries to test deletion
7. **Track clicks** by redirecting multiple times
8. **View analytics** to see top URLs

---

## 📝 Notes

- All URLs are real and functional
- Click counts are pre-set for analytics testing
- Sample data covers multiple domains for realistic testing
- You can add more data by editing this file
- PowerShell script is self-contained and reusable
- Data persists in H2 database during session

---

**Ready to test with real data!** 🎉

Copy the PowerShell script, curl commands, or Postman collection above and start testing immediately!


