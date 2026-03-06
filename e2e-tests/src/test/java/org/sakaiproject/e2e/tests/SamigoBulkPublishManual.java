package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class SamigoBulkPublishManual extends SakaiUiTestBase {

    @Test
    void publishDraftAssessmentsMatchingPrefix() {
        SeedConfig config = SeedConfig.fromEnvironment();

        sakai.login(config.username);
        page.navigate(config.siteUrl);
        sakai.toolClick("Tests");
        String testsUrl = page.url();

        ensureAssessmentsTable(testsUrl);
        filterAssessments(config.prefix);
        setAssessmentPageLength(config.expectedCount * 2);

        assertThat(draftMarkers()).hasCount(config.expectedCount);
        assertThat(closedMarkers()).hasCount(0);

        Set<String> publishedTitles = activeTitles();
        while (publishedTitles.size() < config.expectedCount) {
            Locator draftRow = nextUnpublishedDraftRow(publishedTitles);
            String title = normalizedTitle(draftRow);

            openSettingsForRow(draftRow);
            setAvailabilityWindow(config);
            saveSettingsAndPublish();
            confirmPublishIfPresent();

            ensureAssessmentsTable(testsUrl);
            filterAssessments(config.prefix);
            setAssessmentPageLength(config.expectedCount * 2);

            if (!hasActiveRow(title)) {
                throw new IllegalStateException("Assessment did not become active after publish: " + title);
            }
            publishedTitles.add(title);
        }

        assertThat(assessmentRows()).hasCount(config.expectedCount * 2);
        assertThat(draftMarkers()).hasCount(config.expectedCount);
        assertThat(closedMarkers()).hasCount(0);
        assertThat(activeMarkers()).hasCount(config.expectedCount);
    }

    @Test
    void removeAssessmentsMatchingPrefix() {
        SeedConfig config = SeedConfig.fromEnvironment();

        sakai.login(config.username);
        page.navigate(config.siteUrl);
        sakai.toolClick("Tests");
        String testsUrl = page.url();

        ensureAssessmentsTable(testsUrl);
        filterAssessments(config.prefix);
        setAssessmentPageLength(config.expectedCount);

        assertThat(assessmentRows()).hasCount(config.expectedCount);

        selectAllVisibleRows(assessmentRows());
        assertThat(selectedCheckboxes()).hasCount(config.expectedCount);

        Locator removeSelected = page.locator("#authorIndexForm\\:remove-selected, #remove-selected").first();
        page.onceDialog(dialog -> dialog.accept());
        removeSelected.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        ensureAssessmentsTable(testsUrl);
        filterAssessments(config.prefix);
        setAssessmentPageLength(config.expectedCount);

        assertThat(assessmentRows()).hasCount(0);
    }

    @Test
    void verifyStudentVisibleAssessmentsMatchingPrefix() {
        SeedConfig config = SeedConfig.fromEnvironment();

        sakai.login(config.username);
        page.navigate(config.siteUrl);
        enterStudentView();
        page.navigate(config.siteUrl);
        sakai.toolClick("Tests");
        String testsUrl = page.url();

        ensureStudentAssessmentsTable(testsUrl);
        filterStudentAssessments(config.prefix);
        setStudentAssessmentPageLength(config.expectedCount);

        assertThat(studentAssessmentRows()).hasCount(config.expectedCount);
    }

    @Test
    void verifyExtendedTimeKeepsClosedAssessmentVisibleForNamedStudent() {
        SeedConfig config = SeedConfig.fromEnvironment();

        sakai.login(config.authorUsername);
        page.navigate(config.siteUrl);
        sakai.toolClick("Tests");
        String testsUrl = page.url();

        ensureAssessmentsTable(testsUrl);
        filterAssessments(config.prefix);
        setAssessmentPageLength(config.expectedCount * 2);

        String assessmentTitle = openPublishedSettingsWithoutStudentExtendedTime(config, testsUrl);
        setClosedAvailabilityWindow(config);
        addUserExtendedTime(config);
        savePublishedSettings();

        ensureAssessmentsTable(testsUrl);
        filterAssessments(assessmentTitle);
        setAssessmentPageLength(5);
        assertThat(closedMarkers()).hasCount(1);

        page.navigate(config.siteUrl);
        enterStudentView();
        page.navigate(config.siteUrl);
        sakai.toolClick("Tests");
        String authorStudentViewUrl = page.url();

        ensureStudentAssessmentsTable(authorStudentViewUrl);
        filterStudentAssessments(assessmentTitle);
        setStudentAssessmentPageLength(5);
        assertThat(studentAssessmentRows()).hasCount(0);

        sakai.login(config.extendedTimeUsername);
        page.navigate(config.siteUrl);
        sakai.toolClick("Tests");
        String studentTestsUrl = page.url();

        ensureStudentAssessmentsTable(studentTestsUrl);
        filterStudentAssessments(assessmentTitle);
        setStudentAssessmentPageLength(5);

        Locator matchingStudentRows = studentAssessmentRows().filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile(Pattern.quote(assessmentTitle))));
        assertThat(matchingStudentRows).hasCount(1);
        assertThat(matchingStudentRows.first()).containsText(Pattern.compile(
            config.extendedTimeHours + "\\s+hour",
            Pattern.CASE_INSENSITIVE
        ));
        assertThat(matchingStudentRows.first()).containsText(Pattern.compile(
            config.extendedTimeMinutes + "\\s+minutes",
            Pattern.CASE_INSENSITIVE
        ));
    }

    private void ensureAssessmentsTable(String testsUrl) {
        Locator assessmentsTable = page.locator("#authorIndexForm\\:coreAssessments");
        if (isVisible(assessmentsTable, 10_000)) {
            return;
        }

        maybeClickFirstVisible(page.locator("a:has-text(\"Assessments\")"));
        if (isVisible(assessmentsTable, 10_000)) {
            return;
        }

        page.navigate(testsUrl);
        maybeClickFirstVisible(page.locator("a:has-text(\"Assessments\")"));
        assertThat(assessmentsTable).isVisible();
    }

    private void filterAssessments(String prefix) {
        Locator searchInput = page.locator("#authorIndexForm\\:coreAssessments_filter input").first();
        assertThat(searchInput).isVisible();
        searchInput.fill(prefix);
        searchInput.dispatchEvent("input");
        searchInput.dispatchEvent("keyup");
        page.waitForTimeout(500);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private void enterStudentView() {
        Locator roleSwitchSelect = page.locator("#roleSwitchSelect").first();
        if (isVisible(roleSwitchSelect, 5_000)) {
            roleSwitchSelect.selectOption(new SelectOption().setLabel("Student"));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            Locator exitStudentView = page.locator(
                "#roleSwitchAnchor input[value*=\"Exit Student View\"], #roleSwitchAnchor:has-text(\"Exit Student View\")"
            ).first();
            assertThat(exitStudentView).isVisible();
            return;
        }

        Locator enterStudentView = page.locator("#roleSwitchAnchor, a:has-text(\"Enter Student View\")").first();
        if (isVisible(enterStudentView, 5_000)) {
            enterStudentView.click(new Locator.ClickOptions().setForce(true));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            Locator exitStudentView = page.locator(
                "#roleSwitchAnchor input[value*=\"Exit Student View\"], #roleSwitchAnchor:has-text(\"Exit Student View\")"
            ).first();
            assertThat(exitStudentView).isVisible();
        }
    }

    private void ensureStudentAssessmentsTable(String testsUrl) {
        Locator assessmentsTable = page.locator("#selectIndexForm\\:selectTable");
        if (isVisible(assessmentsTable, 10_000)) {
            return;
        }

        page.navigate(testsUrl);
        assertThat(assessmentsTable).isVisible();
    }

    private void filterStudentAssessments(String prefix) {
        Locator searchInput = page.locator("#selectIndexForm\\:selectTable_filter input").first();
        assertThat(searchInput).isVisible();
        searchInput.fill(prefix);
        searchInput.dispatchEvent("input");
        searchInput.dispatchEvent("keyup");
        page.waitForTimeout(500);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private void setStudentAssessmentPageLength(int expectedCount) {
        Locator lengthSelect = page.locator("#selectIndexForm\\:selectTable_length select").first();
        if (!isVisible(lengthSelect, 5_000)) {
            return;
        }

        String pageLength;
        if (expectedCount <= 5) {
            pageLength = "5";
        } else if (expectedCount <= 10) {
            pageLength = "10";
        } else if (expectedCount <= 20) {
            pageLength = "20";
        } else if (expectedCount <= 50) {
            pageLength = "50";
        } else if (expectedCount <= 100) {
            pageLength = "100";
        } else if (expectedCount <= 200) {
            pageLength = "200";
        } else {
            pageLength = "-1";
        }

        lengthSelect.selectOption(pageLength);
        page.waitForTimeout(500);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private void setAssessmentPageLength(int expectedCount) {
        Locator lengthSelect = page.locator("#authorIndexForm\\:coreAssessments_length select").first();
        if (!isVisible(lengthSelect, 5_000)) {
            return;
        }

        String pageLength;
        if (expectedCount <= 5) {
            pageLength = "5";
        } else if (expectedCount <= 10) {
            pageLength = "10";
        } else if (expectedCount <= 20) {
            pageLength = "20";
        } else if (expectedCount <= 50) {
            pageLength = "50";
        } else if (expectedCount <= 100) {
            pageLength = "100";
        } else if (expectedCount <= 200) {
            pageLength = "200";
        } else {
            pageLength = "-1";
        }

        lengthSelect.selectOption(pageLength);
        page.waitForTimeout(500);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private Locator visibleRows() {
        return page.locator("#authorIndexForm\\:coreAssessments tbody tr");
    }

    private Locator assessmentRows() {
        return visibleRows().filter(new Locator.FilterOptions()
            .setHas(page.locator("input.select-checkbox")));
    }

    private Locator studentAssessmentRows() {
        return page.locator("#selectIndexForm\\:selectTable tbody tr").filter(new Locator.FilterOptions()
            .setHas(page.locator("a[id$='takeAssessment'], a[title='Proctored Assessment Link']")));
    }

    private Locator draftMarkers() {
        Pattern rowPattern = Pattern.compile("Working\\s*Copy|Draft\\s*-", Pattern.CASE_INSENSITIVE);
        return assessmentRows().filter(new Locator.FilterOptions().setHasText(rowPattern));
    }

    private Locator activeMarkers() {
        Pattern rowPattern = Pattern.compile("Active\\s*\\(Published\\)", Pattern.CASE_INSENSITIVE);
        return assessmentRows().filter(new Locator.FilterOptions().setHasText(rowPattern));
    }

    private Locator closedMarkers() {
        Pattern rowPattern = Pattern.compile("Closed\\s*\\(Published\\)", Pattern.CASE_INSENSITIVE);
        return assessmentRows().filter(new Locator.FilterOptions().setHasText(rowPattern));
    }

    private String openPublishedSettingsWithoutStudentExtendedTime(SeedConfig config, String testsUrl) {
        Locator activeRows = activeMarkers();
        int activeRowCount = activeRows.count();
        for (int index = 0; index < activeRowCount; index += 1) {
            Locator assessmentRow = activeRows.nth(index);
            String title = normalizedTitle(assessmentRow);

            openSettingsForRow(assessmentRow);
            expandAllSettingsSections();
            if (!hasExistingExtendedTimeEntry(config.extendedTimeUsername)) {
                return title;
            }

            cancelSettingsEdit();
            ensureAssessmentsTable(testsUrl);
            filterAssessments(config.prefix);
            setAssessmentPageLength(config.expectedCount * 2);
            activeRows = activeMarkers();
            activeRowCount = activeRows.count();
        }

        throw new IllegalStateException(
            "Unable to find an active published assessment without an existing extended time entry for " + config.extendedTimeUsername
        );
    }

    private void expandAllSettingsSections() {
        maybeClickFirstVisible(page.locator("#expandLink, a:has-text(\"Expand All\")"));
        assertThat(page.locator("#assessmentSettingsAction\\:newEntry-user")).isVisible();
    }

    private boolean hasExistingExtendedTimeEntry(String username) {
        Locator extendedTimeTable = page.locator("#assessmentSettingsAction\\:extendedTimeTable").first();
        return isVisible(extendedTimeTable, 2_000)
            && extendedTimeTable.textContent() != null
            && extendedTimeTable.textContent().contains(username);
    }

    private void cancelSettingsEdit() {
        Locator cancel = page.locator(
            "#assessmentSettingsAction input[type=\"submit\"][value=\"Cancel\"], #assessmentSettingsAction button:has-text(\"Cancel\")"
        ).first();
        assertThat(cancel).isVisible();
        cancel.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private Locator selectedCheckboxes() {
        return page.locator("#authorIndexForm\\:coreAssessments tbody input.select-checkbox:checked");
    }

    private void selectAllVisibleRows(Locator rows) {
        int rowCount = rows.count();
        for (int index = 0; index < rowCount; index += 1) {
            Locator checkbox = rows.nth(index).locator("input.select-checkbox").first();
            assertThat(checkbox).isVisible();
            checkbox.check(new Locator.CheckOptions().setForce(true));
        }
    }

    private Locator nextUnpublishedDraftRow(Set<String> publishedTitles) {
        Locator drafts = draftMarkers();
        int draftCount = drafts.count();
        for (int index = 0; index < draftCount; index += 1) {
            Locator draftRow = drafts.nth(index);
            String title = normalizedTitle(draftRow);
            if (!publishedTitles.contains(title)) {
                return draftRow;
            }
        }

        throw new IllegalStateException("Unable to find another unpublished draft row for prefix");
    }

    private Set<String> activeTitles() {
        Set<String> titles = new HashSet<>();
        Locator activeRows = activeMarkers();
        int activeRowCount = activeRows.count();
        for (int index = 0; index < activeRowCount; index += 1) {
            titles.add(normalizedTitle(activeRows.nth(index)));
        }
        return titles;
    }

    private boolean hasActiveRow(String title) {
        Locator activeRows = activeMarkers();
        int activeRowCount = activeRows.count();
        for (int index = 0; index < activeRowCount; index += 1) {
            if (title.equals(normalizedTitle(activeRows.nth(index)))) {
                return true;
            }
        }
        return false;
    }

    private String normalizedTitle(Locator row) {
        String title = row.locator("strong").first().innerText();
        title = title.replaceAll("\\s+", " ").trim();
        if (title.startsWith("Draft - ")) {
            title = title.substring("Draft - ".length()).trim();
        }
        return title;
    }

    private void openSettingsForRow(Locator assessmentRow) {
        assertThat(assessmentRow).isVisible();

        Locator actionsButton = assessmentRow.locator("button.dropdown-toggle, button:has-text(\"Actions\")").first();
        assertThat(actionsButton).isVisible();
        actionsButton.click(new Locator.ClickOptions().setForce(true));

        Locator settingsLink = assessmentRow.locator("a:has-text(\"Settings\")").first();
        if (!isVisible(settingsLink, 3_000)) {
            settingsLink = page.locator(".dropdown-menu.show a:has-text(\"Settings\"), a:has-text(\"Settings\")").first();
        }
        assertThat(settingsLink).isVisible();
        settingsLink.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        assertThat(page.locator("#assessmentSettingsAction\\:startDate")).isVisible();
    }

    private void setAvailabilityWindow(SeedConfig config) {
        sakai.selectDate("#assessmentSettingsAction\\:startDate", config.availableDate);
        sakai.selectDate("#assessmentSettingsAction\\:endDate", config.dueDate);

        Locator noLateSubmissions = page.locator("#assessmentSettingsAction\\:lateHandling\\:0").first();
        if (isVisible(noLateSubmissions, 2_000)) {
            noLateSubmissions.check(new Locator.CheckOptions().setForce(true));
        }
    }

    private void setClosedAvailabilityWindow(SeedConfig config) {
        expandAllSettingsSections();
        sakai.selectDate("#assessmentSettingsAction\\:startDate", config.closedAvailableDate);
        sakai.selectDate("#assessmentSettingsAction\\:endDate", config.closedDueDate);

        Locator noLateSubmissions = page.locator("#assessmentSettingsAction\\:lateHandling\\:0").first();
        if (isVisible(noLateSubmissions, 2_000)) {
            noLateSubmissions.check(new Locator.CheckOptions().setForce(true));
        }
    }

    private void addUserExtendedTime(SeedConfig config) {
        expandAllSettingsSections();

        Locator userRadio = page.locator("#assessmentSettingsAction\\:extendedEnableUser\\:0").first();
        if (isVisible(userRadio, 2_000)) {
            userRadio.check(new Locator.CheckOptions().setForce(true));
        }

        selectOptionContainingText(
            page.locator("#assessmentSettingsAction\\:newEntry-user").first(),
            config.extendedTimeUsername
        );
        sakai.selectDate("#assessmentSettingsAction\\:newEntry-start_date", config.extendedAvailableDate);
        sakai.selectDate("#assessmentSettingsAction\\:newEntry-due_date", config.extendedDueDate);
        page.locator("#assessmentSettingsAction\\:newEntry-hours").first().selectOption(Integer.toString(config.extendedTimeHours));
        page.locator("#assessmentSettingsAction\\:newEntry-mins").first().selectOption(Integer.toString(config.extendedTimeMinutes));

        Locator addEntry = page.locator("#assessmentSettingsAction\\:extendedTimeAdd").first();
        assertThat(addEntry).isVisible();
        addEntry.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        expandAllSettingsSections();
        Locator extendedTimeTable = page.locator("#assessmentSettingsAction\\:extendedTimeTable").first();
        assertThat(extendedTimeTable).containsText(config.extendedTimeUsername);
    }

    private void selectOptionContainingText(Locator select, String textFragment) {
        assertThat(select).isVisible();

        Locator options = select.locator("option");
        int optionCount = options.count();
        for (int index = 0; index < optionCount; index += 1) {
            Locator option = options.nth(index);
            String label = option.textContent();
            String value = option.getAttribute("value");
            if (label != null && label.contains(textFragment) && value != null && !value.isBlank()) {
                select.selectOption(value);
                return;
            }
        }

        throw new IllegalStateException("Unable to find select option containing text: " + textFragment);
    }

    private void saveSettingsAndPublish() {
        Locator saveAndPublish = page.locator(
            "button:has-text(\"Save Settings and Publish\"), input[type=\"submit\"][value*=\"Save Settings and Publish\"]"
        ).first();
        assertThat(saveAndPublish).isVisible();
        saveAndPublish.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private void confirmPublishIfPresent() {
        Locator publish = page.locator(
            "#publishAssessmentForm\\:publish, input[type=\"submit\"][value*=\"Publish\"], button:has-text(\"Publish\")"
        ).first();
        if (!isVisible(publish, 3_000)) {
            return;
        }
        publish.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private void savePublishedSettings() {
        Locator save = page.locator(
            "#assessmentSettingsAction input[type=\"submit\"][value=\"Save\"], #assessmentSettingsAction button:has-text(\"Save\")"
        ).first();
        assertThat(save).isVisible();
        save.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private boolean maybeClickFirstVisible(Locator locator) {
        Locator first = locator.first();
        if (!isVisible(first, 5_000)) {
            return false;
        }
        first.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        return true;
    }

    private boolean isVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static final class SeedConfig {
        private final String siteUrl;
        private final String prefix;
        private final String username;
        private final String authorUsername;
        private final String extendedTimeUsername;
        private final int expectedCount;
        private final String availableDate;
        private final String dueDate;
        private final String closedAvailableDate;
        private final String closedDueDate;
        private final String extendedAvailableDate;
        private final String extendedDueDate;
        private final int extendedTimeHours;
        private final int extendedTimeMinutes;

        private SeedConfig(
            String siteUrl,
            String prefix,
            String username,
            String authorUsername,
            String extendedTimeUsername,
            int expectedCount,
            String availableDate,
            String dueDate,
            String closedAvailableDate,
            String closedDueDate,
            String extendedAvailableDate,
            String extendedDueDate,
            int extendedTimeHours,
            int extendedTimeMinutes
        ) {
            this.siteUrl = siteUrl;
            this.prefix = prefix;
            this.username = username;
            this.authorUsername = authorUsername;
            this.extendedTimeUsername = extendedTimeUsername;
            this.expectedCount = expectedCount;
            this.availableDate = availableDate;
            this.dueDate = dueDate;
            this.closedAvailableDate = closedAvailableDate;
            this.closedDueDate = closedDueDate;
            this.extendedAvailableDate = extendedAvailableDate;
            this.extendedDueDate = extendedDueDate;
            this.extendedTimeHours = extendedTimeHours;
            this.extendedTimeMinutes = extendedTimeMinutes;
        }

        private static SeedConfig fromEnvironment() {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime now = LocalDateTime.now();
            String siteUrl = required("SAMIGO_SITE_URL");
            String prefix = required("SAMIGO_DRAFT_PREFIX");
            String username = value("SAMIGO_USERNAME", "instructor1");
            String authorUsername = value("SAMIGO_AUTHOR_USERNAME", "instructor1");
            String extendedTimeUsername = value("SAMIGO_EXTENDED_TIME_USERNAME", "student0001");
            int expectedCount = Integer.parseInt(value("SAMIGO_EXPECTED_COUNT", "200"));
            String availableDate = value("SAMIGO_AVAILABLE_DATE", now.minusDays(1).format(dateTimeFormatter));
            String dueDate = value("SAMIGO_DUE_DATE", now.plusYears(2).format(dateTimeFormatter));
            String closedAvailableDate = value("SAMIGO_CLOSED_AVAILABLE_DATE", now.minusDays(7).format(dateTimeFormatter));
            String closedDueDate = value("SAMIGO_CLOSED_DUE_DATE", now.minusHours(1).format(dateTimeFormatter));
            String extendedAvailableDate = value("SAMIGO_EXTENDED_AVAILABLE_DATE", now.minusDays(1).format(dateTimeFormatter));
            String extendedDueDate = value("SAMIGO_EXTENDED_DUE_DATE", now.plusDays(2).format(dateTimeFormatter));
            int extendedTimeHours = Integer.parseInt(value("SAMIGO_EXTENDED_TIME_HOURS", "1"));
            int extendedTimeMinutes = Integer.parseInt(value("SAMIGO_EXTENDED_TIME_MINUTES", "30"));
            return new SeedConfig(
                siteUrl,
                prefix,
                username,
                authorUsername,
                extendedTimeUsername,
                expectedCount,
                availableDate,
                dueDate,
                closedAvailableDate,
                closedDueDate,
                extendedAvailableDate,
                extendedDueDate,
                extendedTimeHours,
                extendedTimeMinutes
            );
        }

        private static String required(String key) {
            String value = value(key, null);
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Missing required setting: " + key);
            }
            return value;
        }

        private static String value(String key, String defaultValue) {
            String fromProperty = System.getProperty(key);
            if (fromProperty != null && !fromProperty.isBlank()) {
                return fromProperty;
            }

            String fromEnv = System.getenv(key);
            if (fromEnv != null && !fromEnv.isBlank()) {
                return fromEnv;
            }

            return defaultValue;
        }
    }
}
