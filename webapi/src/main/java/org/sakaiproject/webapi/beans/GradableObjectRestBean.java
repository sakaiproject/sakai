/**
 * Copyright (c) 2026 The Apereo Foundation
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

package org.sakaiproject.webapi.beans;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.sakaiproject.grading.api.Assignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradableObjectRestBean {

    private String siteId;
    private String entityId;
    // The gradebook item's external app name, such as sakai.samigo
    private String toolId;
    private String title;
    private Double pointsPossible;
    private Instant dueDate;
    private Instant openDate;

    public static GradableObjectRestBean of(String siteId, String entityId, Assignment item, Instant openDate) {
        return GradableObjectRestBean.builder()
                .siteId(siteId)
                .entityId(entityId)
                .toolId(item.getExternalAppName())
                .title(item.getName())
                .pointsPossible(item.getPoints())
                .dueDate(Optional.ofNullable(item.getDueDate()).map(Date::toInstant).orElse(null))
                .openDate(openDate)
                .build();
    }
}
