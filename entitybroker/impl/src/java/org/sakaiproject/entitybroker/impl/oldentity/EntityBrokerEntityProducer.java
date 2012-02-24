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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The single standard EntityProducer for all "Entities" managed by the EntityBroker system. Entity
 * objects are only created on demand and will expire when unreferenced.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class EntityBrokerEntityProducer implements EntityProducer {

   private EntityManager entityManager;
   public void setEntityManager(EntityManager entityManager) {
      this.entityManager = entityManager;
   }

   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private EntityBrokerManager entityBrokerManager;
   public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
      this.entityBrokerManager = entityBrokerManager;
   }

   /**
    * <b>NOTE VERY CAREFULLY</b> - current implementation of EntityManager ignores referenceRoot!
    * This will fail if this ever changes
    */
   public void init() {
      entityManager.registerEntityProducer(this, "/");
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getEntity(org.sakaiproject.entity.api.Reference)
    */
   public Entity getEntity(Reference ref) {
      CoreEntityProvider entityProvider = (CoreEntityProvider) entityProviderManager
            .getProviderByPrefix(ref.getType());
      if (entityProvider != null) {
         if (entityProvider.entityExists(ref.getId())) {
            Entity togo = newEntity(ref);
            return togo;
         }
      }
      return null;
   }

   /**
    * @param ref
    * @return
    */
   private Entity newEntity(Reference ref) {
      EntityBrokerEntity togo = new EntityBrokerEntity();
      togo.setEntityBrokerManager(entityBrokerManager);
      togo.setReference(ref);
      return togo;
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityUrl(org.sakaiproject.entity.api.Reference)
    */
   public String getEntityUrl(Reference ref) {
      return entityBrokerManager.getEntityURL(ref.getReference(), null, null);
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityResourceProperties(org.sakaiproject.entity.api.Reference)
    */
   public ResourceProperties getEntityResourceProperties(Reference ref) {
      Entity entity = getEntity(ref);
      return entity == null ? null : entity.getProperties();
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#parseEntityReference(java.lang.String,
    *      org.sakaiproject.entity.api.Reference)
    */
   public boolean parseEntityReference(String reference, Reference ref) {
      EntityReference entityref = null;
      try {
         entityref = entityBrokerManager.parseReference(reference);
         if (entityref == null) {
            return false;
         }
      } catch (Exception e) {
         return false;
      }
   
      // We will not attempt to check that the entity actually exists here,
      // only that the reference has a recognised prefix.
      EntityProvider entityProvider = entityProviderManager.getProviderByPrefix(entityref.prefix);
      if (entityProvider != null) {
         ref.set(entityref.getPrefix(), null, entityref.getId(), null, null);
         return true;
      } else {
         return false;
      }
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getHttpAccess()
    */
   public HttpAccess getHttpAccess() {
      return null; // no more http access through access servlet
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entity.api.EntityProducer#archive(java.lang.String,
    *      org.w3c.dom.Document, java.util.Stack, java.lang.String, java.util.List)
    */
   @SuppressWarnings("unchecked")
   public String archive(String siteId, Document doc, Stack stack, String archivePath,
         List attachments) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityAuthzGroups(org.sakaiproject.entity.api.Reference,
    *      java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public Collection getEntityAuthzGroups(Reference ref, String userId) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityDescription(org.sakaiproject.entity.api.Reference)
    */
   public String getEntityDescription(Reference ref) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entity.api.EntityProducer#getLabel()
    */
   public String getLabel() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entity.api.EntityProducer#merge(java.lang.String, org.w3c.dom.Element,
    *      java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.Set)
    */
   @SuppressWarnings("unchecked")
   public String merge(String siteId, Element root, String archivePath, String fromSiteId,
         Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entity.api.EntityProducer#willArchiveMerge()
    */
   public boolean willArchiveMerge() {
      return false;
   }

}
