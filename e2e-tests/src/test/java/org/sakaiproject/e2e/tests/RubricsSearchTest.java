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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.AbstractToolSearchTest;

class RubricsSearchTest extends AbstractToolSearchTest {

    // rubric reference -> id of the criterion holding the searchable body
    private final Map<String, Long> criterionIds = new HashMap<>();

    @Override
    protected String toolId() {
        return "sakai\\.rubrics";
    }

    @Override
    protected String searchToolName() {
        return "rubrics";
    }

    @Override
    protected String toolLabel() {
        return "Rubrics";
    }

    @Override
    protected String createItem(String siteId, String title, String body) {
        JsonObject rubric = json(api("POST", rubricsPath(siteId) + "/default", null, null));
        long rubricId = rubric.get("id").getAsLong();
        long criterionId = rubric.getAsJsonArray("criteria").get(0).getAsJsonObject().get("id").getAsLong();
        String reference = "/rubrics/" + siteId + "/" + rubricId;

        patch(reference, "", replace("/title", title));
        patch(reference, "/criteria/" + criterionId, replace("/description", body));

        criterionIds.put(reference, criterionId);
        return reference;
    }

    @Override
    protected void updateItem(String siteId, String itemKey, String body) {
        patch(itemKey, "/criteria/" + criterionIds.get(itemKey), replace("/description", body));
    }

    @Override
    protected void deleteItem(String siteId, String itemKey) {
        api("DELETE", rubricPath(itemKey), null, currentVersion(itemKey));
    }

    @Override
    protected void assertOpensTool() {
        // there is no page for a single rubric, so results open the site's rubrics list
        assertTrue(page.url().contains("/portal/site/" + siteId + "/tool/"), page.url());
        assertThat(page.locator("sakai-rubrics-manager")).isVisible();
    }

    @Test
    @Order(10)
    void adhocRubricsAreNeverIndexed() {
        sakai.login(author());
        String adhocWord = uniqueWord();
        String controlWord = uniqueWord();

        String adhoc = createItem(siteId, "Adhoc " + adhocWord, "Grading by " + adhocWord);
        patch(adhoc, "", "[{\"op\":\"replace\",\"path\":\"/adhoc\",\"value\":true}]");
        updateItem(siteId, adhoc, "Graded by " + adhocWord);
        String control = createItem(siteId, "Control " + controlWord, "Grading by " + controlWord);

        // once the later control rubric is searchable, the indexer has been through the ad hoc rubric's events
        awaitItem(controlWord, control);
        JsonArray results = search(adhocWord);
        assertNull(findItem(results, adhoc), "ad hoc rubric was indexed: " + results);
    }

    private static String rubricsPath(String siteId) {
        return "/api/sites/" + siteId + "/rubrics";
    }

    // the api path of the rubric a "/rubrics/{siteId}/{rubricId}" reference names
    private static String rubricPath(String reference) {
        return "/api/sites" + reference.substring("/rubrics".length()).replaceFirst("/(\\d+)$", "/rubrics/$1");
    }

    private static String replace(String path, String value) {
        JsonObject op = new JsonObject();
        op.addProperty("op", "replace");
        op.addProperty("path", path);
        op.addProperty("value", value);
        JsonArray patch = new JsonArray();
        patch.add(op);
        return patch.toString();
    }

    /** Patches the rubric, or a part of it named by subPath, at its current version. */
    private void patch(String reference, String subPath, String jsonPatch) {
        api("PATCH", rubricPath(reference) + subPath, jsonPatch, currentVersion(reference));
    }

    // changes are guarded by optimistic locking on the rubric version, sent as If-Match like the rubrics UI does
    private String currentVersion(String reference) {
        return json(api("GET", rubricPath(reference), null, null)).get("version").getAsString();
    }

    private APIResponse api(String method, String path, String jsonPatch, String version) {
        RequestOptions options = RequestOptions.create().setMethod(method);
        if (version != null) {
            options.setHeader("If-Match", "\"" + version + "\"");
        }
        if (jsonPatch != null) {
            options.setHeader("Content-Type", "application/json-patch+json").setData(jsonPatch);
        }
        APIResponse response = page.request().fetch(path, options);
        assertTrue(response.ok(), method + " " + path + " failed: HTTP " + response.status() + " " + response.text());
        return response;
    }

    private static JsonObject json(APIResponse response) {
        return JsonParser.parseString(response.text()).getAsJsonObject();
    }
}
