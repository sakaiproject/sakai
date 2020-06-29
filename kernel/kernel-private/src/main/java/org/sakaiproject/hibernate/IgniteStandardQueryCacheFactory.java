package org.sakaiproject.hibernate;

import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.QueryCacheFactory;
import org.hibernate.cache.spi.UpdateTimestampsCache;
import org.hibernate.cfg.Settings;

public class IgniteStandardQueryCacheFactory implements QueryCacheFactory {
    @Override
    public QueryCache getQueryCache(
            final String regionName,
            final UpdateTimestampsCache updateTimestampsCache,
            final Settings settings,
            final Properties props) throws HibernateException {
        return new IgniteStandardQueryCache(settings, props, updateTimestampsCache, regionName);
    }
}
