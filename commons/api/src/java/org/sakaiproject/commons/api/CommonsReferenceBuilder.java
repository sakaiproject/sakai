/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.commons.api;

import org.sakaiproject.entity.api.Entity;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;

@Builder
public class CommonsReferenceBuilder {

    private String siteId;
    private String postId;
    private String commentId;

    public String getReference() {

        if (StringUtils.isNotBlank(siteId) || StringUtils.isNotBlank(postId) || StringUtils.isNotBlank(commentId)) {
            if (this.postId != null) {
                return CommonsManager.REFERENCE_ROOT + Entity.SEPARATOR + this.siteId + Entity.SEPARATOR + "posts" + Entity.SEPARATOR + this.postId;
            } else if (this.commentId != null) {
                return CommonsManager.REFERENCE_ROOT + Entity.SEPARATOR + this.siteId + Entity.SEPARATOR + "comments" + Entity.SEPARATOR + this.commentId;
            } else {
                return CommonsManager.REFERENCE_ROOT + Entity.SEPARATOR + this.siteId;
            }
        } else {
            return CommonsManager.REFERENCE_ROOT;
        }
    }

    public CommonsReferenceBuilder reference(String reference) {

        String[] parts = reference.split(Entity.SEPARATOR);

        if (parts.length > 4) {
            siteId = parts[2];
            if ("post".equals(parts[3])) {
                postId = parts[4];
            } else if ("comments".equals(parts[3])) {
                commentId = parts[4];
            }
        }

        return this;
    }

    public String toString() {
        return getReference();
    }
}
