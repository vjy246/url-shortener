# Testing Documentation

Complete testing guide for URL Shortener Service.

## Documents in This Folder

### Core Testing Guides
- **TESTING_GUIDE.md** - Comprehensive testing manual with all testing approaches
- **TESTING_QUICK_REFERENCE.md** - Quick reference card with essential commands
- **TESTING_SCENARIOS.md** - Step-by-step test scenarios with examples

### Test Coverage & Summary
- **TESTING_IMPLEMENTATION_SUMMARY.md** - Summary of all test files and coverage
- **TEST_COVERAGE_SUMMARY.md** - Coverage matrix and statistics

---

## Quick Start

**Run All Tests:**
```bash
./mvnw test
```

**Run Specific Test Class:**
```bash
./mvnw test -Dtest=ShortUrlControllerTest
```

**Generate Coverage Report:**
```bash
./mvnw clean test jacoco:report
# Open: target/site/jacoco/index.html
```

---

## Test Statistics

- **Total Test Methods:** 70+
- **Unit Tests:** 57+
- **Integration Tests:** 13
- **Code Coverage:** 95%+
- **Execution Time:** 15-20 seconds

---

## For More Details

See specific documents in this folder or main INDEX.md for role-based guidance.


