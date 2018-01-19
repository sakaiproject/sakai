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

package org.sakaiproject.tool.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Xml;

/**
 * <p>
 * ToolComponent is the standard implementation of the Sakai Tool API.
 * </p>
 */
@Slf4j
public abstract class ToolComponent implements ToolManager
{
	/** Key in the ThreadLocalManager for binding our current placement. */
	protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

	/** Key in the ThreadLocalManager for binding our current tool. */
	protected final static String CURRENT_TOOL = "sakai:ToolComponent:current.tool";
	
	/** Key in the ToolConfiguration Properties for checking what permissions a tool needs in order to be visible */
	protected static final String TOOLCONFIG_REQUIRED_PERMISSIONS = "functions.require";

	//Tool placement property for visibility
	public static final String PORTAL_VISIBLE = "sakai-portal:visible";
	
	/** The registered tools. */
	protected Map<String,Tool> m_tools = new ConcurrentHashMap<String,Tool>();

	/** tool ids to be hidden - their catagories don't matter, they don't show up on any catagorized listing. */
	protected String[] m_toolIdsToHide = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the ThreadLocalManager collaborator.
	 */
	protected abstract ThreadLocalManager threadLocalManager();
	
	/**
	 * @return the SecurityService collaborator.
	 */
	protected abstract SecurityService securityService();
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** tool ids to be stealthed (hidden). */
	protected Collection<String> m_stealthToolIds = null;

	/**
	 * Configuration - set the list of tool ids to be "stealthed". A stealthed tool does not show up in a category
	 * list of tools. The list of stealthed tools is set by the Sakai distribution.
	 * If a deployment wishes to override the values set they should use the {@link #setHiddenTools(String)} and
	 * {@link #setVisibleTools(String)}
	 *
	 * @param toolIds
	 *        The comma-separated list of tool ids to be stealthed.
	 */
	public void setStealthTools(String toolIds)
	{
		if ((toolIds == null) || (toolIds.length() == 0))
		{
			m_stealthToolIds = null;
		}
		else
		{
			m_stealthToolIds = new Vector<String>();
			String[] items = StringUtil.split(toolIds, ",");
			for (int i = 0; i < items.length; i++)
			{
				m_stealthToolIds.add(items[i]);
			}
		}
	}

	/** tool ids to be visible, not hidden, even if marked hidden or stealthed. */
	protected Collection<String> m_visibleToolIds = null;

	/**
	 * Configuration - set the list of tool ids to be visible, not hidden, even if marked hidden or stealthed.
	 *
	 * @param toolIds
	 *        The comma-separated list of tool ids to be visible.
	 */
	public void setVisibleTools(String toolIds)
	{
		if ((toolIds == null) || (toolIds.length() == 0))
		{
			m_visibleToolIds = null;
		}
		else
		{
			m_visibleToolIds = new Vector<String>();
			String[] items = StringUtil.split(toolIds, ",");
			for (int i = 0; i < items.length; i++)
			{
				m_visibleToolIds.add(items[i]);
			}
		}
	}

	/** tool ids to be hidden, adding to the stealth list (but not trumping the visible list) */
	protected Collection<String> m_hiddenToolIds = null;

	/**
	 * Configuration - set the list of tool ids to be hidden.
	 *
	 * @param toolIds
	 *        The comma-separated list of tool ids to be hidden.
	 */
	public void setHiddenTools(String toolIds)
	{
		if ((toolIds == null) || (toolIds.length() == 0))
		{
			m_hiddenToolIds = null;
		}
		else
		{
			m_hiddenToolIds = new Vector<String>();
			String[] items = StringUtil.split(toolIds, ",");
			for (int i = 0; i < items.length; i++)
			{
				m_hiddenToolIds.add(items[i]);
			}
		}
	}
	
	
	/**
	 * The ResourceLoader for getting tool internationalised tool properties.
	 * May be null.
	 */
	private ResourceLoader m_loader;
	
	/**
	 * Setter for ResourceLoader.
	 */
	public void setResourceLoader(ResourceLoader loader)
	{
		m_loader = loader;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// compute the tools to hide: these are the stealth tools plus the hidden tools, minus the visible ones
		Collection<String> toHide = new HashSet<String>();

		if (m_stealthToolIds != null)
		{
			toHide.addAll(m_stealthToolIds);
		}

		if (m_hiddenToolIds!= null)
		{
			toHide.addAll(m_hiddenToolIds);
		}

		if (m_visibleToolIds != null)
		{
			toHide.removeAll(m_visibleToolIds);
		}

		// collect the hiddens for logging
		StringBuilder hidden = new StringBuilder();

		if (!toHide.isEmpty())
		{
			m_toolIdsToHide = new String[toHide.size()];
			int pos = 0;
			
			for (String i : toHide) {
				
				m_toolIdsToHide[pos] = i;

				hidden.append(m_toolIdsToHide[pos]);
				hidden.append(" ");

				pos++;
			}
			Arrays.sort(m_toolIdsToHide);
		}

		log.info("init(): hidden tools: " + hidden.toString());
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Set<Tool> findTools(Set categories, Set keywords)
	{
		Set<Tool> rv = new HashSet<Tool>();

		for (Iterator<Tool> i = m_tools.values().iterator(); i.hasNext();)
		{
			Tool tool = (Tool) i.next();
			if (matchCriteria(categories, tool.getCategories()) && matchCriteria(keywords, tool.getKeywords()))
			{
				// add if not hidden (requests for no (null) category include all, even hidden items)
				if ((categories == null) || (m_toolIdsToHide == null) || (Arrays.binarySearch(m_toolIdsToHide, tool.getId()) < 0))
				{
					rv.add(tool);
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Placement getCurrentPlacement()
	{
		return (Placement) threadLocalManager().get(CURRENT_PLACEMENT);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tool getCurrentTool()
	{
		return (Tool) threadLocalManager().get(CURRENT_TOOL);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tool getTool(String id)
	{
		return (id != null) ? (Tool) m_tools.get(id) : null;
	}

	/**
	 * Check the target values for a match in the criteria. If criteria is empty or null, the target is a match.
	 *
	 * @param criteria
	 *        The set of String values that is the criteria - any one in the target is a match
	 * @param target
	 *        The set of String values to check against the criteria.
	 * @return true if the target meets the criteria, false if not.
	 */
	@SuppressWarnings("unchecked")
	protected boolean matchCriteria(Set criteria, Set target)
	{
		if ((criteria == null) || (criteria.isEmpty())) return true;

		for (Iterator i = target.iterator(); i.hasNext();)
		{
			String t = (String) i.next();
			if (criteria.contains(t)) return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	
	public void register(Document toolXml)
	{
		Element root = toolXml.getDocumentElement();
		if (!root.getTagName().equals("registration"))
		{
			log.info("register: invalid root element (expecting \"registration\"): " + root.getTagName());
			return;
		}

		// read the children nodes (tools)
		NodeList rootNodes = root.getChildNodes();
		final int rootNodesLength = rootNodes.getLength();
		for (int i = 0; i < rootNodesLength; i++)
		{
			Node rootNode = rootNodes.item(i);
			if (rootNode.getNodeType() != Node.ELEMENT_NODE) continue;
			Element rootElement = (Element) rootNode;

			// for tool
			if (rootElement.getTagName().equals("tool"))
			{
				org.sakaiproject.tool.impl.ToolImpl tool = new org.sakaiproject.tool.impl.ToolImpl(this);

				tool.setId(rootElement.getAttribute("id").trim());
				tool.setTitle(rootElement.getAttribute("title").trim());
				tool.setDescription(rootElement.getAttribute("description").trim());
				tool.setHome(StringUtils.trimToNull(rootElement.getAttribute("home")));

				if ("tool".equals(rootElement.getAttribute("accessSecurity")))
				{
					tool.setAccessSecurity(Tool.AccessSecurity.TOOL);
				}
				else
				{
					tool.setAccessSecurity(Tool.AccessSecurity.PORTAL);
				}

				// collect values for these collections
				Properties finalConfig = new Properties();
				Properties mutableConfig = new Properties();
				Set<String> categories = new HashSet<String>();
				Set<String> keywords = new HashSet<String>();

				NodeList kids = rootElement.getChildNodes();
				final int kidsLength = kids.getLength();
				for (int k = 0; k < kidsLength; k++)
				{
					Node kidNode = kids.item(k);
					if (kidNode.getNodeType() != Node.ELEMENT_NODE) continue;
					Element kidElement = (Element) kidNode;

					// for configuration
					if (kidElement.getTagName().equals("configuration"))
					{
						String name = kidElement.getAttribute("name").trim();
						String value = kidElement.getAttribute("value").trim();
						String type = kidElement.getAttribute("type").trim();
						if (name.length() > 0)
						{
							if ("final".equals(type))
							{
								finalConfig.put(name, value);
							}
							else
							{
								mutableConfig.put(name, value);
							}
						}
					}

					// for category
					if (kidElement.getTagName().equals("category"))
					{
						String name = kidElement.getAttribute("name").trim();
						if (name.length() > 0)
						{
							categories.add(name);
						}
					}

					// for keyword
					if (kidElement.getTagName().equals("keyword"))
					{
						String name = kidElement.getAttribute("name").trim();
						if (name.length() > 0)
						{
							keywords.add(name);
						}
					}
				}

				// set the tool's collected values
				tool.setRegisteredConfig(finalConfig, mutableConfig);
				tool.setCategories(categories);
				tool.setKeywords(keywords);

				register(tool);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void register(File toolXmlFile)
	{
		String path = toolXmlFile.getAbsolutePath();
		if (!path.endsWith(".xml"))
		{
			log.info("register: skipping non .xml file: " + path);
			return;
		}

		log.info("register: file: " + path);

		Document doc = Xml.readDocument(path);
		register(doc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void register(InputStream toolXmlStream)
	{
		Document doc = Xml.readDocumentFromStream(toolXmlStream);
		try
		{
			toolXmlStream.close();
		}
		catch (Exception e)
		{
		}

		register(doc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void register(Tool tool)
	{
		m_tools.put(tool.getId(), tool);
	}

	/**
	 * Establish the Tool associated with the current request / thread
	 *
	 * @param ToolImpl
	 *        The current Tool, or null if there is none.
	 */
	protected void setCurrentPlacement(Placement placement)
	{
		threadLocalManager().set(CURRENT_PLACEMENT, placement);
	}

	/**
	 * Establish the Tool associated with the current request / thread
	 *
	 * @param ToolImpl
	 *        The current Tool, or null if there is none.
	 */
	protected void setCurrentTool(Tool tool)
	{
		threadLocalManager().set(CURRENT_TOOL, tool);
	}

	/**
	 * Add the file passed as part of the tool title localization resource bundled based on it's embedded locale code.
	 *
	 * @param toolId Id string of the tool being set.
	 * @param filename Full filename of the properties file to be added to the resource bundle.
	 * @author Mark Norton for SAK-8908
	 */
	@SuppressWarnings("unchecked")
	public void setResourceBundle (String toolId, String filename)
	{
		File file = new File(filename);
		String shortname = file.getName();
		org.sakaiproject.tool.impl.ToolImpl tool = (org.sakaiproject.tool.impl.ToolImpl) this.getTool(toolId);

		//	Initialize the HashSet if empty.
		if (tool.m_title_bundle == null)
			tool.m_title_bundle = new HashMap<String,Properties>();

		//	Parse out the locale code.
		String locale = null;
		if (shortname.indexOf('_') == -1)
			locale = "DEFAULT";
		else {
			//	Extract the substring after the first underbar to the first period.
			locale = shortname.substring (shortname.indexOf('_')+1, shortname.lastIndexOf('.'));
		}

		//	Load the Properties file.
		Properties props = new Properties ();
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			props.load(in);
		}
		catch (IOException ex) {
			log.warn ("Unable to load "+filename+" as a tool localizaton resource in tool "+tool.getId());
			props = null;
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					
				}
			}
		}

		//	Add the properties set to the resource bundle.
		if (props != null)
			tool.m_title_bundle.put (locale, props);
		
	}
	
	/**
	 * Check whether a tool is visible to the current user in this site,
	 * depending on permissions required to view the tool.
	 * 
	 * The optional tool configuration tag "functions.require" describes a
	 * set of permission lists which decide the visibility of the tool link
	 * for this site user. Lists are separated by "|" and permissions within a
	 * list are separated by ",". Users must have all the permissions included in
	 * at least one of the permission lists.
	 *
	 * For example, a value like "section.role.student,annc.new|section.role.ta"
	 * would let a user with "section.role.ta" see the tool, and let a user with
	 * both "section.role.student" AND "annc.new" see the tool, but not let a user
	 * who only had "section.role.student" see the tool.
	 *
	 * If the configuration tag is not set or is null, then all users see the tool.
	 * 
	 * Based on: portal/portal-impl/impl/src/java/org/sakaiproject/portal/charon/ToolHelperImpl.java
	 * 
	 * @param site		Site this tool is in
	 * @param config	ToolConfiguration of the tool in the site
	 * @return
	 */
	public boolean isVisible(Site site, ToolConfiguration config) {
		
		String toolPermissionsStr = config.getConfig().getProperty(TOOLCONFIG_REQUIRED_PERMISSIONS);
		if (log.isDebugEnabled()) {
			log.debug("tool: " + config.getToolId() + ", permissions: " + toolPermissionsStr);
		}

		//no special permissions required, it's visible
		if(StringUtils.isBlank(toolPermissionsStr)) {
			return true;
		}
		
		//check each set, if multiple permissions in the set, must have all.
		String[] toolPermissionsSets = StringUtils.split(toolPermissionsStr, '|');
		for (int i = 0; i < toolPermissionsSets.length; i++){
			String[] requiredPermissions = StringUtils.split(toolPermissionsSets[i], ',');
			boolean allowed = true;
			for (int j = 0; j < requiredPermissions.length; j++) {
				//since all in a set are required, if we are missing just one permission, set false, break and continue to check next set
				//as that set may override and allow access
				if (!securityService().unlock(requiredPermissions[j].trim(), site.getReference())){
					allowed = false;
					break;
				}
			}
			//if allowed, we have matched the entire set so are satisfied
			//otherwise we will check the next set
			if(allowed) {
				return true;
			}
		}
		
		//no sets were completely matched
		return false;
	}
	
	
	/**
	 * Get optional Localized Tool Properties (i.e. tool title, description)
	 **/
	public String getLocalizedToolProperty(String toolId, String key) {
		if (m_loader == null) {
			return null;
		}
			
		final String toolProp = m_loader.getString(toolId + "." + key, "");
		
		if (toolProp.length() < 1 || toolProp.equals("")) {
			return null;
		}
		else {
			return toolProp;
		}
	}
	
	/**
	 * The optional tool configuration tag "functions.require" describes a
	 * set of permission lists which decide the visibility of the tool link
	 * for this site user. Lists are separated by "|" and permissions within a
	 * list are separated by ",". Users must have all the permissions included in
	 * at least one of the permission lists.
	 *
	 * For example, a value like "section.role.student,annc.new|section.role.ta"
	 * would let a user with "section.role.ta" see the tool, and let a user with
	 * both "section.role.student" AND "annc.new" see the tool, but not let a user
	 * who only had "section.role.student" see the tool.
	 *
	 * If the configuration tag is not set or is null, then all users see the tool.
	 */
	public boolean allowTool(Site site, Placement placement)
	{
		if(allowToolHelper(site, placement)){
			if(!securityService().isSuperUser()){
				try{
					//delegated access sets a session attribute that determines if the user can't view a tool in a site
					//delegatedaccess.deniedToolsMap = SiteId => List{toolid, toolid ...}
					//if this tool shows up, return false, otherwise return true

					Session session = SessionManager.getCurrentSession();
					if(session.getAttribute("delegatedaccess.deniedToolsMap") != null && ((Map) session.getAttribute("delegatedaccess.deniedToolsMap")).containsKey(site.getReference())
							&& arrayContains(((Map) session.getAttribute("delegatedaccess.deniedToolsMap")).get(site.getReference()), placement.getToolId())){
						return false;
					}
					if(session.getAttribute("delegatedaccess.deniedToolsMap") == null ||
							!((Map<String, String[]>) session.getAttribute("delegatedaccess.deniedToolsMap")).containsKey(site.getReference())
							|| ((Map<String, String[]>) session.getAttribute("delegatedaccess.deniedToolsMap")).get(site.getReference()) == null){
						//a delegated access admin would have this map and site (even if it was set to null), if its null, that means the user is just has access to a different site and not this one
						if(site.getMember(session.getUserId()) == null && 
								(site.getProperties().get("shopping-period-public-tools") != null || site.getProperties().get("shopping-period-auth-tools") != null)){
							//this is .anon or .auth role in a site that needs to restrict the tools:
							boolean anonAccess = site.getProperties().get("shopping-period-public-tools") != null 
									&& arrayContains(((String) site.getProperties().get("shopping-period-public-tools")).split(";"), placement.getToolId());
							if(session.getUserId() == null){
								return anonAccess;
							}else{
								return anonAccess || (site.getProperties().get("shopping-period-auth-tools") != null && arrayContains(((String) site.getProperties().get("shopping-period-auth-tools")).split(";"), placement.getToolId()));
							}
						}
					}
				}catch (Exception e) {
				}
			}
			return true;
		}else{
			return false;
		}
	}
	
	private boolean arrayContains(Object obj, String item){
		if(obj != null && obj instanceof String[]){
			String[] array = (String[]) obj;
			for(int i = 0; i < array.length; i++){
				if(array[i].equals(item))
					return true;
			}
		}
		return false;
	}
	
	public boolean allowToolHelper(Site site, Placement placement)
	{
		// No way to render an opinion
		if (placement == null || site == null) return true;

		String requiredPermissionsString = placement.getConfig().getProperty(TOOLCONFIG_REQUIRED_PERMISSIONS);
		if (log.isDebugEnabled()) log.debug("requiredPermissionsString=" + requiredPermissionsString + " for " + placement.getToolId());
		if (requiredPermissionsString == null)
			return true;
		requiredPermissionsString = requiredPermissionsString.trim();
		if (requiredPermissionsString.length() == 0)
			return true;

		String[] allowedPermissionSets = requiredPermissionsString.split("\\|");
		for (int i = 0; i < allowedPermissionSets.length; i++)
		{
			String[] requiredPermissions = allowedPermissionSets[i].split(",");
			if (log.isDebugEnabled()) log.debug("requiredPermissions=" + Arrays.asList(requiredPermissions));
			boolean gotAllInList = true;
			for (int j = 0; j < requiredPermissions.length; j++)
			{
				if (!securityService().unlock(requiredPermissions[j].trim(), site.getReference()))
				{
					gotAllInList = false;
					break;
				}
			}
			if (gotAllInList)
			{
				return true;
			}
		}

		// No permission sets were matched.
		return false;
	}

	/**
	 * Check if the placement is hidden.
	 * @param placement
	 * @return <code>true</code> if the current placement is hidden.
	 */
	public boolean isHidden(Placement placement)
	{
		if (placement == null) return true;
		String visibility = placement.getConfig().getProperty(PORTAL_VISIBLE);
		if ( "false".equals(visibility) ) return true;
		String requiredPermissionsString = StringUtils.trimToNull(placement.getConfig().getProperty(TOOLCONFIG_REQUIRED_PERMISSIONS));
		if (requiredPermissionsString == null)
			return false;
		return requiredPermissionsString.contains("site.upd");
	}
}
