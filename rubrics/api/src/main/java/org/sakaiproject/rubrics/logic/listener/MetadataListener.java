/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.rubrics.logic.listener;

import java.time.LocalDateTime;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.sakaiproject.rubrics.logic.AuthenticatedRequestContext;
import org.sakaiproject.rubrics.logic.model.Metadata;
import org.sakaiproject.rubrics.logic.model.Modifiable;
import org.springframework.security.core.context.SecurityContextHolder;

public class MetadataListener {

    @PrePersist
    public void onCreate(Modifiable modifiable) {
        Metadata metadata = modifiable.getModified();

        if (metadata == null) {
            metadata = new Metadata();
            modifiable.setModified(metadata);
        }

        AuthenticatedRequestContext arc = (AuthenticatedRequestContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        metadata.setCreated(LocalDateTime.now());
        metadata.setModified(LocalDateTime.now());
        metadata.setShared(false);
        metadata.setLocked(false);
        metadata.setCreatorId(arc.getUserId());
        metadata.setOwnerId(arc.getContextId());
        metadata.setOwnerType(arc.getContextType());
    }

    @PreUpdate
    public void onUpdate(Modifiable modifiable) {
        Metadata metadata = modifiable.getModified();
        metadata.setModified(LocalDateTime.now());
    }
}
