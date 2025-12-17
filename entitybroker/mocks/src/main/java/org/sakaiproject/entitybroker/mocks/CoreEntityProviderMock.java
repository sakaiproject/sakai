/**
 * Copyright (c) 2007 The Apereo Foundation
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
 * CoreEntityProviderMock.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.mocks;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;

/**
 * Stub class to make it easier to test things that use an {@link CoreEntityProvider}, will perform
 * like the actual class so it can be reliably used for testing
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class CoreEntityProviderMock extends EntityProviderMock implements CoreEntityProvider {

   /**
    * The valid entity ids for this {@link CoreEntityProvider}, defaults are "1","2","3"
    */
   public String[] ids = new String[] { "1", "2", "3" };

   /**
    * TEST Constructor: allows for easy setup of this stub for testing
    * 
    * @param prefix
    */
   public CoreEntityProviderMock(String prefix) {
      super(prefix);
   }

   /**
    * TEST Constructor: allows for easy setup of this stub for testing
    * 
    * @param prefix
    * @param ids
    */
   public CoreEntityProviderMock(String prefix, String[] ids) {
      super(prefix);
      this.ids = ids;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
    */
   public boolean entityExists(String id) {
      for (int i = 0; i < ids.length; i++) {
         if (ids[i].equals(id)) {
            return true;
         }
      }
      return false;
   }

}
