# Samigo JSP → Facelets migration

The source migration replaces all 248 remaining JSP/JSPF files after combining
the Jakarta branch with [cleanup PR #15095](https://github.com/sakaiproject/sakai/pull/15095)
at `1f322bab87886c6a0a2d9812cc1aa83c2b6226a6`. The combined baseline is local merge
`d0f18d4b136`. Commit `12f87872edb` removes two broken, unused legacy views:
`author/previewAssessment.jsp` and `delivery/reviewAssessment.jsp`. No callers
of their navigation outcomes remain; current preview and review use the shared
delivery flow. Their action-mode strings remain in use and were preserved.

There are 248 public XHTML entry points and 251 templates under
`WEB-INF/samigo-views`, including three JavaScript fragments formerly included
statically by JSP. Shared fragments remain shared. Public wrappers pass the
containing view's legacy component-ID prefix and ordinal offset through scoped
`ui:param` values; nested includes preserve the original tag order. Explicit IDs,
UIData iteration, facets, bindings and listeners retain their source semantics.

## Verification

The isolated comparison uses deployed Sakai services, Spring wiring, permissions,
MariaDB and demo accounts. Nothing in the Samigo service graph is mocked.

* Clean build: all 11 Samigo modules succeed; 243 tests pass, including six view
  access tests. The WAR contains 499 XHTML files and zero JSP/JSPF files.
* Mojarra compilation audit: all 499 public views and internal templates compile.
* Detailed authoring flow: eight matching checkpoints cover empty-title validation,
  two questions, second-row editing, unchanged first-row points and fresh-login
  persistence. The deliberate removal of `AuthorAssessmentListener` is detected by
  both missing server/browser validation and a failing browser assertion.
* Broader suite: 37 matching comparisons, including opening/cancelling every one of
  the 12 available question editors. Existing flows cover calculated questions,
  parts/settings/publishing, preview, student delivery, rubric grading, pools,
  tags/filtering, permissions, print and event-log filtering/sorting.
* The broader suite has the same **12 passing tests and one failing Lessons test**
  on each engine. `deletedQuizIsHiddenFromStudentsInLessons` also fails on the JSP
  baseline. A matching differential report does not turn that failure into a pass.
* QTI download tests parse the downloaded XML, check the filename and verify the
  assessment title and question. The export UI must remain HTML; only
  `xmlDisplay` and `xmlPoolDisplay` declare XML response content types.
* Three Facelets import/access tests pass: valid-package import, traversal rejection,
  instructor creation views, and student denial of staff GET/POST routes including
  direct XHTML templates and all supported JSF mappings.

The observed contract includes component IDs/classes, expressions, listener
delegate types and bindings, converter/validator settings, UIData metadata,
facets, lifecycle flags/messages/navigation, browser controls/options/labels,
scripts/styles and page errors. Jasper's generated Java supplies component tag
order and IDs; Mojarra supplies the native Facelets component tree. Runtime
Jasper output takes precedence over optional offline JspC output. The current
conversion has Jasper provenance for 130 files. Include-only fragments without
standalone JSP compilation retain source-order provenance; the manifest records
that distinction.

The report lists every normalization: fixture site/tool/origin identities,
configured JSP-to-XHTML view changes, opaque ViewState/CSRF values, whitespace
runs in human form text, unconditional transient literal renderers and framework
listener wrappers. Control values remain exact. Generated timestamps are recorded
separately only when they fall within their fixture run, using the centrally
resolved timezone observed on the converters; their format remains significant.
Selected tag IDs are fixture-specific. Final snapshots of unchanged external
tools retain their raw differences separately, with test outcomes compared.

This is evidence for the exercised states, not proof of every possible Samigo
state. Compilation does not apply every template against every bean state.
Group/TA grading fixtures, every question type's submission and grading rules,
all assessment templates, every locale/timezone, timing/retry/concurrency cases,
and every export/import format still require their own flow coverage. Existing
page errors present on both engines remain visible in the captures.

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

## Reproducing the experiment

Requirements: Java 21, Maven dependencies, Playwright Chromium, local MySQL/MariaDB
administrator access and a complete disposable Sakai Jakarta/Tomcat deployment.
Keep its components and libraries identical between variants. The harness owns
new runtime directories, ports and schemas; it never edits the source deployment.
Configurations are copied with mode 0600. Runtimes stop in `finally`; schemas and
captures remain for inspection. Do not run against a production database.

The original one-screen experiment is retained in `../samigo-facelets-flow` as
historical tooling. Its default source-copy mode expects JSP source. Use this
whole-tool harness for the migrated source.

Create `target/baseline-app` from the baseline WAR, including its classes and
libraries. After the source migration, restore the baseline web templates/config
from the baseline commit into that owned directory; do not overwrite deployed
applications. The baseline fixture used here was built from `d0f18d4b136`.
The converter excludes the two subsequently removed views explicitly.

```sh
export JAVA_HOME=/path/to/java-21
python3 build-tools/samigo-facelets-migration/jasper.py \
  --source-tomcat /path/to/disposable-tomcat \
  --app build-tools/samigo-facelets-migration/target/baseline-app \
  --output build-tools/samigo-facelets-migration/target/jasper-compiled
python3 build-tools/samigo-facelets-migration/suite.py \
  --source-tomcat /path/to/disposable-tomcat \
  --jasper-fallback build-tools/samigo-facelets-migration/target/jasper-compiled \
  --output build-tools/samigo-facelets-migration/target/new-suite
python3 build-tools/samigo-facelets-migration/suite_compare.py \
  build-tools/samigo-facelets-migration/target/new-suite
```

JspC may reject fragments whose taglib declarations come from their containing
JSP. Inspect its report; those failures are not evidence of a broken public view.
The actual running Jasper cache is authoritative for the exercised views.
`modular.py --jasper ... --jasper-fallback ... --source-web ... --output ...`
converts the retained source, records provenance and fails on unknown scriptlets,
malformed EL, XML errors, recursive includes or Jasper/source tag mismatches.
`test_migrate.py` and `test_modular.py` cover the conversion traps found in Samigo.

`pilot.py --converted ... --jasper-cache ... --calibrate` runs both variants of
the eight-checkpoint flow using the chosen conversion and matching JSP cache.
`retry_suite.py` measures a fresh candidate against a retained JSP run.
`export_check.py RUN --security --candidate-only` additionally runs the real import
and access tests against the retained Facelets course fixture.

`apply.py` is a one-time source migration tool. It requires an exact source/manifest
match and no conversion failures. It copies the converted views, wires production
handlers/filter/listener and removes the JSPs/prelude. Test probes and compilation
listeners are installed only into disposable applications, never production config.

Use a clean Samigo WAR and remove its old exploded application during deployment
so obsolete JSPs cannot remain from an earlier deployment overlay.

Local evidence is under `target/`: `facelets-source-build.log`,
`full-modular-pr-2/report.json`, `pilot-modular-pr-1/report.json`,
`pilot-modular-pr-1/negative-control.json`, QTI test reports/downloads, browser
HTML/JSON, and the unmodified raw lifecycle traces. These generated artifacts
are ignored by Git.

`production-validation` replaces the stopped, owned candidate application with
the clean built WAR and reruns the persistence, QTI and import/access checks.
Those five browser tests pass; all eight observed JSF form/lifecycle contracts
match the narrow reference. This run reuses a populated course/pool database,
so its generated IDs and pool catalog differ from the fresh JSP reference.
Its report retains those DOM differences. The fresh paired pilot supplies the
exact DOM comparison. Final XML source formatting is also checked against the
tested templates: all 251 internal documents retain identical parsed tags,
attributes, text and tails, including meaningful whitespace and EL values.
