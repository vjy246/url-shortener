# URL Shortener Service - Setup Instructions

**Version:** 1.0.0  
**Last Updated:** July 24, 2026  
**Target Audience:** Developers (Onboarding)

---

## Quick Start (5 minutes)

```bash
# Clone repository
git clone <repo-url>
cd IntelliJSourceCode

# Build and run
./mvnw clean spring-boot:run

# Access application
# API Base: http://localhost:8080/api
# Swagger UI: http://localhost:8080/api/swagger-ui.html
```

---

## Prerequisites

### Required
- **Java 17+** (LTS recommended)
- **Maven 3.9+** (or use bundled `./mvnw`)
- **Git** (for version control)
- **IDE**: IntelliJ IDEA, VS Code, or Eclipse

### Optional
- **Postman** (for API testing)
- **Docker** (for containerized deployment)
- **curl** (command-line API testing)

### System Requirements
- **Disk Space**: 2GB (project + dependencies)
- **RAM**: 4GB minimum
- **Network**: Internet access (Maven Central repository)

---

## Step 1: Environment Setup

### 1.1 Install Java 17

**Windows (Chocolatey):**
```powershell
choco install openjdk17
java -version
```

**macOS (Homebrew):**
```bash
brew install openjdk@17
java -version
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get install openjdk-17-jdk
java -version
```

### 1.2 Install Maven (Optional - Use bundled mvnw)

**Verify Maven:**
```bash
# Use bundled Maven wrapper (recommended)
./mvnw --version

# OR install Maven globally
mvn --version
```

### 1.3 Install Git

**Windows:**
```powershell
choco install git
git --version
```

**macOS:**
```bash
brew install git
git --version
```

**Linux:**
```bash
sudo apt-get install git
git --version
```

---

## Step 2: Clone repository

```bash
# Navigate to projects folder
cd C:\Documents\H1B\PersistenceSystems

# Clone the repository
git clone <repository-url>
cd IntelliJSourceCode

# Verify structure
ls -la
# Should see: src/, pom.xml, README.md, docs/, etc.
```

---

## Step 3: Build the Project

### 3.1 Compile Source Code

```bash
# Clean and compile
./mvnw clean compile

# Expected output:
# BUILD SUCCESS
```

### 3.2 Download Dependencies

Maven will automatically download dependencies on first build. This may take 2-3 minutes.

**Skip tests during initial build (faster):**
```bash
./mvnw clean package -DskipTests
```

---

## Step 4: Run the Application

### 4.1 Start the Service

```bash
# Run using Maven
./mvnw spring-boot:run

# Expected output:
# [main] com.urlshortener.UrlShortenerServiceApplication : Started...
# Tomcat started on port(s): 8080
```

The application is now running on **http://localhost:8080/api**

### 4.2 Verify Service is Running

**Check health endpoint:**
```bash
curl http://localhost:8080/api/health
```

**Expected response:**
```json
{
  "status": "UP",
  "service": "URL Shortener Service",
  "version": "1.0.0",
  "timestamp": 1721779200000
}
```

---

## Step 5: Access the Application

### 5.1 Swagger UI (Interactive Documentation)

**URL:** http://localhost:8080/api/swagger-ui.html

**Features:**
- Browse all endpoints
- Test endpoints with "Try it out"
- View request/response schemas
- Pre-filled examples for testing

### 5.2 H2 Database Console

**URL:** http://localhost:8080/api/h2-console

**Connection Details:**
- JDBC URL: `jdbc:h2:file:./data/urlshortenerdb;MODE=MySQL`
- Username: `sa`
- Password: (empty)

**Use to:**
- Inspect database schema
- Run SQL queries
- View table contents

### 5.3 OpenAPI JSON Spec

**URL:** http://localhost:8080/api/openapi.json

Returns machine-readable API specification for tools like Postman, code generators, etc.

---

## Step 6: Run Tests

### 6.1 Run All Tests

```bash
./mvnw test

# Expected output:
# Tests run: 70+, Failures: 0, Errors: 0
# BUILD SUCCESS
```

### 6.2 Run Specific Test Class

```bash
./mvnw test -Dtest=ShortUrlServiceTest
./mvnw test -Dtest=ShortUrlControllerTest
```

### 6.3 Run with Code Coverage

```bash
./mvnw clean test jacoco:report

# Report location: target/site/jacoco/index.html
# Open in browser to see coverage visualization
```

---

## Step 7: Configure IDE (Optional)

### 7.1 IntelliJ IDEA

1. **Import Project:**
   - File → Open → Select `IntelliJSourceCode` folder
   - Select "Import project from external model" → Maven
   - Click Next and Finish

2. **Configure JDK:**
   - Go to Settings → Project → Project SDK
   - Select JDK 17

3. **Enable Annotation Processing:**
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

4. **Run Configuration:**
   - Run → Edit Configurations
   - Add New Configuration → Spring Boot
   - Class: `com.urlshortener.UrlShortenerServiceApplication`
   - Click Run

### 7.2 VS Code

1. **Install Extensions:**
   - Extension Pack for Java (Microsoft)
   - Spring Boot Extension Pack (Pivotal)
   - Maven for Java (Microsoft)

2. **Open Workspace:**
   - File → Open Folder → Select `IntelliJSourceCode`

3. **Run Application:**
   - Click "Run" above main() method
   - OR use Terminal: `./mvnw spring-boot:run`

---

## Step 8: Basic API Testing

### 8.1 Create a Shortened URL

```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://github.com/spring-projects/spring-boot",
    "customAlias": "spring-boot-repo",
    "description": "Spring Boot Official Repository"
  }'
```

**Expected response:**
```json
{
  "id": 1,
  "shortCode": "abc123XYZ",
  "customAlias": "spring-boot-repo",
  "originalUrl": "https://github.com/spring-projects/spring-boot",
  "clickCount": 0,
  "description": "Spring Boot Official Repository",
  "isActive": true,
  "createdAt": "2026-07-24T12:00:00",
  "updatedAt": "2026-07-24T12:00:00",
  "shortenedUrl": "http://localhost:8080/api/s/abc123XYZ"
}
```

### 8.2 Get All URLs

```bash
curl http://localhost:8080/api/urls
```

### 8.3 Redirect to Original URL

```bash
# With -L flag to follow redirect
curl -L http://localhost:8080/api/urls/redirect/spring-boot-repo
```

### 8.4 Search URLs

```bash
curl "http://localhost:8080/api/urls/search?term=spring"
```

---

## Common Issues & Troubleshooting

### Issue: Java version not found

```bash
# Check installed Java versions
java -version

# Solution: Install Java 17
# See Step 1.1
```

### Issue: Port 8080 already in use

```bash
# Find process using port
lsof -i :8080  # macOS/Linux
Get-NetTCPConnection -LocalPort 8080  # Windows

# Kill process or use different port:
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Issue: Database file cannot be accessed

```bash
# Check file permissions
ls -la ./data/urlshortenerdb*

# Solution: Delete old database (data will reset)
rm -rf ./data/urlshortenerdb*
./mvnw spring-boot:run
```

### Issue: Maven dependency download fails

```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Retry build
./mvnw clean install
```

### Issue: Tests fail with "Cannot find @interface method"

This is a known issue with annotation processing. Fix:

```bash
# Clean and rebuild
./mvnw clean compile

# Or in IDE: Build → Rebuild Project
```

---

## Development Workflow

### 1. Make Code Changes

```bash
# Create/edit files in src/main/java
# IDE will auto-compile on save
```

### 2. Restart Application

```bash
# Stop running app (Ctrl+C)
./mvnw spring-boot:run

# OR use IDE's hot reload:
# In IntelliJ: Run → Rerun (Ctrl+F5)
```

### 3. Run Tests

```bash
./mvnw test
```

### 4. Verify API Changes

```bash
# Check Swagger UI for updated endpoints
# http://localhost:8080/api/swagger-ui.html

# Test with curl or Postman
```

### 5. Commit Changes

```bash
git add .
git commit -m "Feature: Add X functionality"
git push origin feature/x
```

---

## Docker Development (Optional)

### Build Docker Image

```bash
./mvnw clean package
docker build -t url-shortener:1.0.0 .
```

### Run Container

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:h2:file:/data/urlshortenerdb \
  -v $(pwd)/data:/data \
  url-shortener:1.0.0
```

---

## Next Steps

1. ✅ **Complete setup** using steps above
2. 📖 **Read documentation**:
   - [TECHNICAL_ARCHITECTURE.md](./TECHNICAL_ARCHITECTURE.md) - System design
   - [API_REFERENCE.md](./API_REFERENCE.md) - API endpoints
3. 🧪 **Run tests**: `./mvnw test`
4. 🚀 **Start development**: Make changes and test locally
5. 📚 **Review code**: Check Spring Boot best practices
6. 📝 **Ask questions**: Reach out to tech lead

---

## Getting Help

| Problem | Command | Link |
|---------|---------|------|
| Application won't start | `./mvnw spring-boot:run -X` | [Troubleshooting](./TROUBLESHOOTING.md) |
| How to test APIs? | `curl` examples | [API_REFERENCE.md](./API_REFERENCE.md) |
| System architecture? | Read | [TECHNICAL_ARCHITECTURE.md](./TECHNICAL_ARCHITECTURE.md) |
| Database access? | http://localhost:8080/api/h2-console | See Step 5.2 |

---

**You're now ready to develop! 🚀**


