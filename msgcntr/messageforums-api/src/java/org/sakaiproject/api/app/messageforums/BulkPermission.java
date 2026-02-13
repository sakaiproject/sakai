/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
 package org.sakaiproject.api.app.messageforums;

import lombok.Data;

@Data
public class BulkPermission {

    private boolean changeSettings;
    private boolean deleteAny;
    private boolean deleteOwn;
    private boolean markAsNotRead;
    private boolean moderatePostings;
    private boolean movePostings;
    private boolean newResponse;
    private boolean newResponseToResponse;
    private boolean newTopic;
    private boolean postToGradebook;
    private boolean read;
    private boolean reviseAny;
    private boolean reviseOwn;

    public void setAllPermissions(boolean toTrueOrFalse) {
        changeSettings = deleteAny = deleteOwn = markAsNotRead = moderatePostings = movePostings = newTopic
                = newResponse = newResponseToResponse = postToGradebook = read = reviseAny = reviseOwn
                = toTrueOrFalse;
    }
}
