# URL Shortener Service

A Spring Boot REST API service for creating, managing, and tracking shortened URLs with H2 embedded database and Swagger API documentation.

## Features

✅ Create shortened URLs with custom aliases  
✅ Redirect to original URLs with click tracking  
✅ Search and filter shortened URLs  
✅ Analytics - Get top clicked URLs  
✅ H2 Embedded Database (no external DB required)  
✅ Swagger/OpenAPI 3.0 Documentation  
✅ Comprehensive Exception Handling  
✅ Logging with SLF4J  
✅ Unit Tests with JUnit 5 and Mockito  

## Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: H2 Embedded
- **API Documentation**: Springdoc OpenAPI 3.0 (Swagger)
- **Build Tool**: Maven
- **Server**: Embedded Tomcat
- **Testing**: JUnit 5, Mockito
- **Logging**: SLF4J with Logback

## Project Structure

```
url-shortener-service/
├── src/
│   ├── main/
│   │   ├── java/com/urlshortener/
│   │   │   ├── UrlShortenerServiceApplication.java    # Main application class
│   │   │   ├── controller/
│   │   │   │   ├── ShortUrlController.java            # REST endpoints for URL management
│   │   │   │   └── HealthController.java              # Health check endpoints
│   │   │   ├── service/
│   │   │   │   └── ShortUrlService.java               # Business logic
│   │   │   ├── entity/
│   │   │   │   └── ShortUrl.java                      # JPA entity
│   │   │   ├── repository/
│   │   │   │   └── ShortUrlRepository.java            # Data access layer
│   │   │   ├── dto/
│   │   │   │   ├── ShortenUrlRequest.java             # Request DTO
│   │   │   │   └── ShortenUrlResponse.java            # Response DTO
│   │   │   └── exception/
│   │   │       ├── ResourceNotFoundException.java      # Custom exception
│   │   │       ├── DuplicateAliasException.java       # Custom exception
│   │   │       └── GlobalExceptionHandler.java        # Global exception handler
│   │   └── resources/
│   │       └── application.yml                        # Application configuration
│   └── test/
│       └── java/com/urlshortener/service/
│           └── ShortUrlServiceTest.java               # Service unit tests
├── pom.xml                                            # Maven configuration
└── README.md                                          # This file
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Installation & Setup

### 1. Clone or Navigate to the Project

```bash
cd C:\Documents\H1B\PersistenceSystems\IntelliJSourceCode
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or directly run the JAR file after building:

```bash
mvn clean package
java -jar target/url-shortener-service-1.0.0.jar
```

### 4. Access the Application

- **API Base URL**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8080/api/openapi.json`
- **H2 Console**: `http://localhost:8080/api/h2-console`
  - JDBC URL: `jdbc:h2:mem:urlshortenerdb`
  - Username: `sa`
  - Password: (blank)

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Endpoints

#### 1. Health Check
```
GET /health
GET /health/info
```

#### 2. Create Shortened URL
```
POST /urls
Content-Type: application/json

{
  "originalUrl": "https://www.example.com/very/long/url",
  "customAlias": "myalias",
  "description": "My shortened URL"
}

Response (201):
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.example.com/very/long/url",
  "customAlias": "myalias",
  "clickCount": 0,
  "description": "My shortened URL",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "shortenedUrl": "http://localhost:8080/api/s/abc123"
}
```

#### 3. Get Short URL Details
```
GET /urls/{shortCode}

Response (200):
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.example.com/very/long/url",
  "customAlias": "myalias",
  "clickCount": 0,
  "description": "My shortened URL",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "shortenedUrl": "http://localhost:8080/api/s/abc123"
}
```

#### 4. Redirect to Original URL
```
GET /urls/redirect/{shortCode}

Response (302): Redirects to original URL and increments click count
```

#### 5. Get All URLs
```
GET /urls

Response (200): List of all shortened URLs
```

#### 6. Search URLs
```
GET /urls/search?term={searchTerm}

Response (200): List of matching URLs
```

#### 7. Get Top Clicked URLs
```
GET /urls/analytics/top?limit=10

Response (200): List of top 10 clicked URLs
```

#### 8. Update Short URL
```
PUT /urls/{id}
Content-Type: application/json

{
  "originalUrl": "https://new-url.com",
  "description": "Updated description"
}

Response (200): Updated URL details
```

#### 9. Delete Short URL
```
DELETE /urls/{id}

Response (204): No content
```

## Curl Examples

### Create a Shortened URL
```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.spring.io/projects/spring-boot",
    "customAlias": "springboot",
    "description": "Spring Boot Documentation"
  }'
```

### Get Short URL Details
```bash
curl http://localhost:8080/api/urls/abc123
```

### Redirect to Original URL
```bash
curl -L http://localhost:8080/api/urls/redirect/abc123
```

### Get All URLs
```bash
curl http://localhost:8080/api/urls
```

### Search URLs
```bash
curl "http://localhost:8080/api/urls/search?term=spring"
```

### Get Top Clicked URLs
```bash
curl "http://localhost:8080/api/urls/analytics/top?limit=5"
```

### Delete a URL
```bash
curl -X DELETE http://localhost:8080/api/urls/1
```

## Running Tests

```bash
mvn test
```

To run a specific test class:
```bash
mvn test -Dtest=ShortUrlServiceTest
```

## Configuration

Edit `src/main/resources/application.yml` to customize:

- Server port (default: 8080)
- Context path (default: /api)
- H2 database settings
- Logging levels
- Swagger UI path

## Database

The application uses H2 embedded database with automatic schema creation:
- **Driver**: H2
- **URL**: `jdbc:h2:mem:urlshortenerdb`
- **DDL**: Auto-create (create-drop)
- **Console**: Available at `/h2-console`

## Logging

Logging is configured with SLF4J and Logback:
- Log level for application: DEBUG
- Log level for Spring Framework: DEBUG
- Logs include request/response details and service operations

## Error Handling

The application provides comprehensive error handling:

- **400 Bad Request**: Invalid input or validation errors
- **404 Not Found**: Resource not found
- **409 Conflict**: Duplicate custom alias
- **500 Internal Server Error**: Unexpected server errors

All errors return structured JSON responses with:
- Timestamp
- HTTP Status Code
- Error Message
- Request Path
- Validation Details (if applicable)

## Future Enhancements

- [ ] User authentication and authorization
- [ ] Rate limiting
- [ ] QR code generation for shortened URLs
- [ ] Link expiration dates
- [ ] Password protection for links
- [ ] Advanced analytics and reporting
- [ ] Webhook support for events
- [ ] Multi-tenant support
- [ ] Custom domain support
- [ ] API rate limiting with JWT tokens

## License

Apache License 2.0

## Support

For issues or questions, contact: support@urlshortener.com

---

**Last Updated**: 2024-01-15  
**Version**: 1.0.0

