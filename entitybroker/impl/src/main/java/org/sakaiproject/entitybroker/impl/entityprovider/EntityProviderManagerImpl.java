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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base implementation of the entity provider manager
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
@Slf4j
public class EntityProviderManagerImpl implements EntityProviderManager {

    @Setter @Getter private RequestStorageWrite requestStorage;
    @Setter @Getter private RequestGetterWrite requestGetter;
    @Setter @Getter private EntityPropertiesService entityProperties;
    @Setter @Getter private EntityProviderMethodStore entityProviderMethodStore;
    @Setter private ServerConfigurationService serverConfigurationService;

    private boolean filterServices = false;
    private Set<String> allowedServices;

    // Copy-on-write via AtomicReference: reads get a consistent snapshot via get(),
    // writes use a CAS loop to replace the map without blocking readers.
    // WeakReference values prevent cross-webapp classloader leaks: if a provider from another
    // webapp fails to deregister on reload, the weak reference allows GC to reclaim it.
    private final AtomicReference<Map<String, WeakReference<EntityProvider>>> providerMapRef = new AtomicReference<>(Collections.emptyMap());
    private final AtomicReference<Map<String, WeakReference<EntityProviderListener<? extends EntityProvider>>>> listenerMapRef = new AtomicReference<>(Collections.emptyMap());

    /**
     * Base constructor
     * @param requestStorage the request storage service (writeable)
     * @param requestGetter the request getter service
     * @param entityProperties the entity properties service
     * @param entityProviderMethodStore the provider method storage
     */
    public EntityProviderManagerImpl(RequestStorageWrite requestStorage, RequestGetterWrite requestGetter,
            EntityPropertiesService entityProperties, EntityProviderMethodStore entityProviderMethodStore) {
        this.requestStorage = requestStorage;
        this.requestGetter = requestGetter;
        this.entityProperties = entityProperties;
        this.entityProviderMethodStore = entityProviderMethodStore;
        init();
    }

    public void init() {
        //SAK-27902 list of allowed services (prefixes) (default: all services registered. Only set this property if you want to filter the allowed services)
        allowedServices = new HashSet<>();
        String allowedServicesConfig = null;
        if (serverConfigurationService != null) {
            allowedServicesConfig = serverConfigurationService.getString("entitybroker.allowed.services");
        }

        if(allowedServicesConfig != null && !allowedServicesConfig.isEmpty()) {
            filterServices = true;

            // clean the list
            String[] prefixes = allowedServicesConfig.split(",");
            for (String prefix : prefixes) {
                String cleanedPrefix = prefix.trim();
                if (!cleanedPrefix.isEmpty()) {
                    allowedServices.add(cleanedPrefix);
                }
            }
            // must have describe in the list
            allowedServices.add(EntityRequestHandler.DESCRIBE);

            log.info("allowed services: {}", allowedServices);

        }

        // register the describe prefix to reserve them
        registerEntityProvider(() -> EntityRequestHandler.DESCRIBE);
    }

    public void destroy() {
        unRegistrarAllProvidersAndListeners();
    }

    @Override
    public EntityProvider getProviderByPrefix(String prefix) {
        EntityProvider provider = getProviderByPrefixAndCapability(prefix, CoreEntityProvider.class);
        if (provider == null) {
            provider = getProviderByPrefixAndCapability(prefix, EntityProvider.class);
        }
        return provider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EntityProvider> T getProviderByPrefixAndCapability(String prefix, Class<T> capability) {
        T provider;
        if (capability == null) {
            throw new NullPointerException("capability cannot be null");
        }
        String bikey = getBiKey(prefix, capability);
        WeakReference<EntityProvider> ref = providerMapRef.get().get(bikey);
        provider = ref != null ? (T) ref.get() : null;
        return provider;
    }

    @Override
    public Set<String> getRegisteredPrefixes() {
        return providerMapRef.get().entrySet().stream()
                .filter(e -> e.getValue().get() != null)
                .map(e -> EntityProviderManagerImpl.getPrefix(e.getKey()))
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public List<Class<? extends EntityProvider>> getPrefixCapabilities(String prefix) {
        List<Class<? extends EntityProvider>> caps = new ArrayList<>();
        for (Entry<String, WeakReference<EntityProvider>> entry : providerMapRef.get().entrySet()) {
            String bikey = entry.getKey();
            if (entry.getValue().get() == null) continue;
            String curPrefix = EntityProviderManagerImpl.getPrefix(bikey);
            if (curPrefix.equals(prefix)) {
                try {
                    Class<? extends EntityProvider> capability = getCapability(bikey);
                    caps.add( capability );
                } catch (RuntimeException e) {
                    // added because there will be times where we cannot resolve capabilities
                    // because of shifting ClassLoaders or CL visibility and that should not cause this to die
                    log.warn("getPrefixCapabilities: Unable to retrieve class for capability bikey [{}], skipping this capability", bikey);
                }
            }
        }
        caps.sort(Comparator.comparing(Class::getName));
        return caps;
    }

    @Override
    public Map<String, List<Class<? extends EntityProvider>>> getRegisteredEntityCapabilities() {
        Map<String, List<Class<? extends EntityProvider>>> m = new HashMap<>();
        for (Entry<String, WeakReference<EntityProvider>> entry : providerMapRef.get().entrySet()) {
            String bikey = entry.getKey();
            if (entry.getValue().get() == null) continue;
            String prefix = getPrefix(bikey);
            try {
                Class<? extends EntityProvider> capability = getCapability(bikey);
                m.computeIfAbsent(prefix, k -> new ArrayList<>()).add(capability);
            } catch (RuntimeException e) {
                // added because there will be times where we cannot resolve capabilities
                // because of shifting ClassLoaders or CL visibility and that should not cause this to die
                log.warn("Unable to retrieve class for capability bikey [{}], skipping this capability", bikey);
            }
        }
        return m;
    }

    @Override
    @SuppressWarnings("unchecked") // Safe: filtered by capability name which matches the requested type
    public <T extends EntityProvider> List<T> getProvidersByCapability(Class<T> capability) {
        String capName = capability.getName();
        return providerMapRef.get().entrySet().stream()
                .filter(entry -> capName.equals(EntityProviderManagerImpl.getCapabilityName(entry.getKey())))
                .map(entry -> (T) entry.getValue().get())
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(EntityProvider::getEntityPrefix))
                .toList();
    }

    @Override
    public <T extends EntityProvider> List<String> getPrefixesByCapability(Class<T> capability) {
        String capName = capability.getName();
        return providerMapRef.get().entrySet().stream()
                .filter(e -> capName.equals(EntityProviderManagerImpl.getCapabilityName(e.getKey())))
                .filter(e -> e.getValue().get() != null)
                .map(e -> EntityProviderManagerImpl.getPrefix(e.getKey()))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public void registerEntityProvider(EntityProvider entityProvider) {
        if (entityProvider == null) throw new IllegalArgumentException("entityProvider cannot be null");

        String prefix = entityProvider.getEntityPrefix();
        new EntityReference(prefix, ""); // this checks the prefix is valid
        List<Class<? extends EntityProvider>> superClasses = extractCapabilities(entityProvider);

        // Collect all entries to add and perform side-effect setup before updating the map
        Map<String, WeakReference<EntityProvider>> providersToAdd = new HashMap<>();
        int count = 0;
        for (Class<? extends EntityProvider> superClass : superClasses) {
        	// if filtering and prefix not in list, skip registration
            if (filterServices && !allowedServices.contains(prefix)) {
                continue;
            }

            String key = getBiKey(prefix, superClass);
            providersToAdd.put(key, new WeakReference<>(entityProvider));
            count++;

            // special handling for certain EPs if needed
            if (superClass.equals(RequestAware.class)) {
                // need to shove in the requestGetter on registration
                ((RequestAware)entityProvider).setRequestGetter(requestGetter);
            } else if (superClass.equals(RequestStorable.class)) {
                // need to shove in the request storage on registration
                ((RequestStorable)entityProvider).setRequestStorage(requestStorage);
            } else if (superClass.equals(ActionsExecutable.class)) {
                // register the custom actions
                CustomAction[] customActions;
                if (superClasses.contains(ActionsExecutionControllable.class) || superClasses.contains(ActionsDefineable.class)) {
                    customActions = ((ActionsDefineable)entityProvider).defineActions();
                    if (customActions == null) {
                        throw new IllegalArgumentException("ActionsExecutable: defineActions returns null, " +
                        "it must return an array of custom actions (or you can use ActionsExecutable)");
                    }
                    if (!superClasses.contains(ActionsExecutionControllable.class)) {
                        // do the actions defineable validation check
                        EntityProviderMethodStoreImpl.validateCustomActionMethods((ActionsDefineable)entityProvider);
                    }
                } else {
                    // auto detect the custom actions
                    customActions = entityProviderMethodStore.findCustomActions(entityProvider, true);
                }
                // register the actions
                Map<String,CustomAction> actions = new HashMap<>();
                for (CustomAction customAction : customActions) {
                    String action = customAction.action;
                    if (action == null || action.isEmpty() || EntityRequestHandler.DESCRIBE.equals(action)) {
                        throw new IllegalStateException("action keys cannot be null, '', or "
                                +EntityRequestHandler.DESCRIBE+", invalid custom action defined in defineActions");
                    }
                    actions.put(action, customAction);
                }
                entityProviderMethodStore.storeCustomActions(prefix, actions);
            } else if (superClass.equals(Describeable.class)) {
                // need to load up the default properties into the cache
                if (! superClasses.contains(DescribePropertiesable.class)) {
                    // only load if the props are not defined elsewhere
                    ClassLoader cl = entityProvider.getClass().getClassLoader();
                    entityProperties.loadProperties(prefix, null, cl);
                }
            } else if (superClass.equals(DescribePropertiesable.class)) {
                // load up the properties from the provided CL and basename
                ClassLoader cl = ((DescribePropertiesable)entityProvider).getResourceClassLoader();
                String baseName = ((DescribePropertiesable)entityProvider).getBaseName();
                entityProperties.loadProperties(prefix, baseName, cl);
            } else if (superClass.equals(Redirectable.class)) {
                URLRedirect[] redirects = entityProviderMethodStore.findURLRedirectMethods(entityProvider);
                entityProviderMethodStore.addURLRedirects(prefix, redirects);
            } else if (superClass.equals(RedirectDefinable.class)) {
                URLRedirect[] redirects = EntityProviderMethodStoreImpl.validateDefineableTemplates((RedirectDefinable)entityProvider);
                entityProviderMethodStore.addURLRedirects(prefix, redirects);
            } else if (superClass.equals(RedirectControllable.class)) {
                URLRedirect[] redirects = EntityProviderMethodStoreImpl.validateControllableTemplates((RedirectControllable)entityProvider);
                entityProviderMethodStore.addURLRedirects(prefix, redirects);
            }
        }

        // CAS loop: atomically publish all new capabilities at once
        if (!providersToAdd.isEmpty()) {
            Map<String, WeakReference<EntityProvider>> current, next;
            do {
                current = providerMapRef.get();
                next = new HashMap<>(current);
                next.putAll(providersToAdd);
            } while (!providerMapRef.compareAndSet(current, Collections.unmodifiableMap(next)));
        }

        log.info("Registered entity provider [{}], prefix [{}] with {} capabilities", entityProvider.getClass().getName(), prefix, count);

        // call the registered listeners
        listenerMapRef.get().values().stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .forEach(providerListener -> callListener(providerListener, entityProvider));
    }

    @Override
    public void unregisterEntityProvider(EntityProvider entityProvider) {
        final String prefix = entityProvider.getEntityPrefix();
        if (EntityRequestHandler.DESCRIBE.equals(prefix)) {
            throw new IllegalArgumentException(EntityRequestHandler.DESCRIBE + " is a reserved prefix, it cannot be unregistered");
        }
        List<Class<? extends EntityProvider>> superclasses = extractCapabilities(entityProvider);

        boolean hadBaseProvider = getProviderByPrefixAndCapability(prefix, EntityProvider.class) != null;

        // Collect all keys and capabilities to process, skipping the base EntityProvider
        List<String> keysToRemove = new ArrayList<>();
        List<Class<? extends EntityProvider>> capabilitiesToClean = new ArrayList<>();
        for (Class<? extends EntityProvider> capability : superclasses) {
            if (!EntityProvider.class.equals(capability)) {
                keysToRemove.add(getBiKey(prefix, capability));
                capabilitiesToClean.add(capability);
            }
        }

        // CAS loop: atomically remove all capabilities at once
        if (!keysToRemove.isEmpty()) {
            Map<String, WeakReference<EntityProvider>> current, next;
            do {
                current = providerMapRef.get();
                next = new HashMap<>(current);
                keysToRemove.forEach(next::remove);
            } while (!providerMapRef.compareAndSet(current, Collections.unmodifiableMap(next)));
        }

        // Perform capability-specific cleanup after the map update
        for (Class<? extends EntityProvider> capability : capabilitiesToClean) {
            if (ActionsExecutable.class.equals(capability)) {
                entityProviderMethodStore.removeCustomActions(prefix);
            } else if (Redirectable.class.isAssignableFrom(capability)) {
                entityProviderMethodStore.removeURLRedirects(prefix);
            }
        }

        // clean up the properties cache
        entityProperties.unloadProperties(prefix);

        // ensure that the root EntityProvider is never absent from the map unless
        // there is a call to unregisterEntityProviderByPrefix
        if (hadBaseProvider) {
            registerEntityProvider(() -> prefix);
        }

        log.info("Unregistered entity provider [{}] and [{}] capabilities", entityProvider.getClass().getName(), keysToRemove.size());
    }

    public void unregisterCapability(String prefix, Class<? extends EntityProvider> capability) {
        if (prefix == null || capability == null) {
            throw new IllegalArgumentException("prefix and capability cannot be null");
        }
        if (EntityProvider.class.equals(capability)) {
            throw new IllegalArgumentException("Cannot separately unregister root EntityProvider capability - use unregisterEntityProviderByPrefix instead");
        }
        String key = getBiKey(prefix, capability);

        // CAS loop: atomically remove a single capability
        Map<String, WeakReference<EntityProvider>> current, next;
        do {
            current = providerMapRef.get();
            next = new HashMap<>(current);
            next.remove(key);
        } while (!providerMapRef.compareAndSet(current, Collections.unmodifiableMap(next)));

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
        log.info("Unregistered entity provider capability [{}] for prefix [{}]", capability.getName(), prefix);
    }

    @Override
    public void unregisterEntityProviderByPrefix(String prefix) {
        if (prefix == null) {
            throw new NullPointerException("prefix cannot be null");
        }

        // CAS loop: atomically remove all capabilities for this prefix
        Map<String, WeakReference<EntityProvider>> current, next;
        do {
            current = providerMapRef.get();
            List<String> keysToRemove = current.keySet().stream()
                    .filter(bikey -> getPrefix(bikey).equals(prefix))
                    .toList();
            if (keysToRemove.isEmpty()) {
                return;
            }
            next = new HashMap<>(current);
            keysToRemove.forEach(next::remove);
        } while (!providerMapRef.compareAndSet(current, Collections.unmodifiableMap(next)));

        log.info("Unregistered entity prefix [{}]", prefix);
    }

    @Override
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

        // CAS loop: atomically add the listener
        Map<String, WeakReference<EntityProviderListener<?>>> current, next;
        do {
            current = listenerMapRef.get();
            next = new HashMap<>(current);
            next.put(key, new WeakReference<>(listener));
        } while (!listenerMapRef.compareAndSet(current, Collections.unmodifiableMap(next)));

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

    @SuppressWarnings("unchecked")
    private void callListener(EntityProviderListener<?> providerListener, EntityProvider entityProvider) {
        // get the filter values
        String prefix = providerListener.getPrefixFilter();
        Class<? extends EntityProvider> capability = providerListener.getCapabilityFilter();
        // call the listener and give it the provider
        if (capability == null && prefix == null) {
            // any
            ((EntityProviderListener<EntityProvider>) providerListener).run(entityProvider);
        } else if (capability == null) {
            // by prefix only
            if (prefix.equals(entityProvider.getEntityPrefix())) {
                ((EntityProviderListener<EntityProvider>) providerListener).run(entityProvider);
            }
        } else if (prefix == null) {
            // by capability only
            if (capability.isAssignableFrom(entityProvider.getClass())) {
                ((EntityProviderListener<EntityProvider>) providerListener).run(entityProvider);
            }
        } else {
            // by prefix and capability only
            if (prefix.equals(entityProvider.getEntityPrefix())
                    && capability.isAssignableFrom(entityProvider.getClass())) {
                ((EntityProviderListener<EntityProvider>) providerListener).run(entityProvider);
            }
        }
    }

    public <T extends EntityProvider> void unregisterListener(EntityProviderListener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        // CAS loop: find the listener by equality then atomically remove it
        Map<String, WeakReference<EntityProviderListener<?>>> current, next;
        do {
            current = listenerMapRef.get();
            if (current.isEmpty()) {
                return;
            }
            String key = null;
            for (Entry<String, WeakReference<EntityProviderListener<?>>> entry : current.entrySet()) {
                if (listener.equals(entry.getValue().get())) {
                    key = entry.getKey();
                    break;
                }
            }
            if (key == null) {
                return;
            }
            next = new HashMap<>(current);
            next.remove(key);
        } while (!listenerMapRef.compareAndSet(current, Collections.unmodifiableMap(next)));
    }

    @Override
    public void unRegistrarAllProvidersAndListeners() {
        // Take snapshots of current state before clearing
        Map<String, WeakReference<EntityProvider>> currentProviders = providerMapRef.get();
        Map<String, WeakReference<EntityProviderListener<? extends EntityProvider>>> currentListeners = listenerMapRef.get();

        // Perform all cleanup operations on snapshots (safe - maps are unmodifiable)
        Set<String> processedPrefixes = new HashSet<>();
        for (String bikey : currentProviders.keySet()) {
            String prefix = getPrefix(bikey);

            // Only process each prefix once for property cleanup
            if (!processedPrefixes.contains(prefix)) {
                entityProperties.unloadProperties(prefix);
                processedPrefixes.add(prefix);
            }

            // Perform capability-specific cleanup
            try {
                Class<? extends EntityProvider> capability = getCapability(bikey);

                if (ActionsExecutable.class.equals(capability)) {
                    entityProviderMethodStore.removeCustomActions(prefix);
                } else if (Redirectable.class.isAssignableFrom(capability)) {
                    entityProviderMethodStore.removeURLRedirects(prefix);
                }
            } catch (RuntimeException e) {
                log.warn("Failed to get capability for cleanup during reset, bikey [{}]: {}", bikey, e.getMessage());
            }
        }

        // Atomically replace both maps with empty ones
        providerMapRef.set(Collections.emptyMap());
        listenerMapRef.set(Collections.emptyMap());

        log.info("Reset provider manager: cleared {} providers and {} listeners",
            currentProviders.size(), currentListeners.size());
    }


    protected static String getBiKey(String prefix, Class<? extends EntityProvider> clazz) {
        return prefix + "/" + clazz.getName();
    }

    protected static String getPrefix(String bikey) {
        int slashpos = bikey.indexOf('/');
        return bikey.substring(0, slashpos);
    }

    protected static String getCapabilityName(String bikey) {
        int slashpos = bikey.indexOf('/');
        return bikey.substring(slashpos + 1);
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

    /**
     * Get the capabilities (EntityProvider interfaces) implemented by this provider.
     * Examines the provider's class hierarchy to extract all interfaces that extend EntityProvider.
     *
     * @param provider the entity provider to examine
     * @return a list of all EntityProvider interface classes implemented by the provider, with duplicates removed
     */
    @SuppressWarnings("unchecked") // Safe: filtered to only include classes assignable to EntityProvider
    protected static List<Class<? extends EntityProvider>> extractCapabilities(EntityProvider provider) {
        List<Class<?>> superClasses = getSuperClasses(provider.getClass());
        return superClasses.stream()
                .filter(c -> c.isInterface() && EntityProvider.class.isAssignableFrom(c))
                .map(c -> (Class<? extends EntityProvider>) c)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<Class<?>> getSuperClasses(Class<?> clazz) {
        Set<Class<?>> superclasses = new LinkedHashSet<>();
        Queue<Class<?>> queue = new LinkedList<>();
        queue.add(clazz);

        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            if (current != null && superclasses.add(current)) {
                // Add superclass
                if (current.getSuperclass() != null) {
                    queue.add(current.getSuperclass());
                }
                // Add all interfaces (which will be recursively processed)
                Collections.addAll(queue, current.getInterfaces());
            }
        }

        return new ArrayList<>(superclasses);
    }
}
