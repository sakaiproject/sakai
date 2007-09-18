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

}
