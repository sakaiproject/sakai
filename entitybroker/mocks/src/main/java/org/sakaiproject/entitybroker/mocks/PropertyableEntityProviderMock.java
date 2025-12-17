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
 * PropertyableEntityProviderMock.java - entity-broker - 2007 Aug 8, 2007 5:39:49 PM - AZ
 */

package org.sakaiproject.entitybroker.mocks;

import org.sakaiproject.entitybroker.entityprovider.capabilities.Propertyable;

/**
 * Mock which emulates the propertyable abilities, note that by default there are no properties on
 * entities
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class PropertyableEntityProviderMock extends CoreEntityProviderMock implements Propertyable {

   public PropertyableEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

}
