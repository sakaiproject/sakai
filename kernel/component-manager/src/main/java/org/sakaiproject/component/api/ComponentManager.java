/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.component.api;

import java.util.Properties;
import java.util.Set;

/**
 * <p>
 * ...
 * </p>
 */
public interface ComponentManager
{
	/** The java system property name where the full path to the components packages. */
	public static final String SAKAI_COMPONENTS_ROOT_SYS_PROP = "sakai.components.root";

	/**
	 * Find a component that is registered to provide this interface.
	 * 
	 * @param iface
	 *        The interface Class.
	 * @return a component instance, or null if not found.
	 */
	<T> T get(Class <T> iface);

	/**
	 * Find a component that is registered to provide this interface.
	 * 
	 * @param ifaceName
	 *        The fully qualified interface Class name.
	 * @return a component instance, or null if not found.
	 */
	Object get(String ifaceName);

	/**
	 * Check if this interface Class has a registered component.
	 * 
	 * @param iface
	 *        The interface Class.
	 * @return <strong>true</strong> if this interface Class has a registered component, <strong>false</strong> if not.
	 */
	boolean contains(Class iface);

	/**
	 * Check if this interface Class name has a registered component.
	 * 
	 * @param ifaceName
	 *        The fully qualified interface Class name.
	 * @return <strong>true</strong> if this interface has a registered component, <strong>false</strong> if not.
	 */
	boolean contains(String ifaceName);

	/**
	 * Get all interfaces registered in the component manager.
	 * 
	 * @return A Set (String class name) of all interfaces registered in the component manager.
	 */
	Set<String> getRegisteredInterfaces();

	/**
	 * Load a singleton already created component for this interface class as a singleton.
	 * 
	 * @param iface
	 *        The interface class.
	 * @param component
	 *        The alread created component.
	 */
	void loadComponent(Class iface, Object component);

	/**
	 * Load a singleton already created component for this interface class as a singleton.
	 * 
	 * @param ifaceName
	 *        The fully qualified interface Class name.
	 * @param component
	 *        The alread created component.
	 */
	void loadComponent(String ifaceName, Object component);

	/**
	 * Close the component manager, shutting down any created singletons.
	 */
	void close();

	/**
	 * Access the configuration properties used when configuring components.
	 * 
	 * @deprecated This method is redundant, not used by any known client, would expose implementation details,
	 *   and will be removed in a future release. Use the ServerConfigurationService instead.
	 *
	 * @return null
	 */
	@Deprecated Properties getConfig();

	/**
	 * Wait right here till the component manager is fully configured.
	 * @deprecated
	 */
	void waitTillConfigured();
	
	/**
	 * Check if the ComponentManager has already been or is in the processing of being closed.
	 * @return true if closed, false if not.
	 */
	boolean hasBeenClosed();
}
