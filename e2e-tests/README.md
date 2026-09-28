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

### Samigo pool-tag regression (SAK-52380)

On a deployed server with `samigo.author.usetags=true`, give the instructor
`tagservice.manage` in the test site's role. Run the opt-in regression with:

```bash
mvn -Pe2e -pl e2e-tests -Dsakai.test.samigoTags=true \
  '-Dtest=SamigoTest#canLoadAndSavePoolTagsAndFilterFromFreshAssessmentSession' test
```

This verifies tag loading on pool creation/editing, tag persistence, and filtering
when entering Question Pools directly from a new assessment in a fresh session.
It checks the actual tag API URL and HTTP response without mocking requests.

### Assignments by Student TA regression (SAK-51409)

`AssignmentTeachingAssistantTest` uses an opt-in fixture on a server running the fix:

- A course with Assignments, `instructor1` as Instructor, `instructor2` as Teaching
  Assistant, and `student0011` and `student0012` as Students (demo passwords).
- `SAK-51409 TA group` contains `instructor2` and `student0011`;
  `SAK-51409 Other group` contains `student0012`. Use the default course/group permissions.
- Publish two individual, online-text assignments with open dates in the past and
  due/close dates in the future: `SAK-51409 Group assignment`, assigned to the TA
  group, and `SAK-51409 Site assignment`, assigned to the entire site.
- Submit both as `student0011`, enable resubmission, and leave instructor
  submission on behalf enabled. Keep the fixture small enough for one table page.

```bash
mvn -Pe2e -pl e2e-tests -Dtest=AssignmentTeachingAssistantTest \
  -DPLAYWRIGHT_BASE_URL=https://sakai.example \
  -Dsakai.test.assignmentTa.siteUrl=https://sakai.example/portal/site/SITE_ID test
```

The tests check the instructor/TA rosters, group filtering, successful grader
loading, and an enabled submission editor for the TA. They do not save grades or submissions.
