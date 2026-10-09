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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.sakaiproject.e2e.support.SakaiUiTestBase;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Opt-in migration experiment against an isolated full Sakai deployment. */
@EnabledIfSystemProperty(named = "samigo.equivalence.browserOutput", matches = ".+")
class SamigoViewEquivalenceTest extends SakaiUiTestBase {
    @Test
    @EnabledIfSystemProperty(named = "samigo.equivalence.calibration", matches = "true")
    void rejectsEmptyTitle() throws Exception {
        sakai.login("instructor1");
        String site = sakai.createProject("instructor1", List.of("sakai\\.samigo"));
        page.navigate(site);
        sakai.toolClick("Tests");
        step("calibration-opened");
        page.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$"))).first().click();
        assertThat(page.locator("#authorIndexForm\\:title")).isVisible();
        capture("calibration-opened");
        step("calibration-empty-title");
        page.locator("#authorIndexForm\\:createnew").click();
        capture("calibration-empty-title");
        assertThat(page.locator("#authorIndexForm .sak-banner-error")).isVisible();
    }

    @Test
    void createsAssessmentAndPersistsSecondQuestionEdit() throws Exception {
        String title = System.getProperty("samigo.equivalence.title", "Samigo Facelets Flow");
        sakai.login("instructor1");
        String site = sakai.createProject("instructor1", List.of("sakai\\.samigo"));
        page.navigate(site);
        sakai.toolClick("Tests");
        step("create-opened");
        page.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$"))).first().click();
        assertThat(page.locator("#authorIndexForm\\:title")).isVisible();
        capture("create-opened");

        step("empty-title");
        page.locator("#authorIndexForm\\:createnew").click();
        assertThat(page.locator("#authorIndexForm .sak-banner-error")).isVisible();
        capture("empty-title");
        step("assessment-created");
        page.locator("#authorIndexForm\\:title").fill(title);
        page.locator("#authorIndexForm\\:createnew").click();
        assertThat(page.locator("#assessmentForm\\:parts")).isVisible();
        capture("assessment-created");

        addQuestion("10", "First fixture question", "first-question-saved");
        addQuestion("20", "Second fixture question", "second-question-saved");
        step("second-question-opened");
        page.locator("#assessmentForm\\:parts\\:0\\:parts\\:1\\:modify").click();
        assertThat(page.locator("#itemForm textarea").first()).hasValue("Second fixture question");
        capture("second-question-opened");
        step("second-question-edited");
        page.locator("#itemForm\\:answerptr").fill("25");
        save();
        scores();
        capture("second-question-edited");

        context.setExtraHTTPHeaders(Map.of("Accept-Language", "en-US,en;q=0.9"));
        sakai.login("instructor1");
        page.navigate(site);
        sakai.toolClick("Tests");
        step("assessment-reopened");
        Locator row = page.locator("#authorIndexForm\\:coreAssessments tr").filter(new Locator.FilterOptions().setHasText(title));
        assertThat(row).hasCount(1);
        row.locator("button.dropdown-toggle").click();
        row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(Pattern.compile("^Edit$"))).click();
        scores();
        capture("assessment-reopened");
    }

    @Test
    @EnabledIfSystemProperty(named = "samigo.equivalence.allQuestionTypes", matches = "true")
    void rendersAndCancelsEveryAvailableQuestionType() throws Exception {
        sakai.login("instructor1");
        String site = sakai.createProject("instructor1", List.of("sakai\\.samigo"));
        page.navigate(site);
        sakai.toolClick("Tests");
        page.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$"))).first().click();
        page.locator("#authorIndexForm\\:title").fill("Samigo question type contracts");
        page.locator("#authorIndexForm\\:createnew").click();
        Locator select = page.locator("#assessmentForm\\:parts\\:0\\:changeQType");
        List<String> types = select.locator("option").elementHandles().stream()
            .map(option -> option.getAttribute("value"))
            .filter(value -> value.matches("[0-9]+") && !"10".equals(value)).toList();
        if (types.size() < 12) { throw new IllegalStateException("Expected all twelve available authoring types"); }
        for (String type : types) {
            String opened = "question-type-" + type + "-opened";
            step(opened);
            select.selectOption(type);
            assertThat(page.locator("#itemForm")).isVisible();
            capture(opened);
            String cancelled = "question-type-" + type + "-cancelled";
            step(cancelled);
            page.locator("#itemForm input[type=submit][value=Cancel]").first().click();
            assertThat(page.locator("#assessmentForm\\:parts")).isVisible();
            assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(0);
            capture(cancelled);
        }
    }

    private void addQuestion(String points, String text, String checkpoint) throws Exception {
        Locator select = page.locator("#assessmentForm\\:parts\\:0\\:changeQType");
        String value = null;
        for (ElementHandle option : select.locator("option").elementHandles()) {
            if (Pattern.compile("multiple\\s*choice", Pattern.CASE_INSENSITIVE).matcher(option.textContent()).find()) {
                value = option.getAttribute("value"); break;
            }
        }
        if (value == null) { throw new IllegalStateException("Multiple choice option is missing"); }
        step(checkpoint);
        select.selectOption(value);
        page.locator("#itemForm\\:answerptr").fill(points);
        page.locator("#itemForm textarea").first().fill(text);
        for (int index = 0; index < 3; index++) {
            page.locator("#itemForm\\:mcchoices textarea").nth(index).fill("Answer " + (char) ('A' + index));
        }
        page.locator("#itemForm\\:mcchoices input[type=radio]").first().check();
        save();
        capture(checkpoint);
    }

    @Test
    @EnabledIfSystemProperty(named = "samigo.equivalence.export", matches = "true")
    void exportsAssessmentAsWellFormedQtiXml() throws Exception {
        sakai.login("instructor1");
        String site = sakai.createProject("instructor1", List.of("sakai\\.samigo"));
        page.navigate(site);
        sakai.toolClick("Tests");
        page.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$"))).first().click();
        page.locator("#authorIndexForm\\:title").fill("Samigo QTI contract");
        page.locator("#authorIndexForm\\:createnew").click();
        addQuestion("10", "QTI fixture question", "export-question-saved");
        String assessmentId = page.locator("#assessmentForm\\:assessmentId").inputValue();
        String tool = page.url().substring(0, page.url().indexOf("/jsf/"));
        step("export-opened");
        page.navigate(tool + "/jsf/qti/exportAssessment.faces?exportAssessmentId=" + assessmentId);
        assertThat(page.locator("#exportAssessmentForm")).isVisible();
        capture("export-opened");
        step("export-downloaded");
        List<Map<String, String>> headers = new ArrayList<>();
        page.onResponse(response -> {
            if (response.headers().getOrDefault("content-disposition", "").contains("exportAssessment.xml")) {
                headers.add(response.headers());
            }
        });
        Download download = page.waitForDownload(() -> page.locator("#exportAssessmentForm input[type=submit]").first().click());
        assertEquals("exportAssessment.xml", download.suggestedFilename());
        assertEquals(1, headers.size());
        assertTrue(headers.getFirst().get("content-type").startsWith("text/xml"));
        String xml = Files.readString(download.path());
        Path output = Path.of(System.getProperty("samigo.equivalence.browserOutput"));
        Files.writeString(output.resolve("exportAssessment.xml"), xml);
        Files.writeString(output.resolve("export-headers.txt"), headers.getFirst().toString());
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
        int items = 0;
        try {
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && "item".equals(reader.getLocalName())) {
                    items++;
                }
            }
        } finally {
            reader.close();
        }
        assertEquals(1, items);
        assertTrue(xml.contains("QTI fixture question"));
        assertTrue(xml.contains("Samigo QTI contract"));
    }

    private void save() {
        page.locator("#itemForm input[type=submit][value=Save]").first().click();
        assertThat(page.locator("#assessmentForm\\:parts")).isVisible();
    }

    private void scores() {
        assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(2);
        assertThat(page.locator("#assessmentForm\\:parts\\:0\\:parts\\:0\\:answerptr")).hasValue(Pattern.compile("^10(?:\\.0+)?$"));
        assertThat(page.locator("#assessmentForm\\:parts\\:0\\:parts\\:1\\:answerptr")).hasValue(Pattern.compile("^25(?:\\.0+)?$"));
    }

    private void step(String name) {
        context.setExtraHTTPHeaders(Map.of("Accept-Language", "en-US,en;q=0.9", "X-Sakai-Equivalence-Step", name));
    }

    private void capture(String name) throws Exception {
        String json = (String) page.evaluate("""
            () => {
              const form = document.getElementById('assessmentForm') || document.getElementById('itemForm') || document.getElementById('authorIndexForm') || document.getElementById('exportAssessmentForm');
              return JSON.stringify({path: location.pathname, formId: form.id, formAction: form.getAttribute('action'),
                controls: [...form.querySelectorAll('input,select,textarea,button')]
                  .filter(e => !['jakarta.faces.ViewState', 'sakai_csrf_token'].includes(e.name))
                  .map(e => ({tag:e.tagName, id:e.id, name:e.name, type:e.type, value:e.value, checked:e.checked ?? null,
                    disabled:e.disabled, options:e.options ? [...e.options].map(o => ({value:o.value, text:o.text, selected:o.selected})) : null})),
                messages:[...form.querySelectorAll('.sak-banner-error')].map(e => e.innerText), text:form.innerText,
                scripts:[...document.scripts].filter(e=>e.src).map(e=>new URL(e.src).pathname),
                stylesheets:[...document.querySelectorAll('link[rel=stylesheet]')].map(e=>new URL(e.href).pathname)});
            }
            """);
        Path output = Path.of(System.getProperty("samigo.equivalence.browserOutput"));
        Files.createDirectories(output);
        Files.writeString(output.resolve(name + ".json"), json);
        Files.writeString(output.resolve(name + ".html"), (String) page.evaluate("() => (document.getElementById('content') || document.body).innerHTML"));
    }
}
