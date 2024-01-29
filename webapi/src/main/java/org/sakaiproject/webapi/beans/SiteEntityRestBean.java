/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class SiteEntityRestBean {

    private static final String SITE_SEGMENT = "/site/";
    private static final String GROUP_SEGMENT = "/group/";

    private String id;
    private SiteEntityType type;
    private String title;
    private Instant openDate;
    private Instant dueDate;
    private Instant closeDate;
    private Boolean dateRestricted;
    private Set<String> groupRefs;
    private Set<TimeExceptionRestBean> timeExceptions;

    @SuppressWarnings("unchecked")
    public static SiteEntityRestBean of(PublishedAssessmentFacade assessment,
            Set<TimeExceptionRestBean> timeExceptions) {
        String siteId = assessment.getOwnerSiteId();
        Set<String> groupRefs = Optional.ofNullable(assessment.getReleaseToGroups())
                .map(Map::keySet)
                .map(groupIds -> (Set<String>) groupIds)
                .map(groupIds -> groupIds.stream()
                        .map(groupId -> SITE_SEGMENT + siteId + GROUP_SEGMENT + groupId)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        String assessmentId = Optional.ofNullable(assessment.getPublishedAssessmentId())
                .map(id -> id.toString())
                .orElse(null);

        return SiteEntityRestBean.builder()
                .id(assessmentId)
                .type(SiteEntityType.ASSESSMENT)
                .title(assessment.getTitle())
                .openDate(Optional.ofNullable(assessment.getStartDate()).map(Date::toInstant).orElse(null))
                .dueDate(Optional.ofNullable(assessment.getDueDate()).map(Date::toInstant).orElse(null))
                .closeDate(Optional.ofNullable(assessment.getRetractDate()).map(Date::toInstant).orElse(null))
                .dateRestricted(true)
                .groupRefs(groupRefs)
                .timeExceptions(timeExceptions)
                .build();
    }

    public static SiteEntityRestBean of(Assignment assignment) {
        Set<String> groupRefs = Assignment.Access.GROUP.equals(assignment.getTypeOfAccess())
                ? Set.copyOf(assignment.getGroups())
                : Collections.emptySet();

        return SiteEntityRestBean.builder()
                .id(assignment.getId())
                .type(SiteEntityType.ASSIGNMENT)
                .title(assignment.getTitle())
                .openDate(assignment.getOpenDate())
                .dueDate(assignment.getDueDate())
                .closeDate(assignment.getCloseDate())
                .dateRestricted(true)
                .groupRefs(groupRefs)
                .build();
    }

    public static SiteEntityRestBean of(OpenForum forum) {
        return SiteEntityRestBean.builder()
                .id(Optional.ofNullable(forum.getId()).map(id -> id.toString()).orElse(null))
                .type(SiteEntityType.FORUM)
                .title(forum.getTitle())
                .openDate(Optional.ofNullable(forum.getOpenDate()).map(Date::toInstant).orElse(null))
                .closeDate(Optional.ofNullable(forum.getCloseDate()).map(Date::toInstant).orElse(null))
                .dateRestricted(forum.getAvailabilityRestricted())
                .build();
    }

    @SuppressWarnings("unchecked")
    public static SiteEntityRestBean of(ContentEntity resource) {
        boolean dateRestricted = resource.getReleaseInstant() != null || resource.getRetractInstant() != null;
        SiteEntityType type = resource.isResource() ? SiteEntityType.RESOURCE : SiteEntityType.RESOURCE_FOLDER;

        return SiteEntityRestBean.builder()
                .id(resource.getId())
                .type(type)
                .title(resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME))
                .openDate(resource.getReleaseInstant())
                .closeDate(resource.getRetractInstant())
                .groupRefs(Set.copyOf(resource.getGroups()))
                .dateRestricted(dateRestricted)
                .build();
    }

    public static Comparator<SiteEntityRestBean> comparator() {
        return Comparator.comparing(SiteEntityRestBean::getType)
                .thenComparing(SiteEntityRestBean::getTitle);
    }


    public enum SiteEntityType {
        ASSESSMENT,
        ASSIGNMENT,
        FORUM,
        RESOURCE,
        RESOURCE_FOLDER,
    }
}
