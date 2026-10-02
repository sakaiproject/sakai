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

import java.util.Set;
import java.util.function.Predicate;

/** Site permission requirements for Samigo views, independent of the servlet mapping. */
public final class SamigoJsfViewAccess {

    private static final Set<String> ASSESSMENT = Set.of(
        "assessment.createAssessment", "assessment.editAssessment.any", "assessment.editAssessment.own",
        "assessment.deleteAssessment.any", "assessment.deleteAssessment.own",
        "assessment.publishAssessment.any", "assessment.publishAssessment.own",
        "assessment.gradeAssessment.any", "assessment.gradeAssessment.own");
    private static final Set<String> EDIT = Set.of("assessment.editAssessment.any", "assessment.editAssessment.own");
    private static final Set<String> GRADE = Set.of("assessment.gradeAssessment.any", "assessment.gradeAssessment.own");
    private static final Set<String> POOL = Set.of("assessment.questionpool.create", "assessment.questionpool.edit.own",
        "assessment.questionpool.delete.own", "assessment.questionpool.copy.own");
    private static final Set<String> TEMPLATE = Set.of("assessment.template.create", "assessment.template.edit.own",
        "assessment.template.delete.own");

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
            return hasPermission.test("site.upd");
        }
        if (view.equals("/jsf/qti/importAssessment") || view.equals("/jsf/qti/importAssessmentFromRespondus")
                || view.startsWith("/jsf/samlite/")
                || view.equals("/jsf/author/createAssessment_title")) {
            return hasPermission.test("assessment.createAssessment");
        }
        if (view.equals("/jsf/qti/importPool") || view.equals("/jsf/questionpool/addPool")) {
            return hasPermission.test("assessment.questionpool.create");
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
