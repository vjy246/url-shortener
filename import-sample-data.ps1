#!/usr/bin/env pwsh
# URL Shortener - Sample Data Importer Script
# This script creates 12 sample URLs for testing

$BaseUrl = "http://localhost:8080"

# Color codes for output
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warn { Write-Host $args -ForegroundColor Yellow }

Write-Info "╔════════════════════════════════════════════════════╗"
Write-Info "║   URL Shortener - Sample Data Importer             ║"
Write-Info "╚════════════════════════════════════════════════════╝"
Write-Info ""

# Check if server is running
Write-Warn "📡 Checking connection to $BaseUrl..."
try {
    $null = curl -X GET "$BaseUrl/health" -S -o $null
    Write-Success "✓ Server is running!"
}
catch {
    Write-Error "✗ Cannot connect to server at $BaseUrl"
    Write-Error "  Please start the application with: mvnw spring-boot:run"
    exit 1
}

Write-Info ""

# Sample data array
$sampleData = @(
    @{
        originalUrl = "https://github.com/spring-projects/spring-boot"
        customAlias = "spring-boot-repo"
        description = "Spring Boot - Official Repository"
        category = "Programming"
    },
    @{
        originalUrl = "https://www.python.org/downloads"
        customAlias = "python-downloads"
        description = "Python Official Downloads"
        category = "Programming"
    },
    @{
        originalUrl = "https://docs.oracle.com/javase/17/docs/api"
        customAlias = "java-17-docs"
        description = "Java 17 API Documentation"
        category = "Programming"
    },
    @{
        originalUrl = "https://developer.mozilla.org/en-US/docs/Web/JavaScript"
        customAlias = "mdn-javascript"
        description = "MDN JavaScript Documentation"
        category = "Programming"
    },
    @{
        originalUrl = "https://www.linkedin.com/in/jane-developer-123456"
        customAlias = "linkedin-jane"
        description = "Jane Developer's LinkedIn Profile"
        category = "Social Media"
    },
    @{
        originalUrl = "https://twitter.com/techgirl_2024"
        customAlias = "twitter-techgirl"
        description = "TechGirl Twitter Account"
        category = "Social Media"
    },
    @{
        originalUrl = "https://medium.com/@author/how-to-build-microservices-2024"
        customAlias = "microservices-guide"
        description = "How to Build Microservices - Medium Article"
        category = "Articles"
    },
    @{
        originalUrl = "https://www.amazon.com/ASUS-Gaming-Laptop-RTX4060-Backlit/dp/B0CX5KN8NC"
        customAlias = "asus-gaming-laptop"
        description = "ASUS Gaming Laptop on Amazon"
        category = "E-commerce"
    },
    @{
        originalUrl = "https://www.udemy.com/course/the-complete-java-development-bootcamp"
        customAlias = "java-bootcamp-udemy"
        description = "Complete Java Development Bootcamp"
        category = "E-commerce"
    },
    @{
        originalUrl = "https://www.techcrunch.com/2024/01/15/ai-startup-raises-100-million"
        customAlias = "ai-startup-funding"
        description = "TechCrunch - AI Startup Funding News"
        category = "News"
    },
    @{
        originalUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        customAlias = "youtube-tutorial"
        description = "Popular YouTube Tutorial"
        category = "Videos"
    },
    @{
        originalUrl = "https://stackoverflow.com/questions/12345678/how-to-parse-json-in-java"
        customAlias = "so-json-parsing"
        description = "Stack Overflow - JSON Parsing in Java"
        category = "Code Sharing"
    }
)

Write-Info "📋 Total sample data: $($sampleData.Count) URLs"
Write-Info ""

# Import data
Write-Warn "📤 Importing sample data..."
Write-Warn ""

$successCount = 0
$failureCount = 0
$importedUrls = @()

foreach ($item in $sampleData) {
    try {
        # Convert to JSON
        $json = @{
            originalUrl = $item.originalUrl
            customAlias = $item.customAlias
            description = $item.description
        } | ConvertTo-Json

        # Send request
        $response = curl -X POST "$BaseUrl/urls" `
            -H "Content-Type: application/json" `
            -d $json -s

        # Parse response
        $parsedResponse = $response | ConvertFrom-Json -ErrorAction Stop

        $successCount++
        $importedUrls += $parsedResponse

        Write-Success "  ✓ [$successCount] $($item.customAlias) ($($item.category))"
        Write-Warn "     URL: $($item.originalUrl)"
    }
    catch {
        $failureCount++
        Write-Error "  ✗ [$failureCount] $($item.customAlias) - Failed"
        Write-Error "     Error: $_"
    }

    Start-Sleep -Milliseconds 50  # Small delay between requests
}

Write-Info ""
Write-Info "════════════════════════════════════════════════════"
Write-Success "✓ Import Complete!"
Write-Info "════════════════════════════════════════════════════"
Write-Success "  Success: $successCount"
if ($failureCount -gt 0) { Write-Error "  Failed: $failureCount" }
Write-Info ""

# Verify data
Write-Warn "📊 Verifying imported data..."
try {
    $allUrls = curl -X GET "$BaseUrl/urls" -s | ConvertFrom-Json
    Write-Success "✓ Total URLs in database: $($allUrls.Count)"
    Write-Info ""

    Write-Info "📋 Imported URLs:"
    $importedUrls | ForEach-Object {
        Write-Info "   • $($_.customAlias) -> $($_.shortenedUrl)"
    }
}
catch {
    Write-Error "✗ Failed to retrieve URLs from database"
}

Write-Info ""
Write-Info "════════════════════════════════════════════════════"
Write-Info "🎉 Sample data import successful!"
Write-Info "════════════════════════════════════════════════════"
Write-Info ""
Write-Info "Next steps:"
Write-Warn "  1. View in Swagger UI"
Write-Warn "     http://localhost:8080/swagger-ui.html"
Write-Warn ""
Write-Warn "  2. Test GET endpoint"
Write-Warn "     curl http://localhost:8080/urls"
Write-Warn ""
Write-Warn "  3. Test SEARCH endpoint"
Write-Warn "     curl 'http://localhost:8080/urls/search?term=java'"
Write-Warn ""
Write-Warn "  4. Test ANALYTICS endpoint"
Write-Warn "     curl 'http://localhost:8080/urls/analytics/top?limit=10'"
Write-Warn ""
Write-Warn "  5. Test REDIRECT endpoint"
Write-Warn "     curl -L 'http://localhost:8080/urls/redirect/spring-boot-repo'"
Write-Info ""

