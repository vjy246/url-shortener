# Quick Start Guide - URL Shortener Service

## 🚀 Getting Started in 5 Minutes

### Step 1: Build the Project
```bash
cd C:\Documents\H1B\PersistenceSystems\IntelliJSourceCode
mvn clean install
```

### Step 2: Run the Application
```bash
mvn spring-boot:run
```

You should see:
```
Started UrlShortenerServiceApplication in X.XXX seconds
```

### Step 3: Test the API

**Option A: Using Swagger UI** (Recommended)
- Open browser: `http://localhost:8080/api/swagger-ui.html`
- Try out endpoints from the interactive UI

**Option B: Using curl**
```bash
# Create a shortened URL
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://github.com/spring-projects/spring-boot",
    "customAlias": "springboot-github",
    "description": "Spring Boot GitHub Repository"
  }'

# Get URL details
curl http://localhost:8080/api/urls/abc123

# Get all URLs
curl http://localhost:8080/api/urls

# Check API health
curl http://localhost:8080/api/health
```

### Step 4: View Database (Optional)
- Open browser: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:urlshortenerdb`
- Username: `sa`
- Password: (leave blank)
- Click "Connect"

## 📚 Key API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/urls` | Create shortened URL |
| GET | `/urls/{shortCode}` | Get URL details |
| GET | `/urls` | List all URLs |
| GET | `/urls/search?term=x` | Search URLs |
| GET | `/urls/analytics/top?limit=10` | Top clicked URLs |
| GET | `/urls/redirect/{shortCode}` | Redirect & track clicks |
| PUT | `/urls/{id}` | Update URL |
| DELETE | `/urls/{id}` | Delete URL |
| GET | `/health` | Health check |

## 🔍 Example: Create & Access a Shortened URL

### 1. Create a URL
```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.spring.io/projects/spring-boot",
    "customAlias": "my-spring-url",
    "description": "Spring Boot Official"
  }'
```

Response:
```json
{
  "id": 1,
  "shortCode": "xyz789",
  "originalUrl": "https://www.spring.io/projects/spring-boot",
  "customAlias": "my-spring-url",
  "clickCount": 0,
  "shortenedUrl": "http://localhost:8080/api/s/xyz789",
  "createdAt": "2024-01-15T10:30:00"
}
```

### 2. Access the shortened URL
```bash
# This will redirect to original URL
curl -L http://localhost:8080/api/urls/redirect/xyz789
```

### 3. Check click count
```bash
curl http://localhost:8080/api/urls/xyz789
```

Click count will be incremented!

## 📝 Important Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven dependencies and build configuration |
| `src/main/resources/application.yml` | Application configuration (port, database, logging) |
| `src/main/java/com/urlshortener/UrlShortenerServiceApplication.java` | Main app + Swagger config |
| `src/main/java/com/urlshortener/controller/ShortUrlController.java` | REST API endpoints |
| `src/main/java/com/urlshortener/service/ShortUrlService.java` | Business logic |
| `spring-boot-README.md` | Detailed documentation |

## ⚙️ Configuration

Edit `src/main/resources/application.yml` to change:

```yaml
server:
  port: 8080              # Change API port
  
spring:
  application:
    name: url-shortener-service  # App name
  datasource:
    url: jdbc:h2:mem:urlshortenerdb  # Database URL
```

## 🧪 Run Tests

```bash
mvn test
```

## 🛑 Stop the Application

Press `Ctrl+C` in the terminal running the application.

## 📚 Full Documentation

See `spring-boot-README.md` for:
- Detailed API documentation
- Project structure
- All available endpoints
- Configuration options
- More examples

## 🆘 Troubleshooting

### Port 8080 Already in Use
Change port in `application.yml`:
```yaml
server:
  port: 9090  # Use different port
```

### Maven Not Found
- Ensure Maven is installed: `mvn -version`
- Add Maven bin to PATH environment variable

### Java Version Mismatch
- Ensure Java 17+ is installed: `java -version`
- Update `.java.version` in `pom.xml` if needed

### H2 Console Not Accessible
- Make sure you're using: `http://localhost:8080/api/h2-console`
- Check that `spring.h2.console.enabled: true` in `application.yml`

## 🎯 Next Steps

1. ✅ Try all endpoints in Swagger UI
2. ✅ Create some shortened URLs
3. ✅ Check database via H2 Console
4. ✅ Review code in `controller`, `service`, `entity` packages
5. ✅ Explore test cases in `src/test`
6. ✅ Customize the application for your needs

---

**Need Help?** Check `spring-boot-README.md` for comprehensive documentation!

