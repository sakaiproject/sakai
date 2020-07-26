package org.sakaiproject.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.hibernate.HibernateException;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.cache.internal.StandardQueryCache;
import org.hibernate.cache.spi.QueryKey;
import org.hibernate.cache.spi.UpdateTimestampsCache;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.type.TypeHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IgniteStandardQueryCache extends StandardQueryCache {

    /**
     * Constructs a StandardQueryCache instance
     *
     * @param settings              The SessionFactory settings.
     * @param props                 Any properties
     * @param updateTimestampsCache The update-timestamps cache to use.
     * @param regionName            The base query cache region name
     */
    public IgniteStandardQueryCache(Settings settings, Properties props, UpdateTimestampsCache updateTimestampsCache, String regionName) {
        super(settings, props, updateTimestampsCache, regionName);
    }

    @Override
    public List get(QueryKey key, Type[] returnTypes, boolean isNaturalKeyLookup, Set<Serializable> spaces, SessionImplementor session) throws HibernateException {
        List result;
        try {
            result = super.get(key, returnTypes, isNaturalKeyLookup, spaces, session);
        } catch (ClassCastException cce) {
            log.warn("Ignite cache returned an incorrect type of non Serializable: {}", cce.toString());
            List cacheable = (List) getRegion().get(key);

            if (cacheable == null) {
                return null;
            }

            final boolean singleResult = returnTypes.length == 1;
            if (!singleResult && cacheable.size() > 1) {
                for (int i = 1; i < cacheable.size(); i++) {
                    Object row = cacheable.get(i);
                    if (row != null && row.getClass().isArray()) {
                        Object[] rowArray = (Object[]) row;
                        cacheable.set(i, Arrays.copyOf(rowArray, rowArray.length, Serializable[].class));
                    }
                }
            }

            for (int i = 1; i < cacheable.size(); i++) {
                if (singleResult) {
                    returnTypes[0].beforeAssemble((Serializable) cacheable.get(i), session);
                } else {
                    TypeHelper.beforeAssemble((Serializable[]) cacheable.get(i), returnTypes, session);
                }
            }

            result = new ArrayList(cacheable.size() - 1);
            for (int i = 1; i < cacheable.size(); i++) {
                try {
                    if (singleResult) {
                        result.add(returnTypes[0].assemble((Serializable) cacheable.get(i), session, null));
                    } else {
                        result.add(TypeHelper.assemble((Serializable[]) cacheable.get(i), returnTypes, session, null));
                    }
                } catch (RuntimeException ex) {
                    if (isNaturalKeyLookup) {
                        // potentially perform special handling for natural-id look ups.
                        if (UnresolvableObjectException.class.isInstance(ex) || EntityNotFoundException.class.isInstance(ex)) {
                            getRegion().evict(key);

                            // EARLY EXIT !!!!!
                            return null;
                        }
                    }
                    throw ex;
                }
            }
        }
        return result;
    }
}
