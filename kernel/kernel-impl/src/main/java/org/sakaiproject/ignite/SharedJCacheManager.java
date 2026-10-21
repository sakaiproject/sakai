/**
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0.
 * http://opensource.org/licenses/ecl2
 */
package org.sakaiproject.ignite;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorResult;
import javax.cache.spi.CachingProvider;

/**
 * A borrowed view of the kernel-owned JCache manager for Hibernate.
 *
 * Hibernate closes even explicitly supplied managers and region cache handles.
 * Those close calls must not stop Sakai's shared node or another SessionFactory's
 * caches. Only the owning Spring bean closes the delegate. All cache operations
 * are delegated through the public JCache API; no Hibernate or Ignite internals
 * are involved. The view's isClosed state follows its owner.
 */
public final class SharedJCacheManager implements CacheManager {
    private final CacheManager delegate;

    public SharedJCacheManager(CacheManager delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        if (delegate.isClosed()) {
            throw new IllegalArgumentException("An open kernel-owned JCache manager is required");
        }
    }

    @Override
    public CachingProvider getCachingProvider() {
        return delegate.getCachingProvider();
    }

    @Override
    public URI getURI() {
        return delegate.getURI();
    }

    @Override
    public ClassLoader getClassLoader() {
        return delegate.getClassLoader();
    }

    @Override
    public Properties getProperties() {
        return delegate.getProperties();
    }

    @Override
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String name, C configuration) {
        return new SharedCache<>(delegate.createCache(name, configuration));
    }

    @Override
    public <K, V> Cache<K, V> getCache(String name, Class<K> keyType, Class<V> valueType) {
        Cache<K, V> cache = delegate.getCache(name, keyType, valueType);
        return cache == null ? null : new SharedCache<>(cache);
    }

    @Override
    public <K, V> Cache<K, V> getCache(String name) {
        Cache<K, V> cache = delegate.getCache(name);
        return cache == null ? null : new SharedCache<>(cache);
    }

    @Override
    public Iterable<String> getCacheNames() {
        return delegate.getCacheNames();
    }

    @Override
    public void destroyCache(String name) {
        delegate.destroyCache(name);
    }

    @Override
    public void enableManagement(String name, boolean enabled) {
        delegate.enableManagement(name, enabled);
    }

    @Override
    public void enableStatistics(String name, boolean enabled) {
        delegate.enableStatistics(name, enabled);
    }

    @Override
    public void close() {
        // Borrowed: Spring closes the owning manager after its consumers stop.
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return type.isInstance(this) ? type.cast(this) : delegate.unwrap(type);
    }

    private final class SharedCache<K, V> implements Cache<K, V> {
        private final Cache<K, V> cache;

        private SharedCache(Cache<K, V> cache) {
            this.cache = cache;
        }

        @Override
        public V get(K key) {
            return cache.get(key);
        }

        @Override
        public Map<K, V> getAll(Set<? extends K> keys) {
            return cache.getAll(keys);
        }

        @Override
        public boolean containsKey(K key) {
            return cache.containsKey(key);
        }

        @Override
        public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener listener) {
            cache.loadAll(keys, replaceExistingValues, listener);
        }

        @Override
        public void put(K key, V value) {
            cache.put(key, value);
        }

        @Override
        public V getAndPut(K key, V value) {
            return cache.getAndPut(key, value);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> values) {
            cache.putAll(values);
        }

        @Override
        public boolean putIfAbsent(K key, V value) {
            return cache.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(K key) {
            return cache.remove(key);
        }

        @Override
        public boolean remove(K key, V value) {
            return cache.remove(key, value);
        }

        @Override
        public V getAndRemove(K key) {
            return cache.getAndRemove(key);
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            return cache.replace(key, oldValue, newValue);
        }

        @Override
        public boolean replace(K key, V value) {
            return cache.replace(key, value);
        }

        @Override
        public V getAndReplace(K key, V value) {
            return cache.getAndReplace(key, value);
        }

        @Override
        public void removeAll(Set<? extends K> keys) {
            cache.removeAll(keys);
        }

        @Override
        public void removeAll() {
            cache.removeAll();
        }

        @Override
        public void clear() {
            cache.clear();
        }

        @Override
        public <C extends Configuration<K, V>> C getConfiguration(Class<C> type) {
            return cache.getConfiguration(type);
        }

        @Override
        public <T> T invoke(K key, EntryProcessor<K, V, T> processor, Object... arguments) {
            return cache.invoke(key, processor, arguments);
        }

        @Override
        public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys,
                EntryProcessor<K, V, T> processor, Object... arguments) {
            return cache.invokeAll(keys, processor, arguments);
        }

        @Override
        public String getName() {
            return cache.getName();
        }

        @Override
        public CacheManager getCacheManager() {
            return SharedJCacheManager.this;
        }

        @Override
        public void close() {
            // Closing a Hibernate region must not close the node's shared cache.
        }

        @Override
        public boolean isClosed() {
            return delegate.isClosed() || cache.isClosed();
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            return type.isInstance(this) ? type.cast(this) : cache.unwrap(type);
        }

        @Override
        public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> configuration) {
            cache.registerCacheEntryListener(configuration);
        }

        @Override
        public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> configuration) {
            cache.deregisterCacheEntryListener(configuration);
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return cache.iterator();
        }
    }
}
