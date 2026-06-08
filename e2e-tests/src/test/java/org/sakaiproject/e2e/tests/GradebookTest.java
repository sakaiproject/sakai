/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GradebookTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void canCreateNewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng"));
    }

    @Test
    @Order(2)
    void canCreateGradebookCategories() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Settings")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator(".accordion button").filter(new Locator.FilterOptions().setHasText("Categories")).first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("#settingsCategories input[type=\"radio\"]:visible")).hasCount(3);

        Locator weightedCategoryOption = page.locator("#settingsCategories input[name=\"categoryPanel:settingsCategoriesPanel:categoryType\"][value=\"radio4\"]").first();
        assertThat(weightedCategoryOption).isVisible();
        weightedCategoryOption.check(new Locator.CheckOptions().setForce(true));
        assertThat(weightedCategoryOption).isChecked();

        page.locator(".gb-category-row input[name$=\"name\"]").first().fill("A");
        page.locator(".gb-category-weight input[name$=\"weight\"]").first().fill("100");

        page.locator(".act button.active").first().click(new Locator.ClickOptions().setForce(true));
    }

    @Test
    @Order(3)
    void canCreateGradebookItems() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");

        Locator dialog = page.locator("dialog:visible, div[role=\"dialog\"]:visible, .wicket-modal:visible, .modal:visible").first();
        Locator addButton = page.locator("button.gb-add-gradebook-item-button").first();

        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        boolean dialogVisible = false;
        for (int attempt = 0; attempt < 3 && !dialogVisible; attempt++) {
            assertThat(addButton).isVisible();
            try {
                page.waitForFunction("() => { const button = document.querySelector('button.gb-add-gradebook-item-button'); return Boolean(button && window.Wicket && window.Wicket.Ajax); }");
            } catch (RuntimeException ignored) {
            }

            addButton.click();

            try {
                dialog.waitFor(new Locator.WaitForOptions().setTimeout(10_000));
                dialogVisible = true;
            } catch (RuntimeException error) {
                if (page.url().contains("grades?0-1.-form")) {
                    page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
                    page.waitForTimeout(250);
                } else if (attempt == 2) {
                    throw error;
                }
            }
        }

        assertThat(dialog).isVisible();

        Locator cancelButton = dialog.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName(Pattern.compile("Cancel|Close", Pattern.CASE_INSENSITIVE))).first();
        if (cancelButton.count() > 0) {
            cancelButton.click(new Locator.ClickOptions().setForce(true));
        }
    }

    @Test
    @Order(4)
    void courseGradePreviewIsReadableInDarkMode() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");

        page.locator(".navIntraTool a")
            .filter(new Locator.FilterOptions().setHasText("Settings"))
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        Locator gradeReleaseButton = page.locator(".accordion button")
            .filter(new Locator.FilterOptions().setHasText("Grade Release Rules"))
            .first();
        assertThat(gradeReleaseButton).isVisible();
        if (!"true".equals(gradeReleaseButton.getAttribute("aria-expanded"))) {
            gradeReleaseButton.click(new Locator.ClickOptions().setForce(true));
        }

        Locator displayCourseGrade = page.locator("label")
            .filter(new Locator.FilterOptions().setHasText("Display final course grade to students"))
            .locator("input[type=\"checkbox\"]")
            .first();
        assertThat(displayCourseGrade).isVisible();
        if (!displayCourseGrade.isChecked()) {
            displayCourseGrade.check(new Locator.CheckOptions().setForce(true));
        }

        Locator preview = page.locator("#settingsGradeRelease .form-group")
            .filter(new Locator.FilterOptions().setHasText("Preview"))
            .locator("span")
            .nth(1);
        assertThat(preview).isVisible();

        page.evaluate("() => document.documentElement.classList.add('sakaiUserTheme-dark')");
        assertThat(preview).isVisible();
        assertTrue(contrastRatio(preview) >= 4.5,
            "Course grade preview should have readable contrast in dark mode");

        page.evaluate("() => document.documentElement.classList.remove('sakaiUserTheme-dark')");
        assertTrue(contrastRatio(preview) >= 4.5,
            "Course grade preview should have readable contrast in light mode");
    }

    private double contrastRatio(Locator locator) {
        Number contrast = (Number) locator.evaluate("el => {"
            + "const style = getComputedStyle(el);"
            + "const rgb = value => value.match(/\\d+(\\.\\d+)?/g).slice(0, 3).map(Number);"
            + "const channel = value => {"
            + "  const normalized = value / 255;"
            + "  return normalized <= 0.03928 ? normalized / 12.92 : Math.pow((normalized + 0.055) / 1.055, 2.4);"
            + "};"
            + "const luminance = values => 0.2126 * channel(values[0])"
            + "  + 0.7152 * channel(values[1]) + 0.0722 * channel(values[2]);"
            + "const foreground = luminance(rgb(style.color));"
            + "const background = luminance(rgb(style.backgroundColor));"
            + "const light = Math.max(foreground, background);"
            + "const dark = Math.min(foreground, background);"
            + "return (light + 0.05) / (dark + 0.05);"
            + "}");
        return contrast.doubleValue();
    }
}
