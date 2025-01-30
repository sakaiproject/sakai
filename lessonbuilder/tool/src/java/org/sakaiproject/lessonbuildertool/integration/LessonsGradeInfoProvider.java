/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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

package org.sakaiproject.lessonbuildertool.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.grading.api.ExternalAssignmentProvider;
import org.sakaiproject.grading.api.ExternalAssignmentProviderCompat;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonsAccess;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;

@Slf4j
public class LessonsGradeInfoProvider implements ExternalAssignmentProvider, ExternalAssignmentProviderCompat {

    @Setter private GradingService gradingService;
    @Setter private SimplePageToolDao simplePageToolDao;
    @Setter private LessonsAccess lessonsAccess;

    private static final String LESSON_BUILDER_ID_PREPEND = "lesson-builder:question:";

    public void init() {
        log.info("INIT and register LessonsGradeInfoProvider");
        gradingService.registerExternalAssignmentProvider(this);
    }

    public void destroy() {
        log.info("DESTROY and unregister LessonsGradeInfoProvider");
        gradingService.unregisterExternalAssignmentProvider(getAppKey());
    }

    public String getAppKey() {
        return "sakai.lessonbuildertool";
    }

    public boolean isAssignmentGrouped(String id) {
        SimplePageItem simplePageItem = getAssignment(id);
        if (simplePageItem != null) {
            return simplePageItem.getGroups() != null;
        }
        return false;
    }

    public boolean isAssignmentDefined(String externalAppName, String id) {
        if (!externalAppName.equals(getAppKey())) {
          return false;
        }
        return getAssignment(id) != null;
    }

    public boolean isAssignmentVisible(String id, String userId) {
        SimplePageItem simplePageItem = getAssignment(id);
        if (simplePageItem != null) {
            SimplePage simplePage = simplePageToolDao.getPage(simplePageItem.getPageId());
            SimplePageBean simplePageBean = lessonsAccess.makeSimplePageBean(null, simplePage.getSiteId(), simplePage);
            return simplePageBean.isItemVisible(simplePageItem);
        }
        return false;
    }

    public List<String> getExternalAssignmentsForCurrentUser(String gradebookUid) {
        return getAllExternalAssignments(gradebookUid);
    }

    public List<String> getAllExternalAssignments(String gradebookUid) {
        return new ArrayList<>();
    }

    public Map<String, List<String>> getAllExternalAssignments(String gradebookUid, Collection<String> studentIds) {
        return new HashMap<>();
    }

    private SimplePageItem getAssignment(String id) {
        try {
            int questionId = Integer.parseInt(id.replace(LESSON_BUILDER_ID_PREPEND, ""));
            return simplePageToolDao.findItem(questionId);
        } catch(NumberFormatException ex) {
            return null;
        }
    }
}
