/*
 * Copyright (c) 2026 The Apereo Foundation
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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class RubricWeightsTest extends SakaiUiTestBase {

    @Test
    void publishedRubricExplainsIncompleteWeightsAndDraftPreservesThem() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.rubrics"));
        sakai.createRubric("instructor1", siteUrl);
        String title = "Partial weights " + System.currentTimeMillis();
        Locator modal = page.locator("sakai-rubric-edit .modal.show");
        modal.locator("input").fill(title);
        page.waitForResponse(response -> response.url().contains("/rubrics/")
                && response.request().method().equals("PATCH") && response.status() == 200,
                () -> modal.locator("button.btn-primary").click());

        Locator rubric = page.locator("sakai-rubric").filter(new Locator.FilterOptions()
                .setHas(page.locator(".rubric-name").filter(new Locator.FilterOptions().setHasText(title))));
        assertThat(rubric.locator("button.draft")).hasAttribute("aria-label", "Save as Draft");
        page.waitForResponse(response -> response.url().contains("/rubrics/")
                && response.request().method().equals("PATCH") && response.status() == 200,
                () -> rubric.locator("button.weighted").click());
        Locator weight = rubric.locator("input[data-criterion-id]").first();
        Locator save = rubric.locator("button.save-weights");
        assertThat(weight).hasValue("100");
        assertThat(save).isEnabled();
        weight.fill("50");
        assertThat(rubric.locator(".rubric-totals, .card").filter(new Locator.FilterOptions().setHasText("Total Weight"))).containsText("50%");
        assertThat(save).isDisabled();
        assertThat(save).hasCSS("pointer-events", "none");
        assertThat(rubric.locator(".weight-save-help")).containsText("100%");
        assertThat(rubric.locator(".weight-save-help")).containsText("Save as Draft");
        assertThat(rubric.locator(".weight-save-help")).containsText("unlink it");
        assertThat(save).hasAttribute("aria-describedby", Pattern.compile(".+"));

        page.reload();
        rubric.locator(".rubric-toggle").click();
        assertThat(weight).hasValue("100");
        weight.fill("50");
        assertThat(save).isDisabled();
        page.waitForResponse(response -> response.url().contains("/rubrics/")
                && response.request().method().equals("PATCH") && response.status() == 200,
                () -> rubric.locator("button.draft").click());
        assertThat(save).isEnabled();
        assertThat(rubric.locator(".weight-save-help")).hasText("");
        assertThat(rubric.locator(".weight-save-help")).not().hasClass(Pattern.compile(".*sak-banner-warn.*"));
        Response saved = page.waitForResponse(response -> response.url().contains("/rubrics/")
                && response.request().method().equals("PATCH"), () -> save.click());
        assertEquals(200, saved.status());
        assertThat(rubric.locator(".sak-banner-success")).isVisible();
        page.reload();
        rubric.locator(".rubric-toggle").click();
        assertThat(weight).hasValue("50");
        assertThat(rubric.locator("button.draft")).hasAttribute("aria-label", "In order to publish the rubric: Weighting for the criterions must equal 100%");
        weight.fill("100");
        assertThat(rubric.locator(".card").filter(new Locator.FilterOptions().setHasText("Total Weight"))).containsText("100%");
        page.waitForResponse(response -> response.url().contains("/rubrics/")
                && response.request().method().equals("PATCH") && response.status() == 200,
                () -> rubric.locator("button.draft").click());
        assertThat(save).isEnabled();
        assertThat(rubric.locator(".weight-save-help")).hasText("");
        assertThat(rubric.locator(".weight-save-help")).not().hasClass(Pattern.compile(".*sak-banner-warn.*"));
        page.reload();
        rubric.locator(".rubric-toggle").click();
        assertThat(weight).hasValue("100");
        assertThat(rubric.locator("button.draft")).hasAttribute("aria-label", "Save as Draft - Weights will be saved.");
    }
}
