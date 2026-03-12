package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssignmentTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static String groupSubmissionCourseUrl;
    private static final String ASSIGN_TITLE = "Playwright Assignment " + System.currentTimeMillis();
    private static final String SHARED_GROUP_STUDENT_EID = "student0178";
    private static final String TEAM4_ONLY_STUDENT_EID = "student0179";
    private static final String TEAM1_ONLY_STUDENT_EID = "student0180";

    private String ensureCourseUrl() {
        if (sakaiUrl != null && !sakaiUrl.isBlank()) {
            return sakaiUrl;
        }

        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of(
            "sakai\\.rubrics",
            "sakai\\.assignment\\.grades",
            "sakai\\.gradebookng"
        ));
        return sakaiUrl;
    }

    private String ensureGroupSubmissionCourseUrl() {
        if (groupSubmissionCourseUrl != null && !groupSubmissionCourseUrl.isBlank()) {
            return groupSubmissionCourseUrl;
        }

        sakai.login("instructor1");
        groupSubmissionCourseUrl = sakai.createCourse("instructor1", List.of("sakai\\.assignment\\.grades"));
        return groupSubmissionCourseUrl;
    }

    @Test
    @Order(1)
    void canCreateNewCourse() {
        ensureCourseUrl();
        assertThat(page.locator("body")).containsText("SMPL101");
    }

    @Test
    @Order(2)
    void canCreateLetterGradeAssignment() {
        String courseUrl = ensureCourseUrl();
        sakai.login("instructor1");
        page.navigate(courseUrl);
        sakai.toolClick("Assignments");

        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill("Letter Grades");

        Locator gradeType = page.locator("#new_assignment_grade_type").first();
        if (gradeType.count() > 0) {
            gradeType.selectOption(new SelectOption().setLabel("Letter grade"));
        }

        fillAssignmentInstructions("<p>Letter grade assignment prompt.</p>");
        submitAssignmentForm();

        goToAssignmentsList();
        assertThat(page.locator("body")).containsText("Letter Grades");
    }

    @Test
    @Order(3)
    void canCreatePointsAssignment() {
        String courseUrl = ensureCourseUrl();
        sakai.login("instructor1");
        page.navigate(courseUrl);
        sakai.toolClick("Assignments");

        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill(ASSIGN_TITLE);

        Locator honorPledge = page.locator("#new_assignment_check_add_honor_pledge").first();
        if (honorPledge.count() > 0) {
            honorPledge.click(new Locator.ClickOptions().setForce(true));
        }

        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0 && gradeAssignment.isChecked()) {
            gradeAssignment.uncheck(new Locator.UncheckOptions().setForce(true));
        }

        fillAssignmentInstructions("<p>Points assignment prompt.</p>");
        submitAssignmentForm();

        goToAssignmentsList();
        assertThat(page.locator("body")).containsText(ASSIGN_TITLE);
    }

    @Test
    @Order(4)
    void canAssociateRubricWithAssignment() {
        String courseUrl = ensureCourseUrl();
        String rubricAssignmentTitle = "Rubric Assignment " + System.currentTimeMillis();

        sakai.createRubric("instructor1", courseUrl);
        page.locator(".modal-footer button:visible, div.popover button:visible, button:visible")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Save", Pattern.CASE_INSENSITIVE)))
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        page.navigate(courseUrl);
        sakai.toolClick("Assignments");
        goToAssignmentsList();
        openAddAssignmentForm();

        page.locator("#new_assignment_title").fill(rubricAssignmentTitle);

        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0) {
            gradeAssignment.check(new Locator.CheckOptions().setForce(true));
        }

        Locator points = page.locator("#new_assignment_grade_points").first();
        if (points.count() > 0) {
            points.fill("55.13");
        }

        Locator associateRubric = page.locator("input[name=\"rbcs-associate\"][value=\"1\"]").first();
        if (associateRubric.count() > 0) {
            associateRubric.check(new Locator.CheckOptions().setForce(true));
        }

        fillAssignmentInstructions("<p>Rubric assignment prompt.</p>");
        submitAssignmentForm();

        goToAssignmentsList();
        Locator rubricRow = page.locator("tr").filter(new Locator.FilterOptions().setHasText(rubricAssignmentTitle)).first();
        assertThat(rubricRow).isVisible();
        assertThat(rubricRow).containsText(Pattern.compile("55\\.13|55", Pattern.CASE_INSENSITIVE));

        Locator editLink = rubricRow.locator("a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Edit\\b", Pattern.CASE_INSENSITIVE))).first();
        if (editLink.count() > 0) {
            editLink.click(new Locator.ClickOptions().setForce(true));
            assertThat(page.locator("#new_assignment_title")).hasValue(rubricAssignmentTitle);
        }
    }

    @Test
    @Order(5)
    @Disabled("Parity with JS suite: this student submission flow remains intentionally skipped")
    void canSubmitAsStudentOnDesktop() {
    }

    @Test
    @Order(6)
    @Disabled("Parity with JS suite: this mobile student submission flow remains intentionally skipped")
    void canSubmitAsStudentOnIphoneViewport() {
    }

    @Test
    @Order(7)
    void canAllowStudentToResubmit() {
        String courseUrl = ensureCourseUrl();
        sakai.login("instructor1");
        page.navigate(courseUrl);
        sakai.toolClick("Assignments");

        Locator gradeAction = page.locator(".itemAction a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Grade|View Submissions", Pattern.CASE_INSENSITIVE))).first();
        if (gradeAction.count() == 0) {
            fail("Missing grader action for assignment submission; expected Grade/View Submissions link before resubmission flow.");
        }

        gradeAction.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        Locator newGraderToggle = page.locator("sakai-grader-toggle input").first();
        if (newGraderToggle.count() > 0) {
            newGraderToggle.check(new Locator.CheckOptions().setForce(true));
        }

        Locator studentSubmission = page.locator("#submissionList a").filter(new Locator.FilterOptions().setHasText("student0012")).first();
        if (studentSubmission.count() > 0) {
            studentSubmission.click(new Locator.ClickOptions().setForce(true));

            Locator resubmitInput = page.locator(".resubmission-checkbox input").first();
            if (resubmitInput.count() > 0) {
                resubmitInput.check(new Locator.CheckOptions().setForce(true));
            }

            Locator returnButton = page.locator("#grader-save-buttons button[name=\"return\"], input#save-and-return, button:has-text(\"Save and Release\"), input[value*=\"Save and Release\"]").first();
            if (returnButton.count() > 0) {
                returnButton.click(new Locator.ClickOptions().setForce(true));
            }
        }
    }

    @Test
    @Order(8)
    void canCreateNonElectronicAssignment() {
        String nonElectronicTitle = "Non-electronic Assignment " + System.currentTimeMillis();
        String courseUrl = ensureCourseUrl();

        sakai.login("instructor1");
        page.navigate(courseUrl);
        sakai.toolClick("Assignments");
        goToAssignmentsList();

        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill(nonElectronicTitle);

        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0 && gradeAssignment.isChecked()) {
            gradeAssignment.uncheck(new Locator.UncheckOptions().setForce(true));
        }

        Locator subType = page.locator("#subType").first();
        if (subType.count() > 0) {
            subType.selectOption(new SelectOption().setLabel("Non-electronic"));
        }

        fillAssignmentInstructions("<p>Submit a printed essay to office 201.</p>");
        submitAssignmentForm();

        goToAssignmentsList();
        assertThat(page.locator("body")).containsText(nonElectronicTitle);
    }

    @Test
    @Order(9)
    void groupSubmissionReassignmentRefreshesDisplayedGroupMembers() {
        String courseUrl = ensureGroupSubmissionCourseUrl();
        String suffix = sakai.randomId();
        String team1Title = "Team 1 " + suffix;
        String team4Title = "Team 4 " + suffix;
        String assignmentTitle = "Group ownership sync " + suffix;

        sakai.login("instructor1");
        createGroup(courseUrl, team1Title, SHARED_GROUP_STUDENT_EID, TEAM1_ONLY_STUDENT_EID);
        createGroup(courseUrl, team4Title, SHARED_GROUP_STUDENT_EID, TEAM4_ONLY_STUDENT_EID);
        createGroupAssignment(courseUrl, assignmentTitle, team1Title, team4Title);

        sakai.login(SHARED_GROUP_STUDENT_EID);
        submitGroupAssignment(courseUrl, assignmentTitle, team4Title, "<p>Original Team 4 submission.</p>");
        submitGroupAssignment(courseUrl, assignmentTitle, team1Title, "<p>Reassigned to Team 1.</p>");

        sakai.login("instructor1");
        openAssignmentSubmissions(courseUrl, assignmentTitle);

        Locator team1Row = page.locator("tr").filter(new Locator.FilterOptions().setHasText(team1Title)).first();
        Locator team4Rows = page.locator("tr").filter(new Locator.FilterOptions().setHasText(team4Title));
        assertThat(team1Row).isVisible();
        assertThat(team4Rows).hasCount(0);
    }

    private void openAddAssignmentForm() {
        goToAssignmentsList();

        Locator addLink = page.locator(".navIntraTool a, .navIntraTool button, .navIntraTool [role=\"button\"]")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^(Add|New)$", Pattern.CASE_INSENSITIVE)))
            .first();

        String href = null;
        if (addLink.count() > 0) {
            href = addLink.getAttribute("href");
            addLink.click(new Locator.ClickOptions().setForce(true));
        }

        Locator titleInput = page.locator("#new_assignment_title").first();
        if (!isVisible(titleInput, 8_000) && href != null && !href.equals("#")) {
            page.navigate(href);
        }

        if (!isVisible(titleInput, 8_000)) {
            String currentUrl = page.url();
            String direct = currentUrl.contains("?") ? currentUrl + "&sakai_action=doNew_assignment" : currentUrl + "?sakai_action=doNew_assignment";
            page.navigate(direct);
        }

        assertThat(titleInput).isVisible();
    }

    private void openManageGroups(String courseUrl) {
        page.navigate(courseUrl);
        sakai.toolClick("Site Info");
        clickIntraToolAction("Manage Groups");
        assertThat(page.locator("#menu")).isVisible();
    }

    private void createGroup(String courseUrl, String groupTitle, String... memberEids) {
        openManageGroups(courseUrl);
        clickIntraToolAction("Create New Group");

        Locator form = page.locator("#creategroup-form").first();
        assertThat(form).isVisible();

        page.locator("#groupTitle").fill(groupTitle);
        selectUsersByEid("#groupMembers", memberEids);
        waitForEnabled("#create-group-submit-button");
        page.locator("#create-group-submit-button").click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        Locator groupRow = page.locator("#groupTable tr").filter(new Locator.FilterOptions().setHasText(groupTitle)).first();
        assertThat(groupRow).isVisible();
    }

    private void createGroupAssignment(String courseUrl, String assignmentTitle, String... groupTitles) {
        page.navigate(courseUrl);
        sakai.toolClick("Assignments");

        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill(assignmentTitle);

        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0 && gradeAssignment.isChecked()) {
            gradeAssignment.uncheck(new Locator.UncheckOptions().setForce(true));
        }

        Locator groupAssignment = page.locator("#groupAssignment").first();
        assertThat(groupAssignment).isVisible();
        groupAssignment.check(new Locator.CheckOptions().setForce(true));

        selectOptionsByVisibleText("#selectedGroups", groupTitles);
        fillAssignmentInstructions("<p>Switch the submission from Team 4 to Team 1 and verify the roster follows the new owner.</p>");
        submitAssignmentForm();

        goToAssignmentsList();
        assertThat(page.locator("body")).containsText(assignmentTitle);
    }

    private void openAssignmentForCurrentUser(String courseUrl, String assignmentTitle) {
        page.navigate(courseUrl);
        sakai.toolClick("Assignments");
        goToAssignmentsList();

        Locator assignmentLink = page.locator("a[name=\"asnActionLink\"], .title a, a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^" + Pattern.quote(assignmentTitle) + "$")))
            .first();
        assertThat(assignmentLink).isVisible();
        assignmentLink.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    private void submitGroupAssignment(String courseUrl, String assignmentTitle, String groupTitle, String submissionHtml) {
        openAssignmentForCurrentUser(courseUrl, assignmentTitle);
        selectSubmissionGroup(groupTitle);
        fillStudentSubmissionText(submissionHtml);

        Locator confirm = page.locator("#confirm").first();
        if (confirm.count() > 0 && confirm.isVisible()) {
            confirm.click(new Locator.ClickOptions().setForce(true));
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        }

        Locator post = page.locator("#post").first();
        assertThat(post).isVisible();
        post.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    private void openAssignmentSubmissions(String courseUrl, String assignmentTitle) {
        page.navigate(courseUrl);
        sakai.toolClick("Assignments");
        goToAssignmentsList();

        Locator assignmentRow = page.locator("tr").filter(new Locator.FilterOptions().setHasText(assignmentTitle)).first();
        assertThat(assignmentRow).isVisible();

        Locator graderAction = assignmentRow.locator(".itemAction a, a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Grade|View Submissions", Pattern.CASE_INSENSITIVE)))
            .first();
        assertThat(graderAction).isVisible();
        graderAction.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    private void goToAssignmentsList() {
        Locator assignmentsNav = page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Assignments$", Pattern.CASE_INSENSITIVE)))
            .first();
        if (assignmentsNav.count() > 0) {
            assignmentsNav.click(new Locator.ClickOptions().setForce(true));
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        }
    }

    private void clickIntraToolAction(String label) {
        Locator action = page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^" + Pattern.quote(label) + "$", Pattern.CASE_INSENSITIVE)))
            .first();
        assertThat(action).isVisible();
        action.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    private void selectUsersByEid(String selectCss, String... userEids) {
        String[] optionValues = Arrays.stream(userEids)
            .map(eid -> findOptionValueContainingEid(selectCss, eid))
            .toArray(String[]::new);
        page.locator(selectCss).selectOption(optionValues);
        page.locator(selectCss).dispatchEvent("change");
    }

    private void selectOptionsByVisibleText(String selectCss, String... visibleTexts) {
        String[] optionValues = Arrays.stream(visibleTexts)
            .map(text -> findOptionValueByVisibleText(selectCss, text))
            .toArray(String[]::new);
        page.locator(selectCss).selectOption(optionValues);
        page.locator(selectCss).dispatchEvent("change");
    }

    private String findOptionValueContainingEid(String selectCss, String eid) {
        Object value = page.evaluate(
            "(args) => {" +
                "const selector = args[0];" +
                "const eid = args[1];" +
                "const option = Array.from(document.querySelectorAll(selector + ' option'))" +
                    ".find(el => (el.textContent || '').includes('(' + eid + ')'));" +
                "return option ? option.value : null;" +
            "}",
            List.of(selectCss, eid)
        );

        if (!(value instanceof String) || ((String) value).isBlank()) {
            fail("Could not find select option for user eid " + eid + " in " + selectCss);
        }

        return (String) value;
    }

    private String findOptionValueByVisibleText(String selectCss, String visibleText) {
        Object value = page.evaluate(
            "(args) => {" +
                "const selector = args[0];" +
                "const text = args[1];" +
                "const option = Array.from(document.querySelectorAll(selector + ' option'))" +
                    ".find(el => (el.textContent || '').trim().startsWith(text));" +
                "return option ? option.value : null;" +
            "}",
            List.of(selectCss, visibleText)
        );

        if (!(value instanceof String) || ((String) value).isBlank()) {
            fail("Could not find select option for text " + visibleText + " in " + selectCss);
        }

        return (String) value;
    }

    private void selectSubmissionGroup(String groupTitle) {
        Locator row = page.locator("#groupTable tr")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*" + Pattern.quote(groupTitle) + "\\b")))
            .first();
        assertThat(row).isVisible();

        Locator radio = row.locator("input[type=\"radio\"]").first();
        radio.check(new Locator.CheckOptions().setForce(true));
    }

    private void fillStudentSubmissionText(String html) {
        if (sakai.typeFirstCkEditorIfPresent(html)) {
            return;
        }

        Locator fallback = page.locator("textarea:visible, [contenteditable=\"true\"]:visible").first();
        assertThat(fallback).isVisible();
        fallback.fill(html.replaceAll("<[^>]+>", ""));
        fallback.dispatchEvent("input");
        fallback.dispatchEvent("change");
        fallback.dispatchEvent("blur");
    }

    private void waitForEnabled(String selector) {
        page.waitForFunction(
            "(selector) => {" +
                "const button = document.querySelector(selector);" +
                "return Boolean(button) && !button.disabled;" +
            "}",
            selector,
            new Page.WaitForFunctionOptions().setTimeout(10_000)
        );
    }

    private void submitAssignmentForm() {
        Locator submit = page.locator(
            "div.act input[type=\"button\"][name=\"post\"]:visible, .act input[type=\"button\"][name=\"post\"]:visible, " +
            "div.act input[type=\"submit\"][name=\"post\"]:visible, .act input[type=\"submit\"][name=\"post\"]:visible, " +
            "div.act input[type=\"button\"][value*=\"Save and Release\"]:visible, .act input[type=\"button\"][value*=\"Save and Release\"]:visible, " +
            "div.act input[type=\"submit\"][value*=\"Save and Release\"]:visible, .act input[type=\"submit\"][value*=\"Save and Release\"]:visible, " +
            "div.act button:has-text(\"Save and Release\"):visible, .act button:has-text(\"Save and Release\"):visible, " +
            "div.act input[type=\"button\"][name=\"save\"]:visible, .act input[type=\"button\"][name=\"save\"]:visible, " +
            "div.act input[type=\"submit\"][name=\"save\"]:visible, .act input[type=\"submit\"][name=\"save\"]:visible, " +
            "div.act input[type=\"button\"][value=\"Post\"]:visible, .act input[type=\"button\"][value=\"Post\"]:visible, " +
            "div.act input[type=\"submit\"][value=\"Post\"]:visible, .act input[type=\"submit\"][value=\"Post\"]:visible, " +
            "div.act button:has-text(\"Post\"):visible, .act button:has-text(\"Post\"):visible, " +
            "div.act input[type=\"button\"][value=\"Save\"]:visible, .act input[type=\"button\"][value=\"Save\"]:visible, " +
            "div.act input[type=\"submit\"][value=\"Save\"]:visible, .act input[type=\"submit\"][value=\"Save\"]:visible, " +
            "div.act button:has-text(\"Save\"):visible, .act button:has-text(\"Save\"):visible, " +
            "input[type=\"submit\"][name=\"post\"]:visible, input[type=\"button\"][name=\"post\"]:visible, " +
            "input[type=\"submit\"][name=\"save\"]:visible, input[type=\"button\"][name=\"save\"]:visible"
        ).first();
        assertThat(submit).isVisible();
        submit.click(new Locator.ClickOptions().setForce(true));
    }

    private void fillAssignmentInstructions(String html) {
        if (!sakai.typeCkEditorIfPresent("new_assignment_instructions", html)) {
            Locator fallback = page.locator("textarea#new_assignment_instructions, textarea:visible, [contenteditable=\"true\"]:visible").first();
            if (fallback.count() > 0) {
                fallback.fill(html.replaceAll("<[^>]+>", ""));
            }
        }
    }

    private boolean isVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

}
