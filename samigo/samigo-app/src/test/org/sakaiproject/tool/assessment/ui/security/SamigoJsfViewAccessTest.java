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
package org.sakaiproject.tool.assessment.ui.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.Test;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SamigoJsfViewAccessTest {
    private static final Set<String> STUDENT = Set.of("assessment.takeAssessment", "assessment.submitAssessmentForGrade");

    @Test
    public void deploymentDescriptorsWireAuthorizationAndDenyRawTemplates() throws Exception {
        Document faces = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder()
            .parse(Path.of("src/webapp/WEB-INF/faces-config.xml").toFile());
        NodeList listeners = faces.getElementsByTagName("phase-listener");
        boolean registered = false;
        for (int i = 0; i < listeners.getLength(); i++) {
            registered |= listeners.item(i).getTextContent().trim().equals(SamigoJsfAuthorizationListener.class.getName());
        }
        assertTrue("Authorization must apply to every FacesServlet mapping", registered);

        Document web = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder()
            .parse(Path.of("src/webapp/WEB-INF/web.xml").toFile());
        for (String pattern : new String[] {"*.jsp", "*.jspf"}) {
            boolean denied = false;
            NodeList constraints = web.getElementsByTagName("security-constraint");
            for (int i = 0; i < constraints.getLength(); i++) {
                Element constraint = (Element) constraints.item(i);
                if (constraint.getElementsByTagName("auth-constraint").getLength() == 1
                        && constraint.getElementsByTagName("role-name").getLength() == 0) {
                    NodeList patterns = constraint.getElementsByTagName("url-pattern");
                    for (int j = 0; j < patterns.getLength(); j++) {
                        denied |= patterns.item(j).getTextContent().trim().equals(pattern);
                    }
                }
            }
            assertTrue("External template requests must be denied: " + pattern, denied);
        }
    }

    @Test
    public void studentsCannotEnterAnyStaffViewInTheWebapp() throws IOException {
        Path views = Path.of("src/webapp/jsf");
        try (Stream<Path> files = Files.walk(views)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".jsp")).toList()) {
                String view = "/jsf/" + views.relativize(file).toString().replace('\\', '/');
                if (view.startsWith("/jsf/author/") || view.startsWith("/jsf/evaluation/")
                        || view.startsWith("/jsf/questionpool/") || view.startsWith("/jsf/template/")
                        || view.startsWith("/jsf/event/") || view.startsWith("/jsf/section-activity/")
                        || view.startsWith("/jsf/samlite/") || view.startsWith("/jsf/print/")
                        || view.startsWith("/jsf/qti/")) {
                    if (!view.equals("/jsf/author/markForReviewPopUp.jsp") && !view.equals("/jsf/qti/exportDenied.jsp")) {
                        assertFalse(view, SamigoJsfViewAccess.isAllowed(view, STUDENT::contains));
                    }
                }
            }
        }
    }

    @Test
    public void creationRequiresTheSpecificPermissionAcrossEveryMapping() {
        for (String suffix : new String[] {"", ".jsp", ".faces", ".jsf", ".xml"}) {
            for (String view : new String[] {"/jsf/qti/importAssessment", "/jsf/samlite/samLiteEntry",
                    "/jsf/samlite/samLiteValidation", "/jsf/author/createAssessment_title"}) {
                assertFalse(view + suffix, SamigoJsfViewAccess.isAllowed(view + suffix, STUDENT::contains));
                assertFalse(view + suffix, SamigoJsfViewAccess.isAllowed(view + suffix, "assessment.gradeAssessment.any"::equals));
                assertTrue(view + suffix, SamigoJsfViewAccess.isAllowed(view + suffix, "assessment.createAssessment"::equals));
            }
            assertTrue(SamigoJsfViewAccess.isAllowed("/jsf/qti/importPool" + suffix, "assessment.questionpool.create"::equals));
            assertFalse(SamigoJsfViewAccess.isAllowed("/jsf/qti/importPool" + suffix, "assessment.createAssessment"::equals));
        }
    }

    @Test
    public void unrelatedStaffPermissionsDoNotGrantAccessToOtherTools() {
        assertTrue(SamigoJsfViewAccess.isAllowed("/jsf/author/authorIndex_container.jsp", "assessment.questionpool.create"::equals));
        assertTrue(SamigoJsfViewAccess.isAllowed("/jsf/author/authorIndex_container.jsp", "assessment.template.create"::equals));
        assertFalse(SamigoJsfViewAccess.isAllowed("/jsf/author/editAssessment.jsp", "assessment.questionpool.create"::equals));
        assertFalse(SamigoJsfViewAccess.isAllowed("/jsf/evaluation/totalScores.jsp", "assessment.questionpool.create"::equals));
        assertFalse(SamigoJsfViewAccess.isAllowed("/jsf/print/printAssessment.jsp", "assessment.template.create"::equals));
        assertFalse(SamigoJsfViewAccess.isAllowed("/jsf/qti/exportAssessment.jsp", "assessment.questionpool.create"::equals));
        assertTrue(SamigoJsfViewAccess.isAllowed("/jsf/evaluation/totalScores.jsp", "assessment.gradeAssessment.own"::equals));
        assertTrue(SamigoJsfViewAccess.isAllowed("/jsf/author/permissions.jsp", "site.upd"::equals));
        assertFalse(SamigoJsfViewAccess.isAllowed("/jsf/author/permissions.jsp", "assessment.createAssessment"::equals));
    }

    @Test
    public void studentAndAnonymousDeliveryViewsRemainAccessible() {
        for (String view : new String[] {"/jsf/index/mainIndex.jsp", "/jsf/select/selectIndex.jsp",
                "/jsf/delivery/beginTakingAssessment.jsp", "/jsf/delivery/deliverAssessment.jsp",
                "/jsf/review/reviewAssessment.jsp", "/jsf/shared/removeMedia.jsp",
                "/jsf/author/markForReviewPopUp.jsp", "/jsf/delivery/popup/autoSubmit.jsp"}) {
            assertTrue(view, SamigoJsfViewAccess.isAllowed(view, STUDENT::contains));
            assertTrue(view, SamigoJsfViewAccess.isAllowed(view, permission -> false));
        }
        assertTrue(SamigoJsfViewAccess.isAllowed("/jsf/widget/timerBar/timerbar.js", permission -> false));
    }

    @Test
    public void unknownAndAmbiguousPathsAreDeniedEvenToStaff() {
        for (String view : new String[] {"/jsf/new-tool/admin.jsp", "/jsf/upload/uploadFile.jsp",
                "/jsf/select/../qti/importAssessment.jsp", "/jsf/select/%2e%2e/qti/importAssessment.jsp",
                "/jsf/qti//importAssessment.jsp", "/jsf/author//permissions.jsp",
                "/jsf/author/permissions.jsp;ignored", "/jsf/author\\permissions.jsp", null}) {
            assertFalse(String.valueOf(view), SamigoJsfViewAccess.isAllowed(view, permission -> true));
        }
    }
}
