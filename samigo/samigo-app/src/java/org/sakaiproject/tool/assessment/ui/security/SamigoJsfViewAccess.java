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

import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_CREATE_ASSESSMENT;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_DELETE_ASSESSMENT_ANY;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_DELETE_ASSESSMENT_OWN;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_EDIT_ASSESSMENT_ANY;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_EDIT_ASSESSMENT_OWN;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_GRADE_ASSESSMENT_ANY;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_GRADE_ASSESSMENT_OWN;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_PUBLISH_ASSESSMENT_ANY;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_PUBLISH_ASSESSMENT_OWN;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_QUESTIONPOOL_COPY_OWN;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_QUESTIONPOOL_CREATE;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_QUESTIONPOOL_DELETE_OWN;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_QUESTIONPOOL_EDIT_OWN;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_TEMPLATE_CREATE;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_TEMPLATE_DELETE_OWN;
import static org.sakaiproject.samigo.util.SamigoConstants.AUTHZ_TEMPLATE_EDIT_OWN;
import static org.sakaiproject.site.api.SiteService.SECURE_UPDATE_SITE;

import java.util.Set;
import java.util.function.Predicate;

/** Site permission requirements for Samigo views, independent of the servlet mapping. */
public final class SamigoJsfViewAccess {

    private static final Set<String> ASSESSMENT = Set.of(
        AUTHZ_CREATE_ASSESSMENT, AUTHZ_EDIT_ASSESSMENT_ANY, AUTHZ_EDIT_ASSESSMENT_OWN,
        AUTHZ_DELETE_ASSESSMENT_ANY, AUTHZ_DELETE_ASSESSMENT_OWN,
        AUTHZ_PUBLISH_ASSESSMENT_ANY, AUTHZ_PUBLISH_ASSESSMENT_OWN,
        AUTHZ_GRADE_ASSESSMENT_ANY, AUTHZ_GRADE_ASSESSMENT_OWN);
    private static final Set<String> EDIT = Set.of(AUTHZ_EDIT_ASSESSMENT_ANY, AUTHZ_EDIT_ASSESSMENT_OWN);
    private static final Set<String> GRADE = Set.of(AUTHZ_GRADE_ASSESSMENT_ANY, AUTHZ_GRADE_ASSESSMENT_OWN);
    private static final Set<String> POOL = Set.of(AUTHZ_QUESTIONPOOL_CREATE, AUTHZ_QUESTIONPOOL_EDIT_OWN,
        AUTHZ_QUESTIONPOOL_DELETE_OWN, AUTHZ_QUESTIONPOOL_COPY_OWN);
    private static final Set<String> TEMPLATE = Set.of(AUTHZ_TEMPLATE_CREATE, AUTHZ_TEMPLATE_EDIT_OWN,
        AUTHZ_TEMPLATE_DELETE_OWN);

    private SamigoJsfViewAccess() {
    }

    public static boolean isAllowed(String viewId, Predicate<String> hasPermission) {
        if (viewId == null || !viewId.startsWith("/jsf/") || viewId.contains("\\")
                || viewId.contains(";") || viewId.contains("%") || viewId.contains("//")) {
            return false;
        }
        for (String segment : viewId.split("/")) {
            if (segment.equals(".") || segment.equals("..")) {
                return false;
            }
        }
        String view = viewId.replaceFirst("\\.(jsp|faces|jsf|xml)$", "");
        if (viewId.startsWith("/jsf/widget/") && viewId.endsWith(".js")) {
            return true;
        }
        if (view.equals("/jsf/author/markForReviewPopUp") || view.equals("/jsf/qti/exportDenied")) {
            return true;
        }
        if (view.equals("/jsf/author/permissions") || view.equals("/jsf/author/permissionsHeadings")
                || view.equals("/jsf/author/permissionsNav")) {
            return hasPermission.test(SECURE_UPDATE_SITE);
        }
        if (view.equals("/jsf/qti/importAssessment") || view.equals("/jsf/qti/importAssessmentFromRespondus")
                || view.startsWith("/jsf/samlite/")
                || view.equals("/jsf/author/createAssessment_title")) {
            return hasPermission.test(AUTHZ_CREATE_ASSESSMENT);
        }
        if (view.equals("/jsf/qti/importPool") || view.equals("/jsf/questionpool/addPool")) {
            return hasPermission.test(AUTHZ_QUESTIONPOOL_CREATE);
        }
        if (view.equals("/jsf/qti/choosePoolExportType") || view.equals("/jsf/qti/exportPool")
                || view.equals("/jsf/qti/xmlPoolDisplay")) {
            return POOL.stream().anyMatch(hasPermission);
        }
        if (view.equals("/jsf/qti/exportItem") || view.equals("/jsf/qti/xmlDisplay")) {
            return EDIT.stream().anyMatch(hasPermission) || POOL.stream().anyMatch(hasPermission);
        }
        if (view.startsWith("/jsf/qti/")) {
            return EDIT.stream().anyMatch(hasPermission);
        }
        // Pool/template authors also use the assessment landing page to navigate.
        if (view.equals("/jsf/author/authorIndex_container") || view.equals("/jsf/author/authorIndex_content")) {
            return ASSESSMENT.stream().anyMatch(hasPermission) || POOL.stream().anyMatch(hasPermission)
                || TEMPLATE.stream().anyMatch(hasPermission);
        }
        if (view.startsWith("/jsf/author/") || view.startsWith("/jsf/print/")) {
            return ASSESSMENT.stream().anyMatch(hasPermission);
        }
        if (view.startsWith("/jsf/evaluation/")) {
            return GRADE.stream().anyMatch(hasPermission);
        }
        if (view.startsWith("/jsf/questionpool/") || view.startsWith("/jsf/event/")
                || view.startsWith("/jsf/section-activity/")) {
            return POOL.stream().anyMatch(hasPermission);
        }
        if (view.startsWith("/jsf/template/")) {
            return TEMPLATE.stream().anyMatch(hasPermission);
        }
        // These views serve assessment takers (including anonymous assessments).
        // Delivery, review and media listeners retain their assessment/ownership checks.
        return view.startsWith("/jsf/index/") || view.startsWith("/jsf/select/")
            || view.startsWith("/jsf/delivery/") || view.startsWith("/jsf/review/")
            || view.startsWith("/jsf/shared/") || view.equals("/jsf/upload/closeWindow")
            || view.equals("/jsf/security/roleCheckStaticInclude");
    }
}
