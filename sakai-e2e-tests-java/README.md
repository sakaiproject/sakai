# Sakai Playwright Java E2E

Java/JUnit Playwright tests for Sakai UI flows.

## Run locally

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PLAYWRIGHT_BASE_URL=https://sakai.example
mvn -f sakai-e2e-tests-java/pom.xml test
```

Run a single class:

```bash
mvn -f sakai-e2e-tests-java/pom.xml -Dtest=AssignmentTest test
```

## Parallel runs (class-level)

Use `3` workers when demo instructors are available (`instructor,instructor1,instructor2`):

```bash
export PLAYWRIGHT_INSTRUCTOR_POOL=instructor,instructor1,instructor2
mvn -f sakai-e2e-tests-java/pom.xml \
  -Djunit.jupiter.execution.parallel.enabled=true \
  -Djunit.jupiter.execution.parallel.mode.default=same_thread \
  -Djunit.jupiter.execution.parallel.mode.classes.default=concurrent \
  -Djunit.jupiter.execution.parallel.config.strategy=fixed \
  -Djunit.jupiter.execution.parallel.config.fixed.parallelism=3 \
  test
```

## Test helpers

Shared helpers live in `src/test/java/org/sakaiproject/e2e/support`:

- `SakaiUiTestBase`: browser/context lifecycle + trace/video/screenshot artifacts.
- `SakaiHelper`: login/navigation/tool actions/site creation/date selection.
- `SakaiEnvironment`: base URL, browser/headless flags, instructor isolation.

## Adding tests

- Add new `*Test.java` under `src/test/java/org/sakaiproject/e2e/tests`.
- Extend `SakaiUiTestBase`.
- Use `sakai.login(...)`, `sakai.createCourse(...)`, `sakai.toolClick(...)` instead of duplicating flow code.
- Prefer stable selectors; add `data-*` hooks in server templates when UI selectors are ambiguous.
