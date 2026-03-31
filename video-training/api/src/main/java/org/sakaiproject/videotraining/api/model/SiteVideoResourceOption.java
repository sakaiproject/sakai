/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.videotraining.api.model;

import lombok.Getter;

/**
 * Describes an existing content resource in a site's resources that can be selected
 * as the source of a video training video.
 */
@Getter
public class SiteVideoResourceOption {

    private final String reference;
    private final String displayName;
    private final String contentType;

    public SiteVideoResourceOption(String reference, String displayName, String contentType) {
        this.reference = reference;
        this.displayName = displayName;
        this.contentType = contentType;
    }
}
