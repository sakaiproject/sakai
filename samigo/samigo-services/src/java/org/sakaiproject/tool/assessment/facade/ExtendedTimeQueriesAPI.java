/*
 * Copyright (c) 2016, The Apereo Foundation
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
 *
 */

package org.sakaiproject.tool.assessment.facade;

import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;

import java.util.List;

/**
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public interface ExtendedTimeQueriesAPI {
    // Hibernate Named Queries
    String QUERY_GET_ENTRY                  = "getEntry";
    String QUERY_GET_ENTRIES_FOR_ASSESSMENT = "getEntriesForAss";
    String QUERY_GET_ENTRIES_FOR_PUBLISHED  = "getEntriesForPub";
    String QUERY_GET_ENTRY_FOR_PUB_N_USER   = "getEntriesForPubNUser";
    String QUERY_GET_ENTRY_FOR_PUB_N_GROUP  = "getEntriesForPubNGroup";

    // Hibernate Object Fields
    String ENTRY_ID                         = "entryId";
    String ASSESSMENT_ID                    = "assessmentId";
    String PUBLISHED_ID                     = "publishedId";
    String USER_ID                          = "userId";
    String GROUP                            = "groupId";

    /**
     * Gets an extended-time entry by its id.
     *
     * @param id the extended-time entry id
     * @return the matching entry, or {@code null} if none exists
     */
    ExtendedTime getEntry(String id);

    /**
     * Gets all extended-time entries for an assessment.
     *
     * @param ass the assessment
     * @return the matching extended-time entries
     */
    List<ExtendedTime> getEntriesForAss(AssessmentBaseIfc ass);

    /**
     * Gets all extended-time entries for a published assessment.
     *
     * @param pub the published assessment
     * @return the matching extended-time entries
     */
    List<ExtendedTime> getEntriesForPub(PublishedAssessmentIfc pub);

    /**
     * Gets an extended-time entry for a published assessment and user.
     *
     * @param pub the published assessment
     * @param userId the user id
     * @return the matching entry, or {@code null} if none exists
     */
    ExtendedTime getEntryForPubAndUser(PublishedAssessmentIfc pub, String userId);

    /**
     * Gets an extended-time entry for a published assessment and group.
     *
     * @param pub the published assessment
     * @param groupId the group id
     * @return the matching entry, or {@code null} if none exists
     */
    ExtendedTime getEntryForPubAndGroup(PublishedAssessmentIfc pub, String groupId);

    /**
     * Updates an existing extended-time entry.
     *
     * @param e the entry to update
     * @return {@code true} if the update succeeded, {@code false} otherwise
     */
    boolean updateEntry(ExtendedTime e);

    /**
     * Updates a collection of extended-time entries.
     *
     * @param entries the entries to update
     */
    void updateEntries(List<ExtendedTime> entries);

    /**
     * Deletes an extended-time entry.
     *
     * @param e the entry to delete
     * @return {@code true} if the delete succeeded, {@code false} otherwise
     */
    boolean deleteEntry(ExtendedTime e);

    /**
     * Deletes all extended-time entries for a published assessment.
     *
     * @param pub the published assessment
     * @return {@code true} if the delete succeeded, {@code false} otherwise
     */
    boolean deleteEntriesForPub(PublishedAssessmentIfc pub);
}
