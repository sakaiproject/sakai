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

package org.sakaiproject.component.impl;

import au.com.bytecode.opencsv.CSVParser;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigurationListener.BlockingConfigItem;
import org.sakaiproject.component.locales.SakaiLocales;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.SakaiProperties;

/**
 * <p>
 * BasicConfigurationService is a basic implementation of the ServerConfigurationService.
 * </p>
 */
@SuppressWarnings({"rawtypes","unchecked"})
@Slf4j
public class BasicConfigurationService implements ServerConfigurationService, ApplicationContextAware
{
    private static final String SOURCE_GET_STRINGS = "getStrings";

    /**
     * The delegate that handles all the configuration for tools.
     */
    private final ToolConfigurationService toolConfigurationService = new ToolConfigurationService();

    /** The instance id for this app server. */
    private String instanceId = null;

    /** This is computed, joining the configured serverId and the set instanceId. */
    private String serverIdInstance = null;

    /** The map of values from the loaded properties wherein property placeholders have
     * <em>not</em> been dereferenced */
    private Properties rawProperties;


    private static final String SAKAI_LOCALES_KEY = "locales";
    private static final String SAKAI_LOCALES_MORE = "locales.more"; // default is blank/null
    private static final String SAKAI_SYSTEM_PROPERTY_SUFFIX = "@SystemProperty";


    /**********************************************************************************************************************************************************************************************************************************************************
     * Dependencies
     *********************************************************************************************************************************************************************************************************************************************************/

    /**
     * the ThreadLocalManager collaborator.
     */
    private ThreadLocalManager threadLocalManager;

    /**
     * the SessionManager collaborator.
     */
    private SessionManager sessionManager;

    private SakaiProperties sakaiProperties;

    /**********************************************************************************************************************************************************************************************************************************************************
     * Configuration
     *********************************************************************************************************************************************************************************************************************************************************/

    /**
     * Configuration: set the file name for tool order file.
     * 
     * @param string
     *        The file name for tool order file.
     */
    public void setToolOrderFile(String string)
    {
        toolConfigurationService.setToolOrderFile(string);
    }

    /**
     * Configuration: set the resource to load in which the default tool order is defined.
     *
     * @param defaultToolOrderResource
     *        The resource containing a tool order.
     */
    public void setDefaultToolOrderResource(Resource defaultToolOrderResource)
    {
        toolConfigurationService.setDefaultToolOrderResource(defaultToolOrderResource);
    }

    /**********************************************************************************************************************************************************************************************************************************************************
     * Init and Destroy
     *********************************************************************************************************************************************************************************************************************************************************/

    private ApplicationContext applicationContext;
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Final initialization, once all dependencies are set.
     */
    public void init()
    {
        // can enable the output of the complete set of configuration items using: config.dump.to.log
        this.rawProperties = sakaiProperties.getRawProperties();

        // populate the security keys set
        String securedKeys = getRawProperty("config.secured.key.names");
        if (securedKeys != null) {
            String[] keys = securedKeys.split(",");
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < keys.length; i++) {
                String key = StringUtils.trimToNull(keys[i]);
                if (key != null) {
                    this.secureConfigurationKeys.add(key);
                }
            }
        }
        log.info("Configured "+this.secureConfigurationKeys.size()+" secured key names: "+this.secureConfigurationKeys);
	// always add "password@javax.sql.BaseDataSource"
        this.secureConfigurationKeys.add("password@javax.sql.BaseDataSource");

        // load up some things that are not part of the config but are used by it
        this.addConfigItem(new ConfigItemImpl("sakai.home", this.getSakaiHomePath()), "SCS");
        this.addConfigItem(new ConfigItemImpl("sakai.gatewaySiteId", this.getGatewaySiteId()), "SCS");
        this.addConfigItem(new ConfigItemImpl("portal.loggedOutURL", this.getLoggedOutUrl()), "SCS");

        // put all the properties into the configuration map
        Map<String, Properties> allSakaiProps = sakaiProperties.getSeparateProperties();
        for (Entry<String, Properties> entry : allSakaiProps.entrySet()) {
        	// all properties from security.properties should be secured
        	if ("security.properties".equalsIgnoreCase(entry.getKey())) {
        		for (String securedKey : entry.getValue().stringPropertyNames()) {
					this.secureConfigurationKeys.add(securedKey);
				}
        	}
            this.addProperties(entry.getValue(), entry.getKey());
        }
        log.info("Configured "+this.secureConfigurationKeys.size()+" secured keys from all sources");
        log.info("Loaded "+configurationItems.size()+" config items from all initial sources");

        if (this.getBoolean("config.dereference.on.load.initial", true)) {
            int changed = dereferenceConfig();
            log.info("Dereference Initial: Changed (dereferenced) "+changed+" item values out of "+configurationItems.size()+" initial config items");
        }

        // load all the providers which are known (the rest have to manually register their configs), must be singleton without lazy init
        Map<String, ConfigurationProvider> providerBeans = this.applicationContext.getBeansOfType(ConfigurationProvider.class, false, false);
        if (providerBeans != null) {
            int configCounter = 0;
            for (ConfigurationProvider provider : providerBeans.values()) {
                List<ConfigItem> items;
                try {
                    items = provider.registerConfigItems(this.getConfigData());
                    configCounter += items.size();
                    this.addConfigList(items);
                } catch (Exception e) {
                    log.warn("Unable to load the config values from provider ("+provider.getClass()+"): "+e);
                }
            }
            log.info("Found and loaded "+configCounter+" config values from "+providerBeans.size()+" configuration providers");
        }

        if (this.getBoolean("config.dereference.on.load.all", false)) {
            int changed = dereferenceConfig();
            log.info("Dereference All: Changed (dereferenced) "+changed+" item values out of all "+configurationItems.size()+" config items");
        }

        // OTHER STUFF
        try
        {
            // set a unique instance id for this server run
            // Note: to reduce startup dependency, just use the current time, NOT the id service.
            instanceId = Long.toString(System.currentTimeMillis());

            serverIdInstance = getServerId() + "-" + instanceId;
        }
        catch (Exception t)
        {
            log.warn("init(): ", t);
        }

        log.info("init()");

        // Initialise the tool configuration service
        toolConfigurationService.setUseToolGroup(getConfig("config.sitemanage.useToolGroup", false));
        toolConfigurationService.init();
    }

    /**
     * Final cleanup.
     */
    public void destroy()
    {
        this.applicationContext = null;
        this.listeners.clear();
        log.info("destroy()");
    }

    /**********************************************************************************************************************************************************************************************************************************************************
     * ServerConfigurationService implementation
     *********************************************************************************************************************************************************************************************************************************************************/

    /**
     * {@inheritDoc}
     */
    public String getServerId()
    {
        return getConfig("serverId", "localhost"); //return (String) properties.get("serverId");
    }

    /**
     * {@inheritDoc}
     */
    public String getServerInstance()
    {
        return instanceId;
    }

    /**
     * {@inheritDoc}
     */
    public String getServerIdInstance()
    {
        return serverIdInstance;
    }

    /**
     * {@inheritDoc}
     */
    public String getServerUrl()
    {
        // try to get the value pre-computed for this request, to better match the request server naming conventions
        String rv = (String) threadLocalManager.get(CURRENT_SERVER_URL);
        if (rv == null)
        {
            rv = getConfig("serverUrl", "http://localhost:8080"); //rv = (String) properties.get("serverUrl");
        }
        return rv;
    }

    /**
     * {@inheritDoc}
     */
    public String getServerName()
    {
        return getConfig("serverName", "localhost"); //(String) properties.get("serverName");
    }

    /**
     * {@inheritDoc}
     */
    public Collection<String> getServerNameAliases(){
        String[] names = getStrings("serverNameAliases");
        return (names == null) ? Collections.<String>emptyList() : Arrays.asList(names);
    }

    /**
     * {@inheritDoc}
     */
    public String getAccessUrl()
    {
        return getServerUrl() + getAccessPath(); //(String) properties.get("accessPath");
    }

    /**
     * {@inheritDoc}
     */
    public String getAccessPath()
    {
        return getConfig("accessPath", "/access"); //(String) properties.get("accessPath");
    }

    /**
     * {@inheritDoc}
     */
    public String getHelpUrl(String helpContext)
    {
        String rv = getPortalUrl() + getConfig("helpPath", "/help") + "/main"; //(String) properties.get("helpPath") + "/main";
        if (helpContext != null)
        {
            rv += "?help=" + helpContext;
        }

        return rv;
    }

    /**
     * {@inheritDoc}
     */
    public String getPortalUrl()
    {
        /*
		String rv = (String) threadLocalManager.get(CURRENT_PORTAL_PATH);
		if (rv == null)
		{
			rv = (String) properties.get("portalPath");
		}
         */
        //KNL-758, SAK-20431 - don't use the portal path that the RequestFilter gives us,
        //as that is based on the current context. Instead use the actual value we have
        //set in sakai.properties
        String rv = getConfig("portalPath", "/portal"); //(String) properties.get("portalPath");

        @SuppressWarnings("UnnecessaryLocalVariable")
        String portalUrl = getServerUrl() + rv;

        return portalUrl;
    }

    /**
     * {@inheritDoc}
     */
    public String getToolUrl()
    {
        return getServerUrl() + getConfig("toolPath", "/portal/tool"); //(String) properties.get("toolPath");
    }

    /**
     * {@inheritDoc}
     */
    public String getUserHomeUrl()
    {
        // get the configured URL (the text "#UID#" will be repalced with the current logged in user id
        // NOTE: this is relative to the server root
        String rv = getConfig("userHomeUrl", null); //(String) properties.get("userHomeUrl");

        // form a site based portal id if not configured
        if (rv == null)
        {
            rv = getConfig("portalPath", "/portal") + "/site/~#UID#"; // (String) properties.get("portalPath") + "/site/~#UID#"
        }

        // check for a logged in user
        String user = sessionManager.getCurrentSessionUserId();
        boolean loggedIn = (user != null);

        // if logged in, replace the UID in the pattern
        if (loggedIn)
        {
            rv = rv.replaceAll("#UID#", user);
        }

        // make it full, adding the server root
        rv = getServerUrl() + rv;

        return rv;
    }

    /**
     * {@inheritDoc}
     */
    public String getGatewaySiteId()
    {
        String rv = getConfig("gatewaySiteId", "!gateway"); //(String) properties.get("gatewaySiteId");

        if (rv == null)
        {
            rv = "~anon";
        }

        return rv;
    }

    /**
     * {@inheritDoc}
     */
    public String getLoggedOutUrl()
    {
        String rv = getConfig("loggedOutUrl", "/portal"); //(String) properties.get("loggedOutUrl");
        if (rv != null)
        {
            // if not a full URL, add the server to the front
            if (rv.startsWith("/"))
            {
                rv = getServerUrl() + rv;
            }
        }

        // use the portal URL if there's no logout defined
        else
        {
            rv = getPortalUrl();
        }

        return rv;
    }

    /**
     * {@inheritDoc}
     */
    public String getSakaiHomePath()
    {
        return System.getProperty("sakai.home");
    }

    /**
     * {@inheritDoc}
     */
    public String getRawProperty(String name) {
        String rv = null;
        if (this.rawProperties.containsKey(name)) {
            // NOTE: raw properties ONLY contains the data read in from the properties files
            rv = StringUtils.trimToNull((String) this.rawProperties.get(name));
        } else {
            // check the config storage since it is not in the raw props
            ConfigItem ci = getConfigItem(name);
            if (ci != null && ci.getValue() != null) {
                rv = ci.getValue().toString();
            }
        }
        if (rv == null) rv = "";
        return rv;
    }

    /**
     * {@inheritDoc}
     */
    public String getString(String name) {
        return getString(name, ""); //properties);
    }

    /**
     * {@inheritDoc}
     */
    public String getString(String name, String dflt) {
        /**
         * NOTE: everything calls this in order to resolve a configuration setting,
         * we take advantage of that by doing the heavy lifting in this method
         * (which includes variable replacement)
         */
        //return getString(name, dflt, properties);
        String value = dflt;
        // retrieve a registered config item for this name
        ConfigItemImpl ci = findConfigItem(name, dflt);
        if (ci != null) {
            if (ci.getValue() != null) {
                value = StringUtils.trimToNull(ci.getValue().toString());
            } else {
                // if the default value is set then we will return that instead
                //noinspection StatementWithEmptyBody
                if (ci.getDefaultValue() != null) {
                    value = StringUtils.trimToNull(ci.getDefaultValue().toString());
                } else {
                    // the stored value and default value are null so we will allow the dflt to override
                }
            }
        }
        if (StringUtils.isNotEmpty(value)) {
            // check if we need to do any variable replacement
            value = dereferenceValue(value);
        }
        return value;
    }

    /**
     * Pattern to find "${var}" in strings (should match "key" from ${key})
     */
    static Pattern referencePattern = Pattern.compile("\\$\\{(.+?)\\}");
    /**
     * This will search for any values in the string which need to be replaced
     * with actual values from the current known set of properties which are
     * available to the config service
     * 
     * @param value any string (might have a reference like: "thing-${suffix}")
     * @return the value with all matched ${vars} replaced (unmatched ones are left as is), only returns null if the input is null
     */
    protected String dereferenceValue(String value) {
        if (log.isDebugEnabled()) log.debug("dereferenceValue("+value+")");
        /*
         * NOTE: if the performance of this becomes an issue then the right way to handle it is
         * to place a flag on the ConfigItem to indicate if there is replaceable refs in it
         * (probably when this runs the first time) and if there are none then skip this
         * until the value is changed and then reset the flag so it will be checked again,
         * if there are still issues then adding a "lastChecked" timestamp and 
         * a cache of the processed value to the ConfigItem which is used as long as the timestamp
         * has not expired (maybe 15 mins or something), with automatic expiration when the value changes
         */
        String drValue = value;
        if (value != null && value.length() >= 4) { // min length of a replaceable value - "${a}"
            Matcher matcher = referencePattern.matcher(value);
            if (matcher.find()) {
                if (log.isDebugEnabled()) log.debug("dereferenceValue("+value+"), found refs to replace");
                matcher.reset();
                StringBuilder sb = new StringBuilder();
                // loop through and find the vars to replace and write out the new string
                int pointer = 0;
                while (matcher.find()) {
                    String name = matcher.group(1);
                    if (StringUtils.isNotBlank(name)) {
                        // look up the value
                        String replacementValue = null;
                        ConfigItemImpl ci = findConfigItem(name, null);
                        if (ci != null) {
                            // found the config name so we will at least replace with empty string
                            replacementValue = "";
                            if (ci.getValue() != null) {
                                replacementValue = StringUtils.trimToEmpty(ci.getValue().toString());
                            }
                        }
                        sb.append(value.substring(pointer, matcher.start()));
                        if (replacementValue == null) {
                            replacementValue = matcher.group(); // just put the ${name} back in
                        } else {
                            // need to recurse in the case of nested refs
                            replacementValue = dereferenceValue(replacementValue);
                        }
                        sb.append(replacementValue);
                        pointer = matcher.end();
                    }
                }
                sb.append(value.substring(pointer, value.length())); // get the remainder of the value
                drValue = sb.toString();
            }
        }
        if (log.isDebugEnabled()) log.debug("dereferenceValue("+value+"): return="+drValue);
        return drValue;
    }

    /**
     * Goes through the entire config and resolves all references and updates the actual 
     * stored values in the config.
     * NOTE: this is destructive and probably not a good idea to run generally
     * 
     * @return the number of config items which were changed
     */
    protected int dereferenceConfig() {
        int counter = 0;
        for (Entry<String, ConfigItemImpl> entry : configurationItems.entrySet()) {
            ConfigItemImpl configItem = entry.getValue();
            if (configItem.getValue() != null) {
                String currentValue = configItem.getValue().toString();
                String newValue = dereferenceValue(currentValue);
                if (!currentValue.equals(newValue)) {
                    configItem.setValue( newValue );
                    counter++;
                }
            }
        }
        return counter;
    }


    /**
     * {@inheritDoc}
     */
    public String[] getStrings(String name) {
        String[] rv = null;
        // get the count
        int count = getInt(name + ".count", -1);
        if (count == 0) {
            // zero count means empty array
            rv = new String[0];
        } else if (count > 0) {
            rv = new String[count];
            for (int i = 1; i <= count; i++)
            {
                rv[i - 1] = getString(name + "." + i, "");
            }
            // store the array in the properties
            this.addConfigItem(new ConfigItemImpl(name, rv, TYPE_ARRAY, SOURCE_GET_STRINGS), SOURCE_GET_STRINGS);
        } else {
            if (findConfigItem(name, null) != null) {
                // the config name exists
                String value = getString(name);
                if (StringUtils.isBlank(value)) {
                    // empty value is an empty array
                    rv = new String[0];
                    this.addConfigItem(new ConfigItemImpl(name, rv, TYPE_ARRAY, SOURCE_GET_STRINGS), SOURCE_GET_STRINGS);
                } else {
                    CSVParser csvParser = new CSVParser(',','"','\\',false,true); // should configure this for default CSV parsing
                    try {
                        rv = csvParser.parseLine(value);
                        this.addConfigItem(new ConfigItemImpl(name, rv, TYPE_ARRAY, SOURCE_GET_STRINGS), SOURCE_GET_STRINGS);
                    } catch (IOException e) {
                        log.warn("Config property ("+name+") read as multi-valued string, but failure occurred while parsing: "+e, e);
                    }
                }
            }
        }
        return rv;
    }

    /**
     * {@inheritDoc}
     */
    public int getInt(String name, int dflt)
    {
        String value = getString(name);

        if (StringUtils.isEmpty(value)) return dflt;

        return Integer.parseInt(value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(String name, boolean dflt)
    {
        String value = getString(name);

        if (StringUtils.isEmpty(value)) return dflt;

        return Boolean.valueOf(value);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getStringList(String name, List<String> dflt) {

        String value = getString(name, null);
        if (StringUtils.isNotBlank(value)) {
            return Stream.of(StringUtils.split(value, ",")).collect(Collectors.toList());
        } else {
            return dflt != null ? dflt : new ArrayList<>();
        }
    }

    /**
     * Converts the given list of Strings to a list of Pattern objects
     *
     * @param regexps
     *            A list of regex pattern strings
     *
     * @exception IllegalArgumentException
     *            if one of the patterns has invalid regular expression
     *            syntax
     */
    private List<Pattern> getRegExPatterns(List<String> regexps) {

        ArrayList<Pattern> patterns = new ArrayList<>();
        for (String regexp : regexps) {
            String regex = StringUtils.trimToNull(regexp);
            if (regex != null) {
                // if :empty: is in any of the then return an empty list
                if (StringUtils.equals(":empty:", regex)) return new ArrayList<>();

                try {
                    patterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
                } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException("Illegal Regular Expression Syntax: [" + regex + "] - " + e.getMessage());
                }
            }
        }
        return patterns;
    }

    /**
     * {@inheritDoc}
     */
    public List<Pattern> getPatternList(String name, List<String> dflt) {

        List<String> list = getStringList(name, dflt);
        return getRegExPatterns(list);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getToolGroup(String groupName)
    {
        return toolConfigurationService.getToolGroup(groupName);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getCategoryGroups(String category)
    {
        return toolConfigurationService.getCategoryGroups(category);
    }


    /**
     * {@inheritDoc}
     */
    public List<String> getToolOrder(String category)
    {

        return toolConfigurationService.getToolOrder(category);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getToolsRequired(String category)
    {

        return toolConfigurationService.getToolsRequired(category);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getDefaultTools(String category)
    {

        return toolConfigurationService.getDefaultTools(category);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getToolCategories(String category)
    {

        return toolConfigurationService.getToolCategories(category);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getToolCategoriesAsMap(String category)
    {

        return toolConfigurationService.getToolCategoriesAsMap(category);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getToolToCategoryMap(String category)
    {
        return toolConfigurationService.getToolToCategoryMap(category);
    }

    /*
     * Returns true if selected tool is contained in pre-initialized list of selected items
     * @params toolId id of the selected tool
     */
    public boolean toolGroupIsSelected(String groupName, String toolId) 
    {
        return toolConfigurationService.toolGroupIsSelected(groupName, toolId);
    }

    /*
     * Returns true if selected tool is contained in pre-initialized list of required items
     * @parms toolId id of the selected tool
     */
    public boolean toolGroupIsRequired(String groupName, String toolId) 
    {
        return toolConfigurationService.toolGroupIsRequired(groupName, toolId);
    }

    /**
     * Get the list of allowed locales as controlled by config params for {@value #SAKAI_LOCALES_KEY} and {@value #SAKAI_LOCALES_MORE}
     * @return an array of all allowed Locales for this installation
     */
    public Locale[] getSakaiLocales() {
        String localesStr = getString(SAKAI_LOCALES_KEY, SakaiLocales.SAKAI_LOCALES_DEFAULT);
        if (localesStr == null) { // means locales= is set
            localesStr = ""; // empty to get default locale only
        } else if (StringUtils.isBlank(localesStr)) { // missing or not set
            localesStr = SakaiLocales.SAKAI_LOCALES_DEFAULT;
        }
        String[] locales = StringUtils.split(localesStr, ','); // NOTE: these need to be trimmed (which getLocaleFromString will do)
        String[] localesMore = getStrings(SAKAI_LOCALES_MORE);

        locales = (String[]) ArrayUtils.addAll(locales, localesMore);
        HashSet<Locale> localesSet = new HashSet<Locale>();
        // always include the default locale
        localesSet.add(Locale.getDefault());
        if (!ArrayUtils.isEmpty(locales)) {
            // convert from strings to Locales
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < locales.length; i++) {
                localesSet.add(getLocaleFromString(locales[i]));
            }
        }
        // Sort Locales and remove duplicates
        Locale[] localesArray = localesSet.toArray(new Locale[localesSet.size()]);
        Arrays.sort(localesArray, new LocaleComparator());
        return localesArray;
    }

    /**
     * Comparator for sorting locale by DisplayName
     */
    static final class LocaleComparator implements Comparator<Locale> {
        /**
         * Compares Locale objects by comparing the DisplayName
         * 
         * @param localeOne
         *        1st Locale Object for comparison
         * @param localeTwo
         *        2nd Locale Object for comparison
         * @return negative, zero, or positive integer
         *        (obj1 charge is less than, equal to, or greater than the obj2 charge)
         */
        public int compare(Locale localeOne, Locale localeTwo) {
            String displayNameOne = localeOne.getDisplayName(localeOne).toLowerCase();
            String displayNameTwo = localeTwo.getDisplayName(localeTwo).toLowerCase();
            return displayNameOne.compareTo(displayNameTwo);
        }

        @Override
        public boolean equals(Object obj) {
            //noinspection SimplifiableIfStatement
            if (obj instanceof LocaleComparator) {
                return super.equals(obj);
            } else {
                return false;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService#getLocaleFromString(java.lang.String)
     */
    public Locale getLocaleFromString(String localeString) {
        // should this just use LocalUtils.toLocale()? - can't - it thinks en_GB is invalid for example
        if (localeString != null) {
            // force en-US (dash separated) values into underscore style
            localeString = StringUtils.replaceChars(localeString, '-', '_');
        } else {
            return null;
        }
        String[] locValues = localeString.trim().split("_");
        if (locValues.length >= 3 && StringUtils.isNotBlank(locValues[2])) {
            return new Locale(locValues[0], locValues[1], locValues[2]); // language, country, variant
        } else if (locValues.length == 2 && StringUtils.isNotBlank(locValues[1])) {
            return new Locale(locValues[0], locValues[1]); // language, country
        } else if (locValues.length == 1 && StringUtils.isNotBlank(locValues[0])) {
            return new Locale(locValues[0]); // language
        } else {
            return Locale.getDefault();
        }
    }


    public void setSakaiProperties(SakaiProperties sakaiProperties) {
        this.sakaiProperties = sakaiProperties;
    }

    public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
        this.threadLocalManager = threadLocalManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * @deprecated do not use this anymore, use {@link #getConfigData()} to get all properties
     */
    public Properties getProperties() {
        return sakaiProperties.getProperties();
    }


    // new config handling code - 08 Sept 2011

    private ConcurrentHashMap<String, ConfigItemImpl> configurationItems = new ConcurrentHashMap<String, ConfigItemImpl>();
    private HashSet<String> secureConfigurationKeys = new HashSet<String>();
    protected ConcurrentHashMap<String, WeakReference<ConfigurationListener>> listeners = new ConcurrentHashMap<String, WeakReference<ConfigurationListener>>();

    /**
     * INTERNAL
     * Adds a set of config items using the data from a set of properties
     * @param p the properties
     * @param source the source name
     */
    protected void addProperties(Properties p, String source) {
        if (p != null) {
            if (source == null || "".equals(source)) {
                source = UNKNOWN;
            }
            log.info("Adding "+p.size()+" properties from "+source);
            for (Enumeration<Object> e = p.keys(); e.hasMoreElements(); /**/) {
                String name = (String) e.nextElement();
                String value = p.getProperty(name);
		// KNL-1361 - Add support for system-scoped properties
		if ( name != null && name.endsWith(SAKAI_SYSTEM_PROPERTY_SUFFIX) && 
			name.length() > SAKAI_SYSTEM_PROPERTY_SUFFIX.length() ) {
			name = name.substring(0,name.length()-SAKAI_SYSTEM_PROPERTY_SUFFIX.length());
			System.setProperty(name, value);
			log.info("Promoted to system property: "+name);
			continue;
		}
                ConfigItemImpl ci = new ConfigItemImpl(name, value, source);
                this.addConfigItem(ci, source);
            }
        }
    }

    /**
     * INTERNAL
     * Adds a set of config items from a list
     * @param list the list
     */
    protected void addConfigList(List<ConfigItem> list) {
        if (list != null && !list.isEmpty()) {
            log.info("Adding "+list.size()+" config items from a list");
            for (ConfigItem configItem : list) {
                this.registerConfigItem(configItem);
            }
        }
    }

    /**
     * INTERNAL
     * Adds the config item if it does not exist OR updates it if it does
     * 
     * @param configItem the config item
     * @param source the source of the update
     * @return the config item if it was add or null if not (only case would be if the input was null)
     */
    protected ConfigItemImpl addConfigItem(ConfigItemImpl configItem, String source) {
        ConfigItemImpl ci = null;
        if (configItem != null) {
            ConfigItemImpl currentCI = null;
            if (configurationItems.containsKey(configItem.getName())) {
                // item exists
                currentCI = configurationItems.get(configItem.getName());
            }

            // notify the before listeners
            boolean haltProcessing = false;
            if (this.listeners != null && !this.listeners.isEmpty()) {
                for (Entry<String, WeakReference<ConfigurationListener>> entry : this.listeners.entrySet()) {
                    // check if any listener refs are no longer valid
                    ConfigurationListener listener = entry.getValue().get();
                    if (listener != null) {
                        try {
                            ConfigItem rvci = listener.changing(currentCI, configItem);
                            //noinspection StatementWithEmptyBody
                            if (rvci == null) {
                                // continue
                            } else if (rvci instanceof BlockingConfigItem) {
                                haltProcessing = true;
                                log.info("add configItem ("+configItem+") processing halted by "+listener);
                                break; // HALT processing
                            } else {
                                // merge in the safe changes to the config item
                                configItem.merge(rvci);
                            }
                        } catch (Exception e) {
                            log.warn("Exception when calling listener ("+listener+"): "+e);
                        }
                    } else {
                        // cleanup bad listener ref
                        this.listeners.remove(entry.getKey());
                    }
                }
            }

            if (!haltProcessing) {
                // update the config item
                boolean changed = false;
                if (currentCI != null) {
                    // update it
                    if (!SOURCE_GET_STRINGS.equals(source)) {
                        // only update if the source is not the getStrings() method
                        currentCI.changed(configItem.getValue(), source);
                        changed = true;
                        if (!currentCI.isRegistered() && configItem.isRegistered()) {
                            // need to force items which are not yet registered to be registered
                            currentCI.registered = true;
                        }
                    }
                    ci = currentCI;
                } else {
                    // add the new one
                    configItem.setSource(source);
                    if (secureConfigurationKeys.contains(configItem.getName())) {
                        configItem.secured = true;
                    }
                    configurationItems.put(configItem.getName(), configItem);
                    ci = configItem;
                    changed = true;
                }

                // notify the after listeners (only if something changed)
                if (changed) {
                    if (this.listeners != null && !this.listeners.isEmpty()) {
                        for (Entry<String, WeakReference<ConfigurationListener>> entry : this.listeners.entrySet()) {
                            // check if any listener refs are no longer valid
                            ConfigurationListener listener = entry.getValue().get();
                            if (listener != null) {
                                try {
                                    listener.changed(ci, currentCI);
                                } catch (Exception e) {
                                    log.warn("Exception when calling listener ("+listener+"): "+e);
                                }
                            } else {
                                // cleanup bad listener ref
                                this.listeners.remove(entry.getKey());
                            }
                        }
                    }
                }
                // DONE with notifying listeners
            }
        }
        return ci;
    }

    /**
     * INTERNAL
     * Finds a config item by name, use this whenever retrieving the item for lookup
     * 
     * @param name the key name for the config value
     * @return the config item OR null if none exists
     */
    protected ConfigItemImpl findConfigItem(String name, Object defaultValue) {
        ConfigItemImpl ci = null;
        if (name != null && !"".equals(name)) {
            ci = configurationItems.get(name);
            if (ci == null) {
                // add unregistered when not found for tracking later
                ConfigItemImpl configItemImpl = new ConfigItemImpl(name);
                configItemImpl.setDefaultValue(defaultValue);
                configItemImpl.setSource("get");
                this.addConfigItem(configItemImpl, "get");
            } else {
                // update the access log
                ci.requested();
                // https://jira.sakaiproject.org/browse/KNL-1130 - assume string has no default in cases where it is "" or null
                if (ServerConfigurationService.TYPE_STRING.equals(ci.type)) {
                    ci.defaulted = !(defaultValue == null || "".equals(defaultValue));
                } else if (defaultValue != null) {
                    ci.defaulted = true;
                }
                if (!ci.isRegistered()) {
                    // we do not return unregistered config values
                    ci = null;
                }
            }
        }
        return ci;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService#getConfig(java.lang.String, java.lang.Object)
     */
    public <T> T getConfig(String name, T defaultValue) {
        T returnValue;
        Object value = null;
        ConfigItem ci = configurationItems.get(name);
        if (ci != null && ci.getValue() != null) {
            value = ci.getValue();
        }
        if (defaultValue == null) {
            returnValue = (T) this.getString(name);
            if ("".equals(returnValue)) {
                returnValue = null;
            }
        } else {
            if (defaultValue instanceof Number) {
                int num = ((Number) defaultValue).intValue();
                int intValue = this.getInt(name, num);
                returnValue = (T) Integer.valueOf(intValue);
            } else if (defaultValue instanceof Boolean) {
                boolean bool = (Boolean) defaultValue;
                boolean boolValue = this.getBoolean(name, bool);
                returnValue = (T) Boolean.valueOf(boolValue);
            } else if (defaultValue instanceof String) {
                returnValue = (T) this.getString(name, (String) defaultValue);
            } else if (defaultValue.getClass().isArray()) {
                returnValue = (T) value;
            } else {
                returnValue = (T) this.getRawProperty(name);
            }
        }
        return returnValue;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService#getConfigItem(java.lang.String)
     */
    public ConfigItem getConfigItem(String name) {
        ConfigItem ci = configurationItems.get(name);
        if (ci != null) {
            ci = ci.copy();
        }
        return ci;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService#getConfigData()
     */
    public ConfigData getConfigData() {
        ArrayList<ConfigItem> configItems = new ArrayList<ConfigItem>(configurationItems.values());
        return new ConfigDataImpl(configItems);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService#registerConfigItem(org.sakaiproject.component.api.ServerConfigurationService.ConfigItem)
     */
    public ConfigItem registerConfigItem(ConfigItem configItem) {
        if (configItem == null) {
            throw new IllegalArgumentException("configItem must be set");
        }
        ConfigItemImpl ci = null;
        if (StringUtils.isNotBlank(configItem.getName())) {
            ci = new ConfigItemImpl(configItem.getName(), configItem.getValue(), configItem.getSource());
            if (configItem.getValue() != null) {
                ci.setValue(configItem.getValue());
            }
            if (configItem.getDefaultValue() != null) {
                ci.setDefaultValue(configItem.getDefaultValue());
            }
            ci = this.addConfigItem(ci, ci.getSource());
        } else {
            log.warn("Skipping registering invalid config item (name not set): "+configItem);
        }
        return ci;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.component.api.ServerConfigurationService#registerListener(org.sakaiproject.component.api.ServerConfigurationService.ConfigurationListener)
     */
    public void registerListener(ConfigurationListener configurationListener) {
        if (configurationListener != null) {
            String name = configurationListener.getClass().getName() + "@" + configurationListener.hashCode();
            WeakReference<ConfigurationListener> ref = new WeakReference<ConfigurationListener>(configurationListener);
            this.listeners.put(name, ref);
        }
    }

}
