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
    String QUERY_GET_ENTRIES_FOR_ASSESSMENT = "getEntriesForAss";
    String QUERY_GET_ENTRIES_FOR_PUBLISHED  = "getEntriesForPub";
    String QUERY_GET_ENTRY_FOR_PUB_N_USER   = "getEntriesForPubNUser";
    String QUERY_GET_ENTRY_FOR_PUB_N_GROUP  = "getEntriesForPubNGroup";

    // Hibernate Object Fields
    String ASSESSMENT_ID                    = "assessmentId";
    String PUBLISHED_ID                     = "publishedId";
    String USER_ID                          = "userId";
    String GROUP                            = "groupId";

    /**
     *
     * @param ass
     * @return
     */
    List<ExtendedTime>  getEntriesForAss        (AssessmentBaseIfc ass);

    /**
     *
     * @param pub
     * @return
     */
    List<ExtendedTime>  getEntriesForPub        (PublishedAssessmentIfc pub);

    /**
     *
     * @param pub
     * @param userId
     * @return
     */
    ExtendedTime        getEntryForPubAndUser   (PublishedAssessmentIfc pub, String userId);

    /**
     *
     * @param pub
     * @param groupId
     * @return
     */
    ExtendedTime        getEntryForPubAndGroup  (PublishedAssessmentIfc pub, String groupId);

    /**
     *
     * @param e
     * @return
     */
    boolean             updateEntry             (ExtendedTime e);

    /**
     *
     * @param entries
     * @return
     */
    void                updateEntries           (List<ExtendedTime> entries);

    /**
     *
     * @param e
     * @return
     */
    boolean             deleteEntry             (ExtendedTime e);
}
