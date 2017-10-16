/**
 * Copyright (c) 2007-2009 The Apereo Foundation
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
 * TestData.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.mocks.data;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsDefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutionControllable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.BrowseSearchable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.BrowseableCollection;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.DescribePropertiesable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputSerializable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Propertyable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectControllable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.TagProvideable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.mocks.ActionsDefineableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ActionsEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ActionsExecutionEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.BrowsableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.BrowseSearchableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.CoreEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.DescribePropertiesableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.DescribeableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.EntityProviderMock;
import org.sakaiproject.entitybroker.mocks.PropertyableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.RESTfulEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ReferenceParseableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.RequestStoreableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ResolvableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.SerializeableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.TagEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.TaggableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.RedirectControllableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.RedirectDefineableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.RedirectableEntityProviderMock;

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
   public static String SERVER_URL = "http://localhost:8080";
   public static String DIRECT = EntityView.DIRECT_PREFIX;

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
   public static String ENTITY_URL4 = EntityReference.SEPARATOR + PREFIX4 + EntityReference.SEPARATOR + IDS4[0] + "." + Formats.XML;
   public static String INPUT_URL4 = EntityReference.SEPARATOR + PREFIX4 + EntityReference.SEPARATOR + IDS4[0] + "/extra/stuff." + EXTENSION4;

   public static String ENTITY_URL4_XML = EntityReference.SEPARATOR + PREFIX4 + EntityReference.SEPARATOR + IDS4[0] + "." + Formats.XML;
   public static String ENTITY_URL4_JSON = EntityReference.SEPARATOR + PREFIX4 + EntityReference.SEPARATOR + IDS4[0] + "." + Formats.JSON;
   public static String ENTITY_URL4_JSONP = REF4 + "." + Formats.JSONP;   
   public static String COLLECTION_URL4_XML = EntityReference.SEPARATOR + PREFIX4 + "." + Formats.XML;
   public static String COLLECTION_URL4_JSON = EntityReference.SEPARATOR + PREFIX4 + "." + Formats.JSON;

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

   public static String PREFIX7 = "describe-prefix";
   public static String[] IDS7 = new String[] { "seven", "7" };
   public static String REF7 = EntityReference.SEPARATOR + PREFIX7
         + EntityReference.SEPARATOR + IDS7[0];
   public static String REF7_2 = EntityReference.SEPARATOR + PREFIX7
         + EntityReference.SEPARATOR + IDS7[1];

   public static String PREFIX8 = "custom";
   public static String[] IDS8 = new String[] { "eight", "8" };
   public static String REF8 = EntityReference.SEPARATOR + PREFIX8
         + EntityReference.SEPARATOR + IDS8[0];
   public static String REF8_2 = EntityReference.SEPARATOR + PREFIX8
         + EntityReference.SEPARATOR + IDS8[1];

   public static String PREFIX9 = "unregPrefix9";
   public static String[] IDS9 = new String[] { "ninety", "9and1" };
   public static String REF9 = EntityReference.SEPARATOR + PREFIX9
         + EntityReference.SEPARATOR + IDS9[0];
   public static String URL9 = SERVER_URL + DIRECT + REF9;

   public static String PREFIXA = "requestPrefix";
   public static String[] IDSA = new String[] { "aaaaaa", "A" };
   public static String REFA = EntityReference.SEPARATOR + PREFIXA
         + EntityReference.SEPARATOR + IDSA[0];
   public static String REFA_2 = EntityReference.SEPARATOR + PREFIXA
         + EntityReference.SEPARATOR + IDSA[1];

   public static String PREFIXA1 = "actions-prefix";
   public static String SPACEA1 = EntityReference.SEPARATOR + PREFIXA1;
   public static String[] IDSA1 = new String[] { "a1a", "a1BEEE" };
   public static String REFA1 = EntityReference.SEPARATOR + PREFIXA1
         + EntityReference.SEPARATOR + IDSA1[0];
   public static String REFA1_2 = EntityReference.SEPARATOR + PREFIXA1
         + EntityReference.SEPARATOR + IDSA1[1];

   public static String PREFIXA2 = "actionDefinable";
   public static String SPACEA2 = EntityReference.SEPARATOR + PREFIXA2;
   public static String[] IDSA2 = new String[] { "a21111", "a2bbb" };
   public static String REFA2 = EntityReference.SEPARATOR + PREFIXA2
         + EntityReference.SEPARATOR + IDSA2[0];
   public static String REFA2_2 = EntityReference.SEPARATOR + PREFIXA2
         + EntityReference.SEPARATOR + IDSA2[1];

   public static String PREFIXA3 = "actionExecution";
   public static String SPACEA3 = EntityReference.SEPARATOR + PREFIXA3;
   public static String[] IDSA3 = new String[] { "a31", "a32" };
   public static String REFA3 = EntityReference.SEPARATOR + PREFIXA3
         + EntityReference.SEPARATOR + IDSA3[0];
   public static String REFA3_2 = EntityReference.SEPARATOR + PREFIXA3
         + EntityReference.SEPARATOR + IDSA3[1];

   public static String PREFIXU1 = "redirect1";
   public static String SPACEU1 = EntityReference.SEPARATOR + PREFIXU1;
   public static String[] IDSU1 = new String[] { "rA", "rB" };
   public static String REFU1 = EntityReference.SEPARATOR + PREFIXU1
         + EntityReference.SEPARATOR + IDSU1[0];
   public static String REFU1_2 = EntityReference.SEPARATOR + PREFIXU1
         + EntityReference.SEPARATOR + IDSU1[1];

   public static String PREFIXU2 = "redirect2";
   public static String SPACEU2 = EntityReference.SEPARATOR + PREFIXU2;
   public static String[] IDSU2 = new String[] { "rA", "rB" };
   public static String REFU2 = EntityReference.SEPARATOR + PREFIXU2
         + EntityReference.SEPARATOR + IDSU2[0];
   public static String REFU2_2 = EntityReference.SEPARATOR + PREFIXU2
         + EntityReference.SEPARATOR + IDSU2[1];

   public static String PREFIXU3 = "redirect3";
   public static String SPACEU3 = EntityReference.SEPARATOR + PREFIXU3;
   public static String[] IDSU3 = new String[] { "rA", "rB" };
   public static String REFU3 = EntityReference.SEPARATOR + PREFIXU3
         + EntityReference.SEPARATOR + IDSU3[0];
   public static String REFU3_2 = EntityReference.SEPARATOR + PREFIXU3
         + EntityReference.SEPARATOR + IDSU3[1];

   public static String PREFIXT1 = "tagging";
   public static String[] IDST1 = new String[] { "tag111", "tag222", "tag333" };
   public static String REFT1 = EntityReference.SEPARATOR + PREFIXT1
         + EntityReference.SEPARATOR + IDST1[0];
   public static String REFT1_2 = EntityReference.SEPARATOR + PREFIXT1
         + EntityReference.SEPARATOR + IDST1[1];
   public static String REFT1_3 = EntityReference.SEPARATOR + PREFIXT1
         + EntityReference.SEPARATOR + IDST1[2];

   public static String PREFIXB1 = "browse1";
   public static String[] IDSB1 = new String[] { "CCC1", "BBB2", "AAA3" };
   public static String REFB1 = EntityReference.SEPARATOR + PREFIXB1
         + EntityReference.SEPARATOR + IDSB1[0];
   public static String REFB1_2 = EntityReference.SEPARATOR + PREFIXB1
         + EntityReference.SEPARATOR + IDSB1[1];
   public static String REFB1_3 = EntityReference.SEPARATOR + PREFIXB1
         + EntityReference.SEPARATOR + IDSB1[2];

   public static String PREFIXB2 = "browse-search";
   public static String[] IDSB2 = new String[] { "sdf233", "234ess", "zzz" };
   public static String REFB2 = EntityReference.SEPARATOR + PREFIXB2
         + EntityReference.SEPARATOR + IDSB2[0];
   public static String REFB2_2 = EntityReference.SEPARATOR + PREFIXB2
         + EntityReference.SEPARATOR + IDSB2[1];
   public static String REFB2_3 = EntityReference.SEPARATOR + PREFIXB2
         + EntityReference.SEPARATOR + IDSB2[2];

   public static String PREFIXS1 = "serialize";
   public static String[] IDSS1 = new String[] { "AZ", "BZ", "CZ" };
   public static String REFS1 = EntityReference.SEPARATOR + PREFIXS1
         + EntityReference.SEPARATOR + IDSS1[0];
   public static String REFS1_2 = EntityReference.SEPARATOR + PREFIXS1
         + EntityReference.SEPARATOR + IDSS1[1];
   public static String REFS1_3 = EntityReference.SEPARATOR + PREFIXS1
         + EntityReference.SEPARATOR + IDSS1[2];

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
    * Registered provider that implements {@link EntityProvider} and {@link Taggable} and {@link TagProvideable}, this
    * provider builds on {@link #entityProvider1}
    */
   public TaggableEntityProviderMock entityProvider1T = new TaggableEntityProviderMock(PREFIX1, REF1, someTags);
   /**
    * Registered provider that uses the built in tagging support
    */
   public TagEntityProviderMock entityProviderTag = new TagEntityProviderMock(PREFIXT1, IDST1);
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
    * Registered provider which implements {@link CoreEntityProvider} and {@link CRUDable} and {@link Describeable}
    */
   public DescribeableEntityProviderMock entityProvider7 = new DescribeableEntityProviderMock(PREFIX7, IDS7);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link CRUDable} and {@link DescribePropertiesable}
    */
   public DescribePropertiesableEntityProviderMock entityProvider8 = new DescribePropertiesableEntityProviderMock(PREFIX8, IDS8);

   /**
    * Unregistered provider
    */
   public EntityProvider entityProvider9 = new CoreEntityProviderMock(PREFIX9, IDS9);

   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link CRUDable} and {@link RequestAware} and {@link RequestStorable}
    */
   public RequestStoreableEntityProviderMock entityProviderA = new RequestStoreableEntityProviderMock(PREFIXA, IDSA);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link CRUDable} and {@link ActionsExecutable}
    */
   public ActionsEntityProviderMock entityProviderA1 = new ActionsEntityProviderMock(PREFIXA1, IDSA1);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link CRUDable} and {@link ActionsDefineable}
    */
   public ActionsDefineableEntityProviderMock entityProviderA2 = new ActionsDefineableEntityProviderMock(PREFIXA2, IDSA2);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link CRUDable} and {@link ActionsExecutionControllable}
    */
   public ActionsExecutionEntityProviderMock entityProviderA3 = new ActionsExecutionEntityProviderMock(PREFIXA3, IDSA3);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link RESTful} and {@link Redirectable}
    */
   public RedirectableEntityProviderMock entityProviderU1 = new RedirectableEntityProviderMock(PREFIXU1, IDSU1);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link RESTful} and {@link RedirectDefinable}
    */
   public RedirectDefineableEntityProviderMock entityProviderU2 = new RedirectDefineableEntityProviderMock(PREFIXU2, IDSU2);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link RESTful} and {@link RedirectControllable}
    */
   public RedirectControllableEntityProviderMock entityProviderU3 = new RedirectControllableEntityProviderMock(PREFIXU3, IDSU3);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link CRUDable} and {@link BrowseableCollection}
    */
   public BrowsableEntityProviderMock entityProviderB1 = new BrowsableEntityProviderMock(PREFIXB1, IDSB1);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link CRUDable} and {@link BrowseSearchable}
    */
   public BrowseSearchableEntityProviderMock entityProviderB2 = new BrowseSearchableEntityProviderMock(PREFIXB2, IDSB2);
   /**
    * Registered provider which implements {@link CoreEntityProvider} and {@link CRUDable} and {@link OutputSerializable}
    */
   public SerializeableEntityProviderMock entityProviderS1 = new SerializeableEntityProviderMock(PREFIXS1, IDSS1);

   /**
    * Basic constructor initializes test data if needed
    */
   public TestData() {
   }

}
