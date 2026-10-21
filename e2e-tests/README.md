# Sakai Playwright Java E2E

Java/JUnit Playwright tests for Sakai UI flows.

#### Run these commands from the repository root.

### Developers can run tests with:
```bash
mvn -P e2e test
```

### Run tests against a running Sakai instance:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PLAYWRIGHT_BASE_URL=https://sakai.example
mvn -P e2e test
```

### Run headed (watch browser) against a running Sakai instance:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PLAYWRIGHT_BASE_URL=https://sakai.example
export PLAYWRIGHT_HEADLESS=false
mvn -P e2e test
```

### Run a single class:

```bash
mvn -P e2e -Dtest=AssignmentTest test
```

### Run a single test method:

```bash
mvn -P e2e -Dtest=AssignmentTest#canCreatePointsAssignment test
```

### Optional browser override:

```bash
export PLAYWRIGHT_BROWSER=chromium  # default, or firefox/webkit
```

Artifacts (trace/video/final screenshot) are written to `target/playwright-artifacts/`.

## Test helpers

Shared helpers live in `src/test/java/org/sakaiproject/e2e/support`:

- `SakaiUiTestBase`: browser/context lifecycle + trace/video/screenshot artifacts.
- `SakaiHelper`: login/navigation/tool actions/site creation/date selection.
- `SakaiEnvironment`: base URL, browser/headless flags.

## Adding tests

- Add new `*Test.java` under `src/test/java/org/sakaiproject/e2e/tests`.
- Extend `SakaiUiTestBase`.
- Use `sakai.login(...)`, `sakai.createCourse(...)`, `sakai.toolClick(...)` instead of duplicating flow code.
- Prefer stable selectors; add `data-*` hooks in server templates when UI selectors are ambiguous.
