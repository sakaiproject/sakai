/**
 * $Id$
 * $URL$
 * EBlogic.java - entity-broker - Apr 15, 2008 4:29:18 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.entityprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.DescribePropertiesable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.impl.EntityActionsManager;
import org.sakaiproject.entitybroker.util.reflect.ReflectUtil;
import org.sakaiproject.entitybroker.util.refmap.ReferenceMap;
import org.sakaiproject.entitybroker.util.refmap.ReferenceType;

/**
 * Base implementation of the entity provider manager
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class EntityProviderManagerImpl implements EntityProviderManager {

   private static Log log = LogFactory.getLog(EntityProviderManagerImpl.class);

   private RequestStorage requestStorage;
   public void setRequestStorage(RequestStorage requestStorage) {
      this.requestStorage = requestStorage;
   }

   private RequestGetter requestGetter;
   public void setRequestGetter(RequestGetter requestGetter) {
      this.requestGetter = requestGetter;
   }

   private EntityPropertiesService entityProperties;
   public void setEntityProperties(EntityPropertiesService entityProperties) {
      this.entityProperties = entityProperties;
   }

   private EntityActionsManager entityActionsManager;
   public void setEntityActionsManager(EntityActionsManager entityActionsManager) {
      this.entityActionsManager = entityActionsManager;
   }

   protected Map<String, EntityProvider> prefixMap = new ReferenceMap<String, EntityProvider>(ReferenceType.STRONG, ReferenceType.WEAK);

   // old CHMs were switched to RMs to avoid holding strong references and allowing clean classloader unloads
// protected ConcurrentMap<String, EntityProvider> prefixMap = new ConcurrentHashMap<String, EntityProvider>();
// protected ConcurrentMap<String, ReferenceParseable> parseMap = new ConcurrentHashMap<String, ReferenceParseable>();


   public void init() {
      log.info("init");
      // register the describe prefix to reserve it and load up the describe properties
      registerEntityProvider(
            new DescribePropertiesable() {
               public String getEntityPrefix() {
                  return EntityRequestHandler.DESCRIBE;
               }
               public String getBaseName() {
                  return getEntityPrefix();
               }
               public ClassLoader getResourceClassLoader() {
                  return EntityProviderManagerImpl.class.getClassLoader();
               }
            }
      );
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.managers.EntityProviderManager#getProviderByPrefix(java.lang.String)
    */
   public EntityProvider getProviderByPrefix(String prefix) {
      EntityProvider provider = getProviderByPrefixAndCapability(prefix, CoreEntityProvider.class);
      if (provider == null) {
         provider = getProviderByPrefixAndCapability(prefix, EntityProvider.class);
      }
      return provider;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#getProviderByPrefixAndCapability(java.lang.String, java.lang.Class)
    */
   @SuppressWarnings("unchecked")
   public <T extends EntityProvider> T getProviderByPrefixAndCapability(String prefix, Class<T> capability) {
      T provider = null;
      if (capability == null) {
         throw new NullPointerException("capability cannot be null");
      }
      String bikey = getBiKey(prefix, capability);
      provider = (T) prefixMap.get(bikey);
      return provider;
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#getRegisteredPrefixes()
    */
   public Set<String> getRegisteredPrefixes() {
      Set<String> togo = new HashSet<String>();
      for (String bikey : prefixMap.keySet()) {
         togo.add(getPrefix(bikey));
      }
      return togo;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#getPrefixCapabilities(java.lang.String)
    */
   public List<Class<? extends EntityProvider>> getPrefixCapabilities(String prefix) {
      List<Class<? extends EntityProvider>> caps = new ArrayList<Class<? extends EntityProvider>>();
      ArrayList<String> list = new ArrayList<String>( prefixMap.keySet() );
      Collections.sort(list);
      boolean found = false;
      for (String bikey : list) {
         String keyPrefix = getPrefix(bikey);
         if (keyPrefix.equals(prefix)) {
            found = true;
            caps.add( getCapability(bikey) );
         } else {
            // don't keep going though the list if we already found the block of prefix matches
            if (found) break;
         }
      }
      return caps;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#getRegisteredEntityCapabilities()
    */
   public Map<String, List<Class<? extends EntityProvider>>> getRegisteredEntityCapabilities() {
      Map<String, List<Class<? extends EntityProvider>>> m = new HashMap<String, List<Class<? extends EntityProvider>>>();
      ArrayList<String> list = new ArrayList<String>( prefixMap.keySet() );
      Collections.sort(list);
      for (String bikey : list) {
         String prefix = getPrefix(bikey);
         if (! m.containsKey(prefix)) {
            m.put(prefix, new ArrayList<Class<? extends EntityProvider>>());
         }
         m.get(prefix).add( getCapability(bikey) );
      }      
      return m;
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#registerEntityProvider(org.sakaiproject.entitybroker.entityprovider.EntityProvider)
    */
   public void registerEntityProvider(EntityProvider entityProvider) {
      String prefix = new String( entityProvider.getEntityPrefix() ); // copy to make sure this string is in the EB classloader
      new EntityReference(prefix, ""); // this checks the prefix is valid
      // we are now registering describe ourselves
//      if (EntityRequestHandler.DESCRIBE.equals(prefix)) {
//         throw new IllegalArgumentException(EntityRequestHandler.DESCRIBE + " is a reserved prefix, it cannot be used");
//      }
      List<Class<? extends EntityProvider>> superclasses = extractCapabilities(entityProvider);
      int count = 0;
      for (Class<? extends EntityProvider> superclazz : superclasses) {
         registerPrefixCapability(prefix, superclazz, entityProvider);
         count++;
         // special handling for certain EPs if needed
         if (superclazz.equals(RequestAware.class)) {
            // need to shove in the requestGetter on registration
            ((RequestAware)entityProvider).setRequestGetter(requestGetter);
         } else if (superclazz.equals(RequestStorable.class)) {
            // need to shove in the request storage on registration
            ((RequestStorable)entityProvider).setRequestStorage(requestStorage);
         } else if (superclazz.equals(ActionsExecutable.class)) {
            // register the custom actions
            CustomAction[] customActions = ((ActionsExecutable)entityProvider).defineActions();
            if (customActions == null) {
               log.warn("ActionsExecutable: defineActions returns null, it should return an array of custom actions");
            } else {
               Map<String,CustomAction> actions = new HashMap<String, CustomAction>();
               for (CustomAction customAction : customActions) {
                  String action = customAction.action;
                  if (action == null || "".equals(action) || EntityRequestHandler.DESCRIBE.equals(action)) {
                     throw new IllegalStateException("action keys cannot be null, '', or "
                           +EntityRequestHandler.DESCRIBE+", invalid custom action defined in defineActions");
                  }
                  actions.put(action, customAction);
               }
               entityActionsManager.setCustomActions(prefix, actions);
            }
         } else if (superclazz.equals(Describeable.class)) {
            // need to load up the default properties into the cache
            if (! superclasses.contains(DescribePropertiesable.class)) {
               // only load if the props are not defined elsewhere
               ClassLoader cl = entityProvider.getClass().getClassLoader();
               entityProperties.loadProperties(prefix, null, cl);
            }
         } else if (superclazz.equals(DescribePropertiesable.class)) {
            // load up the properties from the provided CL and basename
            ClassLoader cl = ((DescribePropertiesable)entityProvider).getResourceClassLoader();
            String baseName = ((DescribePropertiesable)entityProvider).getBaseName();
            entityProperties.loadProperties(prefix, baseName, cl);
         }
      }
      log.info("EntityBroker: Registered entity provider ("+entityProvider.getClass().getName()
            +") prefix ("+prefix+") with "+count+" capabilities");
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityProviderManager#unregisterEntityBroker(org.sakaiproject.entitybroker.EntityProvider)
    */
   public void unregisterEntityProvider(EntityProvider entityProvider) {
      final String prefix = entityProvider.getEntityPrefix();
      if (EntityRequestHandler.DESCRIBE.equals(prefix)) {
         throw new IllegalArgumentException(EntityRequestHandler.DESCRIBE + " is a reserved prefix, it cannot be unregistered");
      }
      List<Class<? extends EntityProvider>> superclasses = extractCapabilities(entityProvider);
      int count = 0;
      for (Class<? extends EntityProvider> capability : superclasses) {
         // ensure that the root EntityProvider is never absent from the map unless
         // there is a call to unregisterEntityProviderByPrefix
         if (EntityProvider.class.equals(capability)) {
            if (getProviderByPrefixAndCapability(prefix, EntityProvider.class) != null) {
               // needed to ensure that we always have at LEAST the base level EP for a registered entity
               registerEntityProvider(new EntityProvider() {
                  public String getEntityPrefix() {
                     return prefix;
                  }
               });
            }
         } else {
            unregisterCapability(prefix, capability);
            count++;
         }
      }
      // clean up the properties cache
      entityProperties.unloadProperties(prefix);

      log.info("EntityBroker: Unregistered entity provider ("+entityProvider.getClass().getName()+") and "+count+" capabilities");
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#unregisterEntityProviderCapability(java.lang.String,
    *      java.lang.Class)
    */
   public void unregisterCapability(String prefix, Class<? extends EntityProvider> capability) {
      if (capability == EntityProvider.class) {
         throw new IllegalArgumentException(
               "Cannot separately unregister root EntityProvider capability - use unregisterEntityProviderByPrefix instead");
      }
      String key = getBiKey(prefix, capability);
      prefixMap.remove(key);
      // do any cleanup that needs to be done when unregistering
      if (ActionsExecutable.class.equals(capability)) {
         // clean up the list of custom actions
         entityActionsManager.removeCustomActions(prefix);
      } else if (Describeable.class.isAssignableFrom(capability)) {
         // clean up properties cache
         entityProperties.unloadProperties(prefix);
      }
      log.info("EntityBroker: Unregistered entity provider capability ("+capability.getName()+") for prefix ("+prefix+")");
   }

   /*
    * (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityProviderManager#unregisterEntityProviderByPrefix(java.lang.String)
    */
   public void unregisterEntityProviderByPrefix(String prefix) {
      if (prefix == null) {
         throw new NullPointerException("prefix cannot be null");
      }
      for (String bikey : prefixMap.keySet()) {
         String keypref = getPrefix(bikey);
         if (keypref.equals(prefix)) {
            prefixMap.remove(bikey);
         }
      }
      log.info("EntityBroker: Unregistered entity prefix ("+prefix+")");
   }

   /**
    * Allows for easy registration of a prefix and capability
    * 
    * @param prefix
    * @param capability
    * @param provider
    * @return true if the provider is newly registered, false if it was already registered
    */
   public boolean registerPrefixCapability(String prefix,
         Class<? extends EntityProvider> capability, EntityProvider entityProvider) {
      String key = getBiKey(prefix, capability);
      return prefixMap.put(key, entityProvider) == null;
   }

   /**
    * @deprecated use {@link #getProviderByPrefix(String)} instead
    */
   public EntityProvider getProviderByReference(String reference) {
      EntityReference ref = new EntityReference(reference);
      return getProviderByPrefix(ref.prefix);
   }


   // STATICS

   // BIKEY methods
   protected static String getBiKey(String prefix, Class<? extends EntityProvider> clazz) {
      return prefix + "/" + clazz.getName();
   }

   protected static String getPrefix(String bikey) {
      int slashpos = bikey.indexOf('/');
      return bikey.substring(0, slashpos);
   }

   @SuppressWarnings("unchecked")
   protected static Class<? extends EntityProvider> getCapability(String bikey) {
      int slashpos = bikey.indexOf('/');
      String className = bikey.substring(slashpos + 1);
      Class<?> c;
      try {
         c = Class.forName(className);
      } catch (ClassNotFoundException e) {
         throw new RuntimeException("Could not get Class from classname: " + className);
      }
      return (Class<? extends EntityProvider>) c;
   }

   // OTHER

   /**
    * Get the capabilities implemented by this provider
    * 
    * @param provider
    * @return
    */
   @SuppressWarnings("unchecked")
   protected static List<Class<? extends EntityProvider>> extractCapabilities(EntityProvider provider) {
      List<Class<?>> superclasses = ReflectUtil.getSuperclasses(provider.getClass());
      Set<Class<? extends EntityProvider>> capabilities = new HashSet<Class<? extends EntityProvider>>();

      for (Class<?> superclazz : superclasses) {
         if (superclazz.isInterface() && EntityProvider.class.isAssignableFrom(superclazz)) {
            capabilities.add((Class<? extends EntityProvider>) superclazz);
         }
      }
      return new ArrayList<Class<? extends EntityProvider>>(capabilities);
   }

}
