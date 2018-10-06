package org.sakaiproject.hibernate;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public class HibernateUtils {
    /**
     * THIS CAN BE REMOVED WHEN SAKAI IS ON HIBERNATE >= 5.2.10
     *
     * Unproxies a {@link HibernateProxy}. If the proxy is uninitialized, it automatically triggers an initialization.
     * In case the supplied object is null or not a proxy, the object will be returned as-is.
     *
     * @param proxy the {@link HibernateProxy} to be unproxied
     * @return the proxy's underlying implementation object, or the supplied object otherwise
     */
    public static Object unproxy(Object proxy) {
        if ( proxy instanceof HibernateProxy ) {
            HibernateProxy hibernateProxy = (HibernateProxy) proxy;
            LazyInitializer initializer = hibernateProxy.getHibernateLazyInitializer();
            return initializer.getImplementation();
        } else {
            return proxy;
        }
    }
}
