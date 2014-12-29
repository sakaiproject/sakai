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
