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

import org.sakaiproject.site.api.Site;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteDetailsRestBean {

    private String id;
    private String title;
    private Instant createdOn;
    private Instant modifiedOn;
    private Instant softlyDeletedDate;

    public static SiteDetailsRestBean of(Site site) {
        return SiteDetailsRestBean.builder()
                .id(site.getId())
                .title(site.getTitle())
                .createdOn(Optional.ofNullable(site.getCreatedDate()).map(Date::toInstant).orElse(null))
                .modifiedOn(Optional.ofNullable(site.getModifiedDate()).map(Date::toInstant).orElse(null))
                // The soft delete date is a java.sql.Date, which does not support toInstant()
                .softlyDeletedDate(Optional.ofNullable(site.getSoftlyDeletedDate()).map(date -> Instant.ofEpochMilli(date.getTime())).orElse(null))
                .build();
    }
}
