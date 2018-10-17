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
