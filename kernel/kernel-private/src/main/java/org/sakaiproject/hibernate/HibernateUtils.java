/**
 * Copyright (c) 2003-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
