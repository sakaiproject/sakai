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
 * EntityProviderMock.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.mocks;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Stub class to make it easier to test things that use an {@link EntityProvider}, will perform
 * like the actual class so it can be reliably used for testing<br/> <b>NOTE:</b> you will want to
 * use the {@link CoreEntityProvider} stub in almost all cases but this is here for completeness
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityProviderMock implements EntityProvider {

   /**
    * Set this to the prefix that this {@link EntityProvider} handles, default value is "testPrefix"
    */
   public String prefix = "testPrefix";

   /**
    * TEST Constructor: allows for easy setup of this stub for testing
    * 
    * @param prefix
    */
   public EntityProviderMock(String prefix) {
      this.prefix = prefix;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
    */
   public String getEntityPrefix() {
      return prefix;
   }

}
