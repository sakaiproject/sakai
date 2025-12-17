/**
 * $Id$
 * $URL$
 * RESTfulEntityProviderMock.java - entity-broker - Apr 9, 2008 10:31:13 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.entitybroker.mocks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;


/**
 * Stub class to make it possible to test the {@link CRUDable} capabilities, will perform like the
 * actual class so it can be reliably used for testing<br/> 
 * Will perform all {@link CRUDable} operations<br/>
 * Returns {@link MyEntity} objects<br/>
 * Allows for testing {@link Resolvable} and {@link CollectionResolvable} as well, returns 3 {@link MyEntity} objects 
 * if no search restrictions, 1 if "stuff" property is set, none if other properties are set
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class CRUDableEntityProviderMock extends EntityProviderMock implements CoreEntityProvider, CRUDable {

   public Map<String, MyEntity> myEntities = new LinkedHashMap<String, MyEntity>(4);
   
   public CRUDableEntityProviderMock(String prefix, String[] ids) {
      super(prefix);
      for (int i = 0; i < ids.length; i++) {
         myEntities.put(ids[i], new MyEntity(ids[i], "aaron" + i, i) );
      }
   }

   public boolean entityExists(String id) {
      return myEntities.containsKey(id);
   }

   public Object getEntity(EntityReference reference) {
      if (reference.getId() == null) {
         return new MyEntity();
      }
      if (myEntities.containsKey(reference.getId())) {
         return myEntities.get( reference.getId() );
      }
      throw new IllegalArgumentException("Invalid id:" + reference.getId());
   }

   public List<?> getEntities(EntityReference ref, Search search) {
      List<MyEntity> entities = new ArrayList<MyEntity>();
      if (search.isEmpty()) {
         // return all
         for (MyEntity myEntity : myEntities.values()) {
            entities.add( myEntity );
         }
      } else {
         // restrict based on search param
         if (search.getRestrictionByProperty("stuff") != null) {
            for (MyEntity me : myEntities.values()) {
               String sMatch = search.getRestrictionByProperty("stuff").value.toString();
               if (sMatch.equals(me.getStuff())) {
                  entities.add(me);
               }
            }
         }
      }
      return entities;
   }

   /**
    * Returns {@link MyEntity} objects with no id, default number to 10
    * {@inheritDoc}
    */
   public Object getSampleEntity() {
      return new MyEntity(null, 10);
   }

   /**
    * Expects {@link MyEntity} objects
    * {@inheritDoc}
    */
   public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
      MyEntity me = (MyEntity) entity;
      if (me.getStuff() == null) {
         throw new IllegalArgumentException("stuff is not set, it is required");
      }
      String newId = me.getId();
      int counter = 0;
      if (newId == null || "".equals(newId)) {
          while (newId == null) {
             String id = "my"+counter++;
             if (! myEntities.containsKey(id)) {
                newId = id;
             }
          }
          me.setId( newId );
      }
      myEntities.put(newId, me);
      return newId;
   }

   /**
    * Expects {@link MyEntity} objects
    * {@inheritDoc}
    */
   public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
      MyEntity me = (MyEntity) entity;
      if (me.getStuff() == null) {
         throw new IllegalArgumentException("stuff is not set, it is required");
      }
      MyEntity current = myEntities.get(ref.getId());
      if (current == null) {
         throw new IllegalArgumentException("Invalid update, cannot find entity");
      }
      // update the fields
      current.setStuff( me.getStuff() );
      current.setNumber( me.getNumber() );
      current.extra = me.extra;
   }

   public void deleteEntity(EntityReference ref, Map<String, Object> params) {
      if (myEntities.remove(ref.getId()) == null) {
         throw new IllegalArgumentException("Invalid entity id, cannot find entity to remove: " + ref);
      }
   }

}
