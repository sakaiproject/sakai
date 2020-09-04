package org.sakaiproject.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.cache.spi.QueryKey;
import org.hibernate.cache.spi.QueryResultsCache;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cache.spi.QuerySpacesHelper;
import org.hibernate.cache.spi.TimestampsCache;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.type.Type;
import org.hibernate.type.TypeHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IgniteQueryResultsCache implements QueryResultsCache {

    private final QueryResultsRegion cacheRegion;
    private final TimestampsCache timestampsCache;

    IgniteQueryResultsCache(QueryResultsRegion cacheRegion, TimestampsCache timestampsCache) {
        this.cacheRegion = cacheRegion;
        this.timestampsCache = timestampsCache;
    }

    @Override
    public QueryResultsRegion getRegion() {
        return cacheRegion;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean put(QueryKey key, List results, Type[] returnTypes, SharedSessionContractImplementor session) throws HibernateException {
        if (log.isDebugEnabled()) {
            log.debug("Caching query results in region: {}; timestamp={}", cacheRegion.getName(), session.getTransactionStartTimestamp());
        }

        final List resultsCopy = CollectionHelper.arrayList(results.size());

        final boolean isSingleResult = returnTypes.length == 1;
        for (Object aResult : results) {
            final Serializable resultRowForCache;
            if (isSingleResult) {
                resultRowForCache = returnTypes[0].disassemble(aResult, session, null);
            } else {
                resultRowForCache = TypeHelper.disassemble((Object[]) aResult, returnTypes, null, session, null);
            }
            resultsCopy.add(resultRowForCache);
        }

        final CacheItem cacheItem = new CacheItem(session.getTransactionStartTimestamp(), resultsCopy);

        try {
            session.getEventListenerManager().cachePutStart();
            cacheRegion.putIntoCache(key, cacheItem, session);
        } finally {
            session.getEventListenerManager().cachePutEnd();
        }

        return true;
    }

    @Override
    public List get(QueryKey key, Set<Serializable> spaces, Type[] returnTypes, SharedSessionContractImplementor session) throws HibernateException {
        return get(key, QuerySpacesHelper.INSTANCE.toStringArray(spaces), returnTypes, session);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List get(QueryKey key, String[] spaces, Type[] returnTypes, SharedSessionContractImplementor session) throws HibernateException {
        if (log.isDebugEnabled()) {
            log.debug("Checking cached query results in region: {}", cacheRegion.getName());
        }

        final CacheItem cacheItem = getCachedData(key, session);
        if (cacheItem == null) {
            log.debug("Query results were not found in cache");
            return null;
        }

        if (!timestampsCache.isUpToDate(spaces, cacheItem.timestamp, session)) {
            log.debug("Cached query results were not up-to-date");
            return null;
        }

        log.debug("Returning cached query results");

        final boolean singleResult = returnTypes.length == 1;
        for (int i = 0; i < cacheItem.results.size(); i++) {
            if (singleResult) {
                returnTypes[0].beforeAssemble((Serializable) cacheItem.results.get(i), session);
            } else {
                try {
                    TypeHelper.beforeAssemble((Serializable[]) cacheItem.results.get(i), returnTypes, session);
                } catch (ClassCastException cce) {
                    // Ignite can cause a ClassCastException for a Serializable[] type
                    Object row = cacheItem.results.get(i);
                    if (row != null && row.getClass().isArray()) {
                        Object[] rowArray = (Object[]) row;
                        cacheItem.results.set(i, Arrays.copyOf(rowArray, rowArray.length, Serializable[].class));
                        TypeHelper.beforeAssemble((Serializable[]) cacheItem.results.get(i), returnTypes, session);
                    }
                }
            }
        }

        return assembleCachedResult(cacheItem.results, singleResult, returnTypes, session);
    }

    private CacheItem getCachedData(QueryKey key, SharedSessionContractImplementor session) {
        CacheItem cachedItem = null;
        try {
            session.getEventListenerManager().cacheGetStart();
            cachedItem = (CacheItem) cacheRegion.getFromCache(key, session);
        } finally {
            session.getEventListenerManager().cacheGetEnd(cachedItem != null);
        }
        return cachedItem;
    }

    @SuppressWarnings("unchecked")
    private List assembleCachedResult(final List cached, boolean singleResult, final Type[] returnTypes, final SharedSessionContractImplementor session) throws HibernateException {

        final List result = new ArrayList(cached.size());
        if (singleResult) {
            for (Object aCached : cached) {
                result.add(returnTypes[0].assemble((Serializable) aCached, session, null));
            }
        } else {
            for (int i = 0; i < cached.size(); i++) {
                result.add(TypeHelper.assemble((Serializable[]) cached.get(i), returnTypes, session, null));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "QueryResultsCache(" + cacheRegion.getName() + ')';
    }

    public static class CacheItem implements Serializable {
        private final long timestamp;
        private final List results;

        CacheItem(long timestamp, List results) {
            this.timestamp = timestamp;
            this.results = results;
        }
    }
}
