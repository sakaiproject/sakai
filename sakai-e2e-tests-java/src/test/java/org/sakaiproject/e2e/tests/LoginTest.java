package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginTest extends SakaiUiTestBase {

    private static final String USERNAME = "instructor1";
    private static final String PASSWORD = "sakai";

    private String effectiveUsername() {
        return sakai.resolveUsername(USERNAME);
    }

    @Test
    @Order(1)
    void isForcedToAuthWhenNoSession() {
        page.navigate("/portal/site/!admin");
        assertThat(page.locator("h2")).containsText("Log in");
    }

    @Test
    @Order(2)
    void displaysErrorsOnLogin() {
        page.navigate("/portal/");
        page.locator("input[name=\"eid\"]").fill("badusername");
        page.locator("input[name=\"pw\"]").fill("sakai");
        page.locator("input[name=\"pw\"]").press("Enter");

        assertThat(page.locator("div.alert")).containsText("Invalid login");
        assertThat(page).hasURL(Pattern.compile("/portal/xlogin"));
    }

    @Test
    @Order(3)
    void redirectsToPortalOnSuccess() {
        String username = effectiveUsername();
        page.navigate("/portal/");
        page.locator("input[name=\"eid\"]").fill(username);
        page.locator("input[name=\"pw\"]").fill(PASSWORD);
        page.locator("input[name=\"pw\"]").press("Enter");

        assertThat(page).hasURL(Pattern.compile("/portal"));
        List<Cookie> cookies = page.context().cookies();
        boolean hasSakaiCookie = cookies.stream().anyMatch(cookie -> "SAKAIID".equals(cookie.name));
        assertTrue(hasSakaiCookie);
    }

    @Test
    @Order(4)
    void canBypassUiAndStillLoginByRequest() {
        String username = effectiveUsername();
        APIResponse response = page.request().post("/portal/",
            RequestOptions.create().setForm(FormData.create()
                .set("username", username)
                .set("password", PASSWORD)));

        int status = response.status();
        assertTrue(status == 200 || status == 302 || status == 303);
        List<Cookie> cookies = page.context().cookies();
        boolean hasSakaiCookie = cookies.stream().anyMatch(cookie -> "SAKAIID".equals(cookie.name));
        assertTrue(hasSakaiCookie);
    }

    @Test
    @Order(5)
    void reusableLoginHelperCanAccessProtectedPages() {
        String username = effectiveUsername();
        sakai.login(username);

        page.navigate("/portal/");
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Home$", Pattern.CASE_INSENSITIVE))).first()).isVisible();

        page.navigate("/portal/site/~" + username);
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Preferences$", Pattern.CASE_INSENSITIVE))).first()).isVisible();
    }
}
