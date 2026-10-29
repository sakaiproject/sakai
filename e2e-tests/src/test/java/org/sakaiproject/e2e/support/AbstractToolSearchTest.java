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
package org.sakaiproject.e2e.support;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Global search contract every searchable tool must meet. A tool joins by extending this class and saying how to
 * create, update and delete one of its items; every scenario below then runs against that tool.
 *
 * <p>Indexing is asynchronous (the content queue is drained every 60s by default), so expectations about the index
 * are polled through {@code /api/search}, the endpoint behind the portal search panel.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractToolSearchTest extends SakaiUiTestBase {

    private static final long INDEX_TIMEOUT_MS = 15_000;
    private static final long POLL_INTERVAL_MS = 3_000;
    // Content markup a tool may keep. The search UI renders titles and snippets as HTML, so none of it may run.
    protected static final String HOSTILE_MARKUP = "<b>bold</b> &lt;img src=x onerror=\"window.searchXss=1\"&gt;";

    private Boolean searchEnabled;
    protected String siteId;
    protected String itemKey;
    protected String titleWord;
    protected String bodyWord;
    protected String updatedWord;

    /** Tool id for the course fixture, as a regex, e.g. {@code sakai\\.rubrics}. */
    protected abstract String toolId();

    /** The tool name search results carry, e.g. {@code rubrics}. */
    protected abstract String searchToolName();

    /** The tool label the search panel shows for a result. */
    protected abstract String toolLabel();

    /**
     * Creates an item in the site as the current user.
     *
     * @return a key that the item's search reference contains, used to pick the item out of the results
     */
    protected abstract String createItem(String siteId, String title, String body);

    /** Replaces the searchable body of the item. */
    protected abstract void updateItem(String siteId, String itemKey, String body);

    protected abstract void deleteItem(String siteId, String itemKey);

    /** Who authors the content. */
    protected String author() {
        return "instructor1";
    }

    /** A site member who must not find the content, or null when every member may read it. */
    protected String deniedUser() {
        return "student0011";
    }

    @BeforeEach
    void requireSearch() {
        if (searchEnabled == null) {
            sakai.login(author());
            // the portal only offers search when search.enable=true, which CI does not set
            searchEnabled = page.locator("button[data-bs-target='#sakai-search-panel']").count() > 0;
        }
        assumeTrue(searchEnabled, "Search is disabled on this server (search.enable=false)");
    }

    @Test
    @Order(1)
    void createsTheFixtureSite() {
        sakai.login(author());
        // only sites with the Search tool are indexed (search.onlyIndexSearchToolSites defaults to true)
        siteId = sakai.siteIdFromUrl(sakai.createCourse(author(), List.of(toolId(), "sakai\\.search")));
        assertFalse(siteId.isEmpty());
    }

    @Test
    @Order(2)
    void newContentIsFoundByTitleAndBody() {
        sakai.login(author());
        titleWord = uniqueWord();
        bodyWord = uniqueWord();
        itemKey = createItem(siteId, "Search " + titleWord, "Grading by " + bodyWord + " " + HOSTILE_MARKUP);

        JsonObject byBody = awaitItem(bodyWord);
        assertEquals(searchToolName(), byBody.get("tool").getAsString());
        assertTrue(byBody.get("title").getAsString().contains(titleWord), "title: " + byBody.get("title"));
        assertTrue(byBody.get("searchResult").getAsString().contains(bodyWord), "snippet: " + byBody.get("searchResult"));
        assertFalse(byBody.get("url").getAsString().isBlank(), "result has no url");

        assertNotNull(awaitItem(titleWord));
    }

    @Test
    @Order(3)
    void portalSearchShowsTheResultSafely() {
        sakai.login(author());
        Locator result = searchInPortal(bodyWord).filter(new Locator.FilterOptions().setHasText(titleWord));

        assertThat(result).hasCount(1);
        assertThat(result.locator(".search-result-toolname")).hasText(toolLabel());
        assertThat(result.locator(".search-result-title")).containsText(titleWord);
        assertThat(result.locator(".search-result")).containsText(bodyWord);
        assertMarkupNeitherShownNorRendered(result.locator(".search-result"));
        assertThat(page.locator("sakai-search img")).hasCount(0);
        assertEquals("undefined", page.evaluate("typeof window.searchXss"));
    }

    @Test
    @Order(3)
    void siteSearchToolShowsTheResultSafely() {
        sakai.login(author());
        sakai.gotoPath("/portal/site/" + siteId);
        sakai.toolClick("Search");
        page.locator("input#search").fill(bodyWord);
        page.locator("input#search").press("Enter");

        Locator result = page.locator(".searchItem").filter(new Locator.FilterOptions().setHasText(titleWord));
        assertThat(result).hasCount(1);
        assertThat(result.locator(".searchItemBody")).containsText(bodyWord);
        assertMarkupNeitherShownNorRendered(result.locator(".searchItemBody"));
        assertThat(page.locator(".searchResultsContainer img")).hasCount(0);
        assertEquals("undefined", page.evaluate("typeof window.searchXss"));

        // emptying the field drops the search, so the tool is back to having none
        page.locator("input#search").fill("");
        assertThat(page).hasURL(Pattern.compile("[?&]search=(&|#|$)"));
        assertThat(page.locator("input#search")).hasValue("");
        assertThat(page.locator(".searchItem")).hasCount(0);
    }

    @Test
    @Order(4)
    void resultLinkOpensTheTool() {
        sakai.login(author());
        String url = item(search(bodyWord)).get("url").getAsString();

        // urls are absolute on the configured server url, which need not be the host the tests run against
        sakai.gotoPath(url.replaceFirst("^https?://[^/]+", ""));

        assertTrue(page.url().contains(siteId), "result url left the site: " + page.url());
        assertOpensTool();
    }

    /** Checks the page a result link led to. Tools can check for their own markup. */
    protected void assertOpensTool() {
        assertThat(page.locator("body")).containsText(toolLabel());
    }

    @Test
    @Order(5)
    void siteAndToolFiltersApply() {
        sakai.login(author());

        assertNotNull(findItem(search(bodyWord, "&site=" + siteId)));
        assertNotNull(findItem(search(bodyWord, "&tool=" + searchToolName())));
        assertNotNull(findItem(search(bodyWord, "&site=" + siteId + "&tool=" + searchToolName())));
        assertNull(findItem(search(bodyWord, "&site=!admin")));
        assertNull(findItem(search(bodyWord, "&tool=nosuchtool")));
    }

    @Test
    @Order(6)
    void usersWithoutAccessCannotFindIt() {
        String denied = deniedUser();
        if (denied == null) {
            return;
        }
        sakai.login(denied);

        for (String word : List.of(bodyWord, titleWord)) {
            JsonArray results = search(word);
            assertNull(findItem(results), "found by " + denied);
            // the platform may answer with a censored placeholder, which must not reveal anything about the item
            String leaked = results.toString();
            assertFalse(leaked.contains(titleWord) || leaked.contains(bodyWord) || leaked.contains(siteId), leaked);
        }

        searchInPortal(bodyWord);
        Locator panel = page.locator("sakai-search");
        assertThat(panel).not().containsText(titleWord);
        assertThat(panel).not().containsText(toolLabel());
        assertThat(page.locator("sakai-search a[href*='" + siteId + "']")).hasCount(0);
    }

    @Test
    @Order(7)
    void portalSearchSendsTheTermsUnaltered() {
        sakai.login(author());
        String terms = bodyWord + " & #notes + 100%";

        Request request = page.waitForRequest(r -> r.url().contains("/api/search?"), () -> typeSearch(terms));

        assertEquals(terms, queryParam(request.url(), "terms"));
    }

    @Test
    @Order(8)
    void updatedContentIsReindexed() {
        sakai.login(author());
        updatedWord = uniqueWord();

        updateItem(siteId, itemKey, "Grading by " + updatedWord);

        assertNotNull(awaitItem(updatedWord));
        awaitNoItem(bodyWord);
    }

    @Test
    @Order(9)
    void deletedContentLeavesTheIndex() {
        sakai.login(author());

        deleteItem(siteId, itemKey);

        awaitNoItem(updatedWord);
        awaitNoItem(titleWord);
    }

    /**
     * The content's {@code <b>bold</b>} must be stripped: neither shown as literal tags (the raw markup bug)
     * nor rendered as an element. Matched terms are legitimately wrapped in the highlighter's own tags.
     */
    private void assertMarkupNeitherShownNorRendered(Locator snippet) {
        assertThat(snippet).containsText("bold");
        assertThat(snippet).not().containsText("<b>");
        assertThat(snippet.locator("b, strong").filter(new Locator.FilterOptions().setHasText("bold"))).hasCount(0);
    }

    // --- search helpers, for subclasses too ---

    /** Letters only, so analyzers keep it as one token. */
    protected static String uniqueWord() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("zq");
        for (int i = 0; i < 10; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
    }

    /** {@code /api/search} results for the terms, as the current user. */
    protected JsonArray search(String terms) {
        return search(terms, "");
    }

    protected JsonArray search(String terms, String extraParams) {
        APIResponse response = page.request().get("/api/search?terms="
            + URLEncoder.encode(terms, StandardCharsets.UTF_8) + extraParams);
        assertTrue(response.ok(), "search failed: HTTP " + response.status());
        return JsonParser.parseString(response.text()).getAsJsonArray();
    }

    /** The result for this test's item, or null. */
    protected JsonObject findItem(JsonArray results) {
        return findItem(results, itemKey);
    }

    protected JsonObject findItem(JsonArray results, String key) {
        for (JsonElement element : results) {
            JsonObject result = element.getAsJsonObject();
            if (result.get("reference").getAsString().contains(key)) {
                return result;
            }
        }
        return null;
    }

    protected JsonObject item(JsonArray results) {
        JsonObject result = findItem(results);
        assertNotNull(result, "no result for " + itemKey + " in " + results);
        return result;
    }

    /** Polls until searching the terms returns this test's item. */
    protected JsonObject awaitItem(String terms) {
        return awaitItem(terms, itemKey);
    }

    protected JsonObject awaitItem(String terms, String key) {
        return await("'" + terms + "' to find " + key, () -> findItem(search(terms), key), r -> r != null);
    }

    /** Polls until searching the terms no longer returns this test's item. */
    protected void awaitNoItem(String terms) {
        await("'" + terms + "' to stop finding " + itemKey, () -> findItem(search(terms)) == null, found -> found);
    }

    protected <T> T await(String description, Supplier<T> probe, Predicate<T> done) {
        long deadline = System.currentTimeMillis() + INDEX_TIMEOUT_MS;
        T value = probe.get();
        while (!done.test(value)) {
            if (System.currentTimeMillis() > deadline) {
                fail("Timed out after " + INDEX_TIMEOUT_MS / 1000 + "s waiting for " + description);
            }
            page.waitForTimeout(POLL_INTERVAL_MS);
            value = probe.get();
        }
        return value;
    }

    /** Runs a search through the portal search panel and returns the result cards. */
    protected Locator searchInPortal(String terms) {
        typeSearch(terms);
        Locator results = page.locator("sakai-search .search-result-link");
        Locator noResults = page.locator("sakai-search").getByText("No results", new Locator.GetByTextOptions().setExact(false));
        results.first().or(noResults).waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE).setTimeout(15_000));
        return results;
    }

    private void typeSearch(String terms) {
        if (!page.locator("#sakai-search-input").isVisible()) {
            page.locator("button[data-bs-target='#sakai-search-panel']:visible").first().click();
        }
        Locator input = page.locator("#sakai-search-input");
        input.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15_000));
        input.fill(terms);
        page.locator("#sakai-search-button").click();
    }

    private static String queryParam(String url, String name) {
        // parsed by hand: a badly built url is exactly what this has to report, not choke on
        String query = url.substring(url.indexOf('?') + 1).replaceFirst("#.*$", "");
        List<String> values = new ArrayList<>();
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                values.add(URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
            }
        }
        assertEquals(1, values.size(), "expected one " + name + " in " + url);
        return values.get(0);
    }
}
