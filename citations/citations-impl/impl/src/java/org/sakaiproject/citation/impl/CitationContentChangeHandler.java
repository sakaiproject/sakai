/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
