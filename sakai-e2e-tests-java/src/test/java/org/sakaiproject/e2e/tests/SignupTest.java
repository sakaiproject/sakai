package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SignupTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String TITLE = "Playwright Sign-Up " + System.currentTimeMillis();
    private static final String LOCATION = "Room 101";
    private static final String CATEGORY = "General";
    private static final String DESCRIPTION = "This is a Playwright-created sign-up item.";

    @Test
    @Order(1)
    void createsSiteWithSignUp() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.signup"));
    }

    @Test
    @Order(2)
    void addsAndPublishesSignup() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Sign-up");

        assertThat(page.locator(".portletBody").first()).isVisible();
        page.locator(".navIntraTool a, .navIntraTool button").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$", Pattern.CASE_INSENSITIVE))).first().click(new Locator.ClickOptions().setForce(true));

        if (page.locator("[name=\"meeting:name\"]").count() > 0) {
            page.locator("[name=\"meeting:name\"]").fill(TITLE);
        } else {
            page.locator("form input[type=\"text\"]:visible").first().fill(TITLE);
        }

        if (page.locator("[name=\"meeting:customLocation\"]").count() > 0) {
            page.locator("[name=\"meeting:customLocation\"]").fill(LOCATION);
        } else if (page.locator("[name=\"meeting:location\"]").count() > 0) {
            page.locator("[name=\"meeting:location\"]").fill(LOCATION);
        } else {
            page.locator("form input[type=\"text\"]:visible").nth(1).fill(LOCATION);
        }

        if (page.locator("[name=\"meeting:customCategory\"]").count() > 0) {
            Locator field = page.locator("[name=\"meeting:customCategory\"]").first();
            String tag = (String) field.evaluate("node => node.tagName.toLowerCase()");
            if ("select".equals(tag)) {
                field.selectOption(new SelectOption().setLabel(CATEGORY));
            } else {
                field.fill(CATEGORY);
            }
        } else if (page.locator("[name=\"meeting:category\"]").count() > 0) {
            Locator field = page.locator("[name=\"meeting:category\"]").first();
            String tag = (String) field.evaluate("node => node.tagName.toLowerCase()");
            if ("select".equals(tag)) {
                field.selectOption(new SelectOption().setLabel(CATEGORY));
            } else {
                field.fill(CATEGORY);
            }
        } else {
            Locator select = page.locator("form select:visible").first();
            if (select.count() > 0) {
                select.selectOption(new SelectOption().setLabel(CATEGORY));
            }
        }

        boolean hasCkEditor = Boolean.TRUE.equals(page.evaluate("() => Boolean(window.CKEDITOR && Object.keys(window.CKEDITOR.instances || {}).length)"));
        if (hasCkEditor) {
            String editorId = (String) page.evaluate("() => Object.keys(window.CKEDITOR.instances || {})[0]");
            sakai.typeCkEditor(editorId, "<p>" + DESCRIPTION + "</p>");
        } else {
            page.locator("textarea:visible, [contenteditable=\"true\"]:visible, div[role=\"textbox\"]:visible").first().fill(DESCRIPTION);
        }

        page.locator("button:has-text(\"Next\"), input[type=\"submit\"][value*=\"Next\"], .act button:has-text(\"Next\"), .act input[value*=\"Next\"]").first().click(new Locator.ClickOptions().setForce(true));
        page.locator("button:has-text(\"Publish\"), input[type=\"submit\"][value*=\"Publish\"], .act button:has-text(\"Publish\"), .act input[value*=\"Publish\"]").first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.getByRole(AriaRole.LINK, new com.microsoft.playwright.Page.GetByRoleOptions().setName(TITLE)).first()).isVisible();
    }
}
