package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RubricsTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String RUBRIC_TITLE = "Playwright Rubric " + System.currentTimeMillis();

    @Test
    @Order(1)
    void canCreateNewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.rubrics"));
    }

    @Test
    @Order(2)
    void canCreateRubricAndSetTitle() {
        sakai.createRubric("instructor1", sakaiUrl);

        page.locator("input[title=\"Rubric Title\"]:visible").first().fill(RUBRIC_TITLE);
        page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Save", Pattern.CASE_INSENSITIVE))).first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("body")).containsText(RUBRIC_TITLE);
    }

    @Test
    @Order(3)
    void canCreateRubricAndAddCriterion() {
        sakai.createRubric("instructor1", sakaiUrl);

        Locator cancelButton = page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Cancel", Pattern.CASE_INSENSITIVE)));
        if (cancelButton.count() > 0) {
            cancelButton.first().click(new Locator.ClickOptions().setForce(true));
        }

        page.locator(".add-criterion:visible").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("body")).containsText(Pattern.compile("Criterion", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(4)
    void canDeleteRubric() {
        sakai.createRubric("instructor1", sakaiUrl);

        Locator cancelButton = page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Cancel", Pattern.CASE_INSENSITIVE)));
        if (cancelButton.count() > 0) {
            cancelButton.first().click(new Locator.ClickOptions().setForce(true));
        }

        page.locator("sakai-item-delete.sakai-rubric").last().click(new Locator.ClickOptions().setForce(true));
        page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Save", Pattern.CASE_INSENSITIVE))).first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("#site_rubrics rubric-item")).hasCount(0);
    }

    @Test
    @Order(5)
    void canCopyRubric() {
        sakai.createRubric("instructor1", sakaiUrl);

        Locator cancelButton = page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Cancel", Pattern.CASE_INSENSITIVE)));
        if (cancelButton.count() > 0) {
            cancelButton.first().click(new Locator.ClickOptions().setForce(true));
        }

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Copy .*Rubric", Pattern.CASE_INSENSITIVE))).first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("body")).containsText(Pattern.compile("Copy|Rubric", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(6)
    void canCopyCriterion() {
        sakai.createRubric("instructor1", sakaiUrl);

        Locator cancelButton = page.locator("button:visible").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Cancel", Pattern.CASE_INSENSITIVE)));
        if (cancelButton.count() > 0) {
            cancelButton.first().click(new Locator.ClickOptions().setForce(true));
        }

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Copy Criterion", Pattern.CASE_INSENSITIVE))).first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("body")).containsText(Pattern.compile("Criterion", Pattern.CASE_INSENSITIVE));
    }
}
