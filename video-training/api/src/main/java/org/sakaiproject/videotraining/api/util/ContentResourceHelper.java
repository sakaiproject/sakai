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

package org.sakaiproject.videotraining.api.util;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;

public class ContentResourceHelper {

    private final ContentHostingService contentHostingService;

    public ContentResourceHelper(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public String toContentResourceId(String sourceReference) {
        String normalized = sourceReference == null ? "" : sourceReference.trim();
        if (normalized.startsWith(VideoTrainingConstants.REFERENCE_ROOT + "/")) {
            return normalized.substring(VideoTrainingConstants.REFERENCE_ROOT.length());
        }
        return normalized;
    }

    public ContentResource getContentResource(String sourceReference) throws IllegalStateException, IdUnusedException, PermissionException, TypeException {
        return contentHostingService.getResource(toContentResourceId(sourceReference));
    }

    public String getContentUrl(String sourceReference) {
        try {
            String url = contentHostingService.getUrl(toContentResourceId(sourceReference));
            return url == null ? "" : url;
        } catch (Exception e) {
            return "";
        }
    }
}
