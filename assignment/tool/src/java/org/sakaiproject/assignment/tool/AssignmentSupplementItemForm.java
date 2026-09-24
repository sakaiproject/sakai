/*
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.SessionState;

/** Translates supplemental-item form state into service calls. */
final class AssignmentSupplementItemForm {

    /******** Model Answer ************/
    static final String MODELANSWER = "modelAnswer";
    static final String MODELANSWER_TEXT = "modelAnswer.text";
    static final String MODELANSWER_SHOWTO = "modelAnswer.showTo";
    static final String MODELANSWER_ATTACHMENTS = "modelanswer_attachments";
    static final String MODELANSWER_TO_DELETE = "modelanswer.toDelete";
    /******** Note ***********/
    static final String NOTE = "note";
    static final String NOTE_TEXT = "note.text";
    static final String NOTE_SHAREWITH = "note.shareWith";
    static final String NOTE_TO_DELETE = "note.toDelete";
    /******** AllPurpose *******/
    static final String ALLPURPOSE = "allPurpose";
    static final String ALLPURPOSE_TITLE = "allPurpose.title";
    static final String ALLPURPOSE_TEXT = "allPurpose.text";
    static final String ALLPURPOSE_HIDE = "allPurpose.hide";
    static final String ALLPURPOSE_SHOW_FROM = "allPurpose.show.from";
    static final String ALLPURPOSE_SHOW_TO = "allPurpose.show.to";
    static final String ALLPURPOSE_RELEASE_DATE = "allPurpose.releaseDate";
    static final String ALLPURPOSE_RETRACT_DATE = "allPurpose.retractDate";
    static final String ALLPURPOSE_ACCESS = "allPurpose.access";
    static final String ALLPURPOSE_ATTACHMENTS = "allPurpose_attachments";
    static final String ALLPURPOSE_RELEASE_YEAR = "all_purpose_release_year";
    static final String ALLPURPOSE_RELEASE_MONTH = "all_purpose_release_month";
    static final String ALLPURPOSE_RELEASE_DAY = "all_purpose_release_day";
    static final String ALLPURPOSE_RELEASE_HOUR = "all_purpose_release_hour";
    static final String ALLPURPOSE_RELEASE_MIN = "all_purpose_release_min";
    static final String ALLPURPOSE_RETRACT_YEAR = "all_purpose_retract_year";
    static final String ALLPURPOSE_RETRACT_MONTH = "all_purpose_retract_month";
    static final String ALLPURPOSE_RETRACT_DAY = "all_purpose_retract_day";
    static final String ALLPURPOSE_RETRACT_HOUR = "all_purpose_retract_hour";
    static final String ALLPURPOSE_RETRACT_MIN = "all_purpose_retract_min";
    static final String ALLPURPOSE_TO_DELETE = "allPurpose.toDelete";

    static void save(SessionState state, String assignmentId, String siteId, String creatorId,
            ZoneId zoneId, AssignmentSupplementItemService service) {
        if ("true".equals(state.getAttribute(MODELANSWER_TO_DELETE))) {
            service.updateModelAnswer(assignmentId, null, 0, null, true);
        } else if (state.getAttribute(MODELANSWER_TEXT) != null) {
            int showTo = state.getAttribute(MODELANSWER_SHOWTO) == null ? 0
                    : Integer.parseInt((String) state.getAttribute(MODELANSWER_SHOWTO));
            service.updateModelAnswer(assignmentId,
                    (String) state.getAttribute(MODELANSWER_TEXT), showTo,
                    getSupplementAttachmentIds(state, MODELANSWER_ATTACHMENTS), false);
        }

        if ("true".equals(state.getAttribute(NOTE_TO_DELETE))) {
            service.updateNote(assignmentId, null, null, 0, true);
        } else if (state.getAttribute(NOTE_TEXT) != null) {
            int shareWith = state.getAttribute(NOTE_SHAREWITH) == null ? 0
                    : Integer.parseInt((String) state.getAttribute(NOTE_SHAREWITH));
            service.updateNote(assignmentId,
                    creatorId,
                    (String) state.getAttribute(NOTE_TEXT), shareWith, false);
        }

        if ("true".equals(state.getAttribute(ALLPURPOSE_TO_DELETE))) {
            service.updateAllPurposeItem(
                    assignmentId, siteId, null, null, null, true);
        } else if (state.getAttribute(ALLPURPOSE_TITLE) != null) {
            AssignmentAllPurposeItem values = new AssignmentAllPurposeItem();
            values.setTitle((String) state.getAttribute(ALLPURPOSE_TITLE));
            values.setText((String) state.getAttribute(ALLPURPOSE_TEXT));
            boolean hide = Boolean.TRUE.equals(state.getAttribute(ALLPURPOSE_HIDE));
            values.setHide(hide);
            Instant releaseTime = Boolean.TRUE.equals(state.getAttribute(ALLPURPOSE_SHOW_FROM)) && !hide
                    ? getTimeFromState(state, zoneId, ALLPURPOSE_RELEASE_MONTH, ALLPURPOSE_RELEASE_DAY,
                            ALLPURPOSE_RELEASE_YEAR, ALLPURPOSE_RELEASE_HOUR, ALLPURPOSE_RELEASE_MIN)
                    : null;
            Instant retractTime = Boolean.TRUE.equals(state.getAttribute(ALLPURPOSE_SHOW_TO)) && !hide
                    ? getTimeFromState(state, zoneId, ALLPURPOSE_RETRACT_MONTH, ALLPURPOSE_RETRACT_DAY,
                            ALLPURPOSE_RETRACT_YEAR, ALLPURPOSE_RETRACT_HOUR, ALLPURPOSE_RETRACT_MIN)
                    : null;
            values.setReleaseDate(releaseTime == null ? null : Date.from(releaseTime));
            values.setRetractDate(retractTime == null ? null : Date.from(retractTime));
            Set<String> selectedAccess = null;
            if (state.getAttribute(ALLPURPOSE_ACCESS) != null) {
                selectedAccess = new HashSet<>();
                for (Object value : (List<?>) state.getAttribute(ALLPURPOSE_ACCESS)) {
                    selectedAccess.add((String) value);
                }
            }
            service.updateAllPurposeItem(assignmentId, siteId, values,
                    getSupplementAttachmentIds(state, ALLPURPOSE_ATTACHMENTS), selectedAccess, false);
        }
    }

    private static Set<String> getSupplementAttachmentIds(SessionState state, String attachmentKey) {
        Set<String> attachmentIds = new HashSet<>();
        if (state.getAttribute(attachmentKey) != null) {
            for (Object value : (List<?>) state.getAttribute(attachmentKey)) {
                attachmentIds.add(((Reference) value).getReference());
            }
        }
        return attachmentIds;
    }

    private static Instant getTimeFromState(SessionState state, ZoneId zoneId, String monthKey,
            String dayKey, String yearKey, String hourKey, String minuteKey) {
        if (state.getAttribute(monthKey) == null && state.getAttribute(dayKey) == null
                && state.getAttribute(yearKey) == null && state.getAttribute(hourKey) == null
                && state.getAttribute(minuteKey) == null) {
            return null;
        }
        return LocalDateTime.of((Integer) state.getAttribute(yearKey),
                (Integer) state.getAttribute(monthKey), (Integer) state.getAttribute(dayKey),
                (Integer) state.getAttribute(hourKey), (Integer) state.getAttribute(minuteKey))
                .atZone(zoneId).toInstant();
    }

    private AssignmentSupplementItemForm() {}
}
