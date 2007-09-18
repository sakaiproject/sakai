/**
 * TestData.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.mocks.data;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Propertyable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.mocks.CoreEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.EntityProviderMock;
import org.sakaiproject.entitybroker.mocks.PropertyableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ReferenceParseableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ResolvableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.TaggableEntityProviderMock;

/**
 * Contains test data for testing the entity broker
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TestData {

   /**
    * current user, access level user in LOCATION1_ID
    */
   public final static String USER_ID = "user-11111111";
   public final static String USER_DISPLAY = "Aaron Zeckoski";
   /**
    * access level user in LOCATION1_ID
    */
   public final static String ACCESS_USER_ID = "access-2222222";
   public final static String ACCESS_USER_DISPLAY = "Regular User";
   /**
    * maintain level user in LOCATION1_ID
    */
   public final static String MAINT_USER_ID = "maint-33333333";
   public final static String MAINT_USER_DISPLAY = "Maint User";
   /**
    * super admin user
    */
   public final static String ADMIN_USER_ID = "admin";
   public final static String ADMIN_USER_DISPLAY = "Administrator";
   /**
    * Invalid user (also can be used to simulate the anonymous user)
    */
   public final static String INVALID_USER_ID = "invalid-UUUUUU";

   /**
    * current location
    */
   public final static String LOCATION1_ID = "/site/ref-1111111";
   public final static String LOCATION1_TITLE = "Location 1 title";
   public final static String LOCATION2_ID = "/site/ref-22222222";
   public final static String LOCATION2_TITLE = "Location 2 title";
   public final static String INVALID_LOCATION_ID = "invalid-LLLLLLLL";

   // testing constants
   public final static String SERVER_URL = "http://localhost:8001/portal";
   private final static String DIRECT = "/direct";

   public final static String PREFIX1 = "AZprefix1";
   public final static String[] IDS1 = new String[] { "10", "11", "12" };
   public final static String REF1 = EntityReference.SEPARATOR + PREFIX1
         + EntityReference.SEPARATOR + IDS1[0];
   public final static String REF1_1 = EntityReference.SEPARATOR + PREFIX1
         + EntityReference.SEPARATOR + IDS1[1];
   public final static String REF1_INVALID = EntityReference.SEPARATOR + PREFIX1
         + EntityReference.SEPARATOR + "XXXXXX";
   public final static String URL1 = SERVER_URL + DIRECT + REF1;

   public final static String PREFIX2 = "simplePrefix2";
   public final static String REF2 = EntityReference.SEPARATOR + PREFIX2;
   public final static String URL2 = SERVER_URL + DIRECT + REF2;

   public final static String PREFIX3 = "myPrefix3";
   public final static String[] IDS3 = new String[] { "thirty", "31" };
   public final static String REF3 = EntityReference.SEPARATOR + PREFIX3
         + EntityReference.SEPARATOR + IDS3[0];

   public final static String PREFIX4 = "myPrefix4";
   public final static String[] IDS4 = new String[] { "4-one", "4-two" };
   public final static String REF4 = EntityReference.SEPARATOR + PREFIX4
         + EntityReference.SEPARATOR + IDS4[0];
   public final static String REF4_two = EntityReference.SEPARATOR + PREFIX4
         + EntityReference.SEPARATOR + IDS4[1];

   public final static String PREFIX5 = "myPrefix5";
   public final static String[] IDS5 = new String[] { "fiver", "50" };
   public final static String REF5 = EntityReference.SEPARATOR + PREFIX5
         + EntityReference.SEPARATOR + IDS5[0];
   public final static String REF5_2 = EntityReference.SEPARATOR + PREFIX5
         + EntityReference.SEPARATOR + IDS5[1];

   public final static String PREFIX9 = "unregPrefix9";
   public final static String[] IDS9 = new String[] { "ninety", "9and1" };
   public final static String REF9 = EntityReference.SEPARATOR + PREFIX9
         + EntityReference.SEPARATOR + IDS9[0];
   public final static String URL9 = SERVER_URL + DIRECT + REF9;

   public final static String INVALID_REF = "invalid_reference-1";

   public final static String EVENT1_NAME = "event.name.test.1";
   public final static String EVENT2_NAME = "event.name.test.2";

   // property testing constants
   public final static String PROPERTY_NAME5A = "prop5A";
   public final static String PROPERTY_VALUE5A = "value5A";

   public final static String PROPERTY_NAME5B = "prop5B";
   /**
    * Lots of quote chars example
    */
   public final static String PROPERTY_VALUE5B = "Keep movin', movin', movin' Though they're disapprovin' Keep them dogies movin' Rawhide!";

   public final static String PROPERTY_NAME5C = "superLong";
   /**
    * Long string example
    */
   public final static String PROPERTY_VALUE5C = "Futurama:: "
         + "Fry: I must be a robot. Why else would human women refuse to date me? \n"
         + "Leela: Oh, lots of reasons... \n" + "Leela: Okay, this has gotta stop. "
         + "I'm going to remind Fry of his humanity the way only a woman can. \n"
         + "Professor: You're going to do his laundry?";

   // tag testing constants
   public final static String[] someTags = { "test", "aaronz" };

   // MOCK testing objects
   /**
    * Registered provider which implements {@link CoreEntityProvider}
    */
   public EntityProvider entityProvider1 = new CoreEntityProviderMock(PREFIX1, IDS1);
   /**
    * Registered provider that implements {@link EntityProvider} and {@link Taggable}, this
    * provider builds on {@link #entityProvider1}
    */
   public EntityProvider entityProvider1T = new TaggableEntityProviderMock(PREFIX1, REF1, someTags);
   /**
    * Registered provider that only implements {@link EntityProvider}
    */
   public EntityProvider entityProvider2 = new EntityProviderMock(PREFIX2);
   /**
    * Registered provider that implements {@link CoreEntityProvider} and {@link ReferenceParseable}
    */
   public EntityProvider entityProvider3 = new ReferenceParseableEntityProviderMock(PREFIX3, IDS3);
   /**
    * Registered provider that implements {@link CoreEntityProvider} and {@link Resolvable}
    */
   public EntityProvider entityProvider4 = new ResolvableEntityProviderMock(PREFIX4, IDS4);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link Propertyable}
    */
   public EntityProvider entityProvider5 = new PropertyableEntityProviderMock(PREFIX5, IDS5);

   /**
    * Unregistered provider
    */
   public EntityProvider entityProvider9 = new CoreEntityProviderMock(PREFIX9, IDS9);

   /**
    * Basic constructor initializes test data if needed
    */
   public TestData() {
   }

}
