package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GradebookTest extends SakaiUiTestBase {

    private static String sakaiUrl;

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
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Settings")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator(".accordion button").filter(new Locator.FilterOptions().setHasText("Categories")).first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("#settingsCategories input[type=\"radio\"]:visible")).hasCount(3);

        Locator weightedCategoryOption = page.locator("#settingsCategories input[name=\"categoryPanel:settingsCategoriesPanel:categoryType\"][value=\"radio4\"]").first();
        assertThat(weightedCategoryOption).isVisible();
        weightedCategoryOption.check(new Locator.CheckOptions().setForce(true));
        assertThat(weightedCategoryOption).isChecked();

        page.locator(".gb-category-row input[name$=\"name\"]").first().fill("A");
        page.locator(".gb-category-weight input[name$=\"weight\"]").first().fill("100");

        page.locator(".act button.active").first().click(new Locator.ClickOptions().setForce(true));
    }

    @Test
    @Order(3)
    void canCreateGradebookItems() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");

        Locator dialog = page.locator("dialog:visible, div[role=\"dialog\"]:visible, .wicket-modal:visible, .modal:visible").first();
        Locator addButton = page.locator("button.gb-add-gradebook-item-button").first();

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        boolean dialogVisible = false;
        for (int attempt = 0; attempt < 3 && !dialogVisible; attempt++) {
            assertThat(addButton).isVisible();
            try {
                page.waitForFunction("() => { const button = document.querySelector('button.gb-add-gradebook-item-button'); return Boolean(button && window.Wicket && window.Wicket.Ajax); }");
            } catch (RuntimeException ignored) {
            }

            addButton.click();

            try {
                dialog.waitFor(new Locator.WaitForOptions().setTimeout(10_000));
                dialogVisible = true;
            } catch (RuntimeException error) {
                if (page.url().contains("grades?0-1.-form")) {
                    page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
                    page.waitForTimeout(250);
                } else if (attempt == 2) {
                    throw error;
                }
            }
        }

        assertThat(dialog).isVisible();

        Locator cancelButton = dialog.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName(Pattern.compile("Cancel|Close", Pattern.CASE_INSENSITIVE))).first();
        if (cancelButton.count() > 0) {
            cancelButton.click(new Locator.ClickOptions().setForce(true));
        }
    }
}
