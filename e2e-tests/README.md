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

## Samigo group-access regression (SAK-52287)

`SamigoGroupAccessTest` uses a prepared site because the default demo course does
not provide a TA, controlled group membership, or submissions in distinct states.
It is opt-in and does not change memberships, permissions, or grades.

Prepare an English-language site with Tests & Quizzes and these members:

- An instructor with `assessment.all.groups`, also a member of Group A.
- A TA with grading permission but without `assessment.all.groups`, in Group A
  and an empty Group C, but not Group B.
- Two students in Group A and two different students in Group B.

Publish these assessments, with availability dates that keep them active:

| Exact title | Release to | Student activity |
| --- | --- | --- |
| SAK-52287 Site | Entire site | One submission and one in-progress attempt per group |
| SAK-52287 Both Groups | A and B | One submission and one in-progress attempt per group |
| SAK-52287 Own Group | A | One submission and one in-progress attempt |
| SAK-52287 Other Group | B | One submission and one in-progress attempt |
| SAK-52287 Empty Group | C | None |

Use the demo-account password configured by `SakaiHelper`. Run against a server
with the fix deployed:

```bash
PLAYWRIGHT_BASE_URL=https://sakai.example mvn -P e2e test \
  -Dtest=SamigoGroupAccessTest \
  -Dsamigo.groupAccess.siteUrl=https://sakai.example/portal/site/SITE_ID \
  -Dsamigo.groupAccess.instructor=instructor1 \
  -Dsamigo.groupAccess.ta=ta1
```

The tests check instructor access, hidden unrelated group quizzes, both displayed
counts for site and group releases, empty-group visibility, and a TA page reload.

## Duplicate-site assignment regression (SAK-52920)

On a deployed server with `site.setup.allowDuplicateSite=true` and the default
assignment import-as-draft configuration, run:

```bash
PLAYWRIGHT_BASE_URL=https://sakai.example mvn -Pe2e -pl e2e-tests \
  -Dsakai.test.duplicateSite=true \
  '-Dtest=AssignmentImportAnnouncementTest#duplicateSiteImportsPublishedAssignmentAsDraft' test
```

The test creates a published assignment as an instructor, duplicates the site
as admin with a known destination ID, and checks that the imported assignment
is a draft while the source remains published.
