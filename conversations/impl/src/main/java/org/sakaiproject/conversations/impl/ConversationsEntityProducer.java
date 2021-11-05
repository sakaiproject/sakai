/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.conversations.impl;

import java.util.Optional;

import org.sakaiproject.conversations.api.ConversationsReferenceReckoner;
import org.sakaiproject.conversations.api.ConversationsService;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;

import org.springframework.beans.factory.annotation.Autowired;

public class ConversationsEntityProducer implements EntityProducer {

  @Autowired private ConversationsService conversationsService;

  public Optional<String> getEntityUrl(Reference ref, Entity.UrlType urlType) {
    
    switch (urlType) {
      case PORTAL:
        String id = ConversationsReferenceReckoner.reckoner().reference(ref.getReference()).reckon().getId();
        return conversationsService.getTopicPortalUrl(id);
      default:
    }

    return Optional.empty();
  }
}
