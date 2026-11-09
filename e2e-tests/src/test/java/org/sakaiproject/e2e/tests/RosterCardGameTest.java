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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class RosterCardGameTest extends SakaiUiTestBase {

    @Test
    void checksGuessesAndClearsTheSelectionBetweenRounds() {
        try (TestUser first = sakai.createUser("card-game-first", "Ada");
                TestUser second = sakai.createUser("card-game-second", "Grace")) {
            Set<String> picturedUsers = Set.of(uploadPhoto(first), uploadPhoto(second));
            sakai.login("instructor1");
            String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.site\\.roster2"));
            sakai.addMember(siteUrl, first.username(), "Student");
            sakai.addMember(siteUrl, second.username(), "Student");
            playRounds(siteUrl, picturedUsers);
        }
    }

    private String uploadPhoto(TestUser user) {
        sakai.login(user.username());
        String id = (String) page.evaluate("portal.user.id");
        String png = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=";
        APIResponse response = page.request().post("/api/users/" + id + "/profile/image",
            RequestOptions.create().setForm(FormData.create().set("base64", png)));
        assertTrue(response.ok(), response.text());
        assertTrue("SUCCESS".equals(JsonParser.parseString(response.text()).getAsJsonObject().get("status").getAsString()),
            response.text());
        return id;
    }

    private void playRounds(String siteUrl, Set<String> picturedUsers) {
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Roster");
        APIResponse response = page.request().get("/api/sites/" + sakai.siteIdFromUrl(siteUrl) + "/card-game/users");
        assertTrue(response.ok(), response.text());
        JsonArray users = JsonParser.parseString(response.text()).getAsJsonArray();
        Set<String> eligible = StreamSupport.stream(users.spliterator(), false)
            .map(user -> user.getAsJsonObject().get("id").getAsString())
            .collect(Collectors.toSet());
        assertTrue(eligible.containsAll(picturedUsers), "Both test students must be eligible for the card game");

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
