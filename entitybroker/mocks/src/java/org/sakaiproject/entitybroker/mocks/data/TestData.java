/**
 * TestData.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.mocks.data;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Propertyable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.TagSearchable;
import org.sakaiproject.entitybroker.mocks.CoreEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.EntityProviderMock;
import org.sakaiproject.entitybroker.mocks.PropertyableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.RESTfulEntityProviderMock;
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
   public static String USER_ID = "user-11111111";
   public static String USER_DISPLAY = "Aaron Zeckoski";
   /**
    * access level user in LOCATION1_ID
    */
   public static String ACCESS_USER_ID = "access-2222222";
   public static String ACCESS_USER_DISPLAY = "Regular User";
   /**
    * maintain level user in LOCATION1_ID
    */
   public static String MAINT_USER_ID = "maint-33333333";
   public static String MAINT_USER_DISPLAY = "Maint User";
   /**
    * super admin user
    */
   public static String ADMIN_USER_ID = "admin";
   public static String ADMIN_USER_DISPLAY = "Administrator";
   /**
    * Invalid user (also can be used to simulate the anonymous user)
    */
   public static String INVALID_USER_ID = "invalid-UUUUUU";

   /**
    * current location
    */
   public static String LOCATION1_ID = "/site/ref-1111111";
   public static String LOCATION1_TITLE = "Location 1 title";
   public static String LOCATION2_ID = "/site/ref-22222222";
   public static String LOCATION2_TITLE = "Location 2 title";
   public static String INVALID_LOCATION_ID = "invalid-LLLLLLLL";



   // testing constants
   public static String SERVER_URL = "http://localhost:8001/portal";
   public static String DIRECT = "/direct";

   public static String PREFIX1 = "AZprefix1";
   public static String[] IDS1 = new String[] { "10", "11", "12" };
   public static String REF1 = EntityReference.SEPARATOR + PREFIX1
         + EntityReference.SEPARATOR + IDS1[0];
   public static String REF1_1 = EntityReference.SEPARATOR + PREFIX1
         + EntityReference.SEPARATOR + IDS1[1];
   public static String REF1_INVALID = EntityReference.SEPARATOR + PREFIX1
         + EntityReference.SEPARATOR + "XXXXXX";
   public static String URL1 = SERVER_URL + DIRECT + REF1;
   public static String SPACE_URL1 = SERVER_URL + DIRECT + EntityReference.SEPARATOR + PREFIX1;
   public static String EXTENSION1 = Outputable.HTML;
   public static String ENTITY_URL1 = EntityReference.SEPARATOR + PREFIX1 + EntityReference.SEPARATOR + IDS1[0] + "." + EXTENSION1;
   public static String INPUT_URL1 = ENTITY_URL1;

   public static String PREFIX2 = "simplePrefix2";
   public static String REF2 = EntityReference.SEPARATOR + PREFIX2;
   public static String URL2 = SERVER_URL + DIRECT + REF2;
   public static String EXTENSION2 = Outputable.XML;
   public static String ENTITY_URL2 = EntityReference.SEPARATOR + PREFIX2 + "." + EXTENSION2;
   public static String INPUT_URL2 = ENTITY_URL2;

   public static String PREFIX3 = "myPrefix3";
   public static String[] IDS3 = new String[] { "thirty", "31" };
   public static String REF3A = EntityReference.SEPARATOR + PREFIX3
         + EntityReference.SEPARATOR + IDS3[0];
   public static String EXTENSION3A = Outputable.JSON;
   public static String ENTITY_URL3A = EntityReference.SEPARATOR + PREFIX3 + EntityReference.SEPARATOR + IDS3[0] + "." + EXTENSION3A;
   public static String INPUT_URL3A = ENTITY_URL3A;
   public static String REF3B = EntityReference.SEPARATOR + PREFIX3
   + EntityReference.SEPARATOR + IDS3[1];
   public static String EXTENSION3B = Outputable.XML;
   public static String ENTITY_URL3B = EntityReference.SEPARATOR + PREFIX3 + EntityReference.SEPARATOR + IDS3[1] + "." + EXTENSION3B;
   public static String INPUT_URL3B = ENTITY_URL3B;

   public static String PREFIX4 = "myPrefix4";
   public static String[] IDS4 = new String[] { "4-one", "4-two", "4-three" };
   public static String SPACE4 = EntityReference.SEPARATOR + PREFIX4;
   public static String REF4 = EntityReference.SEPARATOR + PREFIX4
         + EntityReference.SEPARATOR + IDS4[0];
   public static String REF4_two = EntityReference.SEPARATOR + PREFIX4
         + EntityReference.SEPARATOR + IDS4[1];
   public static String REF4_3 = EntityReference.SEPARATOR + PREFIX4
         + EntityReference.SEPARATOR + IDS4[2];
   // sample entity objects
   public static MyEntity entity4 = new MyEntity(IDS4[0], "something0");
   public static MyEntity entity4_two = new MyEntity(IDS4[1], "something1");
   public static MyEntity entity4_3 = new MyEntity(IDS4[2], "something2");
   // urls to data
   public static String EXTENSION4 = Outputable.JSON;
   public static String ENTITY_URL4 = EntityReference.SEPARATOR + PREFIX4 + EntityReference.SEPARATOR + IDS4[0] + EXTENSION4;
   public static String INPUT_URL4 = EntityReference.SEPARATOR + PREFIX4 + EntityReference.SEPARATOR + IDS4[0] + "/extra/stuff." + EXTENSION4;

   public static String PREFIX5 = "myPrefix5";
   public static String[] IDS5 = new String[] { "fiver", "50" };
   public static String REF5 = EntityReference.SEPARATOR + PREFIX5
         + EntityReference.SEPARATOR + IDS5[0];
   public static String REF5_2 = EntityReference.SEPARATOR + PREFIX5
         + EntityReference.SEPARATOR + IDS5[1];

   public static String PREFIX6 = "myPrefix6";
   public static String[] IDS6 = new String[] { "6-one", "6-two", "6-three", "6-four" };
   public static String SPACE6 = EntityReference.SEPARATOR + PREFIX6;
   public static String REF6 = EntityReference.SEPARATOR + PREFIX6
         + EntityReference.SEPARATOR + IDS6[0];
   public static String ENTITY_URL6 = EntityReference.SEPARATOR + PREFIX6 + EntityReference.SEPARATOR + IDS6[0];
   public static String INPUT_URL6 = ENTITY_URL6 + "/extra";
   public static String REF6_2 = EntityReference.SEPARATOR + PREFIX6
         + EntityReference.SEPARATOR + IDS6[1];
   public static String REF6_3 = EntityReference.SEPARATOR + PREFIX6
         + EntityReference.SEPARATOR + IDS6[2];
   public static String REF6_4 = EntityReference.SEPARATOR + PREFIX6
         + EntityReference.SEPARATOR + IDS6[3];

   public static String PREFIX9 = "unregPrefix9";
   public static String[] IDS9 = new String[] { "ninety", "9and1" };
   public static String REF9 = EntityReference.SEPARATOR + PREFIX9
         + EntityReference.SEPARATOR + IDS9[0];
   public static String URL9 = SERVER_URL + DIRECT + REF9;

   public static String INVALID_REF = "invalid_reference-1";
   public static String INVALID_URL = "http://bkjskldsalkdsa/sdakljdskl/stuff";

   public static String EVENT1_NAME = "event.name.test.1";
   public static String EVENT2_NAME = "event.name.test.2";

   // property testing constants
   public static String PROPERTY_NAME5A = "prop5A";
   public static String PROPERTY_VALUE5A = "value5A";

   public static String PROPERTY_NAME5B = "prop5B";
   /**
    * Lots of quote chars example
    */
   public static String PROPERTY_VALUE5B = "Keep movin', movin', movin' Though they're disapprovin' Keep them dogies movin' Rawhide!";

   public static String PROPERTY_NAME5C = "superLong";
   /**
    * Long string example
    */
   public static String PROPERTY_VALUE5C = "Futurama:: "
         + "Fry: I must be a robot. Why else would human women refuse to date me? \n"
         + "Leela: Oh, lots of reasons... \n" + "Leela: Okay, this has gotta stop. "
         + "I'm going to remind Fry of his humanity the way only a woman can. \n"
         + "Professor: You're going to do his laundry?";

   // tag testing constants
   public static String[] someTags = { "test", "aaronz" };

   // MOCK testing objects
   /**
    * Registered provider which implements {@link CoreEntityProvider}
    */
   public CoreEntityProvider entityProvider1 = new CoreEntityProviderMock(PREFIX1, IDS1);
   /**
    * Registered provider that implements {@link EntityProvider} and {@link Taggable} and {@link TagSearchable}, this
    * provider builds on {@link #entityProvider1}
    */
   public TaggableEntityProviderMock entityProvider1T = new TaggableEntityProviderMock(PREFIX1, REF1, someTags);
   /**
    * Registered provider that only implements {@link EntityProvider}
    */
   public EntityProvider entityProvider2 = new EntityProviderMock(PREFIX2);
   /**
    * Registered provider that implements {@link CoreEntityProvider} and {@link ReferenceParseable}
    */
   public ReferenceParseableEntityProviderMock entityProvider3 = new ReferenceParseableEntityProviderMock(PREFIX3, IDS3);
   /**
    * Registered provider that implements {@link CoreEntityProvider} and {@link Resolvable} and {@link CollectionResolvable}
    */
   public ResolvableEntityProviderMock entityProvider4 = new ResolvableEntityProviderMock(PREFIX4, IDS4);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link Propertyable}
    */
   public PropertyableEntityProviderMock entityProvider5 = new PropertyableEntityProviderMock(PREFIX5, IDS5);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link RESTful}
    */
   public RESTfulEntityProviderMock entityProvider6 = new RESTfulEntityProviderMock(PREFIX6, IDS6);

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
