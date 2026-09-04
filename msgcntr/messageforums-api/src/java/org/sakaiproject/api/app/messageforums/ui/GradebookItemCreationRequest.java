/**
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
package org.sakaiproject.api.app.messageforums.ui;

import lombok.Getter;

/**
 * Identifies the Gradebook and title for a Gradebook item created from Discussions.
 */
@Getter
public final class GradebookItemCreationRequest {

    private final String gradebookUid;
    private final String title;

    public GradebookItemCreationRequest(String gradebookUid, String title) {
        this.gradebookUid = gradebookUid;
        this.title = title;
    }
}
