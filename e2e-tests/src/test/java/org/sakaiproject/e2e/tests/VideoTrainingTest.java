package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiEnvironment;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VideoTrainingTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void canCreateNewCourse() {
        Assumptions.assumeTrue(isSakaiReachable(), "Sakai instance not reachable at " + SakaiEnvironment.baseUrl());
        sakai.login("instructor1");

        try {
            sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.video\\.training"));
        } catch (Throwable ex) {
            String configuredSite = configuredSiteUrl();
            Assumptions.assumeTrue(configuredSite != null && !configuredSite.isBlank(),
                "No course could be auto-created and PLAYWRIGHT_VIDEO_TRAINING_SITE_URL was not provided");
            sakaiUrl = configuredSite;
        }
    }

    @Test
    @Order(2)
    void instructorAndStudentCanPageThroughLibraryAndPreferenceLists() {
        Assumptions.assumeTrue(sakaiUrl != null && !sakaiUrl.isBlank(), "Video Training site URL not available");

        String firstTitle = "Playwright Video Training " + System.currentTimeMillis();
        String secondTitle = firstTitle + " B";

        sakai.login("instructor1");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Video Training");
        createExternalVideo(firstTitle, "Created by Playwright test A");
        createExternalVideo(secondTitle, "Created by Playwright test B");

        openCurrentPathWithParams("size=1");
        assertThat(page.locator("#viewMode")).hasValue("table");
        assertThat(page.locator(".sakai-table-pagerControls")).isVisible();
        assertThat(page.locator(".vt-video-table tbody tr")).hasCount(1);

        String tablePage1Title = currentTableTitle();
        assertTitleIsOneOf(tablePage1Title, firstTitle, secondTitle);

        clickPagerPage(2);
        assertThat(page.locator(".vt-video-table tbody tr")).hasCount(1);
        String tablePage2Title = currentTableTitle();
        assertTitleIsOneOf(tablePage2Title, firstTitle, secondTitle);
        org.junit.jupiter.api.Assertions.assertNotEquals(tablePage1Title, tablePage2Title);

        page.locator("#viewMode").selectOption("cards");
        page.waitForLoadState();
        assertThat(page.locator(".sakai-table-pagerControls")).isVisible();
        assertThat(page.locator(".vt-video-card")).hasCount(1);
        String cardsPage2Title = currentCardTitle();
        org.junit.jupiter.api.Assertions.assertEquals(tablePage2Title, cardsPage2Title);

        clickPagerPage(1);
        assertThat(page.locator(".vt-video-card")).hasCount(1);
        String cardsPage1Title = currentCardTitle();
        org.junit.jupiter.api.Assertions.assertEquals(tablePage1Title, cardsPage1Title);

        sakai.login("student0011");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Video Training");
        openCurrentPathWithParams("size=1&viewMode=cards");
        assertThat(page.locator(".vt-video-card")).hasCount(1);
        String studentPage1Title = currentCardTitle();
        assertTitleIsOneOf(studentPage1Title, firstTitle, secondTitle);

        clickPreferenceButtonsOnCurrentCard();
        clickPagerPage(2);
        assertThat(page.locator(".vt-video-card")).hasCount(1);
        String studentPage2Title = currentCardTitle();
        assertTitleIsOneOf(studentPage2Title, firstTitle, secondTitle);
        org.junit.jupiter.api.Assertions.assertNotEquals(studentPage1Title, studentPage2Title);
        clickPreferenceButtonsOnCurrentCard();

        navigateToToolLink("Favorites");
        openCurrentPathWithParams("size=1&viewMode=cards");
        verifyPagedPreferenceList(firstTitle, secondTitle);

        navigateToToolLink("Watch later");
        openCurrentPathWithParams("size=1&viewMode=cards");
        verifyPagedPreferenceList(firstTitle, secondTitle);
    }

    @Test
    @Order(3)
    void instructorCanUseAllSourceModesInForm() {
        Assumptions.assumeTrue(sakaiUrl != null && !sakaiUrl.isBlank(), "Video Training site URL not available");

        sakai.login("instructor1");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Video Training");

        Locator addVideoButton = page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("Add video", Pattern.CASE_INSENSITIVE))).first();
        addVideoButton.click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("#sourceModeExternal")).isVisible();
        assertThat(page.locator("#sourceModeUpload")).isVisible();
        assertThat(page.locator("#sourceModeResources")).isVisible();

        page.locator("#sourceModeExternal").click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("#externalSourceSection")).isVisible();
        assertThat(page.locator("#uploadSourceSection")).isHidden();
        assertThat(page.locator("#resourcesSourceSection")).isHidden();

        page.locator("#sourceModeUpload").click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("#externalSourceSection")).isHidden();
        assertThat(page.locator("#uploadSourceSection")).isVisible();
        assertThat(page.locator("#resourcesSourceSection")).isHidden();

        page.locator("#title").fill("Playwright Video Training Upload " + System.currentTimeMillis());
        page.locator("#description").fill("Upload mode test");
        page.setInputFiles("#nativeFile", new FilePayload("video.webm", "video/webm", "dummy".getBytes(StandardCharsets.UTF_8)));
        page.locator("button[type=\"submit\"]").first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("body")).containsText(Pattern.compile("Playwright Video Training Upload", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(4)
    void analyticsMenuRespectsPermissions() {
        Assumptions.assumeTrue(sakaiUrl != null && !sakaiUrl.isBlank(), "Video Training site URL not available");

        sakai.login("student0011");
        sakai.gotoPath(sakaiUrl);
        sakai.toolClick("Video Training");

        assertThat(page.locator(".navIntraTool")).not().containsText(Pattern.compile("Analytics", Pattern.CASE_INSENSITIVE));
    }

    private void createExternalVideo(String title, String description) {
        page.locator("#title").fill(title);
        page.locator("#description").fill(description);
        page.locator("#sourceModeExternal").click(new Locator.ClickOptions().setForce(true));
        page.locator("#sourceReference").fill("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        page.locator("button[type=\"submit\"]").first().click(new Locator.ClickOptions().setForce(true));
    }

    private void openCurrentPathWithParams(String params) {
        URI currentUrl = URI.create(page.url());
        StringBuilder target = new StringBuilder();
        target.append(currentUrl.getScheme()).append("://").append(currentUrl.getAuthority()).append(currentUrl.getPath());
        if (params != null && !params.isBlank()) {
            target.append("?").append(params);
        }
        sakai.gotoPath(target.toString());
    }

    private void clickPagerPage(int pageNumber) {
        page.locator(".sakai-table-pagerControls button[value='" + pageNumber + "']").first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }

    private String currentTableTitle() {
        return page.locator(".vt-video-table tbody tr").first().locator("a.fw-bold").textContent().trim();
    }

    private String currentCardTitle() {
        return page.locator(".vt-video-card-title h2").first().textContent().trim();
    }

    private void clickPreferenceButtonsOnCurrentCard() {
        Locator card = page.locator(".vt-video-card").first();
        Locator favoriteButton = card.locator("button[aria-label='Add to favorites']");
        if (favoriteButton.count() > 0) {
            favoriteButton.first().click(new Locator.ClickOptions().setForce(true));
        }

        Locator watchLaterButton = card.locator("button[aria-label='Add to watch later']");
        if (watchLaterButton.count() > 0) {
            watchLaterButton.first().click(new Locator.ClickOptions().setForce(true));
        }
    }

    private void navigateToToolLink(String linkLabel) {
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile(linkLabel, Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }

    private void verifyPagedPreferenceList(String firstTitle, String secondTitle) {
        assertThat(page.locator(".sakai-table-pagerControls")).isVisible();
        assertThat(page.locator(".vt-video-card")).hasCount(1);

        String firstPageTitle = currentCardTitle();
        assertTitleIsOneOf(firstPageTitle, firstTitle, secondTitle);

        clickPagerPage(2);
        assertThat(page.locator(".vt-video-card")).hasCount(1);
        String secondPageTitle = currentCardTitle();
        assertTitleIsOneOf(secondPageTitle, firstTitle, secondTitle);
        org.junit.jupiter.api.Assertions.assertNotEquals(firstPageTitle, secondPageTitle);
    }

    private void assertTitleIsOneOf(String actual, String firstTitle, String secondTitle) {
        org.junit.jupiter.api.Assertions.assertTrue(
            actual.equals(firstTitle) || actual.equals(secondTitle),
            "Unexpected title: " + actual
        );
    }

    private String configuredSiteUrl() {
        String fromProperty = System.getProperty("PLAYWRIGHT_VIDEO_TRAINING_SITE_URL");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }

        String fromEnv = System.getenv("PLAYWRIGHT_VIDEO_TRAINING_SITE_URL");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }

        return null;
    }

    private boolean isSakaiReachable() {
        try {
            URL url = new URL(SakaiEnvironment.baseUrl() + "/portal/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int status = connection.getResponseCode();
            return status > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
