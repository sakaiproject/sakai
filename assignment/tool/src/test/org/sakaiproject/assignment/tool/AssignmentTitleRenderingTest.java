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
package org.sakaiproject.assignment.tool;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.component.impl.BasicConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.thread_local.impl.ThreadLocalComponent;
import org.sakaiproject.util.BasicConfigItem;
import org.sakaiproject.util.impl.FormattedTextImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AssignmentTitleRenderingTest {

    private static FormattedTextImpl formattedText;

    @BeforeClass
    public static void setUpFormattedText() {
        BasicConfigurationService configuration = new BasicConfigurationService();
        configuration.setThreadLocalManager(new ThreadLocalComponent());
        configuration.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem(
                "content.cleaner.errors.handling", "none", "AssignmentTitleRenderingTest"));
        formattedText = new FormattedTextImpl();
        formattedText.setServerConfigurationService(configuration);
        // FormattedText only consults the database vendor during initialization.
        formattedText.setSqlService(mock(SqlService.class));
        formattedText.init();
    }

    @Test
    public void preservesPunctuationInHtmlTextAndAttributes() {
        for (String title : List.of("Double \"quotes\"", "Single 'quotes'", "Research & Development",
                "2 < 3 > 1", "\"Quotes\" & 'apostrophes' < >", "Café — 日本語")) {
            String storedTitle = sanitize(title);
            String escapedTitle = formattedText.escapeHtml(formattedText.unEscapeHtml(storedTitle));
            Document document = Jsoup.parseBodyFragment(
                    "<span title=\"" + escapedTitle + "\">" + escapedTitle + "</span>");
            assertEquals(title, document.selectFirst("span").text());
            assertEquals(title, document.selectFirst("span").attr("title"));
        }
    }

    @Test
    public void editAndSaveDoNotAccumulateEncoding() {
        String title = "Research \"A & B\" < 10";
        String storedTitle = sanitize(title);
        for (int save = 0; save < 3; save++) {
            String editableTitle = formattedText.unEscapeHtml(storedTitle);
            assertEquals(title, editableTitle);
            String savedAgain = sanitize(editableTitle);
            assertEquals(storedTitle, savedAgain);
            storedTitle = savedAgain;
        }
    }

    @Test
    public void decodesOnlyOneLayerOfEntities() {
        assertEquals("Literal &amp; and &#34;", formattedText.unEscapeHtml("Literal &amp;amp; and &amp;#34;"));
        assertEquals("Literal &amp;amp; and &amp;#34;",
                formattedText.escapeHtml(formattedText.unEscapeHtml("Literal &amp;amp; and &amp;#34;")));
    }

    @Test
    public void sanitizesMarkupBeforeStorage() {
        String storedTitle = sanitize("Title <img src=x onerror=alert(1)><script>alert(2)</script>");
        Document document = Jsoup.parseBodyFragment(storedTitle);
        assertTrue(document.select("script, [onerror], [onclick]").isEmpty());
        assertFalse(storedTitle.contains("onerror"));
        assertFalse(storedTitle.contains("<script"));
    }

    @Test
    public void neverTurnsEncodedMarkupIntoActiveHtml() {
        for (String title : List.of("<img src=x onerror=alert(1)>",
                "&lt;img src=x onerror=alert(1)&gt;",
                "&amp;lt;img src=x onerror=alert(1)&amp;gt;",
                "\" onmouseover=\"alert(1)")) {
            String escapedTitle = formattedText.escapeHtml(formattedText.unEscapeHtml(sanitize(title)));
            Document document = Jsoup.parseBodyFragment(
                    "<span title=\"" + escapedTitle + "\">" + escapedTitle + "</span>");
            assertEquals(1, document.body().childrenSize());
            assertTrue(document.select("img, script, [onerror], [onmouseover]").isEmpty());
        }
    }

    @Test
    public void rendersStoredTitlesThroughTheAssignmentTemplate() throws Exception {
        String title = "Research \"A & B\" < 10";
        Assignment assignment = new Assignment();
        assignment.setTitle(sanitize(title));
        VelocityContext context = new VelocityContext();
        context.put("assignment", assignment);
        context.put("formattedText", formattedText);
        Document document = render("chef_assignments_student_view_group_error.vm", context);
        assertEquals(title, document.selectFirst("table.itemSummary tr td").text());
    }

    @Test
    public void escapesUnsavedTitlesInTheStudentPreview() throws Exception {
        String title = "\"Quotes\" & <img src=x onerror=alert(1)>";
        VelocityContext context = new VelocityContext();
        context.put("value_Title", title);
        context.put("formattedText", formattedText);
        Document document = render("chef_assignments_instructor_preview_assignment.vm", context);
        assertEquals(title, document.selectFirst(".inopPanel h4").text());
        assertTrue(document.select("[onerror]").isEmpty());
    }

    @Test
    public void handlesEmptyTitles() {
        assertEquals("", formattedText.escapeHtml(formattedText.unEscapeHtml(null)));
        assertEquals("", formattedText.escapeHtml(formattedText.unEscapeHtml("")));
        assertEquals("", formattedText.unEscapeHtml(null));
    }

    private Document render(String template, VelocityContext context) throws Exception {
        VelocityEngine engine = new VelocityEngine();
        engine.init();
        StringWriter output = new StringWriter();
        engine.evaluate(context, output, template,
                Files.readString(Path.of("src/webapp/vm/assignment", template)));
        return Jsoup.parseBodyFragment(output.toString());
    }

    private String sanitize(String title) {
        return formattedText.processFormattedText(title, null, true, true);
    }
}
