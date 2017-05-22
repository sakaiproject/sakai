/**
 * $Id$
 * $URL$
 * EBlogic.java - entity-broker - Apr 15, 2008 4:29:18 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.entitybroker.impl.entityprovider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.refmap.ReferenceMap;
import org.azeckoski.reflectutils.refmap.ReferenceType;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderMethodStore;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsDefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutionControllable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.DescribePropertiesable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectControllable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityProviderListener;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.URLRedirect;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.util.core.EntityProviderMethodStoreImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * Base implementation of the entity provider manager
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
@Slf4j
public class EntityProviderManagerImpl implements EntityProviderManager {

    public void init() {
        log.info("EntityProviderManagerImpl init");
        
        //SAK-27902 list of allowed services (prefixes) (default: all services registered. Only set this property if you want to filter the allowed services)
        allowedServices = new HashSet<String>();
        String allowedServicesConfig = null;
        if (serverConfigurationService != null) {
          allowedServicesConfig = serverConfigurationService.getString("entitybroker.allowed.services");
        }

        if(allowedServicesConfig != null && allowedServicesConfig.length()>0) {
        	filterServices = true;
        	
        	//clean the list
        	String[] prefixes = allowedServicesConfig.split(",");
        	for(int i=0; i< prefixes.length; i++) {
        		String cleanedPrefix = prefixes[i].trim();
        		if(cleanedPrefix.length() > 0){
        			allowedServices.add(cleanedPrefix);
        		}
        	}
        	
        	//must have describe in the list
        	allowedServices.add(EntityRequestHandler.DESCRIBE);
        	
        	log.info("Allowed services: " + allowedServices);

        }
        
        // register the describe prefix to reserve them
        registerEntityProvider(
                new EntityProvider() {
                    public String getEntityPrefix() {
                        return EntityRequestHandler.DESCRIBE;
                    }
                }
        );
    }

    /**
     * Empty constructor
     */
    protected EntityProviderManagerImpl() { }

    /**
     * Base constructor
     * @param requestStorage the request storage service (writeable)
     * @param requestGetter the request getter service
     * @param entityProperties the entity properties service
     * @param entityProviderMethodStore the provider method storage
     */
    public EntityProviderManagerImpl(RequestStorageWrite requestStorage, RequestGetterWrite requestGetter,
            EntityPropertiesService entityProperties, EntityProviderMethodStore entityProviderMethodStore) {
        super();
        this.requestStorage = requestStorage;
        this.requestGetter = requestGetter;
        this.entityProperties = entityProperties;
        this.entityProviderMethodStore = entityProviderMethodStore;
        init();
    }

    private RequestStorageWrite requestStorage;
    private RequestGetterWrite requestGetter;
    private EntityPropertiesService entityProperties;
    private EntityProviderMethodStore entityProviderMethodStore;
    private ServerConfigurationService serverConfigurationService;
    private boolean filterServices = false;
    private Set<String> allowedServices;

    protected ReferenceMap<String, EntityProvider> prefixMap = new ReferenceMap<String, EntityProvider>(ReferenceType.STRONG, ReferenceType.SOFT);

    @SuppressWarnings("unchecked")
    protected ReferenceMap<String, EntityProviderListener> listenerMap = new ReferenceMap<String, EntityProviderListener>(ReferenceType.STRONG, ReferenceType.SOFT);

    // old CHMs were switched to RMs to avoid holding strong references and allowing clean classloader unloads
    // protected ConcurrentMap<String, EntityProvider> prefixMap = new ConcurrentHashMap<String, EntityProvider>();
    // protected ConcurrentMap<String, ReferenceParseable> parseMap = new ConcurrentHashMap<String, ReferenceParseable>();

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
        for (String bikey : prefixMap.keySet()) {
            String curPrefix = EntityProviderManagerImpl.getPrefix(bikey);
            if (curPrefix.equals(prefix)) {
                try {
                    Class<? extends EntityProvider> capability = getCapability(bikey);
                    caps.add( capability );
                } catch (RuntimeException e) {
                    // added because there will be times where we cannot resolve capabilities 
                    // because of shifting ClassLoaders or CL visibility and that should not cause this to die
                    log.warn("getPrefixCapabilities: Unable to retrieve class for capability bikey ("+bikey+"), skipping this capability");
                }
            }
        }
        Collections.sort(caps, new ClassComparator());
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
            try {
                Class<? extends EntityProvider> capability = getCapability(bikey);
                m.get(prefix).add( capability );
            } catch (RuntimeException e) {
                // added because there will be times where we cannot resolve capabilities 
                // because of shifting ClassLoaders or CL visibility and that should not cause this to die
                log.warn("getRegisteredEntityCapabilities: Unable to retrieve class for capability bikey ("+bikey+"), skipping this capability");
            }
        }      
        return m;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#getProvidersByCapability(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T extends EntityProvider> List<T> getProvidersByCapability(Class<T> capability) {
        ArrayList<T> providers = new ArrayList<T>();
        String capName = capability.getName();
        for (Entry<String, EntityProvider> entry : prefixMap.entrySet()) {
            String name = EntityProviderManagerImpl.getCapabilityName(entry.getKey());
            if (capName.equals(name)) {
                providers.add((T)entry.getValue());
            }
        }
        Collections.sort(providers, new EntityProviderComparator());
        return providers;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#getPrefixesByCapability(java.lang.Class)
     */
    public <T extends EntityProvider> List<String> getPrefixesByCapability(Class<T> capability) {
        ArrayList<String> prefixes = new ArrayList<String>();
        String capName = capability.getName();
        for (Entry<String, EntityProvider> entry : prefixMap.entrySet()) {
            String name = EntityProviderManagerImpl.getCapabilityName(entry.getKey());
            if (capName.equals(name)) {
                prefixes.add( EntityProviderManagerImpl.getPrefix(entry.getKey()) );
            }
        }
        Collections.sort(prefixes);
        return prefixes;
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#registerEntityProvider(org.sakaiproject.entitybroker.entityprovider.EntityProvider)
     */
    @SuppressWarnings("unchecked")
    public void registerEntityProvider(EntityProvider entityProvider) {
        if (entityProvider == null) {
            throw new IllegalArgumentException("entityProvider cannot be null");
        }
        String prefix = new String( entityProvider.getEntityPrefix() ); // copy to make sure this string is in the EB ClassLoader
        new EntityReference(prefix, ""); // this checks the prefix is valid
        // we are now registering describe ourselves
        //      if (EntityRequestHandler.DESCRIBE.equals(prefix)) {
        //         throw new IllegalArgumentException(EntityRequestHandler.DESCRIBE + " is a reserved prefix, it cannot be used");
        //      }
        List<Class<? extends EntityProvider>> superclasses = extractCapabilities(entityProvider);
        
        int count = 0;
        for (Class<? extends EntityProvider> superclazz : superclasses) {
        	
        	//if filtering and prefix not in list, skip registration
        	if(filterServices) {
        		if(!allowedServices.contains(prefix)) {
        			continue;
        		}
        	}
        	
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
                CustomAction[] customActions = new CustomAction[0];
                if ( superclasses.contains(ActionsExecutionControllable.class) 
                        || superclasses.contains(ActionsDefineable.class) ) {
                    customActions = ((ActionsDefineable)entityProvider).defineActions();
                    if (customActions == null) {
                        throw new IllegalArgumentException("ActionsExecutable: defineActions returns null, " +
                        "it must return an array of custom actions (or you can use ActionsExecutable)");
                    }
                    if (!superclasses.contains(ActionsExecutionControllable.class)) {
                        // do the actions defineable validation check
                        EntityProviderMethodStoreImpl.validateCustomActionMethods((ActionsDefineable)entityProvider);
                    }
                } else {
                    // auto detect the custom actions
                    customActions = entityProviderMethodStore.findCustomActions(entityProvider, true);
                }
                // register the actions
                Map<String,CustomAction> actions = new HashMap<String, CustomAction>();
                for (CustomAction customAction : customActions) {
                    String action = customAction.action;
                    if (action == null || "".equals(action) || EntityRequestHandler.DESCRIBE.equals(action)) {
                        throw new IllegalStateException("action keys cannot be null, '', or "
                                +EntityRequestHandler.DESCRIBE+", invalid custom action defined in defineActions");
                    }
                    actions.put(action, customAction);
                }
                entityProviderMethodStore.setCustomActions(prefix, actions);
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
            } else if (superclazz.equals(Redirectable.class)) {
                URLRedirect[] redirects = entityProviderMethodStore.findURLRedirectMethods(entityProvider);
                entityProviderMethodStore.addURLRedirects(prefix, redirects);
            } else if (superclazz.equals(RedirectDefinable.class)) {
                URLRedirect[] redirects = EntityProviderMethodStoreImpl.validateDefineableTemplates((RedirectDefinable)entityProvider);
                entityProviderMethodStore.addURLRedirects(prefix, redirects);
            } else if (superclazz.equals(RedirectControllable.class)) {
                URLRedirect[] redirects = EntityProviderMethodStoreImpl.validateControllableTemplates((RedirectControllable)entityProvider);
                entityProviderMethodStore.addURLRedirects(prefix, redirects);
            }
        }
        log.info("Registered entity provider ("+entityProvider.getClass().getName()
                +") prefix ("+prefix+") with "+count+" capabilities");

        // call the registered listeners
        for (Iterator<EntityProviderListener> iterator = listenerMap.values().iterator(); iterator.hasNext();) {
            EntityProviderListener<? extends EntityProvider> providerListener = iterator.next();
            callListener(providerListener, entityProvider);
        }
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

        log.info("Unregistered entity provider ("+entityProvider.getClass().getName()+") and "+count+" capabilities");
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#unregisterEntityProviderCapability(java.lang.String,
     *      java.lang.Class)
     */
    public void unregisterCapability(String prefix, Class<? extends EntityProvider> capability) {
        if (prefix == null || capability == null) {
            throw new IllegalArgumentException("prefix and capability cannot be null");
        }
        if (EntityProvider.class.equals(capability)) {
            throw new IllegalArgumentException(
            "Cannot separately unregister root EntityProvider capability - use unregisterEntityProviderByPrefix instead");
        }
        String key = getBiKey(prefix, capability);
        prefixMap.remove(key);
        // do any cleanup that needs to be done when unregistering
        if (ActionsExecutable.class.equals(capability)) {
            // clean up the list of custom actions
            entityProviderMethodStore.removeCustomActions(prefix);
        } else if (Describeable.class.isAssignableFrom(capability)) {
            // clean up properties record
            entityProperties.unloadProperties(prefix);
        } else if (Redirectable.class.isAssignableFrom(capability)) {
            // clean up the redirect URLs record
            entityProviderMethodStore.removeURLRedirects(prefix);
        }
        log.info("Unregistered entity provider capability ("+capability.getName()+") for prefix ("+prefix+")");
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
        log.info("Unregistered entity prefix ("+prefix+")");
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


    // LISTENERS

    @SuppressWarnings("unchecked")
    public <T extends EntityProvider> void registerListener(EntityProviderListener<T> listener, boolean includeExisting) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        // unregister first
        unregisterListener(listener);
        // get the filter values
        String prefix = listener.getPrefixFilter();
        Class<T> capability = listener.getCapabilityFilter();
        // make the key ensuring it is unique
        String key = prefix + ":" + capability + ":" + UUID.randomUUID();
        // store the listener
        listenerMap.put(key, listener);
        // do the immediate calls if requested
        if (includeExisting) {
            if (capability == null && prefix == null) {
                // all
                List<T> providers = (List<T>) getProvidersByCapability(EntityProvider.class);
                for (T provider : providers) {
                    listener.run(provider);
                }
            } else if (capability == null) {
                // get by prefix
                T provider = (T) getProviderByPrefix(prefix);
                if (provider != null) {
                    listener.run(provider);
                }
            } else if (prefix == null) {
                // get by capability
                List<T> l = getProvidersByCapability(capability);
                for (T provider : l) {
                    listener.run(provider);
                }
            } else {
                // get by prefix and capability
                T provider = getProviderByPrefixAndCapability(prefix, capability);
                if (provider != null) {
                    listener.run(provider);
                }
            }
        }
    }

    /**
     * Called from {@link #registerEntityProvider(EntityProvider)} when registering a provider
     */
    @SuppressWarnings("unchecked")
    private void callListener(EntityProviderListener providerListener, EntityProvider entityProvider) {
        // get the filter values
        String prefix = providerListener.getPrefixFilter();
        Class<? extends EntityProvider> capability = providerListener.getCapabilityFilter();
        // call the listener and give it the provider
        if (capability == null && prefix == null) {
            // any
            providerListener.run(entityProvider);
        } else if (capability == null) {
            // by prefix only
            if (prefix.equals(entityProvider.getEntityPrefix())) {
                providerListener.run(entityProvider);
            }
        } else if (prefix == null) {
            // by capability only
            if (capability.isAssignableFrom(entityProvider.getClass())) {
                providerListener.run(entityProvider);
            }
        } else {
            // by prefix and capability only
            if (prefix.equals(entityProvider.getEntityPrefix()) 
                    && capability.isAssignableFrom(entityProvider.getClass())) {
                providerListener.run(entityProvider);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityProvider> void unregisterListener(EntityProviderListener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        if (! listenerMap.isEmpty()) {
            // try to find by the object equality and then remove
            String key = null;
            for (Entry<String, EntityProviderListener> entry : listenerMap.entrySet()) {
                if (listener.equals(entry.getValue())) {
                    key = entry.getKey();
                    break;
                }
            }
            if (key != null) {
                listenerMap.remove(key);
            }
        }
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

    protected static String getCapabilityName(String bikey) {
        int slashpos = bikey.indexOf('/');
        String className = bikey.substring(slashpos + 1);
        return className;
    }

    @SuppressWarnings("unchecked")
    protected static Class<? extends EntityProvider> getCapability(String bikey) {
        int slashpos = bikey.indexOf('/');
        String className = bikey.substring(slashpos + 1);
        Class<?> c;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            try {
                c = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException("Could not get Class from classname: " + className, e);
            }
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
        List<Class<?>> superclasses = ReflectUtils.getSuperclasses(provider.getClass());
        Set<Class<? extends EntityProvider>> capabilities = new HashSet<Class<? extends EntityProvider>>();

        for (Class<?> superclazz : superclasses) {
            if (superclazz.isInterface() && EntityProvider.class.isAssignableFrom(superclazz)) {
                capabilities.add((Class<? extends EntityProvider>) superclazz);
            }
        }
        return new ArrayList<Class<? extends EntityProvider>>(capabilities);
    }

    public static class EntityProviderComparator implements Comparator<EntityProvider>, Serializable {
        public final static long serialVersionUID = 1l;
        public int compare(EntityProvider o1, EntityProvider o2) {
            return o1.getEntityPrefix().compareTo(o2.getEntityPrefix());
        }
    }

    public static class ClassComparator implements Comparator<Class<?>>, Serializable {
        public final static long serialVersionUID = 1l;
        public int compare(Class<?> o1, Class<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }


    // GETTERS and SETTERS

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#getRequestStorage()
     */
    public RequestStorageWrite getRequestStorage() {
        return requestStorage;
    }

    public void setRequestStorage(RequestStorageWrite requestStorage) {
        this.requestStorage = requestStorage;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#getRequestGetter()
     */
    public RequestGetterWrite getRequestGetter() {
        return requestGetter;
    }

    public void setRequestGetter(RequestGetterWrite requestGetter) {
        this.requestGetter = requestGetter;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#getEntityProperties()
     */
    public EntityPropertiesService getEntityProperties() {
        return entityProperties;
    }

    public void setEntityProperties(EntityPropertiesService entityProperties) {
        this.entityProperties = entityProperties;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProviderManager#getEntityProviderMethodStore()
     */
    public EntityProviderMethodStore getEntityProviderMethodStore() {
        return entityProviderMethodStore;
    }

    public void setEntityProviderMethodStore(EntityProviderMethodStore entityProviderMethodStore) {
        this.entityProviderMethodStore = entityProviderMethodStore;
    }
    
    //setter only, don't want to expose this here
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

}
