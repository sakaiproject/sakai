/**
 * Copyright (c) 2007-2008 The Apereo Foundation
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
 * ParseableEntityProviderMock.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.mocks;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;

/**
 * Stub class to make it possible to test the {@link ReferenceParseable} capability, will perform
 * like the actual class so it can be reliably used for testing
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ReferenceParseableEntityProviderMock extends CoreEntityProviderMock implements
      CoreEntityProvider, ReferenceParseable {

   /**
    * TEST Constructor: allows for easy setup of this stub for testing
    * 
    * @param prefix
    * @param ids
    */
   public ReferenceParseableEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable#getParsedExemplar()
    */
   public EntityReference getParsedExemplar() {
      // super simple result for now -AZ
      return new EntityReference();
   }

}
