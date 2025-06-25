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
package org.sakaiproject.samigo.api;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.samigo.util.SamigoConstants;

@Slf4j
public class SamigoReferenceReckoner {

    @Value
    public static class SamigoReference {

        private final String type = "samigo";
        private String site;
        private String subtype;
        private String id;
        @Getter(AccessLevel.NONE) private String reference;

        @Override
        public String toString() {
            String reference = SamigoConstants.REFERENCE_ROOT;

            if (StringUtils.isNotBlank(site)) {
                reference += Entity.SEPARATOR + "s" + Entity.SEPARATOR + site;
            }

            switch (subtype) {
                case "p":
                    // published assessment type
                default:
                    // using published assessment type as default
                    reference = reference + Entity.SEPARATOR + "p";
            }

            if (StringUtils.isNotBlank(id)) {
                reference = reference + Entity.SEPARATOR + id;
            }

            return reference;
        }

        public String getReference() {
            return toString();
        }
    }

    /**
     * This is a builder for an SamigoReference
     *
     * @param assessment
     * @param site
     * @param subtype
     * @param id
     * @param reference
     * @return
     */
    @Builder(builderMethodName = "reckoner", buildMethodName = "reckon")
    public static SamigoReference newSamigoReferenceReckoner(String site, String subtype, String id, String reference) {


        if (StringUtils.startsWith(reference, SamigoConstants.REFERENCE_ROOT)) {
            // we will get null, assignment, [a|c|s|grades|submissions], context, [auid], id
            String[] parts = StringUtils.splitPreserveAllTokens(reference, Entity.SEPARATOR);
            if (parts.length > 3) {
                if (site == null) site = parts[3];

                if (parts.length > 4) {

                    if (subtype == null) subtype = parts[4];

                    // submissions have the assignment unique id as a container
                    if ("p".equals(subtype) && parts.length > 5) {
                        if (id == null) id = parts[5];
                    }
                }
            }
        }

        return new SamigoReference(
                (site == null) ? "" : site,
                (subtype == null) ? "" : subtype,
                (id == null) ? "" : id,
                (reference == null) ? "" : reference);
    }
}
