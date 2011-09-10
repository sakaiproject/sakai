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

package org.sakaiproject.component.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.SakaiProperties;
import org.sakaiproject.util.Xml;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * BasicConfigurationService is a basic implementation of the ServerConfigurationService.
 * </p>
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class BasicConfigurationService implements ServerConfigurationService
{
	private static final String SOURCE_GET_STRINGS = "getStrings";

    /** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BasicConfigurationService.class);

	/** The instance id for this app server. */
	private String instanceId = null;

	/** This is computed, joining the configured serverId and the set instanceId. */
	private String serverIdInstance = null;
	
	/** The map of values from the loaded properties wherein property placeholders have
	 * <em>not</em> been dereferenced */
	private Properties rawProperties;

	/** File name within sakai.home for the tool order file. */
	private String toolOrderFile = null;
	private Resource defaultToolOrderResource;

	/** loaded tool orders - map keyed by category of List of tool id strings. */
    private Map m_toolOrders = new HashMap();

	/** required tools - map keyed by category of List of tool id strings. */
	private Map m_toolsRequired = new HashMap();

	/** default tools - map keyed by category of List of tool id strings. */
	private Map m_defaultTools = new HashMap();

	/** default tool categories in order mapped by site type */
	private Map<String, List<String>> m_toolCategoriesList = new HashMap<String, List<String>>();

	/** default tool categories to tool id maps mapped by site type */
	private Map<String, Map<String, List<String>>> m_toolCategoriesMap = new HashMap<String, Map<String, List<String>>>();

	/** default tool id to tool category maps mapped by site type */
	private Map<String, Map<String, String>> m_toolToToolCategoriesMap = new HashMap<String, Map<String, String>>();


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the ThreadLocalManager collaborator.
	 */
	private ThreadLocalManager threadLocalManager;

	/**
	 * @return the SessionManager collaborator.
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
		toolOrderFile = string;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
	    // can enable the output of the complete set of configuration items using: config.dump.to.log
        this.rawProperties = sakaiProperties.getRawProperties();

        // populate the security keys set
        this.secureConfigurationKeys.add("password@javax.sql.BaseDataSource");
        String securedKeys = getRawProperty("config.secured.key.names");
        if (securedKeys != null) {
            String[] keys = securedKeys.split(",");
            for (int i = 0; i < keys.length; i++) {
                String key = StringUtils.trimToNull(keys[i]);
                if (key != null) {
                    this.secureConfigurationKeys.add(key);
                }
            }
        }
        M_log.info("Configured "+this.secureConfigurationKeys.size()+" secured key names: "+this.secureConfigurationKeys);

        // put all the properties into the configuration map
        Map<String, Properties> allSakaiProps = sakaiProperties.getSeparateProperties();
        for (Entry<String, Properties> entry : allSakaiProps.entrySet()) {
            this.addProperties(entry.getValue(), entry.getKey());
        }
        M_log.info("Loaded "+configurationItems.size()+" config items from all initial sources");

		try
		{
			// set a unique instance id for this server run
			// Note: to reduce startup dependency, just use the current time, NOT the id service.
			instanceId = Long.toString(System.currentTimeMillis());

			serverIdInstance = getServerId() + "-" + instanceId;
		}
		catch (Exception t)
		{
			M_log.warn("init(): ", t);
		}

		// load in the tool order, if specified, from the sakai home area
		if (toolOrderFile != null)
		{
			File f = new File(toolOrderFile);
			if (f.exists())
			{
				FileInputStream fis = null;
				try
				{
					fis = new FileInputStream(f);
					loadToolOrder(fis);
				}
				catch (Exception t)
				{
					M_log.warn("init(): trouble loading tool order from : " + toolOrderFile, t);
				}
				finally {
					if (fis != null) {
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			else
			{
				// start with the distributed defaults from the classpath
				try
				{
					loadToolOrder(defaultToolOrderResource.getInputStream());
				}
				catch (Exception t)
				{
					M_log.warn("init(): trouble loading tool order from default toolOrder.xml", t);
				}
			}
		}

		M_log.info("init()");
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
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
        String rv = StringUtils.trimToNull((String) this.rawProperties.get(name));
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
		//return getString(name, dflt, properties);
	    String value = dflt;
	    ConfigItemImpl ci = findConfigItem(name, dflt);
	    if (ci != null) {
	        if (ci.getValue() != null) {
	            value = StringUtils.trimToNull(ci.getValue().toString());
	        } else {
	            value = null;
	        }
	    }
	    return value;
	}

	/*
	private String getString(String name, Properties fromProperties) {
		return getString(name, "", fromProperties);
	}
	
	private String getString(String name, String dflt, Properties fromProperties) {
		String rv = StringUtils.trimToNull((String) fromProperties.get(name));
		if (rv == null) rv = dflt;

		return rv;
	}
	*/

	/**
	 * {@inheritDoc}
	 */
	public String[] getStrings(String name)
	{
		// get the count
		int count = getInt(name + ".count", 0);
		if (count > 0)
		{
			String[] rv = new String[count];
			for (int i = 1; i <= count; i++)
			{
				rv[i - 1] = getString(name + "." + i, "");
			}
			// store the array in the properties
			this.addConfigItem(new ConfigItemImpl(name, rv, TYPE_ARRAY, SOURCE_GET_STRINGS), SOURCE_GET_STRINGS);
			return rv;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInt(String name, int dflt)
	{
		String value = getString(name);

		if (value.length() == 0) return dflt;

		return Integer.parseInt(value);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(String name, boolean dflt)
	{
		String value = getString(name);

		if (value.length() == 0) return dflt;

		return Boolean.valueOf(value).booleanValue();
	}

	/**
	 * {@inheritDoc}
	 */
    public List getToolOrder(String category)
	{
		if (category != null)
		{
			List order = (List) m_toolOrders.get(category);
			if (order != null)
			{
				return order;
			}
		}

		return new Vector();
	}

	/**
	 * {@inheritDoc}
	 */
	public List getToolsRequired(String category)
	{
		if (category != null)
		{
			List order = (List) m_toolsRequired.get(category);
			if (order != null)
			{
				return order;
			}
		}

		return new Vector();
	}

	/**
	 * {@inheritDoc}
	 */
	public List getDefaultTools(String category)
	{
		if (category != null)
		{
			List order = (List) m_defaultTools.get(category);
			if (order != null)
			{
				return order;
			}
		}

		return new Vector();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getToolCategories(String category)
	{
		if (category != null)
		{
			List<String> categories = m_toolCategoriesList.get(category);
			if (categories != null)
			{
				return categories;
			}
		}

		return new Vector();
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, List<String>> getToolCategoriesAsMap(String category)
	{
		if (category != null)
		{
			Map<String, List<String>> categories = m_toolCategoriesMap.get(category);
			if (categories != null)
			{
				return categories;
			}
		}

		return new HashMap();
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getToolToCategoryMap(String category)
	{
		if (category != null)
		{
			Map<String, String> categories = m_toolToToolCategoriesMap.get(category);
			if (categories != null)
			{
				return categories;
			}
		}

		return new HashMap();
	}

	/**
	 * Load this single file as a registration file, loading tools and locks.
	 * 
	 * @param in
	 *        The Stream to load
	 */
	private void loadToolOrder(InputStream in)
	{
		Document doc = Xml.readDocumentFromStream(in);
		Element root = doc.getDocumentElement();
		if (!root.getTagName().equals("toolOrder"))
		{
			M_log.info("loadToolOrder: invalid root element (expecting \"toolOrder\"): " + root.getTagName());
			return;
		}

		// read the children nodes
		NodeList rootNodes = root.getChildNodes();
		final int rootNodesLength = rootNodes.getLength();
		for (int i = 0; i < rootNodesLength; i++)
		{
			Node rootNode = rootNodes.item(i);
			if (rootNode.getNodeType() != Node.ELEMENT_NODE) continue;
			Element rootElement = (Element) rootNode;

			// look for "category" elements
			if (rootElement.getTagName().equals("category"))
			{
				String name = StringUtils.trimToNull(rootElement.getAttribute("name"));
				if (name != null)
				{
					// form a list for this category
					List order = (List) m_toolOrders.get(name);
					if (order == null)
					{
						order = new Vector();
						m_toolOrders.put(name, order);

						List required = new Vector();
						m_toolsRequired.put(name, required);
						List defaultTools = new Vector();
						m_defaultTools.put(name, defaultTools);

                  List<String> toolCategories = new Vector();
                  m_toolCategoriesList.put(name, toolCategories);

                  Map<String, List<String>> toolCategoryMappings = new HashMap();
                  m_toolCategoriesMap.put(name, toolCategoryMappings);
                  
                  Map<String, String> toolToCategoryMap = new HashMap();
                  m_toolToToolCategoriesMap.put(name, toolToCategoryMap);

						// get the kids
						NodeList nodes = rootElement.getChildNodes();
						final int nodesLength = nodes.getLength();
						for (int c = 0; c < nodesLength; c++)
						{
							Node node = nodes.item(c);
							if (node.getNodeType() != Node.ELEMENT_NODE) continue;
							Element element = (Element) node;

							if (element.getTagName().equals("tool"))
							{
                        processTool(element, order, required, defaultTools);
                     }
                     else if (element.getTagName().equals("toolCategory")) {
                        processCategory(element, order, required, defaultTools, 
                           toolCategories, toolCategoryMappings, toolToCategoryMap);
                     }
                  }
					}
				}
			}
		}
	}

   private void processCategory(Element element, List order, List required,
                                  List defaultTools, List<String> toolCategories,
                                  Map<String, List<String>> toolCategoryMappings, 
                                  Map<String, String> toolToCategoryMap) {
      String name = element.getAttribute("id");      
      NodeList nameList = element.getElementsByTagName("name");
      
      if (nameList.getLength() > 0) {
         Element nameElement = (Element) nameList.item(0);
         name = nameElement.getTextContent();
      }
      
      toolCategories.add(name);
      List<String> toolCategoryTools = new Vector();
      toolCategoryMappings.put(name, toolCategoryTools);
      
      NodeList nodes = element.getChildNodes();
      final int nodesLength = nodes.getLength();
      for (int c = 0; c < nodesLength; c++)
      {
         Node node = nodes.item(c);
         if (node.getNodeType() != Node.ELEMENT_NODE) continue;
         Element toolElement = (Element) node;

         if (toolElement.getTagName().equals("tool"))
         {
            String id = processTool(toolElement, order, required, defaultTools);
            toolCategoryTools.add(id);
            toolToCategoryMap.put(id, name);
         }
      }      
   }

   private String processTool(Element element, List order, List required, List defaultTools) {
								String id = StringUtils.trimToNull(element.getAttribute("id"));
								if (id != null)
								{
									order.add(id);
								}

								String req = StringUtils.trimToNull(element.getAttribute("required"));
								if ((req != null) && (Boolean.TRUE.toString().equalsIgnoreCase(req)))
								{
									required.add(id);
								}

								String sel = StringUtils.trimToNull(element.getAttribute("selected"));
								if ((sel != null) && (Boolean.TRUE.toString().equalsIgnoreCase(sel)))
								{
									defaultTools.add(id);
								}
      return id;
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

    public void setDefaultToolOrderResource(Resource defaultToolOrderResource) {
        this.defaultToolOrderResource = defaultToolOrderResource;
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
	        M_log.info("Adding "+p.size()+" properties from "+source);
	        for (Enumeration<Object> e = p.keys(); e.hasMoreElements(); /**/) {
	            String name = (String) e.nextElement();
	            String value = p.getProperty(name);
	            ConfigItemImpl ci = new ConfigItemImpl(name, value, source);
	            this.addConfigItem(ci, source);
	        }
	    }
    }

	/**
     * INTERNAL
	 * Adds the config item if it does not exist OR updates it if it does
	 * 
	 * @param configItem the config item
	 * @param source the source of the update
	 */
	protected void addConfigItem(ConfigItemImpl configItem, String source) {
	    if (configItem != null) {
	        if (configurationItems.containsKey(configItem.getName())) {
	            // update it
	            ConfigItemImpl currentCI = configurationItems.get(configItem.getName());
	            if (!SOURCE_GET_STRINGS.equals(source)) {
	                // only update if the source is not the getStrings() method
	                currentCI.changed(configItem.getValue(), source);
	            }
	        } else {
	            // add it
	            configItem.source = source;
	            if (secureConfigurationKeys.contains(configItem.getName())) {
	                configItem.secured = true;
	            }
	            configurationItems.put(configItem.getName(), configItem);
	        }
	    }
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
                if (defaultValue != null) {
                    configItemImpl.defaulted = true;
                    configItemImpl.setValue(defaultValue);
                }
                configItemImpl.source = "get";
                this.addConfigItem(configItemImpl, "get");
            } else {
                // update the access log
                ci.requested();
                if (defaultValue != null) {
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

	/**
     * Defines the config data holding class
     */
    public static class ConfigDataImpl implements ConfigData {
        public int totalConfigItems = 0;
        public int registeredConfigItems = 0;
        public int unRegisteredConfigItems = 0;
        public String[] sources = new String[0];
        public List<ConfigItem> items = null;

        public ConfigDataImpl(List<ConfigItem> configItems) {
            ArrayList<ConfigItemImpl> cis = new ArrayList<ConfigItemImpl>(configItems.size());
            HashSet<String> sourceSet = new HashSet<String>();
            for (ConfigItem configItem : configItems) {
                if (configItem != null) {
                    cis.add((ConfigItemImpl)configItem.copy());
                    if (configItem.getSource() != null && !"UNKNOWN".equals(configItem.getSource())) {
                        sourceSet.add(configItem.getSource());
                    }
                    totalConfigItems++;
                    if (configItem.isRegistered()) {
                        registeredConfigItems++;
                    } else {
                        unRegisteredConfigItems++;
                    }
                }
            }
            this.sources = sourceSet.toArray(new String[0]);
            Collections.sort(cis);
            this.items = new ArrayList<ConfigItem>(cis);
        }
        public int getTotalConfigItems() {
            return totalConfigItems;
        }
        
        public int getRegisteredConfigItems() {
            return registeredConfigItems;
        }

        public int getUnRegisteredConfigItems() {
            return unRegisteredConfigItems;
        }

        public String[] getSources() {
            return sources;
        }
        
        public List<ConfigItem> getItems() {
            return items;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (ConfigItem ci : items) {
                i++;
                sb.append("  ");
                sb.append(i);
                sb.append(": ");
                sb.append(ci.toString());
                sb.append("\n");
            }
            return "Config items: "+totalConfigItems+" (" + registeredConfigItems + ", " + unRegisteredConfigItems + ")\n" + sb.toString();
        }

    }

    /**
     * Defines the config item holding class
     */
    public static class ConfigItemImpl implements ConfigItem, Comparable<ConfigItem> {
        /**
         * the name/key for this configuration value
         */
        public String name = null;
        /**
         * the actual stored value for this config (may be null or "")
         */
        public Object value = null;
        /**
         * the type of the value (string, int, boolean, array)
         */
        public String type = TYPE_STRING;
        /**
         * the name of the most recent source for this config value (e.g. sakai/sakai.properties)
         */
        public String source = UNKNOWN;
        /**
         * the number of times the config value was requested
         */
        public int requested = 0;
        /**
         * the number of times this config value was changed
         */
        public int changed = 0;
        /**
         * history of the source:value at each change (comma separated)
         */
        public String history = "";
        /**
         * indicates is this config is registered (true) or if it is only requested (false)
         * (requested means someone asked for it but the setting has not been stored in the config service)
         */
        public boolean registered = false;
        /**
         * indicates is this config is has a default value defined
         * (this can only be known for items which are requested)
         */
        public boolean defaulted = false;
        /**
         * indicates is this config value should not be revealed because there are security implications
         */
        public boolean secured = false;


        /**
         * Use this constructor for making requested (unregistered) config items
         */
        public ConfigItemImpl(String name) {
            if (name == null || "".equals(name)) {
                throw new IllegalArgumentException("Config item name must be set");
            }
            this.name = name;
            this.registered = false;
            this.secured = false;
        }

        /**
         * Only use this if you do not know the source
         */
        public ConfigItemImpl(String name, Object value) {
            if (name == null || "".equals(name)) {
                throw new IllegalArgumentException("Config item name must be set");
            }
            this.name = name;
            this.value = value;
            this.type = setValue(value);
            this.registered = true;
            this.secured = false;
        }

        /**
         * MAIN constructor
         */
        public ConfigItemImpl(String name, Object value, String source) {
            this(name, value);
            this.source = source;
        }

        /**
         * Allows overriding the type detection
         */
        public ConfigItemImpl(String name, Object value, String type, String source) {
            this(name, value);
            this.type = type;
            this.source = source;
        }

        /**
         * FULL (really just for copy and testing)
         */
        public ConfigItemImpl(String name, Object value, String type, String source, 
                int requested, int changed, String history, boolean registered, boolean defaulted, boolean secured) {
            this.name = name;
            this.value = value;
            this.type = type;
            this.source = source;
            this.requested = requested;
            this.changed = changed;
            this.history = history;
            this.registered = registered;
            this.defaulted = defaulted;
            this.secured = secured;
        }

        public int requested() {
            this.requested = this.requested + 1;
            return this.requested;
        }

        public int changed(Object value, String source) {
            this.value = value;
            if (source != null) {
                this.source = source;
            }
            this.history = this.source + ":" + (this.value == null ? "null" : (this.secured?"**SECURITY**":this.value)) + "," + this.history;
            this.changed = this.changed + 1;
            return this.changed;
        }

        public <T> T getTypedValue() {
            return (T) this.value;
        }

        /**
         * Duplicate this config item
         * This is mostly used to ensure we do not send the internal objects out where they could be changed
         * @return a config item with all the same values
         */
        public ConfigItem copy() {
            ConfigItem ci = new ConfigItemImpl(this.name, this.value, this.type, this.source, 
                    this.requested, this.changed, this.history, this.registered, this.defaulted, this.secured);
            return ci;
        }
        
        private String setValue(Object value) {
            String type = TYPE_STRING;
            if (value != null) {
                if (value.getClass().isArray()) {
                    this.value = value;
                    type = TYPE_ARRAY;
                } else if (value instanceof Number) {
                    int num = ((Number) value).intValue();
                    this.value = num;
                    type = TYPE_INT;
                } else if (value instanceof Boolean) {
                    boolean bool = ((Boolean) value).booleanValue();
                    this.value = bool;
                    type = TYPE_BOOLEAN;
                } else if (value instanceof String) {
                    this.value = value;
                    type = TYPE_STRING;
                } else {
                    this.value = value;
                    type = UNKNOWN;
                }
            }
            return type;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ConfigItem other = (ConfigItem) obj;
            if (name == null) {
                if (other.getName() != null)
                    return false;
            } else if (!name.equals(other.getName()))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return name + " => "+(secured?"**SECURITY**":value)+"  "+(registered?"R":"U")+","+(defaulted?"D":"N")+":(" + type + " [" + source + "] " + requested + ", " + changed + ", {"+history+"})";
        }

        public int compareTo(ConfigItem ci) {
            return this.name.compareToIgnoreCase(ci.getName());
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        public String getSource() {
            return source;
        }

        public int getRequested() {
            return requested;
        }

        public int getChanged() {
            return changed;
        }

        public String getHistory() {
            return history;
        }

        public boolean isRegistered() {
            return registered;
        }

        public boolean isDefaulted() {
            return defaulted;
        }

        public boolean isSecured() {
            return secured;
        }

    }

    public <T> T getConfig(String name, T defaultValue) {
        T returnValue = defaultValue;
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
                boolean bool = ((Boolean) defaultValue).booleanValue();
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

    public ConfigItem getConfigItem(String name) {
        ConfigItem ci = configurationItems.get(name);
        if (ci != null) {
            ci = ci.copy();
        }
        return ci;
    }

    public ConfigData getConfigData() {
        ArrayList<ConfigItem> configItems = new ArrayList<ConfigItem>(configurationItems.values());
        return new ConfigDataImpl(configItems);
    }

}
