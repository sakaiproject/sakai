# Samigo JSP → Facelets migration

This branch is rebased onto `upstream/master` at `d4792fb3a1d` (2026-10-15).
The Jakarta migration and [cleanup PR #15095](https://github.com/sakaiproject/sakai/pull/15095)
are already upstream; this PR contains the Facelets migration and its verification
harness, without replaying those earlier infrastructure changes.

The current source has **247 public XHTML entry points and 250 internal templates**
under `WEB-INF/samigo-views`, including three JavaScript fragments formerly included
statically by JSP. The built WAR contains 497 XHTML files and no JSP/JSPF files.
Shared fragments remain shared. Public wrappers pass a component-ID prefix and
ordinal offset through scoped `ui:param` values; nested includes retain explicit
IDs and the component order of the corresponding upstream JSP.

Two broken, unused legacy views, `author/previewAssessment.jsp` and
`delivery/reviewAssessment.jsp`, are removed. Current preview/review use the shared
delivery flow; their active action-mode strings remain intact. Upstream also
removed `evaluation/downloadFileSubmissions.jsp`: neither its public Facelets view
nor its internal template is restored. The `downloadFileSubmissions` navigation
outcome now points to `exportResponses.xhtml`, matching upstream.

## Current validation after the rebase

* `mvn -f samigo/pom.xml -B -ntp clean install`: all 11 modules pass, with 280 tests,
  zero failures/errors/skips.
* `mvn -f e2e-tests/pom.xml -B -ntp -DskipTests test-compile`: all 52 browser test
  source files compile against upstream's Playwright version and API fixture code.
* All 20 Python converter tests pass.
* The static source audit checks 31,377 component declarations across all 247
  expanded view/fragment graphs against the current upstream JSP sources. It
  verifies component order, explicit/generated IDs, nested include offsets,
  navigation targets, XML parsing for all 497 files, and absence of obsolete JSPs.
* Regenerated templates retain upstream's new export/download forms, grading and
  cancellation changes, site-aware tag lookups, and Lit pool tag selectors.
  Changed shared includes required offset updates in 28 internal templates.
* Upload bindings follow the current JSP tag contract: `target` is a String value
  expression, `value` is an Object value expression, including literal expressions.

These checks do not execute the JSF lifecycle. **The rebased branch still needs
fresh browser/differential QA before merge.** Sakai and Tomcat were not started or
restarted during this rebase. Use a server started manually by the developer for
runtime checks. Earlier runtime measurements below are historical evidence, not
fresh test results for the rebased source.

## Historical runtime verification (before the 2026-10-15 rebase)

The original experiment combined the Jakarta branch and the then-unmerged cleanup
PR. It had 248 public views, 251 internal templates and 243 passing Samigo tests.
That earlier version compiled all 499 templates in Mojarra and passed paired
component-tree/lifecycle/browser comparisons at eight detailed authoring
checkpoints and 37 broader suite checkpoints, including all 12 question editors.
A missing-listener mutation was detected by the expected browser failure and
missing server validation. QTI download parsing, valid-package import, traversal
rejection and view-access checks also passed on that version.

At that time one Lessons test failed on both JSP and Facelets. Upstream has since
changed the E2E suite; this is not a claim about that test's current outcome.
The earlier Jasper output supplied the retained public ID prefixes. Component
ordinals and include offsets for changed templates have now been regenerated from
current upstream source and statically audited; they have not been compared with
a fresh running Jasper baseline.

The observed runtime contract included IDs/classes, expressions, listener delegate
bindings, converter/validator settings, UIData metadata, facets, lifecycle flags,
messages/navigation, browser controls/options/labels, scripts/styles and page
errors. Reports listed every normalization: fixture identities and origins,
configured JSP-to-XHTML paths, opaque ViewState/CSRF values, whitespace runs in
human form text, unconditional transient literal renderers and listener wrappers.
Control values were otherwise exact. Generated timestamps and tag IDs were
recorded separately; unchanged external-tool snapshots retained their raw
differences and their test outcomes were compared separately.

Neither compilation nor these finite flows prove every possible Samigo state.
Group/TA fixtures, every question type's submission/grading rules, assessment
templates, locales/timezones and timing/retry/concurrency cases need additional
coverage. Rebase-sensitive export, tag and cancellation flows should be checked
again on the current branch.

## Compatibility adapters

The request filter replaces the JSP header prelude. `SamigoViewPhaseListener`
delegates the former entry-point scriptlets to existing listeners before rendering;
`SamigoViewSupport` exposes request values. Neither keeps request state in a
singleton or thread-local field. The custom Facelets handlers preserve Samigo's
JSP tag attribute types, raw writer EL, literal local input values and date
converter defaults. Explicit Sakai locale/timezone bindings remain intact.
Automatic Facelets Bean Validation is disabled because JSP did not attach an
implicit validator to every input. Direct XHTML template access is denied by the
same servlet constraint protecting JSP templates, and normal routes retain the
Samigo permission checks.

Use explicit stable IDs when adding components to these templates. Legacy ordinal
offsets describe the preserved JSP layout; adding/removing components requires
updating affected offsets in containing templates. There is no runtime compiler
or generated-ID registry. Normal JSF view state must be discarded on redeployment
when the component structure changes.

## Server-free checks

From the repository root, preserve the upstream web source for the static audit:

```sh
mkdir -p target/upstream-baseline
git archive upstream/master samigo/samigo-app/src/webapp | tar -x -C target/upstream-baseline
python3 -m unittest discover -s build-tools/samigo-facelets-migration -p 'test_*.py'
python3 build-tools/samigo-facelets-migration/audit.py \
  --source-web target/upstream-baseline/samigo/samigo-app/src/webapp \
  --baseline "$(git rev-parse upstream/master)" \
  --output target/rebase-template-audit.json
mvn -f samigo/pom.xml -B -ntp clean install
mvn -f e2e-tests/pom.xml -B -ntp -DskipTests test-compile
```

`audit.py` checks source declarations and include/ID structure; it does not build
native JSF component trees. `modular.py --source-web ... --jasper ... --output ...`
regenerates templates and records source/Jasper provenance. Retain existing public
prefixes when regenerating for an upstream change, then audit the full include
graph; a changed shared fragment can shift offsets in many containing views.

## Runtime checks and deployment

The opt-in `SamigoViewEquivalenceTest` includes persistence/second-row editing,
all-question-type rendering/cancellation, QTI export and missing-listener checks.
`SamigoImportAccessTest` checks staff/student routing and real package imports.
The test-only probe and compiler in `../samigo-facelets-flow/src/main/java` record
native JSF observations when installed into a disposable test application.
Production descriptors do not include those probe/compiler classes.

The original experiment's `run.py`, `suite.py`, `pilot.py`, retry and production
check scripts contain automatic Tomcat launch/stop routines. They are retained as
historical experiment tooling. **Do not invoke those orchestration scripts under
the current manual-server policy.** Start the application manually and run the
opt-in browser tests against that server; server startup is not part of Maven's
normal module tests or browser test compilation.

`apply.py` is a one-time source conversion tool requiring an exact source/manifest
match and no conversion failures. Use a clean Samigo WAR and remove the old
exploded Samigo application during deployment so obsolete JSPs cannot remain from
an earlier deployment overlay.

Generated `target/` evidence is ignored by Git. The rebase build logs and static
audit report belong to this checkout; earlier ignored runtime artifacts are not
included in the PR and must not be treated as freshly reproducible results.
