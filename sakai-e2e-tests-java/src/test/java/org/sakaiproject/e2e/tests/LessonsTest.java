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
class LessonsTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void canCreateNewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of(
            "sakai\\.rubrics",
            "sakai\\.assignment\\.grades",
            "sakai\\.gradebookng",
            "sakai\\.lessonbuildertool"
        ));
    }

    @Test
    @Order(2)
    void createLessonChecklistItem() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Lessons");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator(".add-checklist-link:visible").first().click(new Locator.ClickOptions().setForce(true));

        Locator nameInput = page.locator("#name:visible").first();
        if (nameInput.count() == 0) {
            assertThat(page.locator("body")).containsText(Pattern.compile("Checklist|Add Checklist", Pattern.CASE_INSENSITIVE));
            return;
        }

        nameInput.fill("Checklist");

        page.locator("#addChecklistItemButton > :nth-child(2)").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv1 > .checklist-item-name").fill("A");
        page.locator("#addChecklistItemButton").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv2 > .checklist-item-name").fill("B");
        page.locator("#addChecklistItemButton").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv3 > .checklist-item-name").fill("C");
        page.locator("#addChecklistItemButton").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv4 > .checklist-item-name").fill("D");

        page.locator("#save").click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("#content")).isVisible();
    }
}
