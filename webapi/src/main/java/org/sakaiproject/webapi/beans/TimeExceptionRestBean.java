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
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "forEntityRef")
@JsonInclude(Include.NON_NULL)
public class TimeExceptionRestBean {

    private static final String SITE_SEGMENT = "/site/";
    private static final String GROUP_SEGMENT = "/group/";
    private static final String USER_SEGMENT = "/user/";

    private String forEntityRef;
    private Instant openDate;
    private Instant dueDate;
    private Instant closeDate;


    @JsonIgnore
    public boolean isValid() {
        boolean refValid = StringUtils.containsAny(forEntityRef, GROUP_SEGMENT, USER_SEGMENT);

        boolean datesValid = openDate != null && dueDate != null
            && (dueDate.isAfter(openDate) || dueDate.equals(openDate))
            && (closeDate == null || closeDate.isAfter(dueDate) || closeDate.equals(dueDate));

        return refValid && datesValid;
    }

    public ExtendedTime toExtendedTime() {
        ExtendedTime extendedTime = new ExtendedTime();

        if (openDate != null) {
            extendedTime.setStartDate(Date.from(openDate));
        }

        if (dueDate != null) {
            extendedTime.setDueDate(Date.from(dueDate));
        }

        if (closeDate != null) {
            extendedTime.setRetractDate(Date.from(closeDate));
        }

        if (StringUtils.contains(forEntityRef, GROUP_SEGMENT)) {
            extendedTime.setGroup(StringUtils.substringAfter(forEntityRef, GROUP_SEGMENT));
        } else {
            extendedTime.setUser(StringUtils.substringAfter(forEntityRef, USER_SEGMENT));
        }

        return extendedTime;
    }

    public static TimeExceptionRestBean of(String siteId, ExtendedTime extendedTime) {
        String entityRef = Optional.ofNullable(StringUtils.trimToNull(extendedTime.getUser()))
                .map(userId -> USER_SEGMENT + userId)
                .or(() -> Optional.ofNullable(StringUtils.trimToNull(extendedTime.getGroup()))
                        .map(groupId -> SITE_SEGMENT + siteId + GROUP_SEGMENT + groupId))
                .orElse(null);
        return TimeExceptionRestBean.builder()
                .forEntityRef(entityRef)
                .openDate(Optional.ofNullable(extendedTime.getStartDate()).map(Date::toInstant).orElse(null))
                .dueDate(Optional.ofNullable(extendedTime.getDueDate()).map(Date::toInstant).orElse(null))
                .closeDate(Optional.ofNullable(extendedTime.getRetractDate()).map(Date::toInstant).orElse(null))
                .build();
    }
}
