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
package org.sakaiproject.assignment.api;

import static org.sakaiproject.assignment.api.AssignmentServiceConstants.REFERENCE_ROOT;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.entity.api.Entity;

/**
 * Created by enietzel on 5/11/17.
 */

@Slf4j
public class AssignmentReferenceReckoner {

    @Value
    public static class AssignmentReference implements ReferenceReckoner {
        private final String type = "assignment";
        private String container;
        private String context;
        private String id;
        @Getter(AccessLevel.NONE) private String reference;
        private String subtype;

        @Override
        public String toString() {
            String reference = REFERENCE_ROOT;

            switch (subtype) {
                case "s":
                    // submission type
                    reference = reference + Entity.SEPARATOR + "s";
                    break;
                case "grades":
                    // grades type
                    reference = reference + Entity.SEPARATOR + "grades";
                    break;
                case "submissions":
                    // submissions type
                    reference = reference + Entity.SEPARATOR + "submissions";
                    break;
                case "a":
                    // assignment type
                case "c":
                    // assignment content type
                    // deprecated using assignment type
                default:
                    // using assignment type as default when no matching type found
                    reference = reference + Entity.SEPARATOR + "a";
            }

            reference = reference + Entity.SEPARATOR + context;

            if (StringUtils.isNotBlank(id)) {
                if ("s".equals(subtype) && StringUtils.isNotBlank(container)) {
                    reference = reference + Entity.SEPARATOR + container;
                }
                reference = reference + Entity.SEPARATOR + id;
            }

            return reference;
        }

        @Override
        public String getReference() {
            return toString();
        }
    }

    /**
     * This is a builder for an AssignmentReference
     *
     * @param container
     * @param context
     * @param id
     * @param reference
     * @param subtype
     * @return
     */
    @Builder(builderMethodName = "reckoner", buildMethodName = "reckon")
    public static AssignmentReference newAssignmentReferenceReckoner(Assignment assignment, AssignmentSubmission submission, String container, String context, String id, String reference, String subtype) {
        if (StringUtils.startsWith(reference, REFERENCE_ROOT)) {
            // we will get null, assignment, [a|c|s|grades|submissions], context, [auid], id
            String[] parts = StringUtils.splitPreserveAllTokens(reference, Entity.SEPARATOR);
            if (parts.length > 2) {
                if (subtype == null) subtype = parts[2];

                if (parts.length > 3) {
                    // context is the container
                    if (context == null) context = parts[3];

                    // submissions have the assignment unique id as a container
                    if ("s".equals(subtype) && parts.length > 5) {
                        if (container == null) container = parts[4];
                        if (id == null) id = parts[5];
                    } else {
                        // others don't
                        if (parts.length > 4) {
                            if (id == null) id = parts[4];
                        }
                    }
                }
            }
        } else if (assignment != null) {
            context = assignment.getContext();
            id = assignment.getId();
            subtype = "a";
        } else if (submission != null) {
            Assignment submissionAssignment = submission.getAssignment();
            if (submissionAssignment != null) {
                context = submission.getAssignment().getContext();
                container = submission.getAssignment().getId();
                id = submission.getId();
                subtype = "s";
            } else {
                log.warn("no assignment while constructing submission reference");
            }
        }
        return new AssignmentReference(
                (container == null) ? "" : container,
                (context == null) ? "" : context,
                (id == null) ? "" : id,
                (reference == null) ? "" : reference,
                (subtype == null) ? "" : subtype);
    }
}
