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
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.SakaiProperties;
import org.sakaiproject.util.StringUtil;
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
public class BasicConfigurationService implements ServerConfigurationService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BasicConfigurationService.class);

	/** The instance id for this app server. */
	private String instanceId = null;

	/** This is computed, joining the configured serverId and the set instanceId. */
	private String serverIdInstance = null;

	/** The map of values from the loaded properties wherein property placeholders 
	 * <em>have</em> been dereferenced */
	private Properties properties;
	
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
	private Map<String, List<String>> m_toolCategoriesList = new HashMap();

	/** default tool categories to tool id maps mapped by site type */
	private Map<String, Map<String, List<String>>> m_toolCategoriesMap = new HashMap();

	/** default tool id to tool category maps mapped by site type */
	private Map<String, Map<String, String>> m_toolToToolCategoriesMap = new HashMap();


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
		this.rawProperties = sakaiProperties.getRawProperties();
		this.properties = sakaiProperties.getProperties();

		try
		{
			// set a unique instance id for this server run
			// Note: to reduce startup dependency, just use the current time, NOT the id service.
			instanceId = Long.toString(System.currentTimeMillis());

			serverIdInstance = getServerId() + "-" + instanceId;
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}

		// load in the tool order, if specified, from the sakai home area
		if (toolOrderFile != null)
		{
			File f = new File(toolOrderFile);
			if (f.exists())
			{
				try
				{
					loadToolOrder(new FileInputStream(f));
				}
				catch (Throwable t)
				{
					M_log.warn("init(): trouble loading tool order from : " + toolOrderFile, t);
				}
			}
			else
			{
				// start with the distributed defaults from the classpath
				try
				{
					loadToolOrder(defaultToolOrderResource.getInputStream());
				}
				catch (Throwable t)
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
		return (String) properties.get("serverId");
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
			rv = (String) properties.get("serverUrl");
		}

		return rv;

	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerName()
	{
		return (String) properties.get("serverName");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAccessUrl()
	{
		return getServerUrl() + (String) properties.get("accessPath");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAccessPath()
	{
		return (String) properties.get("accessPath");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHelpUrl(String helpContext)
	{
		String rv = getPortalUrl() + (String) properties.get("helpPath") + "/main";
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
		String rv = (String) threadLocalManager.get(CURRENT_PORTAL_PATH);
		if (rv == null)
		{
			rv = (String) properties.get("portalPath");
		}

		String portalUrl = getServerUrl() + rv;

		return portalUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getToolUrl()
	{
		return getServerUrl() + (String) properties.get("toolPath");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserHomeUrl()
	{
		// get the configured URL (the text "#UID#" will be repalced with the current logged in user id
		// NOTE: this is relative to the server root
		String rv = (String) properties.get("userHomeUrl");

		// form a site based portal id if not configured
		if (rv == null)
		{
			rv = (String) properties.get("portalPath") + "/site/~#UID#";
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
		String rv = (String) properties.get("gatewaySiteId");

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
		String rv = (String) properties.get("loggedOutUrl");
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
		return getString(name, rawProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getString(String name)
	{
		return getString(name, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getString(String name, String dflt)
	{
		return getString(name, dflt, properties);
	}
	
	private String getString(String name, Properties fromProperties) {
		return getString(name, "", fromProperties);
	}
	
	private String getString(String name, String dflt, Properties fromProperties) {
		String rv = StringUtil.trimToNull((String) fromProperties.get(name));
		if (rv == null) rv = dflt;

		return rv;
	}

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
				String name = StringUtil.trimToNull(rootElement.getAttribute("name"));
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
								String id = StringUtil.trimToNull(element.getAttribute("id"));
								if (id != null)
								{
									order.add(id);
								}

								String req = StringUtil.trimToNull(element.getAttribute("required"));
								if ((req != null) && (Boolean.TRUE.toString().equalsIgnoreCase(req)))
								{
									required.add(id);
								}

								String sel = StringUtil.trimToNull(element.getAttribute("selected"));
								if ((sel != null) && (Boolean.TRUE.toString().equalsIgnoreCase(sel)))
								{
									defaultTools.add(id);
								}
      return id;
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

	public Properties getProperties() {
		return properties;
	}

	public void setSakaiProperties(SakaiProperties sakaiProperties) {
		this.sakaiProperties = sakaiProperties;
	}
}
