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
