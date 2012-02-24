/**
 * $Id$
 * $URL$
 * EBlogic.java - entity-broker - Apr 15, 2008 4:29:18 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.impl.oldentity;

import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EntityBrokerEntity implements Entity {

   private EntityBrokerManager entityBrokerManager;
   public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
      this.entityBrokerManager = entityBrokerManager;
   }

   private ResourceProperties properties;
   public void setProperties(ResourceProperties properties) {
      this.properties = properties;
   }
   // we use only two fields in Reference - type -> parsed.prefix, id -> parsed.id
   private Reference reference;

   void setReference(Reference ref) {
      this.reference = ref;
   }

   public String getId() {
      return reference.getId();
   }

   public ResourceProperties getProperties() {
      return properties;
   }

   public String getReference() {
      return reference.getReference();
   }

   public String getReference(String rootProperty) {
      return getReference();
   }

   public String getUrl() {
      return entityBrokerManager.getEntityURL(reference.getReference(), null, null);
   }

   public String getUrl(String rootProperty) {
      return entityBrokerManager.getEntityURL(reference.getReference(), null, null);
   }

   @SuppressWarnings("unchecked")
   public Element toXml(Document doc, Stack stack) {
      // TODO Auto-generated method stub
      return null;
   }

}
