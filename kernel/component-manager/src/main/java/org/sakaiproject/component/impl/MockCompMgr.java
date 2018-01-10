/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/component-manager/src/main/java/org/sakaiproject/component/impl/SpringCompMgr.java $
 * $Id: SpringCompMgr.java 69279 2009-11-27 14:44:01Z stephen.marquard@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.component.impl;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ComponentManager;

/**
 * This is a totally fake component manager which is just going to pretend to try to find services,
 * maybe later this could try to use the sakai mock stuff but for now it is just here to stop the CM from firing up during unit tests
 */
@SuppressWarnings("rawtypes")
@Slf4j
public class MockCompMgr implements ComponentManager {
    /**
     * Startup the CM as a mock system for unit tests
     */
    public MockCompMgr(boolean loadMocks) {
        log.warn("LOADING CM in testing mode... this should only ever happen during unit tests");
        if (loadMocks) {
            // TODO load up mocks of the common services
            log.info("Created a populated CM mock with "+components.size()+" services loaded");
        } else {
            log.info("Created an empty CM mock (no services loaded)");
        }
    }

    /**
     * Not really needed but this allows us to at least put fake ones into this CM for tests if we like
     */
    public ConcurrentHashMap<String, Object> components = new ConcurrentHashMap<String, Object>();

    boolean closed = false;

    public void close() {
        components.clear();
        closed = true;
    }

    public boolean hasBeenClosed() {
        return closed;
    }

    public boolean contains(Class iface) {
        String key = iface != null ? iface.getName() : "";
        return components.contains(key);
    }

    public boolean contains(String ifaceName) {
        String key = ifaceName != null ? ifaceName : "";
        return components.contains(key);
    }

    public <T> T get(Class<T> iface) {
        String key = iface != null ? iface.getName() : "";
        return (T) components.get(key);
    }

    public Object get(String ifaceName) {
        String key = ifaceName != null ? ifaceName : "";
        return components.get(key);
    }

    public Properties getConfig() {
        return null;
    }

    public Set<String> getRegisteredInterfaces() {
        return new HashSet<String>( components.keySet() );
    }

    public void loadComponent(Class iface, Object component) {
        if (iface == null) {
            throw new IllegalArgumentException("Failure while attempting to load component: iface is null");
        }
        if (component != null) {
            components.put(iface.getName(), component);
        } else {
            components.remove(iface.getName());
        }
    }

    public void loadComponent(String ifaceName, Object component) {
        if (ifaceName == null) {
            throw new IllegalArgumentException("Failure while attempting to load component: ifaceName is null");
        }
        if (component != null) {
            components.put(ifaceName, component);
        } else {
            components.remove(ifaceName);
        }
    }

    public void waitTillConfigured() {
        // whatever
    }
}
