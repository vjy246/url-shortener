# URL Shortener Service

A Spring Boot REST API for creating, managing, and tracking shortened URLs with an embedded H2 database.

---

## 🌐 Application URLs

| Purpose | URL |
|---------|-----|
| **Base URL** | http://localhost:8080/api |
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html |
| **Swagger UI (index)** | http://localhost:8080/api/swagger-ui/index.html |
| **API Docs (JSON)** | http://localhost:8080/api/openapi.json |
| **H2 Database Console** | http://localhost:8080/api/h2-console |
| **Health Check** | http://localhost:8080/api/health |
| **API Info** | http://localhost:8080/api/health/info |

---

## 🔍 Looking Up URLs - Two Methods

### Method 1: By Custom Alias (Recommended)
```powershell
curl http://localhost:8080/api/urls/spring-boot-repo
```

### Method 2: By Short Code (Auto-Generated)
```powershell
curl http://localhost:8080/api/urls/abc123XYZ
```

**Both methods work for:**
- `GET /api/urls/{shortCode}` - Get URL details
- `GET /api/urls/redirect/{shortCode}` - Redirect to original (tracks click)

### Run the Application
```powershell
./mvnw spring-boot:run
```

### Run All Tests
```powershell
./mvnw test
```

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/urls` | Create a shortened URL |
| `POST` | `/api/urls/bulk` | Create multiple shortened URLs at once (5 by default) |
| `GET` | `/api/urls` | Get all shortened URLs |
| `GET` | `/api/urls/{shortCode}` | Get URL by short code OR custom alias |
| `GET` | `/api/urls/redirect/{shortCode}` | Redirect to original URL (tracks click) - accepts short code OR custom alias |
| `PUT` | `/api/urls/{id}` | Update a shortened URL |
| `DELETE` | `/api/urls/{id}` | Delete a shortened URL |
| `GET` | `/api/urls/search?term=...` | Search URLs by keyword |
| `GET` | `/api/urls/analytics/top?limit=10` | Get top clicked URLs |

---

## 📋 Sample Usage

### Create a Short URL (Examples in Swagger UI)

Visit Swagger UI at: **http://localhost:8080/api/swagger-ui/index.html**

Click on **"POST /api/urls"** → **"Try it out"** → Select example → **"Execute"**

**Manual Examples:**

**Example 1: GitHub Repository**
```powershell
curl -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://github.com/spring-projects/spring-boot","customAlias":"spring-boot-repo","description":"Spring Boot Official Repository"}'
```

**Example 2: YouTube Video**
```powershell
curl -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.youtube.com/watch?v=dQw4w9WgXcQ","customAlias":"youtube-tutorial","description":"Popular YouTube Tutorial"}'
```

**Example 3: Documentation**
```powershell
curl -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://docs.oracle.com/javase/17/docs/api","customAlias":"java-17-docs","description":"Java 17 API Documentation"}'
```

**Example 4: Auto-Generated Alias**
```powershell
curl -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://www.python.org/downloads","description":"Python Downloads Page"}'
```

### Create 5 Sample URLs at Once (Bulk)

```powershell
curl -X POST http://localhost:8080/api/urls/bulk `
  -H "Content-Type: application/json" `
  -d '[
    {"originalUrl":"https://github.com/spring-projects/spring-boot","customAlias":"spring-boot-repo","description":"Spring Boot Repository"},
    {"originalUrl":"https://www.youtube.com/watch?v=dQw4w9WgXcQ","customAlias":"youtube-tutorial","description":"YouTube Tutorial"},
    {"originalUrl":"https://docs.oracle.com/javase/17/docs/api","customAlias":"java-17-docs","description":"Java 17 Docs"},
    {"originalUrl":"https://www.python.org/downloads","customAlias":"python-downloads","description":"Python Downloads"},
    {"originalUrl":"https://developer.mozilla.org/en-US/docs/Web/JavaScript","customAlias":"mdn-javascript","description":"MDN JavaScript"}
  ]'
```

### Test Other Endpoints

**Search URLs**
```powershell
curl "http://localhost:8080/api/urls/search?term=java"
```

**View Analytics**
```powershell
curl "http://localhost:8080/api/urls/analytics/top?limit=5"
```

**Import Sample Data (12 pre-loaded URLs)**
```powershell
./import-sample-data.ps1
```

---

## 🗄️ H2 Database

The application uses an **embedded H2 file-based database** (data persists between restarts).

| Setting | Value |
|---------|-------|
| Console URL | http://localhost:8080/api/h2-console |
| JDBC URL | `jdbc:h2:file:./data/urlshortenerdb` |
| Data Location | `./data/urlshortenerdb.mv.db` |
| Username | `sa` |
| Password | *(empty)* |

### Data Persistence
- Data is now saved to disk in the `./data/` directory
- Database files persist between application restarts
- To reset the database, delete the `./data/` folder and restart the app

