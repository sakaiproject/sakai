package org.sakaiproject.e2e.support;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SakaiHelper {

    private static final Pattern MM_DD_YYYY_12H = Pattern.compile(
        "^(\\d{1,2})/(\\d{1,2})/(\\d{4})\\s+(\\d{1,2}):(\\d{2})\\s*(am|pm)$",
        Pattern.CASE_INSENSITIVE
    );
    private static final Map<String, String> COURSE_URL_CACHE = new ConcurrentHashMap<>();

    private final Page page;
    private final String baseUrl;
    private final String isolationKey;

    public SakaiHelper(Page page, String baseUrl, String isolationKey) {
        this.page = page;
        this.baseUrl = baseUrl;
        this.isolationKey = (isolationKey == null || isolationKey.isBlank()) ? "default" : isolationKey;
    }

    public String randomId() {
        return Long.toString(System.currentTimeMillis());
    }

    public String resolveUsername(String username) {
        return resolveUser(username);
    }

    public void gotoPath(String pathOrUrl) {
        String url = absoluteUrl(pathOrUrl);
        withTransientRetry(() -> page.navigate(url));
    }

    public void login(String username) {
        String resolvedUsername = resolveUser(username);

        page.context().clearCookies();
        page.navigate("about:blank");
        gotoPath("/portal/");

        Locator usernameInput = page.locator("input[name=\"eid\"], #eid").first();
        if (waitForVisible(usernameInput, 5000)) {
            usernameInput.fill(resolvedUsername);
            page.locator("input[name=\"pw\"], #pw").first().fill(passwordFor(resolvedUsername));

            Locator submit = page.locator("#submit, button[type=\"submit\"], input[type=\"submit\"]").first();
            submit.click(new Locator.ClickOptions().setForce(true));
            page.waitForLoadState();
        }

        dismissTutorial();
        setTutorialFlags();
        assertThat(page.locator("body")).isVisible();
    }

    public void toolClick(String label) {
        boolean clicked = clickToolByText(label);
        if (clicked) {
            return;
        }

        Locator expandButtons = page.locator("button[title*=\"Expand tool list\"], button[aria-label*=\"Expand tool list\"]");
        if (expandButtons.count() > 0 && expandButtons.first().isVisible()) {
            expandButtons.first().click(new Locator.ClickOptions().setForce(true));
        }

        Locator allSitesButton = page.locator("button.responsive-allsites-button").first();
        if (allSitesButton.count() > 0 && allSitesButton.isVisible()) {
            allSitesButton.click(new Locator.ClickOptions().setForce(true));
        }

        if (!clickToolByText(label)) {
            throw new IllegalStateException("Unable to click tool navigation item: " + label);
        }
    }

    public void selectDate(String selector, String value) {
        Locator input = page.locator(selector).first();
        assertThat(input).isVisible();
        input.click(new Locator.ClickOptions().setForce(true));

        String type = input.getAttribute("type");
        if ("datetime-local".equals(type)) {
            input.fill(toDateTimeLocal(value));
        } else {
            input.fill(value);
        }

        input.dispatchEvent("input");
        input.dispatchEvent("change");
        input.dispatchEvent("blur");
    }

    public String createCourse(String username, List<String> toolIds) {
        String resolvedUsername = resolveUser(username);
        String courseCacheKey = courseCacheKey(resolvedUsername, toolIds);
        String cachedCourseUrl = COURSE_URL_CACHE.get(courseCacheKey);
        if (cachedCourseUrl != null && !cachedCourseUrl.isBlank()) {
            return cachedCourseUrl;
        }

        gotoPath("/portal/site/~" + resolvedUsername);
        dismissTutorial();

        Locator worksiteSetup = page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Worksite Setup$", Pattern.CASE_INSENSITIVE))).first();
        assertThat(worksiteSetup).isVisible();
        worksiteSetup.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
        dismissTutorial();

        Locator addCourseForm = page.locator("form[name=\"addCourseForm\"]");
        if (!waitForVisible(addCourseForm, 2000)) {
            boolean opened = clickVisible(page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Create New Site$", Pattern.CASE_INSENSITIVE))).first())
                || clickVisible(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Create New Site$", Pattern.CASE_INSENSITIVE))).first())
                || clickVisible(page.locator(".navIntraTool a:has-text(\"Create New Site\"), .navIntraTool button:has-text(\"Create New Site\"), a:has-text(\"Create New Site\"), button:has-text(\"Create New Site\")").first());
            if (opened) {
                page.waitForLoadState();
            }
        }

        if (!waitForVisible(addCourseForm, 2000)) {
            Locator courseRadio = page.locator("input#course").first();
            assertThat(courseRadio).isVisible();
            courseRadio.click(new Locator.ClickOptions().setForce(true));

            Locator termSelect = page.locator("select#selectTerm").first();
            if (termSelect.count() > 0 && termSelect.isVisible()) {
                List<ElementHandle> options = termSelect.locator("option").elementHandles();
                if (options.size() > 1) {
                    String value = options.get(1).getAttribute("value");
                    if (value != null && !value.isBlank()) {
                        termSelect.selectOption(value);
                    }
                }
            }

            Locator submitBuildOwn = page.locator("input#submitBuildOwn").first();
            submitBuildOwn.click(new Locator.ClickOptions().setForce(true));
            page.waitForLoadState();
        }

        assertThat(addCourseForm).isVisible();

        String addCourseText = text(addCourseForm);
        if (addCourseText.contains("select anyway")) {
            Locator selectAnyway = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("select anyway", Pattern.CASE_INSENSITIVE))).first();
            if (selectAnyway.count() > 0 && selectAnyway.isVisible()) {
                selectAnyway.click(new Locator.ClickOptions().setForce(true));
            }
        }

        ensureAnyVisibleCheckboxChecked(addCourseForm.locator("input[type=\"checkbox\"]"));

        Locator courseDesc = page.locator("form input#courseDesc1");
        if (courseDesc.count() > 0 && courseDesc.first().isVisible()) {
            courseDesc.first().click(new Locator.ClickOptions().setForce(true));
        }

        clickContinue(addCourseForm);
        Locator siteTitle = page.locator("textarea").last();
        assertThat(siteTitle).isVisible();
        siteTitle.fill("Playwright Java Testing " + randomId());
        clickContinue(null);

        Locator manageTools = page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(Pattern.compile("Manage Tools", Pattern.CASE_INSENSITIVE))).first();
        if (manageTools.count() > 0) {
            assertThat(manageTools).isVisible();
        }

        Map<String, String> toolLabelFallbacks = toolLabelFallbacks();

        for (String rawToolId : toolIds) {
            String toolId = normalizeToolId(rawToolId);
            Locator checkbox = page.locator("input#" + cssEscape(toolId)).first();
            boolean selected = false;
            if (checkbox.count() > 0 && checkbox.isVisible()) {
                checkbox.check(new Locator.CheckOptions().setForce(true));
                selected = true;
            }

            if (!selected) {
                String label = toolLabelFallbacks.get(toolId);
                if (label != null) {
                    Locator fallbackCheckbox = page.getByRole(AriaRole.CHECKBOX,
                        new Page.GetByRoleOptions().setName(Pattern.compile("^" + Pattern.quote(label) + "$", Pattern.CASE_INSENSITIVE))).first();
                    if (fallbackCheckbox.count() > 0 && fallbackCheckbox.isVisible()) {
                        fallbackCheckbox.check(new Locator.CheckOptions().setForce(true));
                        selected = true;
                    }
                }
            }

            if (!selected && !"sakai.meetings".equals(toolId)) {
                throw new IllegalStateException("Unable to find requested Manage Tools checkbox for " + toolId);
            }
        }

        if (toolIds.stream().map(this::normalizeToolId).anyMatch("sakai.lessonbuildertool"::equals)) {
            Locator lessonContinue = page.locator("#btnContinue").first();
            if (lessonContinue.count() > 0 && lessonContinue.isVisible()) {
                lessonContinue.click(new Locator.ClickOptions().setForce(true));
            }
        }

        clickContinue(null);

        Locator manualPublishing = page.locator("#manualPublishing").first();
        if (manualPublishing.count() > 0 && manualPublishing.isVisible()) {
            manualPublishing.click(new Locator.ClickOptions().setForce(true));
        }

        Locator publish = page.locator("#publish").first();
        if (publish.count() > 0 && publish.isVisible()) {
            publish.click(new Locator.ClickOptions().setForce(true));
        }

        clickContinue(null);

        Locator createSiteButton = page.locator(
            "input#addSite, button#addSite, input[type=\"submit\"][value*=\"Create Site\"], input[type=\"submit\"][value*=\"Add Site\"], button:has-text(\"Create Site\"), button:has-text(\"Add Site\")"
        ).first();
        if (createSiteButton.count() > 0 && createSiteButton.isVisible()) {
            createSiteButton.click(new Locator.ClickOptions().setForce(true));
        }

        page.waitForLoadState();

        String href = null;
        Locator flash = page.locator("#flashNotif").first();
        if (flash.count() > 0 && flash.isVisible()) {
            assertThat(flash).containsText("created");
            Locator flashLink = flash.locator("a").first();
            if (flashLink.count() > 0) {
                href = flashLink.getAttribute("href");
            }
        }

        if (href == null || href.isBlank()) {
            Matcher matcher = Pattern.compile("/portal/site/([^/?#]+)").matcher(page.url());
            if (matcher.find() && !matcher.group(1).startsWith("~")) {
                href = "/portal/site/" + matcher.group(1);
            }
        }

        if (href == null || href.isBlank()) {
            throw new IllegalStateException("Unable to determine newly created site URL");
        }

        COURSE_URL_CACHE.put(courseCacheKey, href);
        return href;
    }

    public void createRubric(String instructor, String sakaiUrl) {
        login(instructor);
        gotoPath(sakaiUrl);
        toolClick("Rubrics");
        page.locator(".add-rubric").first().click(new Locator.ClickOptions().setForce(true));
    }

    public void typeCkEditor(String editorId, String html) {
        if (editorId == null || editorId.isBlank()) {
            return;
        }

        try {
            page.waitForFunction(
                "(id) => Boolean(window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances[id])",
                editorId
            );
            page.evaluate(
                "(args) => {" +
                    "const id = args[0];" +
                    "const html = args[1];" +
                    "const inst = window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances[id];" +
                    "if (!inst) { return false; }" +
                    "return new Promise((resolve) => inst.setData(html, { callback: () => resolve(true) }));" +
                    "}",
                Arrays.asList(editorId, html)
            );
        } catch (RuntimeException ignored) {
            // Some tools may not fully initialize CKEditor; callers should provide textarea fallback.
        }
    }

    public void dismissTutorial() {
        for (int attempt = 0; attempt < 3; attempt++) {
            Locator alertClose = page.locator("[role=\"alert\"] button").first();
            if (alertClose.count() > 0 && alertClose.isVisible()) {
                alertClose.click(new Locator.ClickOptions().setForce(true));
                page.waitForTimeout(100);
                continue;
            }

            Locator tutorialClose = page.locator(
                ".sakai-tutorial .qtip-close, .sakai-tutorial .qtip-titlebar .qtip-close, .sakai-tutorial button:has-text(\"×\"), .sakai-tutorial button:has-text(\"Skip\")"
            ).first();
            if (tutorialClose.count() > 0 && tutorialClose.isVisible()) {
                tutorialClose.click(new Locator.ClickOptions().setForce(true));
                page.waitForTimeout(100);
                continue;
            }
            break;
        }
    }

    private void clickContinue(Locator scope) {
        for (int attempt = 0; attempt < 6; attempt++) {
            dismissTutorial();

            if (scope != null && scope.count() > 0 && scope.first().isVisible()) {
                Locator scopedControls = scope.locator(
                    "input#continueButton, button#continueButton, input[name=\"Continue\"], button[name=\"Continue\"], input[name=\"continue\"], button[name=\"continue\"], input[type=\"submit\"][value*=\"Continue\"], button:has-text(\"Continue\")"
                );
                if (clickVisible(scopedControls.first())) {
                    page.waitForLoadState();
                    page.waitForTimeout(250);
                    return;
                }
            }

            Locator continueControl = page.locator(
                "input#continueButton, button#continueButton, .act input[name=\"Continue\"], .act button[name=\"Continue\"], .act input[name=\"continue\"], .act button[name=\"continue\"], .act input[value*=\"Continue\"], .act button:has-text(\"Continue\"), input[type=\"submit\"][value*=\"Continue\"], button:has-text(\"Continue\")"
            );
            if (clickVisible(continueControl.first())) {
                page.waitForLoadState();
                page.waitForTimeout(250);
                return;
            }

            page.waitForTimeout(250);
        }

        throw new IllegalStateException("Unable to click Continue in site creation flow");
    }

    private boolean clickToolByText(String label) {
        Locator nav = page.locator(".site-list-item-collapse.collapse.show a.btn-nav, ul.site-page-list a.btn-nav");
        int count = nav.count();
        for (int index = 0; index < count; index++) {
            Locator candidate = nav.nth(index);
            String text = text(candidate);
            if (text.toLowerCase().contains(label.toLowerCase()) && candidate.isVisible()) {
                candidate.click(new Locator.ClickOptions().setForce(true));
                page.waitForLoadState();
                return true;
            }
        }
        return false;
    }

    private boolean clickVisible(Locator locator) {
        if (locator.count() > 0 && locator.isVisible()) {
            locator.click(new Locator.ClickOptions().setForce(true));
            return true;
        }
        return false;
    }

    private boolean waitForVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }

    private void ensureAnyVisibleCheckboxChecked(Locator checkboxes) {
        int count = checkboxes.count();

        for (int index = 0; index < count; index++) {
            Locator box = checkboxes.nth(index);
            if (box.isVisible() && box.isChecked()) {
                return;
            }
        }

        for (int index = 0; index < count; index++) {
            Locator box = checkboxes.nth(index);
            if (!box.isVisible()) {
                continue;
            }
            try {
                box.check(new Locator.CheckOptions().setForce(true));
            } catch (PlaywrightException ex) {
                box.click(new Locator.ClickOptions().setForce(true));
            }
            if (box.isChecked()) {
                return;
            }
        }

        throw new IllegalStateException("Unable to select a visible course/section during site creation");
    }

    private String text(Locator locator) {
        String value = locator.textContent();
        return value == null ? "" : value;
    }

    private void setTutorialFlags() {
        page.evaluate("() => {"
            + "try {"
            + "sessionStorage.clear();"
            + "localStorage.clear();"
            + "sessionStorage.setItem('tutorialFlagSet','true');"
            + "localStorage.setItem('tutorialFlagSet','true');"
            + "} catch (e) { }"
            + "}");
    }

    private void withTransientRetry(Runnable runnable) {
        PlaywrightException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                runnable.run();
                return;
            } catch (PlaywrightException ex) {
                last = ex;
                if (!isTransientNetworkError(ex) || attempt == 3) {
                    throw ex;
                }
                page.waitForTimeout(250L * attempt);
            }
        }

        if (last != null) {
            throw last;
        }
    }

    private boolean isTransientNetworkError(RuntimeException error) {
        String message = error.getMessage();
        if (message == null) {
            return false;
        }

        return message.matches("(?is).*(ECONNRESET|ECONNREFUSED|EHOSTUNREACH|ENETUNREACH|ETIMEDOUT|ERR_CONNECTION_RESET|ERR_CONNECTION_CLOSED|ERR_CONNECTION_TIMED_OUT|ERR_NETWORK_CHANGED|ERR_NAME_NOT_RESOLVED|ERR_INTERNET_DISCONNECTED|net::ERR_|NS_ERROR_NET|Navigation timeout).*" );
    }

    private String absoluteUrl(String urlOrPath) {
        if (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://")) {
            return urlOrPath;
        }

        if (urlOrPath.startsWith("/")) {
            return baseUrl + urlOrPath;
        }

        return baseUrl + "/" + urlOrPath;
    }

    private String passwordFor(String username) {
        return "admin".equals(username) ? "admin" : "sakai";
    }

    private String resolveUser(String username) {
        return SakaiEnvironment.resolveUser(username, isolationKey);
    }

    private String courseCacheKey(String username, List<String> toolIds) {
        List<String> normalizedToolIds = new ArrayList<>();
        if (toolIds != null) {
            for (String toolId : toolIds) {
                normalizedToolIds.add(normalizeToolId(toolId));
            }
        }
        Collections.sort(normalizedToolIds);
        String toolKey = String.join(",", normalizedToolIds);
        return isolationKey + "|" + username + "|" + toolKey;
    }

    private String toDateTimeLocal(String value) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value);
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException ignored) {
            // Continue into MM/DD/YYYY parser.
        }

        Matcher match = MM_DD_YYYY_12H.matcher(value.trim());
        if (!match.matches()) {
            return value;
        }

        int month = Integer.parseInt(match.group(1));
        int day = Integer.parseInt(match.group(2));
        int year = Integer.parseInt(match.group(3));
        int hour = Integer.parseInt(match.group(4));
        int minute = Integer.parseInt(match.group(5));
        String ampm = match.group(6).toLowerCase();

        if ("pm".equals(ampm) && hour < 12) {
            hour += 12;
        }
        if ("am".equals(ampm) && hour == 12) {
            hour = 0;
        }

        LocalDateTime parsed = LocalDateTime.of(year, month, day, hour, minute);
        return parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    private String normalizeToolId(String toolId) {
        return toolId.replace("\\.", ".");
    }

    private String cssEscape(String idValue) {
        return idValue
            .replace("\\", "\\\\")
            .replace(".", "\\.")
            .replace(":", "\\:");
    }

    private Map<String, String> toolLabelFallbacks() {
        Map<String, String> map = new HashMap<>();
        map.put("sakai.announcements", "Announcements");
        map.put("sakai.assignment.grades", "Assignments");
        map.put("sakai.schedule", "Calendar");
        map.put("sakai.commons", "Commons");
        map.put("sakai.feedback", "Contact Us");
        map.put("sakai.conversations", "Conversations");
        map.put("sakai.dashboard", "Dashboard");
        map.put("sakai.forums", "Discussions");
        map.put("sakai.mailtool", "Email");
        map.put("sakai.gradebookng", "Gradebook");
        map.put("sakai.lessonbuildertool", "Lessons");
        map.put("sakai.poll", "Polls");
        map.put("sakai.rubrics", "Rubrics");
        map.put("sakai.scorm.tool", "SCORM Player");
        map.put("sakai.signup", "Sign-up");
        map.put("sakai.sitestats", "Statistics");
        map.put("sakai.samigo", "Tests & Quizzes");
        map.put("sakai.meetings", "Meetings");
        return map;
    }
}
