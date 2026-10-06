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


### SAK-52130 cancellation category fixtures

`SamigoCancellationCategoryTest` requires five **fresh, isolated** sites owned by
`instructor1`, with Tests & Quizzes and Gradebook, and `student0011` enrolled.
It fails clearly when required fixture properties are missing; it never skips.
Use English Sakai account/site language for these fixtures. Other enrolled
students may remain ungraded; assertions target `student0011`.

In every site create three published default-export quizzes named
`SAK-52130 TQ-1`, `SAK-52130 TQ-2`, and `SAK-52130 TQ-3`. Each has three fixed
1-point true/false questions, with no EMI or random pool questions. Submit as
`student0011`: TQ-1 earns 2/3 with question 1 answered correctly, TQ-2 earns
1/3, and TQ-3 earns 3/3. Export all three to the site's only category, named
`Quizzes`. Enable instructor Gradebook percentage display. There must be no
other graded items, overrides, or extra-credit items. Allow editing published
assessments with submissions. Verify every quiz starts with 3 possible points.

| Required JVM property | Initial category settings | Flow |
| --- | --- | --- |
| `samigo.cancellation.restrictedSiteUrl` | Keep Highest 2; unequal weighting | Evaluation and authoring disable reduction; dismissal preserves 83.33% and 3 possible points |
| `samigo.cancellation.redistributionSiteUrl` | Keep Highest 2; unequal weighting | Destructive redistribution; question 1 becomes 0, others become 1.5, Gradebook retains 3 possible points |
| `samigo.cancellation.equalWeightSiteUrl` | Keep Highest 2; unequal weighting | Test enables equal weighting, then reduction reaches 2 possible points |
| `samigo.cancellation.ordinarySiteUrl` | No Keep/Drop; unequal weighting | Reduction reaches 2 possible points |
| `samigo.cancellation.staleSiteUrl` | No Keep/Drop; unequal weighting | Test opens reduction, enables Keep Highest 2 in a second page, then submits the stale form; both tools retain 3 possible points |

All five values are normal portal site URLs. Optional
`samigo.cancellation.instructor` overrides the instructor login. The four
mutation fixtures must be recreated before each rerun; do not reuse a site
from a previous destructive test. Deploy the changed Gradebook implementation,
Samigo services, and Samigo UI before running:

```sh
PLAYWRIGHT_BASE_URL=https://sakai.example mvn -P e2e -pl e2e-tests test \
  -Dtest=SamigoCancellationCategoryTest \
  -Dsamigo.cancellation.restrictedSiteUrl=<restricted-site-url> \
  -Dsamigo.cancellation.redistributionSiteUrl=<redistribution-site-url> \
  -Dsamigo.cancellation.equalWeightSiteUrl=<equal-weight-site-url> \
  -Dsamigo.cancellation.ordinarySiteUrl=<ordinary-site-url> \
  -Dsamigo.cancellation.staleSiteUrl=<stale-site-url>
```

Redistribution changes earned scores; the test does not promise an unchanged
category percentage after regrading. No browser responses are mocked.
