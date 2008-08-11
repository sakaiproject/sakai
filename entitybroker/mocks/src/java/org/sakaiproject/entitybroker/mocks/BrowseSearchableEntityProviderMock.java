/**
 * $Id$
 * $URL$
 * BrowseSearchableEntityProviderMock.java - entity-broker - Apr 9, 2008 10:31:13 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
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
