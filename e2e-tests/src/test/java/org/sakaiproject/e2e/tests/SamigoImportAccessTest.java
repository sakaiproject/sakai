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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.sakaiproject.e2e.support.SakaiEnvironment;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Uses a Tests & Quizzes site with an instructor and a student who can only take assessments. */
@EnabledIfSystemProperty(named = "samigo.security.siteUrl", matches = ".+")
class SamigoImportAccessTest extends SakaiUiTestBase {

    @Test
    void studentCannotGetOrPostStaffViewsThroughAnyJsfMapping() {
        String tool = openTool(System.getProperty("samigo.security.student", "student0011"));
        for (String view : new String[] {"qti/importAssessment", "qti/importPool", "qti/exportAssessment",
                "qti/exportPool", "qti/exportItem", "qti/xmlDisplay", "samlite/samLiteEntry",
                "samlite/samLiteValidation", "print/printAssessment", "author/createAssessment_title",
                "evaluation/totalScores", "questionpool/poolList", "template/templateIndex",
                "event/eventLog", "section-activity/sectionActivity"}) {
            for (String suffix : new String[] {"", ".faces", ".jsf", ".xml", ".jsp"}) {
                assertForbidden(tool + "/jsf/" + view + suffix);
            }
        }
        String base = SakaiEnvironment.baseUrl();
        for (String suffix : new String[] {".faces", ".jsf", ".xml", ".jsp"}) {
            assertForbidden(base + "/samigo-app/jsf/qti/importAssessment" + suffix);
        }
        assertForbidden(base + "/samigo-app/faces/jsf/qti/importAssessment.jsp");
        Matcher placement = Pattern.compile(".*/tool/([^/?]+)").matcher(tool);
        if (!placement.matches()) {
            throw new AssertionError("Cannot determine tool placement: " + tool);
        }
        assertGetForbidden(base + "/samigo-app/servlet/PrintAssessmentPdf?sakai.tool.placement.id=" + placement.group(1));

        Response response = page.navigate(tool + "/jsf/select/selectIndex");
        assertNotNull(response);
        assertEquals(200, response.status());
        assertThat(page.locator("#importAssessmentForm")).hasCount(0);
        assertThat(page.locator("#selectIndexForm")).isVisible();
    }

    @Test
    void instructorCanOpenAssessmentPoolAndSamliteCreationViews() {
        String tool = openTool(System.getProperty("samigo.security.instructor", "instructor1"));
        for (String view : new String[] {"qti/importAssessment", "qti/importPool", "samlite/samLiteEntry"}) {
            Response response = page.navigate(tool + "/jsf/" + view);
            assertNotNull(response);
            assertEquals(200, response.status(), view);
            assertThat(page.locator(view.startsWith("qti/") ? "input[type=file]" : "#samLiteEntryForm")).isVisible();
        }
    }

    @Test
    void instructorImportRejectsTraversalAndStillAcceptsValidPackages() throws IOException {
        String tool = openTool(System.getProperty("samigo.security.instructor", "instructor1"));
        String title = "QTI security regression " + UUID.randomUUID();
        String qti = Files.readString(Path.of("../samigo/samigo-app/src/test/data/sample12Assessment.xml"))
            .replace("title=\"Marith Open Fri\"", "title=\"" + title + "\"");
        page.navigate(tool + "/jsf/qti/importAssessment");
        uploadPackage(qti, true);
        assertThat(page.locator("body")).containsText("There was an error importing this assessment.");

        page.navigate(tool + "/jsf/qti/importAssessment");
        uploadPackage(qti, false);
        assertThat(page.locator("#authorIndexForm\\:coreAssessments")).containsText(title);
    }

    private String openTool(String username) {
        sakai.login(username);
        page.navigate(System.getProperty("samigo.security.siteUrl"));
        sakai.toolClick("Tests");
        page.waitForURL(Pattern.compile(".*/tool/[^/?]+(?:/.*)?"));
        Matcher matcher = Pattern.compile("(.*/tool/[^/?]+).*?").matcher(page.url());
        if (!matcher.matches()) {
            throw new AssertionError("Cannot determine Tests & Quizzes tool URL: " + page.url());
        }
        return matcher.group(1);
    }

    private void assertForbidden(String url) {
        assertGetForbidden(url);
        APIResponse post = context.request().post(url, RequestOptions.create()
            .setMultipart(FormData.create().set("sourceType", "1")));
        try {
            assertEquals(403, post.status(), "POST " + url);
        } finally {
            post.dispose();
        }
    }

    private void assertGetForbidden(String url) {
        APIResponse get = context.request().get(url);
        try {
            assertEquals(403, get.status(), "GET " + url);
        } finally {
            get.dispose();
        }
    }

    private void uploadPackage(String qti, boolean traversal) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("exportAssessment.xml"));
            zip.write(qti.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            if (traversal) {
                zip.putNextEntry(new ZipEntry("../security-regression-" + UUID.randomUUID() + ".txt"));
                zip.write("inert regression marker".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        page.locator("input[type=file]").setInputFiles(new FilePayload("assessment.zip", "application/zip", bytes.toByteArray()));
        page.getByRole(AriaRole.BUTTON, new com.microsoft.playwright.Page.GetByRoleOptions().setName("Import").setExact(true)).click();
    }
}
