# Sample Data Documentation

Ready-to-use sample data and automation for testing the URL Shortener Service.

## Documents in This Folder

### Sample Data Files
- **SAMPLE_DATA.md** - Complete sample data catalog with 12+ pre-built URLs
- **SAMPLE_DATA_SETUP.md** - Setup guide with multiple import methods
- **SAMPLE_DATA_QUICK_START.md** - 3-step quick start guide
- **SAMPLE_DATA_QUICK_COMMANDS.md** - Copy & paste curl commands for all operations

### Automation Files
- **import-sample-data.ps1** - PowerShell script to auto-import all sample data
- **postman-collection-sample-data.json** - Postman collection with pre-configured requests

---

## Quick Start (3 Steps)

### 1. Start Application
```bash
./mvnw spring-boot:run
```

### 2. Import Sample Data
```powershell
# Using PowerShell (Windows)
.\import-sample-data.ps1

# OR using curl (all platforms)
curl -X POST http://localhost:8080/api/urls/bulk \
  -H "Content-Type: application/json" \
  -d @sample-data.json
```

### 3. Test with Sample Data
```bash
# Get all URLs
curl http://localhost:8080/api/urls

# Search
curl "http://localhost:8080/api/urls/search?term=spring"

# View analytics
curl "http://localhost:8080/api/urls/analytics/top?limit=5"
```

---

## Sample Data Categories

The sample data includes URLs from:
- ✅ Programming (GitHub, Stack Overflow, Java Docs)
- ✅ Video (YouTube tutorials)
- ✅ Documentation (Official docs)
- ✅ News & Media
- ✅ Social Networks
- ✅ E-commerce

---

## Import Methods

1. **PowerShell Automation** (Easiest)
   - Automated setup with verification
   - Color-coded output
   - One command: `.\import-sample-data.ps1`

2. **Postman Collection** (Interactive UI)
   - Pre-configured requests
   - Visual testing interface
   - Import JSON file into Postman

3. **Curl Commands** (Manual/Scripting)
   - Full control over each request
   - Easy to integrate into CI/CD
   - Platform-independent

4. **SQL Inserts** (Direct DB)
   - Direct database population
   - For advanced users
   - See SAMPLE_DATA.md for SQL

---

## For More Details

- Quick start: See **SAMPLE_DATA_QUICK_START.md**
- Detailed setup: See **SAMPLE_DATA_SETUP.md**
- All curl commands: See **SAMPLE_DATA_QUICK_COMMANDS.md**
- Complete documentation: See **SAMPLE_DATA.md**


