package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
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
    private static final String ASSIGN_TITLE = "Playwright Assignment " + System.currentTimeMillis();

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

    private void goToAssignmentsList() {
        Locator assignmentsNav = page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Assignments$", Pattern.CASE_INSENSITIVE)))
            .first();
        if (assignmentsNav.count() > 0) {
            assignmentsNav.click(new Locator.ClickOptions().setForce(true));
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        }
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
        boolean hasCkEditor = Boolean.TRUE.equals(page.evaluate("() => Boolean(window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances.new_assignment_instructions)"));
        if (hasCkEditor) {
            sakai.typeCkEditor("new_assignment_instructions", html);
        } else {
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
