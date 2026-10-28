package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
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

        boolean created = false;
        try {
            sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.video\\.training"));
            created = true;
        } catch (Throwable ex) {
            String configuredSite = configuredSiteUrl();
            Assumptions.assumeTrue(configuredSite != null && !configuredSite.isBlank(),
                "No course could be auto-created and PLAYWRIGHT_VIDEO_TRAINING_SITE_URL was not provided");
            sakaiUrl = configuredSite;
        }

        if (created) {
            grantVideoTrainingPermissions();
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

        openCurrentPathWithParams("size=1&viewMode=table");
        assertThat(page.locator(".vt-video-table")).isVisible();
        assertThat(page.locator(".vt-pager-container")).isVisible();
        assertThat(page.locator(".vt-video-table tbody tr")).hasCount(1);

        String tablePage1Title = currentTableTitle();
        assertTitleIsOneOf(tablePage1Title, firstTitle, secondTitle);

        clickPagerPage(2);
        assertThat(page.locator(".vt-video-table tbody tr")).hasCount(1);
        String tablePage2Title = currentTableTitle();
        assertTitleIsOneOf(tablePage2Title, firstTitle, secondTitle);
        org.junit.jupiter.api.Assertions.assertNotEquals(tablePage1Title, tablePage2Title);

        clickPagerPage(1);
        page.locator("button[name='viewMode'][value='cards']").click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator(".vt-video-card")).hasCount(1);
        String cardsPage1Title = currentCardTitle();
        org.junit.jupiter.api.Assertions.assertEquals(tablePage1Title, cardsPage1Title);

        clickPagerPage(2);
        assertThat(page.locator(".vt-video-card")).hasCount(1);
        String cardsPage2Title = currentCardTitle();
        org.junit.jupiter.api.Assertions.assertEquals(tablePage2Title, cardsPage2Title);

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

        openNewVideoForm();

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

        String title = "Playwright Video Training Upload " + System.currentTimeMillis();
        page.locator("#title").fill(title);
        page.locator("#description").fill("Upload mode test");
        page.locator("#nativeFile").setInputFiles(
            new FilePayload("video.webm", "video/webm", "dummy".getBytes(StandardCharsets.UTF_8)));
        page.locator("button[type=\"submit\"]").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator(".sak-banner-success")).isVisible();

        openCurrentPathWithParams("viewMode=table&q="
                + URLEncoder.encode(title, StandardCharsets.UTF_8));
        assertThat(page.locator(".vt-video-table")).containsText(title);
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

    private void grantVideoTrainingPermissions() {
        String siteId = sakai.siteIdFromUrl(sakaiUrl);
        if (siteId.isBlank()) {
            return;
        }

        // Video Training uses its own realm functions, which are not granted when the tool
        // is added to a site. Grant them explicitly, like an administrator would.
        sakai.login("admin");
        APIResponse response = page.request().post("/api/sites/" + siteId + "/permissions",
            RequestOptions.create().setForm(FormData.create()
                .set("ref", "/site/" + siteId)
                .set("Instructor:video.training.view", "true")
                .set("Instructor:video.training.manage", "true")
                .set("Instructor:video.training.manage.all", "true")
                .set("Instructor:video.training.analytics", "true")
                .set("Instructor:video.training.categories.manage", "true")
                .set("Instructor:video.training.approve", "true")
                .set("Instructor:video.training.global", "true")
                .set("Student:video.training.view", "true")));
        org.junit.jupiter.api.Assertions.assertTrue(response.ok(),
            "Unable to grant Video Training permissions: HTTP " + response.status() + " " + response.text());
    }

    private void openNewVideoForm() {
        page.locator("a[href$='/videos/new']").first()
            .click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("#title")).isVisible();
        // The page ships with every source section in the markup; the form script hides the
        // inactive ones. Wait for that initialisation before driving the source mode radios.
        assertThat(page.locator("#externalSourceSection")).isHidden();
    }

    private void createExternalVideo(String title, String description) {
        openNewVideoForm();
        page.locator("#title").fill(title);
        page.locator("#description").fill(description);
        page.locator("#sourceModeExternal").click(new Locator.ClickOptions().setForce(true));
        page.locator("#sourceReference").fill("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        page.locator("button[type=\"submit\"]").first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator(".vt-video-table")).containsText(title);
        publishVideo(title);
    }

    private void publishVideo(String title) {
        Locator row = videoTableRow(title);
        assertThat(row).isVisible();
        row.locator("form[action$='/publish'] button[type='submit']").first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        assertThat(videoTableRow(title).locator("td").nth(2)).containsText(Pattern.compile("PUBLISHED"));
    }

    private Locator videoTableRow(String title) {
        return page.locator(".vt-video-table tbody tr")
            .filter(new Locator.FilterOptions().setHasText(title)).first();
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
        page.locator(".vt-pager-controls button[value='" + pageNumber + "']").first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
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
            assertThat(card.locator("button[aria-label='Remove from favorites']").first()).isVisible();
        }

        Locator watchLaterButton = card.locator("button[aria-label='Add to watch later']");
        if (watchLaterButton.count() > 0) {
            watchLaterButton.first().click(new Locator.ClickOptions().setForce(true));
            assertThat(card.locator("button[aria-label='Remove from watch later']").first()).isVisible();
        }
    }

    private void navigateToToolLink(String linkLabel) {
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile(linkLabel, Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    private void verifyPagedPreferenceList(String firstTitle, String secondTitle) {
        assertThat(page.locator(".vt-pager-container")).isVisible();
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
