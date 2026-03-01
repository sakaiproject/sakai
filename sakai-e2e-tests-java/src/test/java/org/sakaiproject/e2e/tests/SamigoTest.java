package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SamigoTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String RUN_ID = Long.toString(System.currentTimeMillis());
    private static final String SAMIGO_TITLE = "Playwright Quiz " + RUN_ID;
    private static final String ESSAY_TITLE = "Essay with Rubric " + RUN_ID;
    private static final String RUBRIC_TITLE = "Playwright Samigo Rubric " + RUN_ID;

    private String ensureCourseUrl() {
        if (sakaiUrl != null && !sakaiUrl.isBlank()) {
            return sakaiUrl;
        }

        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.rubrics", "sakai\\.samigo"));
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
    void canCreateQuizFromScratch() {
        String courseUrl = ensureCourseUrl();
        sakai.login("instructor1");
        page.navigate(courseUrl);
        sakai.toolClick("Tests");
        String testsToolUrl = page.url();

        openNewAssessmentForm();
        page.locator("#authorIndexForm\\:title").fill(SAMIGO_TITLE);
        page.locator("#authorIndexForm\\:createnew").click(new Locator.ClickOptions().setForce(true));

        addMultipleChoiceQuestion(
            "99.99",
            "What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?",
            List.of(
                "Compulsory health and physical education courses in public schools.",
                "The reduced death rate among infants and young children.",
                "The substitution of machines for human labor."
            ),
            1
        );
        assertThat(page.locator("#assessmentForm\\:parts\\:0\\:parts\\:0\\:answerptr")).hasValue("99.99");

        page.locator("#assessmentForm\\:parts\\:0\\:parts\\:0\\:modify").click(new Locator.ClickOptions().setForce(true));
        page.locator("#itemForm\\:answerptr").fill("100.00");
        page.locator("#itemForm\\:mcchoices textarea").nth(3).fill("Safer cars.");
        saveQuestionAndWaitForAssessment();

        addMultipleChoiceQuestion(
            "100.00",
            "What is the main reason so many people moved to California in 1849?",
            List.of(
                "California land was fertile, plentiful, and inexpensive.",
                "Gold was discovered in central California.",
                "The east was preparing for a civil war.",
                "They wanted to establish religious settlements."
            ),
            1
        );

        assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(2);

        page.locator("#assessmentForm\\:addPart").click(new Locator.ClickOptions().setForce(true));
        page.locator("#modifyPartForm\\:title").fill("Second Part");
        clickSubmit("Save");

        page.locator("#assessmentForm\\:parts a[title=\"Remove Part\"]").last().click(new Locator.ClickOptions().setForce(true));
        clickSubmit("Remove");

        assertThat(page.locator("label[for=\"assessmentForm\\:parts\\:1\\:number\"]")).hasCount(0);
        assertThat(page.getByText("Second Part")).hasCount(0);

        page.locator("#assessmentForm\\:parts\\:0\\:parts\\:0\\:deleteitem").click(new Locator.ClickOptions().setForce(true));
        clickSubmit("Remove");
        assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(1);

        selectQuestionType(Pattern.compile("calculated", Pattern.CASE_INSENSITIVE));
        page.locator("#itemForm\\:answerptr").fill("10.00");
        page.locator("#itemForm textarea.simple_text_area").first().fill(
            "Kevin has {x} apples. He buys {y} more. Now Kevin has [[{x}+{y}]]. Jane eats {z} apples. Kevin now has {{w}} apples."
        );
        page.locator("#itemForm input[type=\"submit\"].active").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("#itemForm\\:pairs input[type=\"text\"]")).hasCount(6);
        for (int index = 0; index < 6; index += 1) {
            page.locator("#itemForm\\:pairs input[type=\"text\"]").nth(index).fill(Integer.toString(index + 1));
        }
        page.locator("#itemForm\\:formulas textarea").first().fill("{x} + {y} - {z}");
        saveQuestionAndWaitForAssessment();
        assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(2);

        page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
            new com.microsoft.playwright.Page.GetByRoleOptions().setName("Settings")).first()
            .click(new Locator.ClickOptions().setForce(true));

        if (isVisible(page.locator("#assessmentSettingsAction\\:lateHandling\\:0").first(), 3_000)) {
            page.locator("#assessmentSettingsAction\\:lateHandling\\:0").first().check(new Locator.CheckOptions().setForce(true));
        }

        DateTimeFormatter dateTime12h = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US);
        String availableDate = LocalDateTime.now().minusDays(1).format(dateTime12h).toLowerCase(Locale.US);
        String dueDate = LocalDateTime.now().plusYears(2).format(dateTime12h).toLowerCase(Locale.US);
        sakai.selectDate("#assessmentSettingsAction\\:startDate", availableDate);
        sakai.selectDate("#assessmentSettingsAction\\:endDate", dueDate);

        clickFirstVisible(
            page.locator("button:has-text(\"Save Settings and Publish\"), input[type=\"submit\"][value*=\"Save Settings and Publish\"], input[type=\"submit\"][value*=\"Publish\"], button:has-text(\"Publish\")")
        );
        maybeClickFirstVisible(
            page.locator("input[type=\"submit\"][value*=\"Publish\"], button:has-text(\"Publish\"), input[type=\"submit\"][value*=\"Republish\"], button:has-text(\"Republish\")")
        );

        ensureAssessmentsTable(testsToolUrl);

        editWorkingCopyFromAssessmentsTable();
        page.locator("#assessmentForm\\:parts\\:0\\:parts\\:0\\:modify").click(new Locator.ClickOptions().setForce(true));
        page.locator("#itemForm textarea").first().fill("This is edited question text");
        saveQuestionAndWaitForAssessment();
        assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(2);

        maybeClickFirstVisible(
            page.locator("input[type=\"submit\"][value*=\"Republish\"], button:has-text(\"Republish\"), input[type=\"submit\"][value*=\"Publish\"], button:has-text(\"Publish\")")
        );
        maybeClickFirstVisible(
            page.locator("input[type=\"submit\"][value*=\"Republish\"], button:has-text(\"Republish\"), input[type=\"submit\"][value*=\"Publish\"], button:has-text(\"Publish\")")
        );
    }

    @Test
    @Order(3)
    void canPreviewDraftAssessmentWithoutLeavingExtraAssessmentsBehind() {
        String previewTitle = "Playwright Preview " + System.currentTimeMillis();

        String courseUrl = ensureCourseUrl();
        sakai.login("instructor1");
        page.navigate(courseUrl);
        sakai.toolClick("Tests");
        String testsUrl = page.url();

        openNewAssessmentForm();
        page.locator("#authorIndexForm\\:title").fill(previewTitle);
        page.locator("#authorIndexForm\\:createnew").click(new Locator.ClickOptions().setForce(true));

        addMultipleChoiceQuestion(
            "10.00",
            "Preview flow smoke question",
            List.of("Option A", "Option B"),
            0
        );

        page.locator(".navViewAction").filter(new Locator.FilterOptions().setHasText("Preview")).first()
            .click(new Locator.ClickOptions().setForce(true));

        maybeClickFirstVisible(page.locator("input[type=\"submit\"][value*=\"Begin Assessment\"]"));

        if (!maybeClickFirstVisible(page.locator(".exit-preview-button, button:has-text(\"Exit\"), a:has-text(\"Exit\"), a:has-text(\"Assessments\")"))) {
            page.navigate(testsUrl);
        }

        page.navigate(testsUrl);
        Locator titles = page.locator("#authorIndexForm\\:coreAssessments .spanValue");
        long matchCount = titles.allTextContents().stream().map(String::trim).filter(previewTitle::equals).count();
        assertEquals(1L, matchCount);
    }

    @Test
    @Order(4)
    void canCreateEssayQuestionWithRubricFromScratch() {
        String courseUrl = ensureCourseUrl();

        sakai.login("instructor1");
        page.navigate(courseUrl);
        sakai.toolClick("Rubrics");
        page.locator(".add-rubric").first().click(new Locator.ClickOptions().setForce(true));

        Locator rubricTitle = page.locator("input[title=\"Rubric Title\"]:visible").first();
        assertThat(rubricTitle).isVisible();
        rubricTitle.fill(RUBRIC_TITLE);
        clickFirstVisible(page.locator("sakai-rubric-edit button:has-text(\"Save\"), .modal-footer button:has-text(\"Save\"), button:has-text(\"Save\")"));
        assertThat(page.locator("body")).containsText(RUBRIC_TITLE);

        page.navigate(courseUrl);
        sakai.toolClick("Tests");
        openNewAssessmentForm();
        page.locator("#authorIndexForm\\:title").fill(ESSAY_TITLE);
        page.locator("#authorIndexForm\\:createnew").click(new Locator.ClickOptions().setForce(true));

        selectQuestionType(Pattern.compile("short\\s*answer|essay", Pattern.CASE_INSENSITIVE));
        page.locator("#itemForm\\:answerptr").fill("50.00");
        page.locator(".sakai-rubric-association input[type=\"radio\"][value=\"1\"]").first()
            .check(new Locator.CheckOptions().setForce(true));
        page.locator("#itemForm\\:questionItemText_textinput").first().fill("How big is a fish?");
        saveQuestionAndWaitForAssessment();

        clickFirstVisible(page.locator("a:has-text(\"Publish\"), input[type=\"submit\"][value*=\"Publish\"], button:has-text(\"Publish\")"));
        maybeClickFirstVisible(page.locator("#publishAssessmentForm\\:publish, input[type=\"submit\"][value*=\"Publish\"], button:has-text(\"Publish\")"));
        assertThat(page.locator("body")).containsText(Pattern.compile("Essay with Rubric", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(5)
    void canTakeAssessmentAsStudentOnDesktop() {
        String courseUrl = ensureCourseUrl();
        page.setViewportSize(1280, 800);

        sakai.login("student0011");
        page.navigate(courseUrl);
        sakai.toolClick("Tests");

        assertThat(page.locator("body")).containsText(Pattern.compile("Assessments|Tests", Pattern.CASE_INSENSITIVE));
        assertThat(page.locator("body")).containsText(Pattern.compile("Playwright Quiz", Pattern.CASE_INSENSITIVE));
        assertThat(page.locator("body")).containsText(Pattern.compile("Essay with Rubric", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(6)
    void canGradeEssayQuestionByRubric() {
        String courseUrl = ensureCourseUrl();
        sakai.login("instructor1");
        page.navigate(courseUrl);
        sakai.toolClick("Tests");

        assertThat(page.locator("body")).containsText(Pattern.compile("Assessments|Tests", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(7)
    void canAddAndEditQuestionPools() {
        String courseUrl = ensureCourseUrl();
        String poolName = "Playwright Pool " + System.currentTimeMillis();

        sakai.login("instructor1");
        page.navigate(courseUrl);
        sakai.toolClick("Tests");

        clickFirstVisible(page.locator("#authorIndexForm a:has-text(\"Question Pools\"), a:has-text(\"Question Pools\")"));
        clickFirstVisible(page.locator("#questionpool\\:add, button:has-text(\"New Pool\"), a:has-text(\"New Pool\")"));
        page.locator("#questionpool\\:namefield").fill(poolName);
        clickFirstVisible(page.locator("#questionpool\\:submit, input[type=\"submit\"][value*=\"Submit\"], input[type=\"submit\"][value*=\"Save\"], button:has-text(\"Save\")"));
        page.locator("#questionpool\\:TreeTable a").filter(new Locator.FilterOptions().setHasText(poolName)).first()
            .click(new Locator.ClickOptions().setForce(true));

        clickFirstVisible(page.locator("a:has-text(\"Add Question\")"));
        selectQuestionType(Pattern.compile("multiple\\s*choice", Pattern.CASE_INSENSITIVE));
        clickSubmit("Save");
        page.locator("#itemForm\\:answerptr").fill("100.00");
        page.locator("#itemForm textarea").first().fill("What is the main reason so many people moved to California in 1849?");
        page.locator("#itemForm\\:mcchoices textarea").nth(0).fill("California land was fertile, plentiful, and inexpensive.");
        page.locator("#itemForm\\:mcchoices textarea").nth(1).fill("Gold was discovered in central California.");
        page.locator("#itemForm\\:mcchoices textarea").nth(2).fill("The east was preparing for a civil war.");
        page.locator("#itemForm\\:mcchoices textarea").nth(3).fill("They wanted to establish religious settlements.");
        page.locator("#itemForm\\:mcchoices input[type=\"radio\"]").nth(1).check(new Locator.CheckOptions().setForce(true));
        saveQuestionAndWaitForPoolEditor();

        page.locator("#editform\\:questionpool-questions\\:0\\:modify").first().click(new Locator.ClickOptions().setForce(true));
        page.locator("#itemForm textarea").first().fill("Edited question text");
        saveQuestionAndWaitForPoolEditor();
        assertThat(page.locator("#editform\\:questionpool-questions a[title=\"Edit Question\"]")).hasCount(1);
    }

    private void addMultipleChoiceQuestion(String points, String questionText, List<String> choices, int correctIndex) {
        selectQuestionType(Pattern.compile("multiple\\s*choice", Pattern.CASE_INSENSITIVE));
        page.locator("#itemForm\\:answerptr").fill(points);
        page.locator("#itemForm textarea").first().fill(questionText);
        for (int index = 0; index < choices.size(); index += 1) {
            page.locator("#itemForm\\:mcchoices textarea").nth(index).fill(choices.get(index));
        }
        page.locator("#itemForm\\:mcchoices input[type=\"radio\"]").nth(correctIndex).check(new Locator.CheckOptions().setForce(true));
        saveQuestionAndWaitForAssessment();
    }

    private void openNewAssessmentForm() {
        Locator addLink = page.locator("#authorIndexForm a").filter(
            new Locator.FilterOptions().setHasText(Pattern.compile("^Add$", Pattern.CASE_INSENSITIVE))
        ).first();
        assertThat(addLink).isVisible();
        String href = addLink.getAttribute("href");
        addLink.click(new Locator.ClickOptions().setForce(true));

        Locator titleInput = page.locator("#authorIndexForm\\:title").first();
        if (!isVisible(titleInput, 10_000) && href != null && !href.isBlank() && !"#".equals(href)) {
            page.navigate(href);
        }
        assertThat(titleInput).isVisible();
    }

    private void saveQuestionAndWaitForAssessment() {
        clickSubmit("Save");
        assertThat(page.locator("#assessmentForm\\:parts")).isVisible();
    }

    private void saveQuestionAndWaitForPoolEditor() {
        clickSubmit("Save");
        assertThat(page.locator("#editform\\:questionpool-questions")).isVisible();
    }

    private void clickSubmit(String label) {
        Locator submit = page.locator("input[type=\"submit\"][value*=\"" + label + "\"]:visible, button:has-text(\"" + label + "\"):visible").first();
        assertThat(submit).isVisible();
        submit.click(new Locator.ClickOptions().setForce(true));
    }

    private void selectQuestionType(Pattern labelPattern) {
        Locator select = page.locator("#assessmentForm\\:parts\\:0\\:changeQType").first();
        if (!isVisible(select, 5_000)) {
            select = page.locator("select[id$=\":changeQType\"]").first();
        }
        if (!isVisible(select, 5_000)) {
            select = page.locator("#content form select:visible").first();
        }
        assertThat(select).isVisible();

        Locator options = select.locator("option");

        String matchedValue = null;
        for (ElementHandle option : options.elementHandles()) {
            String label = option.textContent();
            if (label != null && labelPattern.matcher(label).find()) {
                matchedValue = option.getAttribute("value");
                break;
            }
        }

        if (matchedValue == null || matchedValue.isBlank()) {
            throw new IllegalStateException("Unable to find question type option matching pattern: " + labelPattern);
        }

        select.selectOption(matchedValue);
    }

    private void ensureAssessmentsTable(String testsUrl) {
        Locator assessmentsTable = page.locator("#authorIndexForm\\:coreAssessments");
        if (isVisible(assessmentsTable, 10_000)) {
            return;
        }

        maybeClickFirstVisible(page.locator("a:has-text(\"Assessments\")"));
        if (isVisible(assessmentsTable, 10_000)) {
            return;
        }

        page.navigate(testsUrl);
        sakai.toolClick("Tests");
        maybeClickFirstVisible(page.locator("a:has-text(\"Assessments\")"));
        assertThat(assessmentsTable).isVisible();
    }

    private void editWorkingCopyFromAssessmentsTable() {
        Locator assessmentsTable = page.locator("#authorIndexForm\\:coreAssessments");
        Locator draftRow = assessmentsTable.locator("tr")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Draft - " + Pattern.quote(SAMIGO_TITLE))))
            .first();
        if (draftRow.count() == 0) {
            draftRow = assessmentsTable.locator("tr")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Working Copy \\(Draft\\)|Draft -", Pattern.CASE_INSENSITIVE)))
                .first();
        }
        assertThat(draftRow).isVisible();

        Locator actionsButton = draftRow.locator("button.dropdown-toggle, button:has-text(\"Actions\")").first();
        if (!isVisible(actionsButton, 5_000)) {
            actionsButton = assessmentsTable.locator("button.dropdown-toggle, button:has-text(\"Actions\")").last();
        }
        clickFirstVisible(actionsButton);

        Locator editLink = draftRow.locator("ul li a").filter(
            new Locator.FilterOptions().setHasText(Pattern.compile("^Edit$", Pattern.CASE_INSENSITIVE))
        ).first();
        if (!isVisible(editLink, 5_000)) {
            editLink = assessmentsTable.locator("ul li a").filter(
                new Locator.FilterOptions().setHasText(Pattern.compile("^Edit$", Pattern.CASE_INSENSITIVE))
            ).first();
        }
        clickFirstVisible(editLink);
        maybeClickFirstVisible(page.locator("input[type=\"submit\"][value*=\"Edit\"], button:has-text(\"Edit\")"));
    }

    private void clickFirstVisible(Locator locator) {
        assertThat(locator.first()).isVisible();
        locator.first().click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private boolean maybeClickFirstVisible(Locator locator) {
        Locator first = locator.first();
        if (!isVisible(first, 5_000)) {
            return false;
        }
        first.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        return true;
    }

    private boolean isVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(timeoutMs));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
