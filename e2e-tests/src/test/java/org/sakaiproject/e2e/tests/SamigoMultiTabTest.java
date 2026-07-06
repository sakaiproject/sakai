package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/**
 * Acceptance tests for SAK-44349: opening the same assessment in multiple
 * browser tabs must never lose or corrupt saved answers.
 *
 * Against an unfixed build the stale-tab scenarios fail (that is the repro);
 * with the delivery state guard deployed the whole suite must be green. The
 * single-tab scenarios are regression canaries that must pass either way.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SamigoMultiTabTest extends SakaiUiTestBase {

    /**
     * Deep assertions verify answer rows directly in the database. They run
     * only when SAKAI_E2E_DB_CMD (env var or system property) holds a command
     * prefix that accepts "-N -B -e <sql>", e.g. for the docker dev stack:
     * export SAKAI_E2E_DB_CMD="docker exec sakai-mariadb mysql -uroot -psakairoot sakai"
     * Without it, UI-level assertions still run; scenarios that cannot even be
     * staged without DB access (retakes, time limits) are skipped via assumptions.
     */
    private static final String DB_CMD = System.getProperty("SAKAI_E2E_DB_CMD",
        System.getenv().getOrDefault("SAKAI_E2E_DB_CMD", ""));

    private static boolean dbChecksEnabled() {
        return DB_CMD != null && !DB_CMD.isBlank();
    }

    private static void assumeDb() {
        org.junit.jupiter.api.Assumptions.assumeTrue(dbChecksEnabled(),
            "scenario requires SAKAI_E2E_DB_CMD for staging/verification");
    }

    private static final String RUN_ID = Long.toString(System.currentTimeMillis());
    private static final String QUIZ_NAV = "MultiTab Nav " + RUN_ID;
    private static final String QUIZ_RACE = "MultiTab Race " + RUN_ID;
    private static final String QUIZ_TIMED = "MultiTab Timed " + RUN_ID;
    private static final String ESSAY_ANSWER = "The mitochondria is the powerhouse of the cell " + RUN_ID;
    private static final String STUDENT = "student0011";

    private static String courseUrl;

    private String ensureCourseUrl() {
        if (courseUrl != null && !courseUrl.isBlank()) {
            return courseUrl;
        }
        sakai.login("instructor1");
        courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        return courseUrl;
    }

    @Test
    @Order(1)
    void setupPublishesMultiTabQuizzes() {
        ensureCourseUrl();
        sakai.login("instructor1");

        for (String title : List.of(QUIZ_NAV, QUIZ_RACE, QUIZ_TIMED)) {
            createAndPublishTwoQuestionQuiz(title);
        }

        if (dbChecksEnabled()) {
            // Unlimited submissions lets scenarios retake without republishing.
            for (String title : List.of(QUIZ_NAV, QUIZ_RACE, QUIZ_TIMED)) {
                long id = publishedAssessmentId(title);
                db("UPDATE SAM_PUBLISHEDACCESSCONTROL_T SET UNLIMITEDSUBMISSIONS=1, SUBMISSIONSALLOWED=NULL WHERE ASSESSMENTID=" + id);
            }
            // 2-minute time limit for the expiry scenario; set via DB so the UI flow stays simple.
            db("UPDATE SAM_PUBLISHEDACCESSCONTROL_T SET TIMELIMIT=120 WHERE ASSESSMENTID=" + publishedAssessmentId(QUIZ_TIMED));

            assertEquals("1", dbScalar("SELECT CAST(UNLIMITEDSUBMISSIONS AS UNSIGNED) FROM SAM_PUBLISHEDACCESSCONTROL_T WHERE ASSESSMENTID="
                + publishedAssessmentId(QUIZ_NAV)));
        }
    }

    @Test
    @Order(2)
    void singleTabTakeSaveSubmitStillWorks() {
        ensureCourseUrl();

        sakai.login(STUDENT);
        enterQuiz(page, QUIZ_NAV);

        selectMcOption(page, 1);
        clickDelivery(page, "next");
        fillEssay(page, ESSAY_ANSWER);
        clickDelivery(page, "save");

        if (dbChecksEnabled()) {
            long quizId = publishedAssessmentId(QUIZ_NAV);
            List<String[]> rows = gradingRows(quizId);
            assertEquals(2, rows.size(), "expected one MC row and one essay row, got: " + describe(rows));
            assertTrue(rows.stream().anyMatch(r -> !"NULL".equals(r[2])), "MC answer missing: " + describe(rows));
            assertTrue(rows.stream().anyMatch(r -> r[3].contains("powerhouse")), "essay text missing: " + describe(rows));
        }

        clickDelivery(page, "submitForGrade");
        // Confirmation page repeats the submit button id.
        if (page.locator("#takeAssessmentForm\\:submitForGrade").count() > 0) {
            clickDelivery(page, "submitForGrade");
        }
        assertThat(page.locator("body")).containsText(Pattern.compile("submi", Pattern.CASE_INSENSITIVE));
        if (dbChecksEnabled()) {
            assertEquals("1", dbScalar("SELECT CAST(FORGRADE AS UNSIGNED) FROM SAM_ASSESSMENTGRADING_T WHERE PUBLISHEDASSESSMENTID="
                + publishedAssessmentId(QUIZ_NAV)
                + " ORDER BY ASSESSMENTGRADINGID DESC LIMIT 1"), "attempt should be submitted for grade");
        }
    }

    @Test
    @Order(3)
    void staleTabPostMustNotDestroyDataAndMustRecover() {
        assumeDb();
        ensureCourseUrl();
        long quizId = publishedAssessmentId(QUIZ_NAV);

        sakai.login(STUDENT);
        String toolUrl = enterTestsTool(page);
        Page tabA = page;
        beginQuiz(tabA, QUIZ_NAV);

        // Second tab: same session, re-enters the in-progress attempt.
        Page tabB = context.newPage();
        tabB.navigate(toolUrl);
        beginQuiz(tabB, QUIZ_NAV);

        // Tab B becomes the live tab: answers MC with option 2 and advances.
        selectMcOption(tabB, 2);
        clickDelivery(tabB, "next");
        String liveAnswerId = latestMcAnswerId(quizId);
        assertFalse("NULL".equals(liveAnswerId), "tab B's answer should be persisted");

        // Tab A is now stale. It posts option 1 from an outdated page.
        selectMcOption(tabA, 1);
        clickDelivery(tabA, "next");

        // INVARIANT: the persisted answer survives no matter what the UI shows.
        assertEquals(liveAnswerId, latestMcAnswerId(quizId),
            "stale tab post must not overwrite the live tab's saved answer");

        // Post-fix UX: friendly resync instead of the fatal Data Discrepancy dead end.
        String bodyText = tabA.locator("body").textContent();
        assertFalse(bodyText.contains("Data Discrepancy"),
            "stale tab should get the friendly resync page, not the fatal discrepancy error");
        assertThat(tabA.locator("#samigo-stale-tab-marker")).hasCount(1);

        // One-click recovery back into the live attempt.
        clickFirstVisible(tabA, "input[type=\"submit\"][value*=\"Continue\"], button:has-text(\"Continue\"), a:has-text(\"Continue\")");
        assertThat(tabA.locator("#takeAssessmentForm")).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(20_000));

        tabB.close();
    }

    @Test
    @Order(4)
    void concurrentSavesFromTwoTabsNeverCorruptRows() {
        ensureCourseUrl();

        sakai.login(STUDENT);
        String toolUrl = enterTestsTool(page);
        Page tabA = page;
        beginQuiz(tabA, QUIZ_RACE);

        Page tabB = context.newPage();
        tabB.navigate(toolUrl);
        beginQuiz(tabB, QUIZ_RACE);

        for (int round = 1; round <= 3; round++) {
            ensureOnDeliveryPage(tabA, toolUrl, QUIZ_RACE);
            ensureOnDeliveryPage(tabB, toolUrl, QUIZ_RACE);
            selectMcOption(tabA, 1);
            selectMcOption(tabB, 2);

            // Fire both saves as close to simultaneously as possible. Playwright
            // Java is not thread-safe, so instead of Java threads each page arms
            // a JS timer and the clicks fire concurrently browser-side.
            tabA.evaluate("() => setTimeout(() => document.getElementById('takeAssessmentForm:save').click(), 100)");
            tabB.evaluate("() => setTimeout(() => document.getElementById('takeAssessmentForm:save').click(), 100)");
            tabA.waitForLoadState(LoadState.DOMCONTENTLOADED);
            tabB.waitForLoadState(LoadState.DOMCONTENTLOADED);
            tabA.waitForTimeout(2_000);

            if (!dbChecksEnabled()) {
                continue;
            }
            long quizId = publishedAssessmentId(QUIZ_RACE);
            // INVARIANTS, every round: at most one row per published item, never a
            // blank/deleted MC answer once one was saved, no constraint violations.
            List<String[]> rows = gradingRows(quizId);
            long mcRows = rows.stream().filter(r -> !"NULL".equals(r[2])).count();
            assertEquals(1, mcRows, "round " + round + ": exactly one MC answer row expected: " + describe(rows));
            // Group by item only: two rows with DIFFERENT answer ids on a
            // single-select item are exactly the duplicate artifact to catch.
            String dupes = dbScalar("SELECT COUNT(*) FROM SAM_ITEMGRADING_T ig JOIN SAM_ASSESSMENTGRADING_T ag"
                + " ON ig.ASSESSMENTGRADINGID = ag.ASSESSMENTGRADINGID"
                + " WHERE ag.PUBLISHEDASSESSMENTID=" + quizId
                + " GROUP BY ig.ASSESSMENTGRADINGID, ig.PUBLISHEDITEMID HAVING COUNT(*) > 1 LIMIT 1");
            assertTrue(dupes == null || dupes.isBlank(), "round " + round + ": duplicate item grading rows detected");
        }

        tabB.close();
    }

    @Test
    @Order(5)
    void timedExpiryFromStaleBlankTabMustNotBlankSavedAnswers() {
        assumeDb();
        ensureCourseUrl();
        long quizId = publishedAssessmentId(QUIZ_TIMED);

        sakai.login(STUDENT);
        String toolUrl = enterTestsTool(page);

        // Tab B first: navigate it to the essay page, where it will hold the
        // stale BLANK rendering. (Position is shared session state, so tab A
        // will resume on the same page.)
        Page tabB = context.newPage();
        tabB.navigate(toolUrl);
        beginQuiz(tabB, QUIZ_TIMED);
        clickDelivery(tabB, "next");
        assertThat(tabB.locator("#takeAssessmentForm textarea, #takeAssessmentForm .cke")).not().hasCount(0);

        // Tab A: resumes the same attempt. Re-entry resets to Q1 in random
        // access mode, so navigate to the essay page (tab A becomes the live
        // tab there), then type the essay and save it.
        Page tabA = page;
        tabA.navigate(toolUrl);
        beginQuiz(tabA, QUIZ_TIMED);
        if (tabA.locator("#takeAssessmentForm textarea:visible").count() == 0) {
            clickDelivery(tabA, "next");
        }
        fillEssay(tabA, ESSAY_ANSWER);
        clickDelivery(tabA, "save");

        List<String[]> saved = gradingRows(quizId);
        assertTrue(saved.stream().anyMatch(r -> r[3].contains("powerhouse")),
            "essay should be saved before the forced submit: " + describe(saved));

        // Fire the forced timeout submit from the stale blank tab: this is the
        // exact button (submitNoCheck -> delivery.submitFromTimeoutPopup) the
        // timer popup clicks when time expires, and it bypasses all checks.
        // Its blank essay field binds into the shared page contents; without
        // the backstop this blanks the saved essay (SAK-43421's scenario).
        tabB.evaluate("() => document.getElementById('takeAssessmentForm:submitNoCheck').click()");
        tabB.waitForLoadState(LoadState.DOMCONTENTLOADED);
        waitForSubmission(quizId, tabA, tabB, 60_000);

        List<String[]> after = gradingRows(quizId);
        assertTrue(after.stream().anyMatch(r -> r[3].contains("powerhouse")),
            "essay answer was blanked by the stale tab's forced submit: " + describe(after));

        tabB.close();
    }

    @Test
    @Order(6)
    void currentTabTimeoutSubmitKeepsFullAuthority() {
        // The forced timeout submit from the CURRENT tab must keep today's
        // semantics: the student's last (unsaved) change wins, with exactly one
        // row per single-select item - no duplicate rows, no constraint abort.
        assumeDb();
        ensureCourseUrl();
        long quizId = publishedAssessmentId(QUIZ_TIMED);

        sakai.login(STUDENT);
        enterQuiz(page, QUIZ_TIMED);
        selectMcOption(page, 1);
        clickDelivery(page, "save");
        String firstAnswer = latestMcAnswerId(quizId);
        assertFalse("NULL".equals(firstAnswer), "first MC answer should be saved");

        // Change the answer but do NOT save, then let the "timer" force-submit.
        selectMcOption(page, 2);
        page.evaluate("() => document.getElementById('takeAssessmentForm:submitNoCheck').click()");
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        waitForSubmission(quizId, page, page, 60_000);

        List<String[]> rows = gradingRows(quizId);
        long mcRows = rows.stream().filter(r -> !"NULL".equals(r[2])).count();
        assertEquals(1, mcRows, "exactly one MC row after a current-tab timeout submit: " + describe(rows));
        assertFalse(firstAnswer.equals(latestMcAnswerId(quizId)),
            "the changed answer must win on a current-tab timeout submit (no over-freeze)");
    }

    @Test
    @Order(7)
    void autosaveKeepsTheActiveTabValid() {
        // Accepted autosaves verify WITHOUT rotating the guard token, so the
        // active tab must remain valid across autosave cycles regardless of
        // whether the response scrape in saveForm.js finds anything. This also
        // covers the lost-response case: since the response's content plays no
        // part in the tab's validity (nothing rotates), an autosave whose
        // response is eaten by the network leaves the tab exactly as valid as
        // this test proves it to be after a processed one.
        // Requires samigo.autoSave.repeat.milliseconds=15000.
        ensureCourseUrl();

        sakai.login(STUDENT);
        enterQuiz(page, QUIZ_RACE);
        selectMcOption(page, 1);

        // Wait past one autosave cycle (15s interval + request time).
        page.waitForTimeout(25_000);
        assertTrue(page.locator("#stale-tab-warning").isHidden(),
            "the active tab's own autosave must never trip the stale-tab warning");

        // The manual save after an autosave is the regression: it must be
        // accepted, not bounced to the resync page.
        clickDelivery(page, "next");
        String body = page.locator("body").textContent();
        assertFalse(body.contains("This page is out of date"),
            "active tab was invalidated by its own autosave (token scrape broken)");
        if (dbChecksEnabled()) {
            assertTrue(gradingRows(publishedAssessmentId(QUIZ_RACE)).stream().anyMatch(r -> !"NULL".equals(r[2])),
                "answer should be saved after autosave + next");
        }
    }

    @Test
    @Order(8)
    void tableOfContentsNavigationStaysSafe() {
        assumeDb();
        ensureCourseUrl();
        long quizId = publishedAssessmentId(QUIZ_NAV);

        sakai.login(STUDENT);
        enterQuiz(page, QUIZ_NAV);
        selectMcOption(page, 1);
        clickDelivery(page, "next");
        String savedAnswer = latestMcAnswerId(quizId);
        assertFalse("NULL".equals(savedAnswer), "MC answer should persist before TOC nav");

        // Table of Contents round trip back to Q1.
        clickFirstVisible(page, "a[id$='showTOC']");
        assertThat(page.locator("#tableOfContentsForm")).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(20_000));
        Locator questionLink = page.locator("#tableOfContentsForm a").filter(
            new Locator.FilterOptions().setHasText("Pick an option")).first();
        questionLink.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(page.locator("#takeAssessmentForm")).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(20_000));

        assertEquals(savedAnswer, latestMcAnswerId(quizId), "TOC navigation must not disturb saved answers");
        String body = page.locator("body").textContent();
        assertFalse(body.contains("This page is out of date"),
            "single-tab TOC navigation must not trip the stale-tab guard");
        assertFalse(body.contains("Discrepancy in Data"),
            "single-tab TOC navigation must not trip the discrepancy page");
    }

    @Test
    @Order(10)
    void duplicatedTabCannotSilentlyOverwrite() {
        // Duplicating a tab is a plain GET re-render of the delivery view; it
        // must rotate the guard token so the two copies cannot share one valid
        // token and autosave/post over each other (the classic silent loss).
        assumeDb();
        ensureCourseUrl();
        long quizId = publishedAssessmentId(QUIZ_RACE);

        sakai.login(STUDENT);
        enterQuiz(page, QUIZ_RACE);
        selectMcOption(page, 1);
        clickDelivery(page, "save");
        String saved = latestMcAnswerId(quizId);
        assertFalse("NULL".equals(saved), "answer should be saved before duplication");

        Page dup = context.newPage();
        dup.navigate(page.url());
        dup.locator("#takeAssessmentForm").waitFor(
            new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(20_000));

        // The original tab is now the stale copy: its post must resync, never
        // silently apply.
        selectMcOption(page, 2);
        clickDelivery(page, "next");
        assertThat(page.locator("#samigo-stale-tab-marker")).hasCount(1);
        assertEquals(saved, latestMcAnswerId(quizId),
            "a duplicated tab's sibling must not overwrite saved answers");

        dup.close();
    }

    // ---------------------------------------------------------------- helpers

    private void createAndPublishTwoQuestionQuiz(String title) {
        page.navigate(courseUrl);
        sakai.toolClick("Tests");

        Locator addLink = page.locator("#authorIndexForm a").filter(
            new Locator.FilterOptions().setHasText(Pattern.compile("^Add$", Pattern.CASE_INSENSITIVE))).first();
        assertThat(addLink).isVisible();
        addLink.click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("#authorIndexForm\\:title")).isVisible();
        page.locator("#authorIndexForm\\:title").fill(title);
        page.locator("#authorIndexForm\\:createnew").click(new Locator.ClickOptions().setForce(true));

        // Q1: multiple choice, correct = second option.
        selectQuestionType(Pattern.compile("multiple\\s*choice", Pattern.CASE_INSENSITIVE));
        page.locator("#itemForm\\:answerptr").fill("10.00");
        page.locator("#itemForm textarea").first().fill("Pick an option (multi-tab repro)");
        page.locator("#itemForm\\:mcchoices textarea").nth(0).fill("Option one");
        page.locator("#itemForm\\:mcchoices textarea").nth(1).fill("Option two");
        page.locator("#itemForm\\:mcchoices textarea").nth(2).fill("Option three");
        page.locator("#itemForm\\:mcchoices input[type=\"radio\"]").nth(1).check(new Locator.CheckOptions().setForce(true));
        clickSubmit(page, "Save");
        assertThat(page.locator("#assessmentForm\\:parts")).isVisible();

        // Q2: short answer / essay.
        selectQuestionType(Pattern.compile("short\\s*answer|essay", Pattern.CASE_INSENSITIVE));
        page.locator("#itemForm\\:answerptr").fill("10.00");
        page.locator("#itemForm\\:questionItemText_textinput").first().fill("Explain something (multi-tab repro)");
        clickSubmit(page, "Save");
        assertThat(page.locator("#assessmentForm\\:parts")).isVisible();

        // Settings: open availability window, then publish.
        page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Settings")).first().click(new Locator.ClickOptions().setForce(true));

        DateTimeFormatter dateTime12h = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US);
        sakai.selectDate("#assessmentSettingsAction\\:startDate",
            LocalDateTime.now().minusDays(1).format(dateTime12h).toLowerCase(Locale.US));
        sakai.selectDate("#assessmentSettingsAction\\:endDate",
            LocalDateTime.now().plusYears(2).format(dateTime12h).toLowerCase(Locale.US));

        clickFirstVisible(page,
            "button:has-text(\"Save Settings and Publish\"), input[type=\"submit\"][value*=\"Save Settings and Publish\"], input[type=\"submit\"][value*=\"Publish\"], button:has-text(\"Publish\")");
        clickFirstVisibleIfPresent(page,
            "input[type=\"submit\"][value*=\"Publish\"], button:has-text(\"Publish\")");
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private void selectQuestionType(Pattern labelPattern) {
        Locator select = page.locator("select[id$=\":changeQType\"]").first();
        if (!isVisible(select, 5_000)) {
            select = page.locator("#content form select:visible").first();
        }
        assertThat(select).isVisible();
        String matchedValue = null;
        for (ElementHandle option : select.locator("option").elementHandles()) {
            String label = option.textContent();
            if (label != null && labelPattern.matcher(label).find()) {
                matchedValue = option.getAttribute("value");
                break;
            }
        }
        if (matchedValue == null || matchedValue.isBlank()) {
            throw new IllegalStateException("No question type option matches " + labelPattern);
        }
        select.selectOption(matchedValue);
    }

    private String enterTestsTool(Page target) {
        target.navigate(sakaiAbsolute(courseUrl));
        if (target == page) {
            sakai.toolClick("Tests");
        } else {
            Locator tool = target.locator("a.btn-nav").filter(
                new Locator.FilterOptions().setHasText(Pattern.compile("Tests", Pattern.CASE_INSENSITIVE))).first();
            tool.click(new Locator.ClickOptions().setForce(true));
            target.waitForLoadState(LoadState.DOMCONTENTLOADED);
        }
        return target.url();
    }

    private void enterQuiz(Page target, String title) {
        enterTestsTool(target);
        beginQuiz(target, title);
    }

    private void beginQuiz(Page target, String title) {
        Locator link = target.locator("#selectIndexForm\\:selectTable a[id$=':takeAssessment']").filter(
            new Locator.FilterOptions().setHasText(title)).first();
        link.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(20_000));
        link.click(new Locator.ClickOptions().setForce(true));
        target.waitForLoadState(LoadState.DOMCONTENTLOADED);

        Locator begin = target.locator(
            "input[type=\"submit\"][value*=\"Begin\"], input[type=\"submit\"][value*=\"Continue\"], button:has-text(\"Begin\"), button:has-text(\"Continue\")").first();
        if (isVisible(begin, 10_000)) {
            // The begin page may require the Honor Pledge checkbox before starting.
            Locator pledge = target.locator("#takeAssessmentForm input[type=\"checkbox\"]:visible");
            if (pledge.count() > 0) {
                pledge.first().check(new Locator.CheckOptions().setForce(true));
            }
            begin.click(new Locator.ClickOptions().setForce(true));
            target.waitForLoadState(LoadState.DOMCONTENTLOADED);
            // A timed assessment interposes one more confirmation.
            Locator confirm = target.locator(
                "input[type=\"submit\"][value*=\"Begin\"], button:has-text(\"Begin\")").first();
            if (target.locator("#takeAssessmentForm input[type=\"radio\"]").count() == 0 && isVisible(confirm, 3_000)) {
                confirm.click(new Locator.ClickOptions().setForce(true));
                target.waitForLoadState(LoadState.DOMCONTENTLOADED);
            }
        }
        // The begin page and the delivery page share the same form id, so wait
        // for actual question content, not just the form.
        target.locator("#takeAssessmentForm input[type=\"radio\"], #takeAssessmentForm textarea").first().waitFor(
            new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(20_000));
    }

    private void ensureOnDeliveryPage(Page target, String toolUrl, String title) {
        if (target.locator("#takeAssessmentForm input[type=\"radio\"]").count() > 0) {
            return;
        }
        target.navigate(toolUrl);
        beginQuiz(target, title);
    }

    private void selectMcOption(Page target, int oneBasedOption) {
        Locator radios = target.locator("#takeAssessmentForm input[type=\"radio\"]");
        radios.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(20_000));
        radios.nth(oneBasedOption - 1).check(new Locator.CheckOptions().setForce(true));
    }

    private void fillEssay(Page target, String text) {
        Locator textarea = target.locator("#takeAssessmentForm textarea:visible").first();
        if (isVisible(textarea, 5_000)) {
            textarea.fill(text);
            return;
        }
        // Rich-text delivery: write through CKEditor's backing textarea.
        Boolean done = (Boolean) target.evaluate(
            "(text) => { const inst = window.CKEDITOR && Object.values(window.CKEDITOR.instances)[0];"
            + "if (!inst) return false; inst.setData(text); inst.updateElement(); return true; }", text);
        if (!Boolean.TRUE.equals(done)) {
            throw new IllegalStateException("No essay input found in delivery page");
        }
    }

    private void clickDelivery(Page target, String buttonId) {
        Locator button = target.locator("#takeAssessmentForm\\:" + buttonId).first();
        button.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(20_000));
        button.click(new Locator.ClickOptions().setForce(true));
        target.waitForLoadState(LoadState.DOMCONTENTLOADED);
        target.waitForTimeout(500);
    }

    private void waitForSubmission(long quizId, Page tabA, Page tabB, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            String forGrade = dbScalar("SELECT CAST(FORGRADE AS UNSIGNED) FROM SAM_ASSESSMENTGRADING_T WHERE PUBLISHEDASSESSMENTID="
                + quizId + " ORDER BY ASSESSMENTGRADINGID DESC LIMIT 1");
            if ("1".equals(forGrade)) {
                return;
            }
            tabA.waitForTimeout(5_000);
        }
        throw new AssertionError("Timed attempt was never auto-submitted within " + timeoutMs + "ms");
    }

    private static final java.util.Map<String, Long> ASSESSMENT_IDS = new java.util.concurrent.ConcurrentHashMap<>();

    private long publishedAssessmentId(String title) {
        return ASSESSMENT_IDS.computeIfAbsent(title, t -> {
            String id = dbScalar("SELECT ID FROM SAM_PUBLISHEDASSESSMENT_T WHERE TITLE='" + t + "'");
            if (id == null || id.isBlank()) {
                throw new IllegalStateException("Published assessment not found: " + t);
            }
            return Long.parseLong(id.trim());
        });
    }

    /** Latest attempt's item rows: ITEMGRADINGID, PUBLISHEDITEMID, PUBLISHEDANSWERID, ANSWERTEXT. */
    private List<String[]> gradingRows(long quizId) {
        String out = db("SELECT ig.ITEMGRADINGID, ig.PUBLISHEDITEMID, IFNULL(ig.PUBLISHEDANSWERID,'NULL'),"
            + " IFNULL(ig.ANSWERTEXT,'NULL') FROM SAM_ITEMGRADING_T ig WHERE ig.ASSESSMENTGRADINGID ="
            + " (SELECT MAX(ASSESSMENTGRADINGID) FROM SAM_ASSESSMENTGRADING_T WHERE PUBLISHEDASSESSMENTID=" + quizId + ")");
        List<String[]> rows = new ArrayList<>();
        for (String line : out.split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            String[] cols = line.split("\t", -1);
            if (cols.length >= 4) {
                rows.add(cols);
            }
        }
        return rows;
    }

    private String latestMcAnswerId(long quizId) {
        return gradingRows(quizId).stream()
            .map(r -> r[2])
            .filter(v -> !"NULL".equals(v))
            .findFirst()
            .orElse("NULL");
    }

    private String describe(List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        for (String[] row : rows) {
            sb.append(String.join("|", row)).append(" ; ");
        }
        return sb.toString();
    }

    private String dbScalar(String sql) {
        String out = db(sql);
        return out == null ? null : out.strip();
    }

    private String db(String sql) {
        try {
            java.util.List<String> command = new ArrayList<>(java.util.Arrays.asList(DB_CMD.trim().split("\\s+")));
            command.addAll(java.util.Arrays.asList("-N", "-B", "-e", sql));
            Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Warning:")) {
                        continue;
                    }
                    output.append(line).append('\n');
                }
            }
            int exit = process.waitFor();
            if (exit != 0) {
                throw new IllegalStateException("mysql exited " + exit + " for: " + sql + "\n" + output);
            }
            return output.toString();
        } catch (java.io.IOException | InterruptedException e) {
            throw new IllegalStateException("db query failed: " + sql, e);
        }
    }

    private String sakaiAbsolute(String pathOrUrl) {
        if (pathOrUrl.startsWith("http")) {
            return pathOrUrl;
        }
        return org.sakaiproject.e2e.support.SakaiEnvironment.baseUrl() + pathOrUrl;
    }

    private void clickSubmit(Page target, String label) {
        Locator submit = target.locator(
            "input[type=\"submit\"][value*=\"" + label + "\"], button:has-text(\"" + label + "\")");
        int count = (int) submit.count();
        for (int index = 0; index < count; index++) {
            Locator candidate = submit.nth(index);
            if (isVisible(candidate, 5_000)) {
                candidate.click(new Locator.ClickOptions().setForce(true));
                target.waitForLoadState(LoadState.DOMCONTENTLOADED);
                return;
            }
        }
        throw new AssertionError("No visible submit for label: " + label);
    }

    private void clickFirstVisible(Page target, String selector) {
        Locator locator = target.locator(selector).first();
        assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15_000));
        locator.click(new Locator.ClickOptions().setForce(true));
        target.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private boolean clickFirstVisibleIfPresent(Page target, String selector) {
        Locator locator = target.locator(selector);
        int count = (int) locator.count();
        for (int index = 0; index < count; index++) {
            Locator candidate = locator.nth(index);
            if (isVisible(candidate, 5_000)) {
                candidate.click(new Locator.ClickOptions().setForce(true));
                target.waitForLoadState(LoadState.DOMCONTENTLOADED);
                return true;
            }
        }
        return false;
    }

    private boolean isVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(timeoutMs));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
