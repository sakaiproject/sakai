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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.sakaiproject.component.locales.SakaiLocales;

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
	 * Access alternative names
	 *
	 * @return The server alternative names (doesn't contain the server name)
	 *         or an empty collection if there is no alternative name
	 */
	Collection<String> getServerNameAliases();

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
	 * 1) IF "name=value" THEN this will return "value"
	 * 2) IF "name=" THEN this will return null
	 * 3) IF name is not defined in the config THEN this will return "" (empty string)
	 * 
	 * @param name The configuration value name (or key).
	 * @return The configuration value for this name OR null if defined as 'blank' OR "" (empty string) if not defined.
	 */
	String getString(String name);

	/**
	 * Access some named configuration value as a string.
	 * 1) IF "name=value" THEN this will return "value"
	 * 2) IF "name=" THEN this will return null
	 * 3) IF name is not defined in the config THEN this will return the provided default value
	 * 
	 * @param name The configuration value name (or key).
	 * @param dflt The value to return if not found in the config.
	 * @return The configuration value for this name OR null if defined as 'blank' OR default value if not defined.
	 */
	String getString(String name, String dflt);

	/**
	 * Access some named configuration values as an array of strings. 
	 * There are 2 ways this is indicated in the system:
	 * 1) The name is the base name. name + ".count" must be defined to be a positive integer - how many are defined. name + "." + i (1..count) must be defined to be the values.
	 * 2) A comma delimited list of values: name=val1,val2,val3
	 * If count is 0 or the value is empty then an empty string array is the resulting return value.
	 * Null is returned ONLY in the case the value cannot be found at all.
	 * 
	 * @param name
	 *        The configuration value name base.
	 * @return The configuration value with this name, empty array if no values or count=0, OR null if config name is not found.
	 */
	String[] getStrings(String name);

	/**
	 * Get the browser.feature.allow configuration as a semicolon-separated string suitable
	 * for the iframe {@code allow} attribute. Used by LTI, Assignments, Lessons, Web Content,
	 * and other tools that embed external content. Includes local-network-access for Chrome 142+.
	 *
	 * @return The joined allow string, or a default including local-network-access if unconfigured
	 */
	default String getBrowserFeatureAllowString() {
		String[] allow = getStrings("browser.feature.allow");
		if (allow != null && allow.length > 0) {
			return String.join(";", allow);
		}
		return "camera; fullscreen; microphone; local-network-access *";
	}

	/**
	 * Access some named configuration value as a long
	 *
	 * @param name
	 *        The configuration value name.
	 * @param dflt
	 *        The value to return if not found.
	 * @return The configuration value with this name, or the default value if not found.
	 */
	long getLong(String name, long dflt);

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
	 * Access some named configuration value as an double.
	 *
	 * @param name
	 *        The configuration value name.
	 * @param defaultValue
	 *        The value to return if not found.
	 * @return The configuration value with this name, or the default value if not found.
	 */
	double getDouble(String name, double defaultValue);

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
	 * Access some named configuration value as a <code>List<String></code>.
	 * The value must be a comma separated set of values.
	 *
	 * @param name
	 *        The configuration value name.
	 * @param dflt
	 *        The value to return if not found.
	 * @return The configuration value with this name, as
	 * 		   a <code>List<String></code>, or the default value if not found. If the default
	 *		   is null, an empty list is returned.
	 */
	List<String> getStringList(String name, List<String> dflt);

	/**
	 * Access some named configuration value as a <code>List<Pattern></code>.
	 * The value must be a comma separated set of regexes.
	 *
	 * @param name
	 *        The configuration value name.
	 * @param dflt
	 *        The value to return if not found.
	 * @return The configuration value with this name, as
	 * 		   a <code>List<Pattern></code>, or the default value if not found.
	 */
	List<Pattern> getPatternList(String name, List<String> dflt);
	
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
	 * KNL-989
	 * Access the list of tools by group
	 * 
	 * @param category
	 *        The tool category
	 * @return An unordered list of tool ids (String) in selected group, or an empty list if there are none for this category.
	 */
	List<String> getToolGroup(String category);

	/** 
	 * KNL-989
	 * Access the list of tool ids in order for this category, to impose on the displays of many tools
	 * 
	 * @param category	Site type
	 * @return An unordered list of group names (String), or an empty list if there are none for this category.
	 */
	List<String> getToolOrder(String category);

	/** 
	 * KNL-989
	 * Returns true if selected tool is contained in pre-initialized list of selected items
	 * @parms toolId id of the selected tool
	 */
	public boolean toolGroupIsSelected(String groupName, String toolId) ;

	/** 
	 * KNL-989
	 * Returns true if selected tool is contained in pre-initialized list of required items
	 * @parms toolId id of the selected tool
	 */
	public boolean toolGroupIsRequired(String groupName, String toolId);
	 
	/** 
	 * KNL-989
	 * Access the list of groups by category (site type)
	 * 
	 * @param category
	 *			 The tool category
	 * @return An ordered list of tool ids (String) indicating the desired tool display order, or an empty list if there are none for this category.
	 */
	List<String> getCategoryGroups(String category);
	
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

   /**
    * Get the list of allowed locales as controlled by config params for "locales" and "locales.more"
    * Defaults when nothing is specified in the config files come from {@link SakaiLocales#SAKAI_LOCALES_DEFAULT}
    * @return an array of all allowed Locales for this installation
    * @see SakaiLocales
    */
   public Locale[] getSakaiLocales();

   /**
    * Parse a string into a Locale
    * @return Locale based on its string representation (language_region) OR default Locale if the string cannot be parsed
    */
   public Locale getLocaleFromString(String localeString);

   /**
    * Retrieves the string property by key, then splits it by comma. The trimmed tokens
    * are then returned in a set. If the key isn't present, an empty set will be returned.
    *
    * @param key The property key
    * @return A set of trimmed tokens from a comma separated list
    */
   public Set<String> getCommaSeparatedListAsSet(String key);

   /**
    * Retrieves the 'smtpServer' property value. If it's not defined it will use localhost
    *
    * @return The SMTP server for email notifications.
    */
   public String getSmtpServer();

   /**
    * Retrieves the 'setup.request' property value. If it's not defined it will use the EmailService configuration.
    * If no value has been set then returns no-reply@${serverName}
    *
    * @return The SMTP from address for email notifications.
    */
   public String getSmtpFrom();

   /**
    * Retrieves the 'smtpPort' property value from the EmailService. If it's not defined it will return 25.
    *
    * @return The SMTP from address for email notifications.
    */
   public String getSmtpPort();

   // improved methods

   public static final String UNKNOWN = "UNKNOWN";
   public static final String TYPE_BOOLEAN = "boolean";
   public static final String TYPE_INT = "int";
   public static final String TYPE_ARRAY = "array";
   public static final String TYPE_STRING = "string";
   public static final String[] TYPES = {
       TYPE_BOOLEAN,
       TYPE_INT,
       TYPE_ARRAY,
       TYPE_STRING,
       UNKNOWN
   };

   /**
    * Retrieves config values from the configuration service
    * 
    * @param name the name of the setting to retrieve, Should be a string name: e.g. auto.ddl,
    *            mystuff.config, etc.
    * @param defaultValue a specified default value to return if this setting cannot be found, 
    *            <b>NOTE:</b> You can set the default value to null but you must specify the class type in parens
    * @return the value of the configuration setting OR the default value if none can be found
    */
   public <T> T getConfig(String name, T defaultValue);

   /**
    * Retrieve the internally stored the config item,
    * this is not really for general use, if you want the value of a configuration variable then
    * you should use {@link #getConfig(String, Object)}
    * 
    * @param name the name of the setting to retrieve, Should be a string name: e.g. auto.ddl,
    *            mystuff.config, etc.
    * @return the config item OR null if none exists
    */
   public ConfigItem getConfigItem(String name);

   /**
    * Register a configuration item (or override an existing config),
    * this should be called when changing or creating a configuration setting
    * 
    * @param configItem an instance of the {@link ConfigItem} interface with the name, value, source params set,
    * use {@link BasicConfigItem} as an easy way to construct a {@link ConfigItem}
    * @return the registered {@link ConfigItem}
    * @throws IllegalArgumentException if the {@link ConfigItem} is not valid (does not have all required fields set)
    */
   public ConfigItem registerConfigItem(ConfigItem configItem);

   /**
    * Register a listener which will be notified whenever there is a configuration setting change,
    * there is no need to unregister a listener as all listener references will be held weakly
    * (if there are no more variables which reference the listener then it will be GCed and removed from the list of listeners)
    * 
    * Registering the same {@link ConfigurationListener} object multiple times has no effect
    * 
    * @param configurationListener a {@link ConfigurationListener} object
    */
   public void registerListener(ConfigurationListener configurationListener);

   /**
    * Returns data about all the configuration values which are known
    * to the system at the time and some stats which are useful
    * 
    * @return a config data class with data about the known configuration values
    */
   public ConfigData getConfigData();

   // STATICS

   /**
    * Defines the config data holding class
    */
   public static interface ConfigData {
       /**
        * @return the total number of config items known to the service
        */
       public int getTotalConfigItems();
       /**
        * @return the total number of registered config items known to the service
        */
       public int getRegisteredConfigItems();
       /**
        * @return the total number of requested but not registered config items known to the service
        */
       public int getUnRegisteredConfigItems();
       /**
        * @return the array of all current source names
        */
       public String[] getSources();
       /**
        * @return the total set of all known config items
        */
       public List<ConfigItem> getItems();
   }

   /**
    * Defines the config item holding class
    */
   public static interface ConfigItem {

       /**
        * Called whenever this config item is requested
        * @return the current number of times requested
        */
       public int requested();

       /**
        * Called whenever this config item is changed
        * @param value the new value
        * @param source the source which is making the change
        * @return the current number of times the item was changed
        */
       public int changed(Object value, String source);

       /**
        * Duplicate this config item
        * This is mostly used to ensure we do not send the internal objects out where they could be changed
        * @return a config item with all the same values
        */
       public ConfigItem copy();

       /**
        * @return the name/key for this configuration value
        */
       public String getName();
       /**
        * @return the actual stored value for this config (null indicates it is not set)
        */
       public Object getValue();
       /**
        * @return the type of the value (string, int, boolean, array) - from {@link ServerConfigurationService#TYPES}
        */
       public String getType();
       /**
        * @return the name of the most recent source for this config value (e.g. sakai/sakai.properties)
        */
       public String getSource();
       /**
        * @return the default value for this config (null indicates it is not set)
        */
       public Object getDefaultValue();
       /**
        * @return the human readable description of this configuration value
        */
       public String getDescription();
       /**
        * @return the number of times the config value was requested (or looked up)
        */
       public int getRequested();
       /**
        * @return the number of times this config value was changed (or updated)
        */
       public int getChanged();
       /**
        * @return the version of this config item (a newly created item is version 1), incremented with each change
        */
       public int getVersion();
       /**
        * @return the history of the config item over time (empty array if it has never changed)
        */
       public ConfigHistory[] getHistory();
       /**
        * @return indicates if this config is registered (true) or if it is only requested (false)
        * (requested means someone asked for it but the setting has not been stored in the config service)
        */
       public boolean isRegistered();
       /**
        * @return indicates is this config is has a default value defined
        * (this can only be known for items which are requested)
        */
       public boolean isDefaulted();
       /**
        * @return indicates is this config value should not be revealed because there are security implications
        */
       public boolean isSecured();
       /**
        * Indicates is this config item is dynamic or static (default is static)
        * @return true is this config can be modified at runtime (dynamic), false if runtime changes are not allowed (static)
        */
       public boolean isDynamic();
   }

   /**
    * Defines the config item history class
    */
   public static interface ConfigHistory {
       /**
        * @return the version of the config item this history refers to (a newly created item is version 1)
        */
       public int getVersion();
       /**
        * @return the time at which this historical version of the config item became irrelevant (this is when this version was replaced)
        */
       public long getTimestamp();
       /**
        * @return the source name for this version of the config item
        */
       public String getSource();
       /**
        * @return the value of the config item in this version of it
        */
       public Object getValue();
   }

   /**
    * Allows registration of configuration settings (config items) from outside the 
    * Server Configuration Service and the standard set of properties files which
    * will be loaded early on the configuration cycle
    * 
    * NOTE: the implemented ConfigurationProvider MUST be a Spring singleton and it
    * MUST be registered in the main Sakai application context from a component (from a webapp will not work),
    * it also must be set explicitly to not lazy initialize (lazy-init="false"),
    * it is always possible to update the configuration later using {@link #registerConfigItems(ConfigData)}
    * so this is mainly for loading configurations very early in the system startup
    */
   public static interface ConfigurationProvider {
       /* This is part of supporting a more flexible and persistent configuration -AZ
        */

       /**
        * Register a set of configuration items,
        * these will be loaded after the base properties are loaded (from properties files) 
        * but before the rest of Sakai starts up so they will be allowed to override the base properties
        * @param configData the set of current config data that currently exists
        * @return a list of ConfigItems which will be loaded into the current configuration, 
        * these MUST have the name, value (OR defaultValue), and source set to non-null/non-empty values,
        * use {@link BasicConfigItem} as an easy way to ensure the configitems are setup correctly
        */
       public List<ConfigItem> registerConfigItems(ConfigData configData);
   }

   /**
    * Allows a service to be notified when configuration settings are changed,
    * It is up to the implementor to ignore the changes they do not care about.
    * Filter on the {@link ConfigItem#getName()}.
    * 
    * NOTE: this does NOT include any changes which happen during the initial properties file loading
    */
   public static interface ConfigurationListener {
       /* This is part of supporting a more flexible and persistent configuration -AZ
        */

       /**
        * This will be called each time a {@link ConfigItem} is changed,
        * this will be called before the item has been changed and will reflect 
        * the current values for this config item.
        * NOTE: the default implementation of this should be to just return null
        * if processing should be allowed to continue unimpeded
        * 
        * NOTE: it does NOT include the the initial registration of that config item and 
        * initial startup of the {@link ServerConfigurationService}
        * 
        * @param currentConfigItem the {@link ConfigItem} which is changing (will be null if item is new),
        *   this item should not be changed by this method
        * @param newConfigItem the {@link ConfigItem} which will become the new one
        * @return null to allow processing to continue as usual
        *   OR instance of {@link BlockingConfigItem} to block the change from happening (change will be discarded and processing will stop)
        *   OR the modified newConfigItem item which will be used as the new config value
        */
       public ConfigItem changing(ConfigItem currentConfigItem, ConfigItem newConfigItem);

       /**
        * This will be called each time a {@link ConfigItem} is changed,
        * this will be called after the item has been changed and will reflect the new values
        * for this config item
        * 
        * NOTE: it does NOT include the the initial registration of that config item and 
        * initial startup of the {@link ServerConfigurationService}
        * 
        * @param configItem the {@link ConfigItem} which changed,
        *   this item should not be changed by this method
        * @param previousConfigItem the {@link ConfigItem} before the change (will be null if item is new)
        */
       public void changed(ConfigItem configItem, ConfigItem previousConfigItem);

       /**
        * This is a special marker class that is used in the {@link ServerConfigurationService.ConfigurationListener},
        * returning this indicates that the config change should stop processing the change and retain the original value
        */
       public static class BlockingConfigItem implements ConfigItem {

           public static BlockingConfigItem instance() {
               return new BlockingConfigItem();
           }

           /**
            * SPECIAL marker class, indicates that the config change should stop processing the change and retain the original value
            */
           public BlockingConfigItem() {}

           public int requested() {
               return 0;
           }
           public int changed(Object value, String source) {
               return 0;
           }
           public ConfigItem copy() {
               return new BlockingConfigItem();
           }
           public String getName() {
               return "BLOCKING";
           }
           public Object getValue() {
               return null;
           }
           public String getType() {
               return "BLOCKING";
           }
           public String getDescription() {
               return null;
           }
           public String getSource() {
               return null;
           }
           public Object getDefaultValue() {
               return null;
           }
           public int getRequested() {
               return 0;
           }
           public int getChanged() {
               return 0;
           }
           public int getVersion() {
               return 0;
           }
           public ConfigHistory[] getHistory() {
               return null;
           }
           public boolean isRegistered() {
               return false;
           }
           public boolean isDefaulted() {
               return false;
           }
           public boolean isSecured() {
               return false;
           }
           public boolean isDynamic() {
               return false;
           }
       }

   }

}
