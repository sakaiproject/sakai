/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business.util;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.gradebookng.business.GbEvent;
import org.sakaiproject.gradebookng.business.GbRole;

import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.api.Event;

public class EventHelper {

    private static String EVENT_REF_PREFIX = "/gradebookng";

    public static void postAddAssignmentEvent(Gradebook gradebook, long assignmentId, Assignment assignment, GbRole currentRole) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
            String.valueOf(assignmentId),
            assignment.getName(),
            String.valueOf(assignment.getPoints()),
            String.valueOf(assignment.getDueDate() == null ? -1 : assignment.getDueDate().getTime()),
            String.valueOf(assignment.isReleased()),
            String.valueOf(assignment.isCounted()),
            assignment.getCategoryId() == null ? "uncategorized" : String.valueOf(assignment.getCategoryId()),
            currentRole.toString().toLowerCase()
        };

        postEvent(createEvent(GbEvent.ADD_ASSIGNMENT, String.join("/", bits), true));
    }

    public static void postUpdateAssignmentEvent(Gradebook gradebook, Assignment assignment, GbRole currentRole) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
            String.valueOf(assignment.getId()),
            assignment.getName(),
            String.valueOf(assignment.getPoints()),
            String.valueOf(assignment.getDueDate() == null ? -1 : assignment.getDueDate().getTime()),
            String.valueOf(assignment.isReleased()),
            String.valueOf(assignment.isCounted()),
            assignment.getCategoryId() == null ? "uncategorized" : String.valueOf(assignment.getCategoryId()),
            currentRole.toString().toLowerCase()
        };

        postEvent(createEvent(GbEvent.UPDATE_ASSIGNMENT, String.join("/", bits), true));
    }


    public static void postDeleteAssignmentEvent(Gradebook gradebook, Long assignmentId, GbRole currentRole) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
            String.valueOf(assignmentId),
            currentRole.toString().toLowerCase()
        };

        postEvent(createEvent(GbEvent.DELETE_ASSIGNMENT, String.join("/", bits), true));
    }


    public static void postUpdateGradeEvent(Gradebook gradebook, Long assignmentId, String studentUid, String grade, GradeSaveResponse gsr, GbRole currentRole) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
            String.valueOf(assignmentId),
            studentUid,
            grade,
            gsr.toString(),
            currentRole.toString().toLowerCase()
        };

        postEvent(createEvent(GbEvent.UPDATE_GRADE, String.join("/", bits), true));
    }


    public static void postUpdateUngradedEvent(Gradebook gradebook, Long assignmentId, String grade, GbRole currentRole) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
            String.valueOf(assignmentId),
            grade,
            currentRole.toString().toLowerCase()
        };

        postEvent(createEvent(GbEvent.UPDATE_UNGRADED, String.join("/", bits), true));
    }


    public static void postUpdateCommentEvent(Gradebook gradebook, Long assignmentId, String studentUid, String comment, GbRole currentRole) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
            String.valueOf(assignmentId),
            studentUid,
            String.valueOf(StringUtils.length(comment)),
            currentRole.toString().toLowerCase()
        };

        postEvent(createEvent(GbEvent.UPDATE_COMMENT, String.join("/", bits), true));
    }


    public static void postUpdateSettingsEvent(Gradebook gradebook) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
        };

        postEvent(createEvent(GbEvent.UPDATE_SETTINGS, String.join("/", bits), true));
    }


    public static void postStudentViewEvent(Gradebook gradebook, String studentUid) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
            studentUid,
        };

        postEvent(createEvent(GbEvent.STUDENT_VIEW, String.join("/", bits), false));
    }


    public static void postExportEvent(Gradebook gradebook, boolean isCustomExport) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
            isCustomExport ? "custom" : "full"
        };

        postEvent(createEvent(GbEvent.EXPORT, String.join("/", bits), false));
    }


    public static void postImportBeginEvent(Gradebook gradebook) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId())
        };

        postEvent(createEvent(GbEvent.IMPORT_BEGIN, String.join("/", bits), false));
    }


    public static void postImportCompletedEvent(Gradebook gradebook, boolean success) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
            success ? "success" : "errors"
        };

        postEvent(createEvent(GbEvent.IMPORT_COMPLETED, String.join("/", bits), false));
    }


    public static void postOverrideCourseGradeEvent(Gradebook gradebook, String studentUid, String courseGrade, boolean isOverride) {
        String[] bits = new String[] {
            EVENT_REF_PREFIX,
            String.valueOf(gradebook.getId()),
            studentUid,
            courseGrade,
            isOverride ? "override" : "reverted"
        };

        postEvent(createEvent(GbEvent.OVERRIDE_COURSE_GRADE, String.join("/", bits), true));
    }


    private static Event createEvent(GbEvent type, String ref, boolean causedModify) {
        return EventTrackingService.newEvent(type.getEvent(), ref, causedModify);
    }

    private static void postEvent(Event event) {
        EventTrackingService.post(event);
    }

}
