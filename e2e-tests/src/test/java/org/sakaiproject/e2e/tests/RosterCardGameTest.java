/**
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class RosterCardGameTest extends SakaiUiTestBase {

    @Test
    void checksGuessesAndClearsTheSelectionBetweenRounds() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.site\\.roster2"));
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Roster");
        APIResponse response = page.request().get("/api/sites/" + sakai.siteIdFromUrl(siteUrl) + "/card-game/users");
        assertTrue(response.ok(), response.text());
        JsonArray users = JsonParser.parseString(response.text()).getAsJsonArray();
        assumeTrue(users.size() >= 2,
            "Card-game fixtures need two eligible students with photos, or cardgame.skipNoImageUsers=false");

        page.locator("#navbar_card_game_link a").click();
        Locator game = page.locator("#card-game");
        Locator scope = game.locator("[data-select-scope]");
        Locator picker = game.locator("sakai-user-picker");
        Locator input = picker.getByRole(AriaRole.COMBOBOX);
        assertThat(scope.or(input)).isVisible();
        if (scope.isVisible()) {
            scope.click();
        }
        assertThat(input).isVisible();

        selectGuess(game, picker, users, true);
        game.locator("[data-check]").click();
        assertThat(game.locator("#feedback-banner.sak-banner-success")).isVisible();
        assertThat(input).isDisabled();
        assertThat(game.locator("[data-continue]")).isFocused();

        game.locator("[data-continue]").click();
        assertThat(input).isEnabled();
        assertThat(picker.getByRole(AriaRole.BUTTON)).hasCount(0);
        selectGuess(game, picker, users, false);
        game.locator("[data-check]").click();
        assertThat(game.locator("#feedback-banner.sak-banner-error")).isVisible();
    }

    private void selectGuess(Locator game, Locator picker, JsonArray users, boolean correct) {
        String image = Objects.requireNonNull(game.locator("img.img-thumbnail").getAttribute("src"));
        Locator input = picker.getByRole(AriaRole.COMBOBOX);
        for (JsonElement entry : users) {
            JsonObject user = entry.getAsJsonObject();
            String id = user.get("id").getAsString();
            if (image.contains("/api/users/" + id + "/") == correct) {
                String name = user.get("displayName").getAsString();
                input.fill(name);
                Locator option = picker.getByRole(AriaRole.OPTION).filter(new Locator.FilterOptions().setHasText(name));
                if (correct) {
                    option.click();
                    return;
                }
                if (option.count() == 1) {
                    option.click();
                    return;
                }
            }
        }
        throw new AssertionError("No suitable guess available in the site roster");
    }
}
