/**
 * EntityProviderManagerImplTest.java - created by aaronz on Jul 26, 2007
 */

package org.sakaiproject.entitybroker.impl.test;

import java.util.Set;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;
import org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl;
import org.sakaiproject.entitybroker.mocks.data.TestData;

import junit.framework.TestCase;

/**
 * Testing the entity provider manager
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityProviderManagerImplTest extends TestCase {

   protected EntityProviderManagerImpl entityProviderManager;
   private TestData td;

   @Override
   protected void setUp() throws Exception {
      super.setUp();
      // setup things
      td = new TestData();

      entityProviderManager = new EntityProviderManagerImpl();
      entityProviderManager.init();
      entityProviderManager.registerEntityProvider(td.entityProvider1);
      entityProviderManager.registerEntityProvider(td.entityProvider2);
      entityProviderManager.registerEntityProvider(td.entityProvider3);
      entityProviderManager.registerEntityProvider(td.entityProvider4);

      entityProviderManager.registerEntityProvider(td.entityProvider1T);

   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#init()}.
    */
   public void testInit() {
      // simply make sure this does not fail
      entityProviderManager.init();
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#getProviderByReference(java.lang.String)}.
    */
   public void testGetProviderByReference() {
      EntityProvider ep = null;

      // get valid providers
      ep = entityProviderManager.getProviderByReference(TestData.REF1);
      assertNotNull(ep);
      assertEquals(td.entityProvider1, ep);

      ep = entityProviderManager.getProviderByReference(TestData.REF3);
      assertNotNull(ep);
      assertEquals(td.entityProvider3, ep);

      // test unregistered provider returns null
      ep = entityProviderManager.getProviderByReference(TestData.REF9);
      assertNull(ep);

      // ensure invalid prefix dies
      try {
         ep = entityProviderManager.getProviderByReference(TestData.INVALID_REF);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#getProviderByPrefix(java.lang.String)}.
    */
   public void testGetProviderByPrefix() {
      EntityProvider ep = null;

      // get valid providers
      ep = entityProviderManager.getProviderByPrefix(TestData.PREFIX1);
      assertNotNull(ep);
      assertEquals(td.entityProvider1, ep);

      ep = entityProviderManager.getProviderByPrefix(TestData.PREFIX2);
      assertNotNull(ep);
      assertEquals(td.entityProvider2, ep);

      ep = entityProviderManager.getProviderByPrefix(TestData.PREFIX3);
      assertNotNull(ep);
      assertEquals(td.entityProvider3, ep);

      // ensure invalid prefix simply returns null
      ep = entityProviderManager.getProviderByPrefix(TestData.INVALID_REF);
      assertNull(ep);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#getProviderByPrefixAndCapability(java.lang.String, java.lang.Class)}.
    */
   public void testGetProviderByPrefixAndCapability() {
      EntityProvider ep = null;

      // get valid providers
      ep = entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX1,
            CoreEntityProvider.class);
      assertNotNull(ep);
      assertEquals(td.entityProvider1, ep);

      // get valid providers for sub caps
      ep = entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX1, Taggable.class);
      assertNotNull(ep);
      assertEquals(td.entityProvider1T, ep);

      ep = entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX4,
            Resolvable.class);
      assertNotNull(ep);
      assertEquals(td.entityProvider4, ep);

      // attempt to get providers when there are none
      ep = entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX2,
            CoreEntityProvider.class);
      assertNull(ep);

      ep = entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX1,
            Resolvable.class);
      assertNull(ep);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#getRegisteredPrefixes()}.
    */
   public void testGetRegisteredPrefixes() {
      Set<String> s = null;

      s = entityProviderManager.getRegisteredPrefixes();
      assertNotNull(s);
      assertFalse(s.isEmpty());
      assertTrue(s.contains(TestData.PREFIX1));
      assertTrue(s.contains(TestData.PREFIX2));
      assertTrue(s.contains(TestData.PREFIX3));
      assertTrue(s.contains(TestData.PREFIX4));
      assertFalse(s.contains(TestData.PREFIX9));
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#registerEntityProvider(org.sakaiproject.entitybroker.entityprovider.EntityProvider)}.
    */
   public void testRegisterEntityProvider() {
      // test registering unregistered provider
      entityProviderManager.registerEntityProvider(td.entityProvider9);

      // test registering an already registered provider
      entityProviderManager.registerEntityProvider(td.entityProvider1);

      // test registering null dies horribly
      try {
         entityProviderManager.registerEntityProvider(null);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#unregisterEntityProvider(org.sakaiproject.entitybroker.entityprovider.EntityProvider)}.
    */
   public void testUnregisterEntityProvider() {
      // test unregistering registered EP
      entityProviderManager.unregisterEntityProvider(td.entityProvider1);

      // test unregistering non registered EP
      entityProviderManager.unregisterEntityProvider(td.entityProvider9);

      // test unregistering null dies horribly
      try {
         entityProviderManager.unregisterEntityProvider(null);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#unregisterEntityProviderByPrefix(java.lang.String)}.
    */
   public void testUnregisterEntityProviderByPrefix() {
      // test unregistering registered EP
      entityProviderManager.unregisterEntityProviderByPrefix(TestData.PREFIX1);
      assertNull(entityProviderManager.getProviderByPrefix(TestData.PREFIX1));

      // test unregistering non registered EP
      entityProviderManager.unregisterEntityProviderByPrefix(TestData.PREFIX9);

      // test unregistering null dies horribly
      try {
         entityProviderManager.unregisterEntityProviderByPrefix(null);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#unregisterCapability(java.lang.String, java.lang.Class)}.
    */
   public void testUnregisterEntityProviderCapability() {
      // test unregistering an added capability
      entityProviderManager.unregisterCapability(TestData.PREFIX1, Taggable.class);
      assertNull(entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX1,
            Taggable.class));

      // test unregistering something we just unregistered (should be ok)
      entityProviderManager.unregisterCapability(TestData.PREFIX1, Taggable.class);

      // test unregistering something not registered
      entityProviderManager.unregisterCapability(TestData.PREFIX2, Taggable.class);

      // test unregistering null dies horribly
      try {
         entityProviderManager.unregisterCapability(TestData.PREFIX3, null);
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
      }
   }

}
