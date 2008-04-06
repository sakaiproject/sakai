/**
 * MyEntity.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.mocks.data;

/**
 * This is a sample entity object for testing, it is a pea with default values
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class MyEntity {

   public String id = "num1";
   public String stuff = "something";

   public MyEntity(String id) {
      super();
      this.id = id;
   }

   @Override
   public boolean equals(Object obj) {
      if (null == obj) return false;
      if (!(obj instanceof MyEntity)) return false;
      else {
         MyEntity castObj = (MyEntity) obj;
         if (null == this.id || null == castObj.id) return false;
         else return (
               this.id.equals(castObj.id)
         );
      }
   }

   @Override
   public int hashCode() {
      if (null == this.id) return super.hashCode();
      String hashStr = this.getClass().getName() + ":" + this.id.hashCode();
      return hashStr.hashCode();
   }

   @Override
   public String toString() {
      return "id:" + this.id;
   }

}
