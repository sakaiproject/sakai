/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.component.cover;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.sakaiproject.component.impl.MockCompMgr;
import org.sakaiproject.component.impl.SpringCompMgr;

/**
 * <p>
 * ComponentManager is a static Cover for the
 * {@link org.sakaiproject.component.api.ComponentManager Component Manager};
 * see that interface for usage details.
 * </p>
 * <p>
 * This cover is special. As a cover for the component manager, it cannot use
 * the component manager to find the instance. Instead, this is where a static
 * single-instance singleton ComponentManger of a particular type is created.
 * </p>
 */
@SuppressWarnings("unchecked")
public class ComponentManager {
	/** A component manager - use the Spring based one. */
	protected static org.sakaiproject.component.api.ComponentManager m_componentManager = null;

	/**
	 * If true, covers will cache the components they find once - good for
	 * production, bad for some unit testing.
	 */
	public static final boolean CACHE_COMPONENTS = true;

	public static java.lang.String SAKAI_COMPONENTS_ROOT_SYS_PROP = org.sakaiproject.component.api.ComponentManager.SAKAI_COMPONENTS_ROOT_SYS_PROP;

	// Making this non-fair works around a bug in JDK-1.5 in which the current thread can't reaquire the lock
	// correctly and so deadlocks.
	private static Lock m_lock = new ReentrantLock(false);

	private static boolean lateRefresh = false;

	/**
	 * Setup the CM in testingMode if this is true (this is to be used for unit tests only),
	 * has no effect if the CM is already initialized
	 */
	public static boolean testingMode = false;

	/**
	 * Prevent Direct Instantiation of the ComponentManager
	 * 
	 * Use the factory method getInstance()
	 */
	private ComponentManager() {
	}

	/**
	 * @return true if this CM is in testing mode
	 */
	public static boolean isTestingMode() {
	    return (m_componentManager == null && testingMode) || m_componentManager instanceof MockCompMgr;
	}
	/**
	 * TESTING ONLY <br/>
	 * closes and then destroys the component manager <br/>
	 * WARNING: this is NOT safe to do in a production system 
	 */
	public static void shutdown() {
		m_lock.lock();
        try {
            if (m_componentManager != null) {
        	    m_componentManager.close();
        	    m_componentManager = null;
            }
        } finally {
        	m_lock.unlock();
        }
	}

	/**
	 * Access the component manager of the single instance.
	 * 
	 * @return The ComponentManager.
	 */
	public static org.sakaiproject.component.api.ComponentManager getInstance() {
		// make sure we make only one instance
		m_lock.lock();
		try {
			// if we do not yet have our component manager instance, create and
			// init / populate it
			if (m_componentManager == null) {
			    if (testingMode) {
			        m_componentManager = new MockCompMgr(false);
			    } else {
			        m_componentManager = new SpringCompMgr(null);
			        ((SpringCompMgr) m_componentManager).init(lateRefresh);
			    }
			}
		} finally {
			m_lock.unlock();
		}

		return m_componentManager;
	}

	public static <T> T get(Class<T> iface) {
		return getInstance().get(iface);
	}

	public static Object get(String ifaceName) {
		return getInstance().get(ifaceName);
	}

	public static boolean contains(Class iface) {
		return getInstance().contains(iface);
	}

	public static boolean contains(String ifaceName) {
		return getInstance().contains(ifaceName);
	}

	public static Set getRegisteredInterfaces() {
		return getInstance().getRegisteredInterfaces();
	}

	public static void loadComponent(Class iface, Object component) {
		getInstance().loadComponent(iface, component);
	}

	public static void loadComponent(String ifaceName, Object component) {
		getInstance().loadComponent(ifaceName, component);
	}

	public static void close() {
		getInstance().close();
	}

	/**
	 * @deprecated This method is redundant, not used by any known client, would
	 *             expose implementation details, and will be removed in a
	 *             future release. Use the ServerConfigurationService instead.
	 */
	@Deprecated
	public static java.util.Properties getConfig() {
		return getInstance().getConfig();
	}

	/**
	 * When the component manager this can be called by a background thread to wait until
	 * the component manager has been configured. If the component manager hasn't yet 
	 * started it will return straight away. If the component manager fails to start it will
	 * return. This is to prevent a deadlock between shutdown and startup.
	 */
	public static void waitTillConfigured() {
		try {
			m_lock.lockInterruptibly();
			m_lock.unlock();
		} catch (InterruptedException e) {
			// We got interrupted and so should shutdown.
		} 
	}

	public static boolean hasBeenClosed() {
		return getInstance().hasBeenClosed();
	}

	public static void setLateRefresh(boolean b) {
		lateRefresh = b;
	}

}
