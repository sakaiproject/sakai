# Sakai Playwright Java E2E

Java/JUnit Playwright tests for Sakai UI flows.

#### Run these commands from the repository root.

### Developers can run tests with:
```bash
mvn -Pe2e -pl e2e-tests test
```

### Run tests against a running Sakai instance:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PLAYWRIGHT_BASE_URL=https://sakai.example
mvn -Pe2e -pl e2e-tests test
```

### Run headed (watch browser) against a running Sakai instance:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PLAYWRIGHT_BASE_URL=https://sakai.example
export PLAYWRIGHT_HEADLESS=false
mvn -Pe2e -pl e2e-tests test
```

### Run a single class:

```bash
mvn -Pe2e -pl e2e-tests -Dtest=LoginTest test
```

### Run a single test method:

```bash
mvn -Pe2e -pl e2e-tests -Dtest=LoginTest#redirectsToPortalOnSuccess test
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

## GitHub Actions on 25.x

`.github/workflows/playwright.yml` is adapted from master's Playwright workflow.
It runs for pull requests, pushes to `25.x` or `codex-25.x-security`, and manual dispatch.
The runner builds the checked-out commit with Java 17, Node 22 and Tomcat 9,
creates a disposable MySQL database, and runs this module's Java/JUnit Playwright
suite with matching Chromium binaries. The ordinary Maven workflow is unchanged.
The obsolete Cypress workflow has been removed.

CI uses `ci/sakai.properties` and demo users (`instructor1` / `sakai`,
`admin` / `admin`). Server URL and browser URL both use `http://localhost:8080`;
the cookie name is `SAKAIID`. Tomcat's Java-module and scanning configuration is
reused from the pinned `nightly-config` revision in the workflow. Maven artifacts
use a separate 25.x cache. Server readiness and fatal errors are checked, and
Tomcat logs, Surefire reports, traces, videos and screenshots are uploaded.

The two `SamigoGroupAccessTest` methods remain optional: they require an existing
site supplied through `-Dsamigo.groupAccess.siteUrl=...`. An ordinary CI run does
not provision this fixture and skips these two methods; it does not claim Samigo
TA group-access acceptance coverage. To run them against a prepared instance,
create a course with instructor `instructor1`, TA `ta1` belonging to only their
own release group, and assessments named `SAK-52287 Site`, `SAK-52287 Both Groups`,
`SAK-52287 Other Group`, `SAK-52287 Own Group`, and `SAK-52287 Empty Group`.
The fixture must contain the submission/in-progress counts asserted in that
class. Pass the site URL and, if needed, override `samigo.groupAccess.instructor`
and `samigo.groupAccess.ta` on the Maven command line. The other test classes
create their own sites using the demo users.

The workflow starts Tomcat only on the disposable GitHub runner. Local runs
require an instance already started by the developer.
