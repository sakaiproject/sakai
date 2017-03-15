/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.model;


import lombok.Data;
import org.sakaiproject.rubrics.security.model.AuthenticatedRequestContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.Instant;

@MappedSuperclass
@Data
public abstract class BaseResource<T extends BaseMetadata> {

    @Embedded
    protected T metadata;

    public abstract Long getId();

    @PrePersist
    protected void onCreate() {

        AuthenticatedRequestContext authenticatedRequestContext =
                (AuthenticatedRequestContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        T metadata = getMetadata();
        metadata.setCreated(Instant.now());
        metadata.setModified(Instant.now());
        metadata.setCreatorId(authenticatedRequestContext.getUserId());
        metadata.setOwnerId(authenticatedRequestContext.getContextId());
        metadata.setOwnerType(authenticatedRequestContext.getContextType());
        this.setMetadata(metadata);
    }

    @PreUpdate
    protected void onUpdate() {
        this.getMetadata().setModified(Instant.now());
    }

}
