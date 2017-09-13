/**
 * Copyright (c) 2007-2009 The Apereo Foundation
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
/**
 * ResolvableEntityProviderMock.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.mocks;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;

/**
 * Stub class to make it possible to test the {@link Resolvable} capability, will perform like the
 * actual class so it can be reliably used for testing<br/> 
 * Returns {@link MyEntity} objects<br/>
 * Allows for testing {@link CollectionResolvable} as well, returns 3 {@link MyEntity} objects 
 * if no search restrictions, 1 if "stuff" property is set, none if other properties are set<br/>
 * Finally, this allows us to test the outputting of data because it implements outputable
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ResolvableEntityProviderMock extends CoreEntityProviderMock implements
      CoreEntityProvider, Resolvable, CollectionResolvable, Outputable {

   /**
    * TEST Constructor: allows for easy setup of this stub for testing
    * 
    * @param prefix
    * @param ids
    */
   public ResolvableEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

   public String[] getHandledOutputFormats() {
      return new String[] {Formats.HTML, Formats.JSON, Formats.JSONP, Formats.XML};
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#getEntity(org.sakaiproject.entitybroker.EntityReference)
    */
   public Object getEntity(EntityReference reference) {
      if (reference.getId() == null) {
         return new MyEntity();
      }
      return new MyEntity(reference.getId(), "something");
   }

   public List<?> getEntities(EntityReference ref, Search search) {
      List<MyEntity> entities = new ArrayList<MyEntity>();
      if (search.isEmpty()) {
         // return all
         for (int i = 0; i < ids.length; i++) {
            entities.add( new MyEntity( ids[i], "something" + i ) );
         }
      } else {
         // restrict based on search param
         if (search.getRestrictionByProperty("stuff") != null) {
            entities.add( new MyEntity( ids[1], "something1" ) );
         }
      }
      return entities;
   }

}
