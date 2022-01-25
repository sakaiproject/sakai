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
package org.sakaiproject.conversations.api;

import static org.sakaiproject.conversations.api.ConversationsService.REFERENCE_ROOT;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.conversations.api.model.Post;
import org.sakaiproject.conversations.api.model.Topic;
import org.sakaiproject.entity.api.Entity;

/**
 * Created by enietzel on 5/11/17.
 */

@Slf4j
public class ConversationsReferenceReckoner {

    @Value
    public static class ConversationsReference {

        private String siteId;
        private String type;
        private String id;
        //private String reference;

        @Override
        public String toString() {

            String reference = REFERENCE_ROOT;

            switch (type) {
                case "topics":
                    // topic type
                    reference = reference + Entity.SEPARATOR + "sites" + Entity.SEPARATOR + siteId + Entity.SEPARATOR + "topics" + Entity.SEPARATOR + id;
                    break;
                default:
                    // using topic type as default when no matching type found
                    reference = reference + Entity.SEPARATOR + "sites" + Entity.SEPARATOR + siteId + Entity.SEPARATOR + "topics" + Entity.SEPARATOR + id;
            }

            return reference;
        }

        public String getReference() {
            return toString();
        }
    }

    /**
     * This is a builder for an AssignmentReference
     *
     * @param context
     * @param id
     * @param reference
     * @param subtype
     * @return
     */
    @Builder(builderMethodName = "reckoner", buildMethodName = "reckon")
    public static ConversationsReference referenceReckoner(Topic topic, Post post, String siteId, String type, String id, String reference) {

        if (StringUtils.startsWith(reference, REFERENCE_ROOT)) {
            String[] parts = StringUtils.splitPreserveAllTokens(reference, Entity.SEPARATOR);
            if (siteId == null) siteId = parts[2];
            type = parts[3];
            if (id == null) id = parts[4];
        } else if (topic != null) {
            siteId = topic.getSiteId();
            type = "topics";
            id = topic.getId();
        }

        return new ConversationsReference(
            (siteId == null) ? "" : siteId,
            type,
            (id == null) ? "" : id);
    }
}
