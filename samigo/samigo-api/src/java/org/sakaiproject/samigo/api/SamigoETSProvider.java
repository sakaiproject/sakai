/**
 * Copyright (c) 2015, The Apereo Foundation
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
package org.sakaiproject.samigo.api;

import org.sakaiproject.event.api.Event;


import java.util.List;
import java.util.Map;

public interface SamigoETSProvider {


    void init();

    void notify(String templateKey, Map<String, Object> notificationValues, Event event);

    /**
     * Notifies by email that the AutoSubmit job encountered errors
     * @param count the number of errors
     */
    void notifyAutoSubmitFailures(int count);

    /**
     * emails students that their instructor has added comments or
     * updated the grading on an assessment, with a link into the site's
     * Tests &amp; Quizzes tool. Delivery honors each student's Tests &amp;
     * Quizzes notification preference (immediate or digest; ignore is
     * skipped), and students without an email address are skipped.
     *
     * @param studentUserIds  the user ids to notify
     * @param siteId          the site holding the assessment
     * @param assessmentTitle the assessment title for the message
     * @return the userIds that were actually delivered to (sent or digested);
     *         callers must not treat skipped users (no email, unknown, opted
     *         out) as notified.
     */
    List<String> notifyGradingUpdated(List<String> studentUserIds, String siteId, String assessmentTitle);
}
