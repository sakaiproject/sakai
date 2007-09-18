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
