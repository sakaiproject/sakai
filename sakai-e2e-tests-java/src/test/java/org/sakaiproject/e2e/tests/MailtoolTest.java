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
class MailtoolTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String SUBJECT = "Playwright Mail " + System.currentTimeMillis();
    private static final String BODY = "This is a Playwright test email body.";

    @Test
    @Order(1)
    void createsSiteWithEmail() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.mailtool"));
    }

    @Test
    @Order(2)
    void sendsEmailToAll() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Email");

        Locator allLabel = page.locator("label")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^All$")))
            .first();
        if (allLabel.count() > 0) {
            String forId = allLabel.getAttribute("for");
            if (forId != null && !forId.isBlank()) {
                String escaped = forId.replace(":", "\\:").replace(".", "\\.");
                page.locator("#" + escaped).check(new Locator.CheckOptions().setForce(true));
            } else {
                allLabel.locator("..").locator("input[type=\"checkbox\"]").first().check(new Locator.CheckOptions().setForce(true));
            }
        } else {
            page.locator("input[type=\"checkbox\"]:visible").first().check(new Locator.CheckOptions().setForce(true));
        }

        page.locator("form input[type=\"text\"]:visible").first().fill(SUBJECT);

        boolean hasCkEditor = Boolean.TRUE.equals(page.evaluate("() => Boolean(window.CKEDITOR && Object.keys(window.CKEDITOR.instances || {}).length)"));
        if (hasCkEditor) {
            String editorId = (String) page.evaluate("() => Object.keys(window.CKEDITOR.instances || {})[0]");
            sakai.typeCkEditor(editorId, "<p>" + BODY + "</p>");
        } else {
            page.locator("textarea:visible, [contenteditable=\"true\"]:visible, div[role=\"textbox\"]:visible").first().fill(BODY);
        }

        page.locator("button:has-text(\"Send Mail\"), input[type=\"submit\"][value*=\"Send Mail\"]").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.getByText(Pattern.compile("Message sent to", Pattern.CASE_INSENSITIVE))).isVisible();
    }
}
