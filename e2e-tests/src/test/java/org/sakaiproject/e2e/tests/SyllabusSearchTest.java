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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.AbstractToolSearchTest;

class SyllabusSearchTest extends AbstractToolSearchTest {

    @Override
    protected String toolId() {
        return "sakai\\.syllabus";
    }

    @Override
    protected String searchToolName() {
        return "syllabus";
    }

    @Override
    protected String toolLabel() {
        return "Syllabus";
    }

    // students read posted syllabus items, so every member may find them
    @Override
    protected String deniedUser() {
        return null;
    }

    /** Adds a posted item the way the tool's Add dialog does. */
    @Override
    protected String createItem(String siteId, String title, String body) {
        syllabusPost("0", FormData.create().set("add", "true").set("title", title).set("siteId", siteId)
            .set("published", "true").set("content", body));
        return "/syllabus/" + siteId + "/" + itemId(title);
    }

    /** Edits the body in place, as the tool's inline editor does. */
    @Override
    protected void updateItem(String siteId, String itemKey, String body) {
        syllabusPost(id(itemKey), FormData.create().set("name", "body").set("value", body));
    }

    @Override
    protected void deleteItem(String siteId, String itemKey) {
        syllabusPost(id(itemKey), FormData.create().set("delete", "true"));
    }

    @Test
    @Order(7)
    void markupInSearchTermsNeitherFailsNorMatchesMarkup() {
        sakai.login(author());

        // the item's body holds <b> tags, which are markup, not text
        assertNull(findItem(search("<b>")));
        assertNull(findItem(search("<p>")));
        // the panel answers with results or "No Results"; a failed search shows neither and times out
        searchInPortal("<p>");
    }

    @Test
    @Order(10)
    void attachmentsAreFoundByNameAndContent() {
        sakai.login(author());
        String fileWord = uniqueWord();
        String pdfWord = uniqueWord();
        String fileName = "Course_" + fileWord + ".pdf";
        String title = "Attached " + uniqueWord();
        String key = createItem(siteId, title, "Reading list");

        openItemEditor(title);
        page.locator("input[type=submit][value='Add attachments']").click();
        page.locator("input[type=file]").first().setInputFiles(
            new FilePayload(fileName, "application/pdf", pdf("Week one covers " + pdfWord)));
        page.locator("#attachButton").click();
        page.locator("input[type=submit][value='Publish']").click();

        // found by the whole name and by a word of it
        awaitItem(fileName, key);
        awaitItem(fileWord, key);
        awaitItem(pdfWord, key);

        // removal is saved at once, without publishing again, so it alone has to update search
        openItemEditor(title);
        // the remove action only shows while hovering its row
        Locator attachmentRow = page.locator("tr").filter(new Locator.FilterOptions().setHasText(fileName));
        attachmentRow.hover();
        attachmentRow.locator("a").filter(new Locator.FilterOptions().setHasText("Remove")).first().click();
        page.locator("input[type=submit][value='Delete']").click();
        await("'" + pdfWord + "' to stop finding " + key + " after its attachment was removed",
            () -> findItem(search(pdfWord), key) == null, gone -> gone);
    }

    @Test
    @Order(11)
    void unpublishedItemsLeaveTheIndex() {
        sakai.login(author());
        String word = uniqueWord();
        String key = createItem(siteId, "Draft " + word, "Grading by " + word);
        awaitItem(word, key);

        syllabusPost(id(key), FormData.create().set("toggle", "publish").set("status", "false"));

        await("'" + word + "' to stop finding the unpublished " + key, () -> findItem(search(word), key) == null, gone -> gone);
    }

    private void syllabusPost(String id, FormData form) {
        APIResponse response = page.request().post("/direct/syllabus/" + id + ".json", RequestOptions.create().setForm(form));
        assertTrue(response.ok(), "syllabus " + id + " failed: HTTP " + response.status() + " " + response.text());
    }

    private static String id(String reference) {
        return reference.substring(reference.lastIndexOf('/') + 1);
    }

    // the entity feed has no ids, so read the id from the tool page like its own scripts do
    private String itemId(String title) {
        sakai.gotoPath("/portal/site/" + siteId);
        sakai.toolClick("Syllabus");
        Locator item = page.locator("div.group[syllabusItem]").filter(new Locator.FilterOptions().setHasText(title));
        item.first().waitFor();
        return item.first().getAttribute("syllabusItem").trim();
    }

    private void openItemEditor(String title) {
        sakai.gotoPath("/portal/site/" + siteId);
        sakai.toolClick("Syllabus");
        page.locator("button, a, input").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*Manage\\s*$"))).first().click();
        page.locator("tr, li, div").filter(new Locator.FilterOptions().setHasText(title)).locator("a")
            .filter(new Locator.FilterOptions().setHasText("Edit Details")).first().click();
    }

    /** A one page PDF whose text PDFBox can extract. */
    private static byte[] pdf(String text) {
        String stream = "BT /F1 18 Tf 72 720 Td (" + text + ") Tj ET";
        String[] objects = {
            "<< /Type /Catalog /Pages 2 0 R >>",
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>",
            "<< /Length " + stream.length() + " >>\nstream\n" + stream + "\nendstream",
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"
        };
        StringBuilder sb = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            offsets[i] = sb.length();
            sb.append(i + 1).append(" 0 obj\n").append(objects[i]).append("\nendobj\n");
        }
        int xref = sb.length();
        sb.append("xref\n0 ").append(objects.length + 1).append("\n0000000000 65535 f \n");
        for (int offset : offsets) {
            sb.append(String.format("%010d 00000 n \n", offset));
        }
        sb.append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF\n");
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }
}
