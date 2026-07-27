# URL Shortener - Sample Data Setup Guide

## 📦 What I've Created For You

I've prepared **4 new files** with sample data and automation to get you testing immediately:

### 1. **`SAMPLE_DATA.md`** - Complete Documentation
   - 12 categorized sample URLs (Programming, Social Media, E-commerce, etc.)
   - Ready-to-use JSON payloads
   - PowerShell import script
   - SQL INSERT statements
   - Postman setup guide

### 2. **`import-sample-data.ps1`** - Automated PowerShell Script
   - Imports all 12 sample URLs automatically
   - Color-coded output with status messages
   - Verifies server connection
   - Shows import results and statistics
   - **Just run it:** `./import-sample-data.ps1`

### 3. **`postman-collection-sample-data.json`** - Postman Collection
   - Pre-configured requests with sample data
   - All CRUD operations ready to use
   - Search and analytics endpoints
   - Import into Postman directly

### 4. **`SAMPLE_DATA_QUICK_COMMANDS.md`** - Quick Reference
   - All curl commands ready to copy & paste
   - Organized by operation (CREATE, GET, REDIRECT, etc.)
   - Complete test workflows
   - Bulk click simulator for testing analytics

---

## 🚀 Quick Start (3 Steps)

### Step 1: Start Your Application
```powershell
cd C:\Documents\H1B\PersistenceSystems\IntelliJSourceCode
mvnw spring-boot:run
```
**Wait for:** `"Started UrlShortenerServiceApplication in X seconds"`

### Step 2: Import Sample Data
```powershell
# Open new PowerShell window in the same directory
.\import-sample-data.ps1
```

**You'll see:**
```
✓ [1] spring-boot-repo
✓ [2] python-downloads
✓ [3] java-17-docs
... (12 total)
```

### Step 3: Test It
```powershell
# Verify data was imported
curl http://localhost:8080/urls

# Get specific URL
curl http://localhost:8080/urls/spring-boot-repo

# Test analytics
curl "http://localhost:8080/urls/analytics/top?limit=5"
```

**Done!** You now have 12 sample URLs ready to test! 🎉

---

## 📋 Sample Data Categories

| Category | Count | Aliases |
|----------|-------|---------|
| **Programming** | 4 | spring-boot-repo, python-downloads, java-17-docs, mdn-javascript |
| **Social Media** | 2 | linkedin-jane, twitter-techgirl |
| **Articles** | 1 | microservices-guide |
| **E-commerce** | 2 | asus-gaming-laptop, java-bootcamp-udemy |
| **News** | 1 | ai-startup-funding |
| **Videos** | 1 | youtube-tutorial |
| **Code Sharing** | 1 | so-json-parsing |
| **TOTAL** | **12** | **Ready to use!** |

---

## 🎯 What You Can Do With Sample Data

### 1. Test CREATE Operations
```powershell
# The sample data includes 12 pre-configured URLs to create
./import-sample-data.ps1
```

### 2. Test GET Operations
```powershell
# Get any sample URL
curl http://localhost:8080/urls/spring-boot-repo
curl http://localhost:8080/urls/python-downloads
curl http://localhost:8080/urls/java-17-docs
```

### 3. Test SEARCH
```powershell
# Search for Java-related URLs (returns java-17-docs and java-bootcamp-udemy)
curl "http://localhost:8080/urls/search?term=java"

# Search for tutorial
curl "http://localhost:8080/urls/search?term=tutorial"
```

### 4. Test ANALYTICS (Click Tracking)
```powershell
# Get top clicked URLs
curl "http://localhost:8080/urls/analytics/top?limit=5"

# Simulate clicks by redirecting multiple times
for ($i=0; $i -lt 10; $i++) {
  curl -L "http://localhost:8080/urls/redirect/spring-boot-repo"
}

# Check updated click count
curl "http://localhost:8080/urls/analytics/top?limit=5"
```

### 5. Test UPDATE
```powershell
# Update a URL (using ID from response)
curl -X PUT http://localhost:8080/urls/1 `
  -H "Content-Type: application/json" `
  -d '{"description":"Updated Description"}'
```

### 6. Test DELETE
```powershell
# Delete a URL
curl -X DELETE http://localhost:8080/urls/1
```

---

## 📂 File Locations

```
C:\Documents\H1B\PersistenceSystems\IntelliJSourceCode\
├── SAMPLE_DATA.md                          ← Full documentation
├── SAMPLE_DATA_QUICK_COMMANDS.md          ← Copy-paste curl commands
├── import-sample-data.ps1                 ← Run this script (⭐ EASIEST)
└── postman-collection-sample-data.json    ← Import into Postman
```

---

## 🔥 Three Ways to Load Sample Data

### Method 1: PowerShell Script (Easiest) ⭐⭐⭐
```powershell
./import-sample-data.ps1
```
- **Pros:** Color-coded output, verification, user-friendly
- **Time:** ~10 seconds
- **Best for:** Quick testing, first-time setup

### Method 2: Manual Curl Commands
Copy from `SAMPLE_DATA_QUICK_COMMANDS.md` and paste curl commands
- **Pros:** Fine-grained control, see each request
- **Time:** ~2-3 minutes
- **Best for:** Understanding the API

### Method 3: Direct Database SQL
Open H2 console and run SQL INSERT statements
- **Pros:** Direct database manipulation
- **Time:** ~1 minute
- **Best for:** Advanced users, batch operations

---

## 📊 Sample Data Distribution

```
Programming Sites (4) ━ Spring Boot, Python, Java, MDN
Social Media (2) ━ LinkedIn, Twitter  
E-commerce (2) ━ Amazon, Udemy
Entertainment (1) ━ YouTube
News/Articles (2) ━ TechCrunch, Medium
Code Sharing (1) ━ Stack Overflow
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
TOTAL: 12 URLs (Realistic mix for testing)
```

---

## ✅ Complete Test Flow

### 1. Prerequisites ✓
- [ ] Application running: `mvnw spring-boot:run`
- [ ] PowerShell window open in project directory

### 2. Load Sample Data ✓
- [ ] Run: `./import-sample-data.ps1`
- [ ] See: "✓ Import Complete!" message

### 3. Verify Import ✓
```powershell
curl http://localhost:8080/urls
```
- [ ] Should see 12 URLs returned

### 4. Run Unit Tests ✓
```powershell
mvnw test
```
- [ ] All tests should pass

### 5. Test with Swagger UI ✓
```
http://localhost:8080/swagger-ui.html
```
- [ ] Try creating/reading/updating/deleting
- [ ] Search and analytics endpoints

### 6. Test Scenarios ✓
- [ ] Create new URL
- [ ] Search by term
- [ ] Track clicks via redirect
- [ ] View analytics
- [ ] Update and delete

---

## 🎓 Learning Path

**Beginner:**
1. Run `./import-sample-data.ps1`
2. View in Swagger UI
3. Explore endpoints with cURL

**Intermediate:**
1. Manually create a URL
2. Update it
3. Delete it
4. Track clicks

**Advanced:**
1. Bulk create multiple URLs
2. Simulate click patterns
3. Run search queries
4. Export to Postman
5. Automate with PowerShell loops

---

## 💡 Pro Tips

### Tip 1: View Database Console
```
http://localhost:8080/h2-console
```
- See all imported URLs in the database
- Run custom SQL queries
- Check click counts

### Tip 2: Use Swagger UI for Testing
```
http://localhost:8080/swagger-ui.html
```
- Try every endpoint with pre-filled sample data
- See actual responses
- No curl knowledge needed

### Tip 3: Chain Commands
```powershell
# Create, redirect, view all in one go
curl -X POST http://localhost:8080/urls -d '...' | ConvertFrom-Json
curl -L "http://localhost:8080/urls/redirect/alias"
curl "http://localhost:8080/urls/analytics/top?limit=5"
```

### Tip 4: Format JSON Output
```powershell
curl http://localhost:8080/urls | ConvertFrom-Json | ConvertTo-Json | Write-Host
```

### Tip 5: Batch Operations
```powershell
# Click simulation
for ($i=0; $i -lt 100; $i++) {
  curl -L "http://localhost:8080/urls/redirect/spring-boot-repo" -o $null
}
```

---

## 🐛 Troubleshooting

### Problem: "Connection refused"
**Solution:** Make sure app is running
```powershell
mvnw spring-boot:run
```

### Problem: Script won't run
**Solution:** Set execution policy
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Problem: Port already in use
**Solution:** Kill the process
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Problem: Sample data not showing
**Solution:** Verify import succeeded
```powershell
curl http://localhost:8080/urls
```

---

## 📖 Documentation Reference

| Document | Purpose |
|----------|---------|
| **SAMPLE_DATA.md** | Complete sample data guide |
| **SAMPLE_DATA_QUICK_COMMANDS.md** | All curl commands ready to copy |
| **import-sample-data.ps1** | Automated import script |
| **postman-collection-sample-data.json** | Postman collection |
| **TESTING_GUIDE.md** | Complete testing guide |
| **TESTING_QUICK_REFERENCE.md** | Testing commands quick ref |

---

## 🚀 Next Steps

### Immediate (Now)
1. ✅ Start application: `mvnw spring-boot:run`
2. ✅ Import data: `./import-sample-data.ps1`
3. ✅ Verify: `curl http://localhost:8080/urls`

### Short Term (Next 10 minutes)
1. ✅ Test Swagger UI: http://localhost:8080/swagger-ui.html
2. ✅ Try some curl commands from SAMPLE_DATA_QUICK_COMMANDS.md
3. ✅ Search for specific URLs
4. ✅ View analytics

### Later (Testing)
1. ✅ Run unit tests: `mvnw test`
2. ✅ Run integration tests
3. ✅ Test all CRUD operations
4. ✅ Verify error handling

---

## 🎉 You're All Set!

Your URL Shortener is ready to test with:

- ✅ **12 sample URLs** across multiple categories
- ✅ **Automated import script** for quick setup
- ✅ **Ready-to-use curl commands** for manual testing
- ✅ **Postman collection** for GUI testing
- ✅ **Complete documentation** for reference

**Time to first test:** ~5 minutes! ⏱️

---

## Summary

| Item | Location | How to Use |
|------|----------|-----------|
| Sample Data Docs | `SAMPLE_DATA.md` | Read for full details |
| Quick Commands | `SAMPLE_DATA_QUICK_COMMANDS.md` | Copy & paste from here |
| Auto Import | `import-sample-data.ps1` | Run this script |
| Postman Setup | `postman-collection-sample-data.json` | Import into Postman |

**Start here:** `./import-sample-data.ps1` 🚀


