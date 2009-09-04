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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.api;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * ServerConfigurationService provides information about how the server is configured.
 * </p>
 */
public interface ServerConfigurationService
{
	/** This string can be used to find the service in the service manager. */
	public static final String SERVICE_NAME = ServerConfigurationService.class.getName();

	/** Key in the ThreadLocalManager for the serverUrl based on the current request. */
	final static String CURRENT_SERVER_URL = "sakai:request.server.url";

	/** Key in the ThreadLocalManager for the path based on the current request. */
	final static String CURRENT_PORTAL_PATH = "sakai:request.portal.path";
   
	/**
	 * Access the unique (to the cluster) id of the server.
	 * 
	 * @return The unique (to the cluster) id of the server.
	 */
	String getServerId();

	/**
	 * Access the unique (to the cluster) instance id of the server.
	 * 
	 * @return The unique (to the cluster) instance id of the server.
	 */
	String getServerInstance();

	/**
	 * Access the combined server / instance id.
	 * 
	 * @return The combined server / instance id.
	 */
	String getServerIdInstance();

	/**
	 * Access the server DNS name.
	 * 
	 * @return The server DNS name.
	 */
	String getServerName();

	/**
	 * Access the URL to the root of the server - append any additional path to the end.
	 * 
	 * @return The URL to the root of the server.
	 */
	String getServerUrl();

	/**
	 * Access the URL to the help service on the server - append in the path the tool well known id for context sensitive help.
	 * 
	 * @param helpContext
	 *        The context string.
	 * @return The URL to the help service on the server.
	 */
	String getHelpUrl(String helpContext);

	/**
	 * Access the URL to the access service on the server - append any additional path to the end.
	 * 
	 * @return The URL to the access service on the server.
	 */
	String getAccessUrl();

	/**
	 * Access the path to the access service on the server relative to the base URL for the server.
	 * 
	 * @return The path to the access service on the server.
	 */
	String getAccessPath();

	/**
	 * Access the URL to the portal service on the server - append any additional path to the end.
	 * 
	 * @return The URL to the portal service on the server.
	 */
	String getPortalUrl();

	/**
	 * Access the URL to the tool dispatcher service on the server - append any additional path to the end.
	 * 
	 * @return The URL to the tool dispatcher service on the server.
	 */
	String getToolUrl();

	/**
	 * Access the site id for the gateway (public) site.
	 * 
	 * @return The site id for the gateway (public) site.
	 */
	String getGatewaySiteId();

	/**
	 * Access the URL to use as a redirect when the user has logged out.
	 * 
	 * @return The URL to use as a redirect when the user has logged out.
	 */
	String getLoggedOutUrl();

	/**
	 * Access the URL to the user's "home" (My Workspace) in the service.
	 * 
	 * @return The URL to the user's "home" (My Workspace) in the service.
	 */
	String getUserHomeUrl();

	/**
	 * Access the file path to the "sakai home" on the app server.
	 * 
	 * @return The file path to the "sakai home" on the app server.
	 */
	String getSakaiHomePath();

	/**
	 * Access some named configuration value as a string.
	 * 
	 * @param name
	 *        The configuration value name.
	 * @return The configuration value with this name, or "" if not found.
	 */
	String getString(String name);

	/**
	 * Access some named configuration value as a string.
	 * 
	 * @param name
	 *        The configuration value name.
	 * @param dflt
	 *        The value to return if not found.
	 * @return The configuration value with this name, or the default value if not found.
	 */
	String getString(String name, String dflt);

	/**
	 * Access some named configuration values as an array of strings. The name is the base name. name + ".count" must be defined to be a positive integer - how many are defined. name + "." + i (1..count) must be defined to be the values.
	 * 
	 * @param name
	 *        The configuration value name base.
	 * @return The configuration value with this name, or the null if not found.
	 */
	String[] getStrings(String name);

	/**
	 * Access some named configuration value as an int.
	 * 
	 * @param name
	 *        The configuration value name.
	 * @param dflt
	 *        The value to return if not found.
	 * @return The configuration value with this name, or the default value if not found.
	 */
	int getInt(String name, int dflt);

	/**
	 * Access some named configuration value as a boolean.
	 * 
	 * @param name
	 *        The configuration value name.
	 * @param dflt
	 *        The value to return if not found.
	 * @return The configuration value with this name, or the default value if not found.
	 */
	boolean getBoolean(String name, boolean dflt);
	
	/**
	 * Access the undereferenced value of the given property. That is,
	 * Spring-style property placeholders will not be expanded as
	 * they would be in other getters on this interface. For example,
	 * consider the following configuration:
	 * 
	 * <pre>
	 * <code>
	 * property1=foo
	 * property2=${property1}
	 * </code>
	 * </pre>
	 * 
	 * <p>Invoking {@link #getString(String)}, passing <code>"property2"</code>
	 * will return <code>"foo"</code>. However, invoking this method
	 * with the same argument will return <code>"${property1}"</code>.</p>
	 * 
	 * <p>Typically, a client of this method, e.g. a dynamic 
	 * configuration management tool, is interested in reporting on the system's 
	 * actual state from which return values of other getters are calculated. 
	 * In such cases, caller-specified default values have no utility. Thus this 
	 * method does not accept an argument specifying the property's default value.</p>
	 * 
	 * <p>For a given undefined property "X", this method should return
	 * exactly the same value as <code>getString("X")</code>. Thus it may
	 * not be possible in all cases to distinguish between defined and
	 * undefined properties.</p>
	 * 
	 * @param name a property name. Must not be <code>null</code>
	 * @return the property value. Property placeholders will not have
	 *   been dereferenced. The value representing an non-existent property
	 *   is implementation dependent.
	 */
	String getRawProperty(String name);
	
	/**
	 * Access the list of tool ids in order for this category, to impose on the displays of many tools
	 * 
	 * @param category
	 *        The tool category
	 * @return An ordered list of tool ids (String) indicating the desired tool display order, or an empty list if there are none for this category.
	 */
	List<String> getToolOrder(String category);

	/**
	 * Access the list of tool ids that are required for this category.
	 * 
	 * @param category
	 *        The tool category.
	 * @return A list of tool ids (String) that are required for this category, or an empty list if there are none for this category.
	 */
	List<String> getToolsRequired(String category);

	/**
	 * Access the list of tool ids that are selected by default for this category.
	 * 
	 * @param category
	 *        The tool category.
	 * @return A list of tool ids (String) for this category to use by default, or an empty list if there are none for this category.
	 */
	List<String> getDefaultTools(String category);

   /**
    * access the list of tool categories for the given site type
    *
    * @param category the site type
    * @return a list of tool category ids in order
    */
   List<String> getToolCategories(String category);

   /**
    * access the map of tool categories to tool ids for this site type
    * @param category the site type
    * @return a map of tool category ids to tool ids
    */
   Map<String, List<String>> getToolCategoriesAsMap(String category);

   /**
    * access a map of tool id to tool category id for this site type
    * @param category the site type
    * @return map with tool id as key and category id as value
    */
   Map<String, String> getToolToCategoryMap(String category);
}
