# Real Samigo JSP → Facelets flow experiment

This experiment converts Samigo's **assessment creation screen** to Facelets and runs the same real application flow against JSP and Facelets. It uses the deployed Sakai services, Spring wiring, permission checks, demo users, MariaDB persistence and portal integration. It does not mock the tool or its services.

The question list and question editors remain JSP. Their repeated rows are exercised by creating two questions, editing the second row and checking the persisted result after a fresh login. This is not evidence that those JSP loops have been migrated safely: that requires a separate conversion and comparison.

## Flow and observed result

The browser test records eight checkpoints:

1. Open Add Assessment.
2. Submit an empty title and verify Samigo's error message.
3. Create the assessment successfully.
4. Add the first multiple-choice question, worth 10 points.
5. Add the second multiple-choice question, worth 20 points.
6. Open the second question and verify its text.
7. Change its score to 25 points; verify the first question still has 10 points.
8. Log in again, reopen the draft through Actions → Edit, and verify both scores.

The JSP and Facelets runs passed this flow. The final comparison has **zero differences at all eight checkpoints** in the observed form controls, component contracts and lifecycle results. The permission tests passed all six cases, including the `.xhtml` creation view.

The negative control also succeeded: removing the creation listener caused the empty-title error to disappear. The normal browser assertion failed, and the server trace recorded both the absent listener and absent message. `negative-control.json` records this deliberate failure as a detected regression; the template was restored afterward.

The first Facelets run exposed a genuine routing defect: `SamigoJsfTool` removed only `.jsp` from form action URLs. A Facelets form therefore submitted to a template URL and bypassed the JSF lifecycle. The source fix uses the existing `JSF_FACELETS_EXT` list from Sakai's base JSF tool. The other source fix adds `.xhtml` to `SamigoJsfViewAccess` normalization so creation retains its specific permission requirement.

## What is converted

`convert.py` handles only `jsf/author/createAssessment_title.jsp` and its static toolbar includes. It expands static JSP includes, removes JSP directives/comments, replaces the header scriptlet with a request attribute expression, and validates the result as XML. It does not attempt arbitrary JSP scriptlet, JSTL or custom Samigo tag conversion.

The form contains 46 component tags. The converter verifies their order, types and existing explicit IDs against the baseline component tree, then assigns the 35 captured implicit JSP IDs explicitly. `conversion.json` records every component ID and its provenance. The source JSP remains unchanged; the generated XHTML and navigation change exist only in the disposable candidate deployment.

A test-only header filter supplies the same request attributes as Samigo's JSP header prelude. It preserves the existing portal library integration. The header output must have no explicit ID, because an explicit ID makes `h:outputText` emit an HTML wrapper that can change script placement.

## Comparison contract

`SamigoViewProbe` records phase traces only when the output property and a valid experiment request header are present. Trace state stays in the Faces request map. It captures component classes, families, renderers, IDs, value expressions, actions, action listeners, validators, converter classes, input state, facets, children and Faces messages. It does not evaluate arbitrary model value expressions or invoke listeners.

The comparator checks:

- The final rendered form's component hierarchy and metadata.
- The completing request's phase sequence, postback/validation flags, messages and view transitions.
- Browser control IDs, names, types, values, options, checked/disabled state, visible form text and script/stylesheet paths in order.

Every raw phase trace and captured HTML fragment is retained. Other requests needed to reach a checkpoint are retained too; the report identifies the completing trace selected for each checkpoint.

Expected representation differences are recorded explicitly in `report.json`: the two configured local runtime origins, the exact generated fixture site/tool UUIDs, the migrated view suffix, JSP literal-markup components versus Facelets `UIInstructions`, and opaque browser view-state/CSRF fields. Lazy Facelets action-listener wrappers are compared by their declared delegate type read from their public serialization contract; raw implementation classes are retained. Listener count, order and delegate type remain exact.

Unchanged JSP pages get the **same compiled Jasper classes** in both runtimes. Jasper generated different ID prefixes when compiling identical templates in separate directories, including hidden submitted field names. Before copying the cache, the harness verifies that all 265 JSP/JSP fragment/include templates are byte-identical. This avoids suppressing generated-ID differences. `jsp-cache-alignment.json` records the check.

No assessment/question IDs, arbitrary numbers, input names or submitted values are broadly normalized. The final runs use separate fresh databases with identical initial fixtures.

This is a bounded differential check, not a proof for every user, locale, permission combination, navigation branch, AJAX request, session-expiry case or concurrent edit. It also does not compare pixels, every HTML attribute or every intermediate model value. A separate run without the probe would be needed to establish observer neutrality explicitly.

## Run

Requirements: Java 21, Maven, Python 3, the repository's Playwright browsers, a complete compatible local demo Sakai/Tomcat deployment and a MariaDB administrator connection that can create isolated schemas and grant access to the deployment's existing database user. Build Samigo first so its Maven dependencies, including Lombok, are cached.

From the repository root:

```sh
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home
python3 build-tools/samigo-facelets-flow/run.py \
  --source-tomcat /private/tmp/sakai-jakarta-e2e/clean-tomcat \
  --calibrate
```

Use `--mysql-admin-config /absolute/path/to/private-client.cnf` when administrator credentials are required. Without that option the harness uses local root TCP with no password. Credentials are copied only into protected runtime configuration files, never into the report.

Use `--output /absolute/path/to/new-output`, `--jsp-port` and `--facelets-port` to choose another location or free ports. Output must be new. The script never replaces an existing runtime and always stops its servers, including on failure. Its generated schemas and artifacts remain for inspection; their names are in `runtimes.json`. Other deployed applications and runtime libraries are referenced read-only from the source deployment, so keep it available throughout the run.

The normal E2E suite skips this class unless `samigo.equivalence.browserOutput` is set. The calibration method also requires its separate opt-in property. It deliberately removes `AuthorAssessmentListener` from the disposable XHTML, runs the empty-title assertion, checks for the missing listener and missing error message, and restores the template in `finally`. Its expected failing Maven log is recorded separately from the passing migration flow.

## Files

- `run.py`: prepare, launch, exercise, stop and compare both variants.
- `runtime.py`: isolated deployment/database setup and compilation.
- `convert.py`: narrow creation-view converter and captured-ID manifest.
- `compare.py`: explicit normalization and structured differences.
- `calibrate.py`: missing-listener negative control.
- `src/main/java/...`: test-only lifecycle probe and header adapter.
- `../../e2e-tests/src/test/java/org/sakaiproject/e2e/tests/SamigoViewEquivalenceTest.java`: real browser flow.

The current recovery run is under `target/`; its primary artifacts are `report.json`, `conversion.json`, `jsp-cache-alignment.json`, the JSP/Facelets test reports, browser snapshots and raw phase traces. `routing-failure/` retains the original routing failure. `compilation-context-differences.json` retains the ID differences observed before reusing unchanged JSP bytecode.

The original worktree was removed during the experiment. The recoverable source work is in `/private/tmp/samigo-facelets-recovery`; the original checkout's earlier captures could not be recovered, so both variants were rerun here.
