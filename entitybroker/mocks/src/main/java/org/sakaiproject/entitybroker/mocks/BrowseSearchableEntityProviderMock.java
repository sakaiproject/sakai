/**
 * $Id$
 * $URL$
 * BrowseSearchableEntityProviderMock.java - entity-broker - Apr 9, 2008 10:31:13 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.BrowseSearchable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;


/**
 * Stub class to make it possible to test the {@link BrowseSearchable} capability, will perform like the
 * actual class so it can be reliably used for testing<br/> 
 * Gets 3 entities, user=aaronz gets entity 2, assoc=siteAZ gets entity 3<br/>
 * Will perform all {@link CRUDable} operations as well as allowing for internal data output processing<br/>
 * Returns {@link MyEntity} objects<br/>
 * Allows for testing {@link Resolvable} and {@link CollectionResolvable} as well, returns 2 {@link MyEntity} objects 
 * if no search restrictions, 1 if "stuff" property is set, none if other properties are set
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class BrowseSearchableEntityProviderMock extends CRUDableEntityProviderMock implements CoreEntityProvider, BrowseSearchable {

   public BrowseSearchableEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

   public List<EntityData> browseEntities(Search search, String userReference,
         String associatedReference, Map<String, Object> params) {
      List<EntityData> entities = new ArrayList<EntityData>();
      if (search.isEmpty()) {
         // return all
         for (MyEntity myEntity : myEntities.values()) {
            EntityData ed = makeEntityData(myEntity);
            entities.add( ed );
         }
      } else {
         // restrict based on search param
         if (search.getRestrictionByProperty("stuff") != null) {
            for (MyEntity me : myEntities.values()) {
               String sMatch = search.getRestrictionByProperty("stuff").value.toString();
               if (sMatch.equals(me.getStuff())) {
                  EntityData ed = makeEntityData(me);
                  entities.add( ed );
               }
            }
         }
      }
      if (userReference != null || associatedReference != null) {
         entities.clear();
         if ("/user/aaronz".equals(userReference)) {
            // allow aaronz to get entity 2
            MyEntity me = myEntities.get(TestData.IDSB2[1]);
            EntityData ed = makeEntityData(me);
            entities.add( ed );
         }
         if ("/site/siteAZ".equals(associatedReference)) {
            // allow siteAZ to get to entity 3
            MyEntity me = myEntities.get(TestData.IDSB2[2]);
            EntityData ed = makeEntityData(me);
            entities.add( ed );
         }
      }
      return entities;
   }

   private EntityData makeEntityData(MyEntity myEntity) {
      EntityReference ref = new EntityReference(getEntityPrefix(), myEntity.getId());
      EntityData ed = new EntityData(ref, myEntity.getStuff(), myEntity);
      return ed;
   }

}
