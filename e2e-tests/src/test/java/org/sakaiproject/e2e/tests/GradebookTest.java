package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GradebookTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    private static String toolId = "sakai.gradebookng";

    @Test
    @Order(1)
    void canCreateNewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng"));
    }

    @Test
    @Order(2)
    void canCreateGradebookCategories() {
        sakai.login("instructor1");
        if (sakaiUrl == null) {
            sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng"));
        }
        page.navigate(sakaiUrl);
        sakai.toolClick(toolId, true);

        Locator settingsLink = page.getByTestId("settings-page-link");
        assertThat(settingsLink).isVisible();
        settingsLink.click(new Locator.ClickOptions().setForce(true));

        page.getByTestId("settings-categories-accordion-button").click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("#settingsCategories input[type=\"radio\"]:visible")).hasCount(3);

        Locator weightedCategoryOption = page.getByTestId("categories-and-weighting-radio").first();
        assertThat(weightedCategoryOption).isVisible();
        weightedCategoryOption.check(new Locator.CheckOptions().setForce(true));
        assertThat(weightedCategoryOption).isChecked();

        page.locator(".gb-category-row input[name$=\"name\"]").first().fill("A");
        page.locator(".gb-category-row input[name$=\"weight\"]").first().fill("100");

        page.getByTestId("settings-submit-button").click(new Locator.ClickOptions().setForce(true));
    }

    @Test
    @Order(3)
    void canCreateGradebookItems() {
        sakai.login("instructor1");
        if (sakaiUrl == null) {
            sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng"));
        }
        page.navigate(sakaiUrl);
        sakai.toolClick(toolId, true);

        Locator dialog = launchAddItemDialog();

        Locator cancelButton = dialog.getByTestId("cancel-button").first();
        if (cancelButton.count() > 0) {
            cancelButton.click(new Locator.ClickOptions().setForce(true));
        }
    }

    @Test
    @Order(4)
    void canSelectLetterGrading() {
        sakai.login("instructor1");
        if (sakaiUrl == null) {
            sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng"));
        }
        page.navigate(sakaiUrl);
        sakai.toolClick(toolId, true);

        selectLetterGrading();
    }

    @Test
    @Order(5)
    void canLetterGrade() {
        sakai.login("instructor1");
        if (sakaiUrl == null) {
            sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng"));
        }
        page.navigate(sakaiUrl);
        sakai.toolClick(toolId, true);

        selectLetterGrading();

        String itemTitle = "Sports";
        createLetterItem(itemTitle);

        Locator itemHeader = page.locator("div.tabulator-col[aria-label='" + itemTitle + "']").first();
        assertThat(itemHeader).isVisible();
        String assignmentId = itemHeader.getAttribute("data-assignment-id");

        Locator itemCell = page.locator("div.tabulator-cell[data-assignment-id='" + assignmentId + "']").first();
        assertThat(itemCell).isVisible();

        itemCell.dblclick();

        Locator list = page.locator("div.tabulator-edit-list").first();

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        boolean editorVisible = false;
        for (int attempt = 0; attempt < 3 && !editorVisible; attempt++) {
            itemCell.dblclick();

            try {
                list.waitFor(new Locator.WaitForOptions().setTimeout(10_000));
                editorVisible = true;
            } catch (RuntimeException error) {
                page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
                page.waitForTimeout(250);
            }
        }

        assertThat(list).isVisible();
        Locator aPlus = page.locator("div.tabulator-edit-list > div").first();
        aPlus.waitFor(new Locator.WaitForOptions().setTimeout(10_000));
        assertThat(aPlus).isVisible();
        assertThat(aPlus).hasText("A+");
        aPlus.click();

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        itemCell = page.locator("div.tabulator-cell[data-assignment-id='" + assignmentId + "'] span.gb-value").first();
        assertThat(itemCell).hasText("A+");
    }

    @Test
    @Order(6)
    void canRapidLetterGrade() {
        sakai.login("instructor1");
        if (sakaiUrl == null) {
            sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng"));
        }
        page.navigate(sakaiUrl);
        sakai.toolClick(toolId, true);

        selectLetterGrading();

        String itemTitle = "Eggs";
        createLetterItem(itemTitle);

        Locator quickEntryLink = page.getByTestId("quick-entry-link");
        assertThat(quickEntryLink).isVisible();

        quickEntryLink.click();

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        Locator itemPicker = page.getByTestId("quick-entry-item-picker");
        itemPicker.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));
        assertThat(itemPicker).isVisible();
        itemPicker.selectOption(itemTitle);

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        String newGrade = "A+";

        Locator gradePicker = page.locator("form tr:first-child select.enabledGrade");
        assertThat(gradePicker).isVisible();
        gradePicker.selectOption(newGrade);
        page.getByTestId("quick-entry-submit-button").click(new Locator.ClickOptions().setForce(true));

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        Locator gradesLink = page.getByTestId("grades-link");
        assertThat(gradesLink).isVisible();
        gradesLink.click(new Locator.ClickOptions().setForce(true));

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        Locator itemCell = page.locator(".gb-editable > .gb-value").first();
        assertThat(itemCell).isVisible();
        assertThat(itemCell).hasText(newGrade);
    }

    private void createLetterItem(String itemTitle) {
        Locator gradesLink = page.getByTestId("grades-link");
        assertThat(gradesLink).isVisible();
        gradesLink.click(new Locator.ClickOptions().setForce(true));

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        Locator dialog = launchAddItemDialog();

        assertThat(dialog.locator("input[name$='points']")).hasCount(0);
        Locator title = dialog.locator("input[name$='title']").first();
        assertThat(title).isVisible();

        title.fill(itemTitle);

        Locator submitButton = dialog.getByTestId("edit-grade-item-submit-button");
        assertThat(submitButton).isVisible();
        submitButton.click();

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    private void selectLetterGrading() {
        Locator settingsLink = page.getByTestId("settings-page-link").first();
        assertThat(settingsLink).isVisible();
        settingsLink.click(new Locator.ClickOptions().setForce(true));

        page.getByTestId("settings-grade-entry-accordion-button").click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("#settingsGradeEntry input[type=\"radio\"]:visible")).hasCount(3);

        Locator letterOption = page.getByTestId("letter-grade-entry-radio").first();
        assertThat(letterOption).isVisible();
        letterOption.check(new Locator.CheckOptions().setForce(true));
        assertThat(letterOption).isChecked();

        page.getByTestId("settings-submit-button").click(new Locator.ClickOptions().setForce(true));

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    private Locator launchAddItemDialog() {
        Locator addButton = page.locator("button.gb-add-gradebook-item-button");
        addButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));
        assertThat(addButton).isVisible();
        addButton.click();
        return waitForWicketModal();
    }
}
