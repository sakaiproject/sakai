package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
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

        if (!sakai.typeFirstCkEditorIfPresent("<p>" + DESCRIPTION + "</p>")) {
            page.locator("textarea:visible, [contenteditable=\"true\"]:visible, div[role=\"textbox\"]:visible").first().fill(DESCRIPTION);
        }

        page.locator("button:has-text(\"Next\"), input[type=\"submit\"][value*=\"Next\"], .act button:has-text(\"Next\"), .act input[value*=\"Next\"]").first().click(new Locator.ClickOptions().setForce(true));
        page.locator("form#meeting input#meeting\\:goNextPage[value=\"Publish\"]").first().click();
        page.waitForURL(Pattern.compile(".*/signupMeetings(?:\\?.*)?$"), new Page.WaitForURLOptions().setTimeout(30_000));

        sakai.toolClick("Sign-up");
        Locator meetingsTable = page.locator("[id=\"items:meetinglist\"], [id=\"meetinglist\"], [id$=\":meetinglist\"]").first();
        if (!meetingTitleVisible(meetingsTable)) {
            Locator viewByRange = page.locator("select[id=\"items:viewByRange\"], select[id=\"viewByRange\"], select[id$=\":viewByRange\"]").first();
            if (viewByRange.count() > 0 && viewByRange.isVisible() && viewByRange.isEnabled()) {
                @SuppressWarnings("unchecked")
                List<String> viewValues = (List<String>) viewByRange.locator("option")
                    .evaluateAll("options => options.filter(option => option.value && !option.disabled).map(option => option.value)");

                for (String viewValue : viewValues) {
                    viewByRange.selectOption(viewValue);
                    page.waitForLoadState();
                    if (meetingTitleVisible(meetingsTable)) {
                        return;
                    }
                }
            }
        } else {
            return;
        }

        assertThat(page.locator("body")).containsText(TITLE, new LocatorAssertions.ContainsTextOptions().setTimeout(15_000));
    }

    private boolean meetingTitleVisible(Locator meetingsTable) {
        if (meetingsTable.count() == 0 || !meetingsTable.isVisible()) {
            return false;
        }

        try {
            assertThat(meetingsTable).containsText(TITLE, new LocatorAssertions.ContainsTextOptions().setTimeout(2_000));
            return true;
        } catch (AssertionError ignored) {
            return false;
        }
    }
}
