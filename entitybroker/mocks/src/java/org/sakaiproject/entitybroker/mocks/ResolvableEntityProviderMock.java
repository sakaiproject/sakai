/**
 * ResolvableEntityProviderMock.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.mocks;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;

/**
 * Stub class to make it possible to test the {@link Resolvable} capability, will perform like the
 * actual class so it can be reliably used for testing<br/> Returns {@link MyEntity} objects
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ResolvableEntityProviderMock extends CoreEntityProviderMock implements
      CoreEntityProvider, Resolvable {

   /**
    * TEST Constructor: allows for easy setup of this stub for testing
    * 
    * @param prefix
    * @param ids
    */
   public ResolvableEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#getEntity(org.sakaiproject.entitybroker.EntityReference)
    */
   public Object getEntity(EntityReference reference) {
      return new MyEntity(((IdEntityReference) reference).id);
   }

}
