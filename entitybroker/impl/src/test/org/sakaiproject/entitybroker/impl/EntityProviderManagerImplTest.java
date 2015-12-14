/**
 * $Id$
 * $URL$
 * EntityHandlerImplTest.java - entity-broker - Apr 6, 2008 12:08:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.EntityViewUrlCustomizable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.TagProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityProviderListener;
import org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl;
import org.sakaiproject.entitybroker.mocks.data.TestData;

/**
 * Testing the entity provider manager
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityProviderManagerImplTest {

   protected EntityProviderManagerImpl entityProviderManager;
   private TestData td;

   @Before
   public void setUp() throws Exception {
      td = new TestData();
      entityProviderManager = new ServiceTestManager(td).entityProviderManager;
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#init()}.
    */
   @Test
   public void testInit() {
      // simply make sure this does not fail
      entityProviderManager.init();
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#getProviderByReference(java.lang.String)}.
    */
   @SuppressWarnings("deprecation")
   @Test
   public void testGetProviderByReference() {
      EntityProvider ep = null;

      // get valid providers
      ep = entityProviderManager.getProviderByReference(TestData.REF1);
      Assert.assertNotNull(ep);
      Assert.assertEquals(td.entityProvider1, ep);

      ep = entityProviderManager.getProviderByReference(TestData.REF3A);
      Assert.assertNotNull(ep);
      Assert.assertEquals(td.entityProvider3, ep);

      // test unregistered provider returns null
      ep = entityProviderManager.getProviderByReference(TestData.REF9);
      Assert.assertNull(ep);

      // ensure invalid prefix dies
      try {
         ep = entityProviderManager.getProviderByReference(TestData.INVALID_REF);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#getProviderByPrefix(java.lang.String)}.
    */
   @Test
   public void testGetProviderByPrefix() {
      EntityProvider ep = null;

      // get valid providers
      ep = entityProviderManager.getProviderByPrefix(TestData.PREFIX1);
      Assert.assertNotNull(ep);
      Assert.assertEquals(td.entityProvider1, ep);

      ep = entityProviderManager.getProviderByPrefix(TestData.PREFIX2);
      Assert.assertNotNull(ep);
      Assert.assertEquals(td.entityProvider2, ep);

      ep = entityProviderManager.getProviderByPrefix(TestData.PREFIX3);
      Assert.assertNotNull(ep);
      Assert.assertEquals(td.entityProvider3, ep);

      // ensure invalid prefix simply returns null
      ep = entityProviderManager.getProviderByPrefix(TestData.INVALID_REF);
      Assert.assertNull(ep);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#getProviderByPrefixAndCapability(java.lang.String, java.lang.Class)}.
    */
   @Test
   public void testGetProviderByPrefixAndCapability() {
      EntityProvider ep = null;

      // get valid providers
      ep = entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX1,
            CoreEntityProvider.class);
      Assert.assertNotNull(ep);
      Assert.assertEquals(td.entityProvider1, ep);

      // get valid providers for sub caps
      ep = entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX1, Taggable.class);
      Assert.assertNotNull(ep);
      Assert.assertEquals(td.entityProvider1T, ep);

      ep = entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX4,
            Resolvable.class);
      Assert.assertNotNull(ep);
      Assert.assertEquals(td.entityProvider4, ep);

      // attempt to get providers when there are none
      ep = entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX2,
            CoreEntityProvider.class);
      Assert.assertNull(ep);

      ep = entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX1,
            Resolvable.class);
      Assert.assertNull(ep);
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#getRegisteredPrefixes()}.
    */
   @Test
   public void testGetRegisteredPrefixes() {
      Set<String> s = null;

      s = entityProviderManager.getRegisteredPrefixes();
      Assert.assertNotNull(s);
      Assert.assertFalse(s.isEmpty());
      Assert.assertTrue(s.contains(TestData.PREFIX1));
      Assert.assertTrue(s.contains(TestData.PREFIX2));
      Assert.assertTrue(s.contains(TestData.PREFIX3));
      Assert.assertTrue(s.contains(TestData.PREFIX4));
      Assert.assertFalse(s.contains(TestData.PREFIX9));
   }

   @Test
   public void testGetPrefixCapabilities() {
      List<Class<? extends EntityProvider>> caps = null;

      caps = entityProviderManager.getPrefixCapabilities(TestData.PREFIX1);
      Assert.assertNotNull(caps);
      Assert.assertEquals(4, caps.size());
      Assert.assertTrue(caps.contains(EntityProvider.class));
      Assert.assertTrue(caps.contains(CoreEntityProvider.class));
      Assert.assertTrue(caps.contains(Taggable.class));
      Assert.assertTrue(caps.contains(TagProvideable.class));

      caps = entityProviderManager.getPrefixCapabilities(TestData.PREFIX4);
      Assert.assertNotNull(caps);
      Assert.assertEquals(5, caps.size());
      Assert.assertTrue(caps.contains(EntityProvider.class));
      Assert.assertTrue(caps.contains(CoreEntityProvider.class));
      Assert.assertTrue(caps.contains(Resolvable.class));
      Assert.assertTrue(caps.contains(CollectionResolvable.class));
      Assert.assertTrue(caps.contains(Outputable.class));

   }

   @Test
   public void testGetRegisteredEntityCapabilities() {
      Map<String, List<Class<? extends EntityProvider>>> m = null;
      
      m = entityProviderManager.getRegisteredEntityCapabilities();
      Assert.assertNotNull(m);
      Assert.assertTrue(m.size() > 5);
      Assert.assertTrue(m.containsKey(TestData.PREFIX1));
      Assert.assertTrue(m.containsKey(TestData.PREFIX2));
      Assert.assertTrue(m.containsKey(TestData.PREFIX3));
      Assert.assertTrue(m.containsKey(TestData.PREFIX4));
      Assert.assertFalse(m.containsKey(TestData.PREFIX9));

      List<Class<? extends EntityProvider>> caps = m.get(TestData.PREFIX1);
      Assert.assertNotNull(caps);
      Assert.assertEquals(4, caps.size());
      Assert.assertTrue(caps.contains(EntityProvider.class));
      Assert.assertTrue(caps.contains(CoreEntityProvider.class));
      Assert.assertTrue(caps.contains(Taggable.class));
      Assert.assertTrue(caps.contains(TagProvideable.class));
      
   }

   @Test
   public void testGetProvidersByCapability() {
      List<? extends EntityProvider> providers = null;

      providers = entityProviderManager.getProvidersByCapability(CRUDable.class);
      Assert.assertNotNull(providers);
      Assert.assertTrue(providers.size() >= 4);
      Assert.assertTrue(providers.contains(td.entityProvider6));
      Assert.assertTrue(providers.contains(td.entityProvider7));
      Assert.assertTrue(providers.contains(td.entityProvider8));
      Assert.assertFalse(providers.contains(td.entityProvider1));
      Assert.assertFalse(providers.contains(td.entityProvider2));
      Assert.assertFalse(providers.contains(td.entityProvider3));

      providers = entityProviderManager.getProvidersByCapability(ActionsExecutable.class);
      Assert.assertNotNull(providers);
      Assert.assertTrue(providers.size() >= 3);
      Assert.assertTrue(providers.contains(td.entityProviderA1));
      Assert.assertTrue(providers.contains(td.entityProviderA2));
      Assert.assertTrue(providers.contains(td.entityProviderA3));
      Assert.assertFalse(providers.contains(td.entityProvider1));
      Assert.assertFalse(providers.contains(td.entityProvider2));
      Assert.assertFalse(providers.contains(td.entityProvider3));

      providers = entityProviderManager.getProvidersByCapability(EntityViewUrlCustomizable.class);
      Assert.assertNotNull(providers);
      Assert.assertEquals(0, providers.size());
   }

   @Test
   public void testGetPrefixesByCapability() {
      List<String> prefixes = null;

      prefixes = entityProviderManager.getPrefixesByCapability(CRUDable.class);
      Assert.assertNotNull(prefixes);
      Assert.assertTrue(prefixes.size() >= 4);
      Assert.assertTrue(prefixes.contains(TestData.PREFIX6));
      Assert.assertTrue(prefixes.contains(TestData.PREFIX7));
      Assert.assertTrue(prefixes.contains(TestData.PREFIX8));
      Assert.assertFalse(prefixes.contains(TestData.PREFIX1));
      Assert.assertFalse(prefixes.contains(TestData.PREFIX2));
      Assert.assertFalse(prefixes.contains(TestData.PREFIX3));

      prefixes = entityProviderManager.getPrefixesByCapability(ActionsExecutable.class);
      Assert.assertNotNull(prefixes);
      Assert.assertTrue(prefixes.size() >= 3);
      Assert.assertTrue(prefixes.contains(TestData.PREFIXA1));
      Assert.assertTrue(prefixes.contains(TestData.PREFIXA2));
      Assert.assertTrue(prefixes.contains(TestData.PREFIXA3));
      Assert.assertFalse(prefixes.contains(TestData.PREFIX1));
      Assert.assertFalse(prefixes.contains(TestData.PREFIX2));
      Assert.assertFalse(prefixes.contains(TestData.PREFIX3));

      prefixes = entityProviderManager.getPrefixesByCapability(EntityViewUrlCustomizable.class);
      Assert.assertNotNull(prefixes);
      Assert.assertEquals(0, prefixes.size());
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#registerEntityProvider(org.sakaiproject.entitybroker.entityprovider.EntityProvider)}.
    */
   @Test
  public void testRegisterEntityProvider() {
      // test registering unregistered provider
      entityProviderManager.registerEntityProvider(td.entityProvider9);

      // test registering an already registered provider
      entityProviderManager.registerEntityProvider(td.entityProvider1);

      // test registering null dies horribly
      try {
         entityProviderManager.registerEntityProvider(null);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#unregisterEntityProvider(org.sakaiproject.entitybroker.entityprovider.EntityProvider)}.
    */
   @Test
   public void testUnregisterEntityProvider() {
      // test unregistering registered EP
      entityProviderManager.unregisterEntityProvider(td.entityProvider1);

      // test unregistering non registered EP
      entityProviderManager.unregisterEntityProvider(td.entityProvider9);

      // test unregistering null dies horribly
      try {
         entityProviderManager.unregisterEntityProvider(null);
         Assert.fail("Should have thrown exception");
      } catch (NullPointerException e) {
         Assert.assertNotNull(e);
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#unregisterEntityProviderByPrefix(java.lang.String)}.
    */
   @Test
   public void testUnregisterEntityProviderByPrefix() {
      // test unregistering registered EP
      entityProviderManager.unregisterEntityProviderByPrefix(TestData.PREFIX1);
      Assert.assertNull(entityProviderManager.getProviderByPrefix(TestData.PREFIX1));

      entityProviderManager.unregisterEntityProviderByPrefix(TestData.PREFIX7);
      Assert.assertNull(entityProviderManager.getProviderByPrefix(TestData.PREFIX7));

      entityProviderManager.unregisterEntityProviderByPrefix(TestData.PREFIX8);
      Assert.assertNull(entityProviderManager.getProviderByPrefix(TestData.PREFIX8));

      entityProviderManager.unregisterEntityProviderByPrefix(TestData.PREFIXA);
      Assert.assertNull(entityProviderManager.getProviderByPrefix(TestData.PREFIXA));

      // test unregistering non registered EP
      entityProviderManager.unregisterEntityProviderByPrefix(TestData.PREFIX9);

      // test unregistering null dies horribly
      try {
         entityProviderManager.unregisterEntityProviderByPrefix(null);
         Assert.fail("Should have thrown exception");
      } catch (NullPointerException e) {
         Assert.assertNotNull(e);
      }
   }

   /**
    * Test method for
    * {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#unregisterCapability(java.lang.String, java.lang.Class)}.
    */
   @Test
   public void testUnregisterEntityProviderCapability() {
      // test unregistering an added capability
      entityProviderManager.unregisterCapability(TestData.PREFIX1, Taggable.class);
      Assert.assertNull(entityProviderManager.getProviderByPrefixAndCapability(TestData.PREFIX1,
            Taggable.class));

      // test unregistering something we just unregistered (should be ok)
      entityProviderManager.unregisterCapability(TestData.PREFIX1, Taggable.class);

      // test unregistering something not registered
      entityProviderManager.unregisterCapability(TestData.PREFIX2, Taggable.class);

      // test unregistering null dies horribly
      try {
         entityProviderManager.unregisterCapability(TestData.PREFIX3, null);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }
   }

   @Test
   public void testRegisterListener() {
       final int[] calls = new int[1];
       final Map<String, EntityProvider> providers = new ConcurrentHashMap<String, EntityProvider>();
       // test basic fetch
       class Listener1 implements EntityProviderListener<EntityProvider> {
           public Class<EntityProvider> getCapabilityFilter() {
               return null;
           }
           public String getPrefixFilter() {
               return null;
           }
           public void run(EntityProvider provider) {
               calls[0]++;
               String prefix = provider.getEntityPrefix();
               providers.put(prefix, provider);
           }
       }

       entityProviderManager.registerListener(new Listener1(), true);
       Assert.assertNotNull(providers);
       int psize = providers.size();
       Assert.assertTrue(psize > 15);
       Assert.assertEquals(psize, calls[0]); // assure we are not calling things too often

       // test not getting existing ones
       providers.clear();
       entityProviderManager.registerListener(new Listener1(), false);
       Assert.assertNotNull(providers);
       Assert.assertEquals(0, providers.size());

       // test filter by prefix
       class Listener2 implements EntityProviderListener<EntityProvider> {
           public Class<EntityProvider> getCapabilityFilter() {
               return null;
           }
           public String getPrefixFilter() {
               return TestData.PREFIX4;
           }
           public void run(EntityProvider provider) {
               String prefix = provider.getEntityPrefix();
               providers.put(prefix, provider);
           }
       }

       providers.clear();
       entityProviderManager.registerListener(new Listener2(), true);
       Assert.assertNotNull(providers);
       Assert.assertEquals(1, providers.size());
       Assert.assertEquals(td.entityProvider4, providers.get(TestData.PREFIX4));

       // test hit when new registration happens
       providers.clear();
       entityProviderManager.unregisterEntityProviderByPrefix(TestData.PREFIX4);
       Assert.assertNotNull(providers);
       Assert.assertEquals(0, providers.size());
       entityProviderManager.registerEntityProvider(td.entityProvider4);
       Assert.assertNotNull(providers);
       Assert.assertEquals(1, providers.size());
       Assert.assertEquals(td.entityProvider4, providers.get(TestData.PREFIX4));

       // test filter by capability
       class Listener3 implements EntityProviderListener<RedirectDefinable> {
           public Class<RedirectDefinable> getCapabilityFilter() {
               return RedirectDefinable.class;
           }
           public String getPrefixFilter() {
               return null;
           }
           public void run(RedirectDefinable provider) {
               String prefix = provider.getEntityPrefix();
               providers.put(prefix, provider);
           }
       }

       providers.clear();
       entityProviderManager.registerListener(new Listener3(), true);
       Assert.assertNotNull(providers);
       Assert.assertEquals(1, providers.size());
       Assert.assertEquals(td.entityProviderU2, providers.get(TestData.PREFIXU2));
       Assert.assertTrue(RedirectDefinable.class.isAssignableFrom(providers.get(TestData.PREFIXU2).getClass()));

       // test filter by both
       class Listener4 implements EntityProviderListener<RedirectDefinable> {
           public Class<RedirectDefinable> getCapabilityFilter() {
               return RedirectDefinable.class;
           }
           public String getPrefixFilter() {
               return TestData.PREFIXU2;
           }
           public void run(RedirectDefinable provider) {
               String prefix = provider.getEntityPrefix();
               providers.put(prefix, provider);
           }
       }

       providers.clear();
       entityProviderManager.registerListener(new Listener4(), true);
       Assert.assertNotNull(providers);
       Assert.assertEquals(1, providers.size());
       Assert.assertEquals(td.entityProviderU2, providers.get(TestData.PREFIXU2));
       Assert.assertTrue(RedirectDefinable.class.isAssignableFrom(providers.get(TestData.PREFIXU2).getClass()));

       // test filter by invalid stuff (should get nothing)
       class Listener5 implements EntityProviderListener<EntityProvider> {
           public Class<EntityProvider> getCapabilityFilter() {
               return null;
           }
           public String getPrefixFilter() {
               return "XXXXXXXXXXXXXX";
           }
           public void run(EntityProvider provider) {
               String prefix = provider.getEntityPrefix();
               providers.put(prefix, provider);
           }
       }

       providers.clear();
       entityProviderManager.registerListener(new Listener5(), true);
       Assert.assertNotNull(providers);
       Assert.assertEquals(0, providers.size());

       // make sure null dies
       try {
           entityProviderManager.registerListener(null, true);
           Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
           Assert.assertNotNull(e);
        }
   }

   @Test
   public void testUnregisterListeners() {
       final Map<String, EntityProvider> providers = new ConcurrentHashMap<String, EntityProvider>();
       class Listener2 implements EntityProviderListener<EntityProvider> {
           public Class<EntityProvider> getCapabilityFilter() {
               return null;
           }
           public String getPrefixFilter() {
               return TestData.PREFIX4;
           }
           public void run(EntityProvider provider) {
               String prefix = provider.getEntityPrefix();
               providers.put(prefix, provider);
           }
       }

       providers.clear();
       EntityProviderListener<EntityProvider> listener = new Listener2();

       entityProviderManager.unregisterListener(listener); // nothing happens
       Assert.assertNotNull(providers);
       Assert.assertEquals(0, providers.size());

       entityProviderManager.registerListener(listener, true);
       Assert.assertNotNull(providers);
       Assert.assertEquals(1, providers.size());

       providers.clear();
       entityProviderManager.unregisterListener(listener);
       Assert.assertNotNull(providers);
       Assert.assertEquals(0, providers.size());

       entityProviderManager.unregisterEntityProviderByPrefix(TestData.PREFIX4);
       entityProviderManager.registerEntityProvider(td.entityProvider4);
       Assert.assertNotNull(providers);
       Assert.assertEquals(0, providers.size());

       // make sure null dies
       try {
           entityProviderManager.unregisterListener(null);
           Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
           Assert.assertNotNull(e);
        }
   }

}
