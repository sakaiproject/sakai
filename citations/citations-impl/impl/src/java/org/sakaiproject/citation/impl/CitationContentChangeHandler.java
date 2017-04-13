package org.sakaiproject.citation.impl;

import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentChangeHandlerImpl;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;

public class CitationContentChangeHandler extends ContentChangeHandlerImpl {

    protected EntityManager entityManager;
    protected CitationService citationService;

    public CitationContentChangeHandler(){
        this.entityManager = ComponentManager.get(EntityManager.class);
        this.citationService = ComponentManager.get(CitationService.class);

    }
    @Override
    public void copy(ContentResource resource) {
        Reference reference = entityManager.newReference(resource.getReference());
        citationService.copyCitationCollection(reference);
    }
}
