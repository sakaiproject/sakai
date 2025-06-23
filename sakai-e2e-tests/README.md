# Sakai E2E Tests

End-to-end tests for Sakai LMS using Playwright.

## Setup

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 16+ (for Playwright)

### Installation

1. Install Playwright browsers:
```bash
cd sakai-e2e-tests
npm install
npm run install-browsers
```

2. Or use Maven to install browsers:
```bash
mvn test-compile
```

## Configuration

Configure test environment in `src/test/resources/test.properties` or via system properties:

```properties
sakai.baseUrl=http://localhost:8080
sakai.testUser=admin
sakai.testPassword=admin
```

## Running Tests

### Using Maven
```bash
# Run all E2E tests
mvn test -Pe2e-tests

# Run with custom configuration
mvn test -Pe2e-tests -Dsakai.baseUrl=http://localhost:8080 -Dsakai.testUser=admin

# Run specific test
mvn test -Pe2e-tests -Dtest=LoginTest
```

### Using Playwright directly
```bash
cd sakai-e2e-tests
npx playwright test

# Run in headed mode (see browser)
npx playwright test --headed

# Run with UI
npx playwright test --ui

# Debug mode
npx playwright test --debug
```

## Test Structure

```
sakai-e2e-tests/
├── src/test/java/          # Java test utilities
├── src/test/resources/     # Test configuration
├── tests/                  # Playwright test files
│   └── login.spec.js      # Login functionality tests
├── playwright.config.js   # Playwright configuration
└── package.json           # Node.js dependencies
```

## Available Tests

All tests have been converted from the original Cypress test suite:

### Login Tests (`tests/login.spec.js`)
- Login form display and validation
- Invalid/valid credentials handling
- API-based login bypassing UI
- Form accessibility and keyboard navigation

### Announcements Tests (`tests/announcements.spec.js`)
- Course creation with announcements tool
- Future, past, and current announcement creation
- Date-based announcement visibility
- Student vs instructor announcement access

### Assignments Tests (`tests/assignments.spec.js`)
- Letter grade and points-based assignments
- Assignment creation with rubrics
- Student submissions (desktop and mobile)
- Assignment grading and resubmission
- Non-electronic assignments

### Gradebook Tests (`tests/gradebook.spec.js`)
- Gradebook category creation and management
- Gradebook item creation with categories
- Category weighting and grading

### Accessibility Tests (`tests/accessibility.spec.js`)
- Jump-to-content functionality
- ARIA attributes and landmarks
- Form label accessibility
- Image alt text validation
- Heading hierarchy

### Commons Tests (`tests/commons.spec.js`)
- Commons post creation
- Student and instructor post interactions

### Lessons Tests (`tests/lessons.spec.js`)
- Lesson item creation
- Checklist functionality

### Become User Tests (`tests/become-user.spec.js`)
- Admin user impersonation functionality

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `BASE_URL` | Sakai base URL | `http://localhost:8080` |
| `SAKAI_TEST_USER` | Test username | `admin` |
| `SAKAI_TEST_PASSWORD` | Test password | `admin` |

## Maven Profiles

### `e2e-tests`
Runs end-to-end tests with default configuration:
```bash
mvn test -Pe2e-tests
```

## Troubleshooting

### Browser Installation Issues
```bash
# Install browsers manually
npx playwright install chromium

# Check browser installation
npx playwright --version
```

### Test Failures
- Check that Sakai is running on configured URL
- Verify test credentials are correct
- Review test output and screenshots in `test-results/`

### Debugging
```bash
# Run single test in debug mode
npx playwright test login.spec.js --debug

# Run with trace viewer
npx playwright test --trace on
npx playwright show-trace trace.zip
```

## CI/CD Integration

Tests can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Run E2E Tests
  run: |
    cd sakai-e2e-tests
    npm install
    npx playwright install chromium
    npx playwright test
```

## Contributing

When adding new tests:
1. Follow existing test structure
2. Use descriptive test names
3. Add appropriate assertions
4. Include error handling
5. Update this README if needed