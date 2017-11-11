/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.PortletInfoDD;
import org.apache.pluto.internal.InternalPortletContext;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.tool.api.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Tool is a utility class that implements the Tool interface.
 * </p>
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
@Slf4j
public class PortletTool implements org.sakaiproject.tool.api.Tool, Comparable
{
	/** The access security. */
	protected PortletTool.AccessSecurity m_accessSecurity = PortletTool.AccessSecurity.PORTAL;

	/** The set of categories. */
	protected Set m_categories = new HashSet();

	/** The description string. */
	protected String m_description = null;

	/**
	 * The configuration properties that are set by registration and may not be
	 * changed by confguration.
	 */
	protected Properties m_finalConfig = new Properties();

	/** Home destination. */
	protected String m_home = null;

	/** The well known identifier string. */
	protected String m_id = null;

	/** The set of keywords. */
	protected Set m_keywords = new HashSet();

	/** The configuration properties that may be changed by configuration. */
	protected Properties m_mutableConfig = new Properties();

	/** The title string. */
	protected String m_title = null;

	/** The parsed tool registration (if any) * */
	protected Tool m_tool = null;

	// Note the Tool as a parameter has property data to copy - we
	// copy data from that - and create ourselves as a new instance
	public PortletTool(PortletDD pdd, InternalPortletContext portlet,
			ServletContext portalContext, Tool t)
	{
		String portletSupport = ServerConfigurationService.getString("portlet.support");

		String portletName = pdd.getPortletName();
		String appName = portlet.getApplicationId();

		if (t != null )
		{
			m_id = t.getId();
			m_title = t.getTitle();
			m_description = t.getDescription();
			m_categories = t.getCategories();
			// get the FinalConfig from the tool and make a copy
			// locally as we will add information
			Properties rv = t.getFinalConfig();
			m_finalConfig.putAll(rv);
			m_keywords = t.getKeywords();
			m_mutableConfig = t.getMutableConfig();
			// RegisteredConfig is derived in the getter of this class
			log.info("Portlet registered from tool registration with Sakai toolId="
				+ m_id);
		}
		else
		{
			m_id = "portlet." + portlet.getApplicationId() + "." + portletName;
			PortletInfoDD pidd = pdd.getPortletInfo();
			if (pidd != null)
			{
				m_title = pidd.getShortTitle();
				m_description = pidd.getTitle();
			}
			if (m_title == null) m_title = portletName;
			if (m_description == null) m_description = portletName;

			if ("stealth".equals(portletSupport))
			{
				log.info("Portlet stealth-registered with Sakai toolId=" + m_id);
			}
			else
			{
				m_categories.add("myworkspace");
				m_categories.add("project");
				m_categories.add("course");
				log.info("Portlet auto-registered with Sakai toolId=" + m_id);
			}
		}

		// Indicate that these tools are indeed portlets and where to dispatch
		// the portlet
		m_finalConfig.setProperty(PortalService.TOOL_PORTLET_CONTEXT_PATH, appName);
		m_finalConfig.setProperty(PortalService.TOOL_PORTLET_NAME, portletName);
		m_finalConfig.setProperty(PortalService.TOOL_PORTLET_APP_NAME, appName);

	}

	/**
	 * @inheritDoc
	 */
	public int compareTo(Object obj)
	{
		// let it throw a class case exception if the obj is not some sort of
		// Tool
		org.sakaiproject.tool.api.Tool tool = (org.sakaiproject.tool.api.Tool) obj;

		// do an id based
		return getId().compareTo(tool.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof PortletTool))
		{
			return false;
		}

		return ((PortletTool) obj).getId().equals(getId());
	}

	/**
	 * @inheritDoc
	 */
	public PortletTool.AccessSecurity getAccessSecurity()
	{
		return m_accessSecurity;
	}

	/**
	 * @inheritDoc
	 */
	public Set getCategories()
	{
		return Collections.unmodifiableSet(m_categories);
	}

	/**
	 * @inheritDoc
	 */
	public String getDescription()
	{
		return m_description;
	}

	/**
	 * @inheritDoc
	 */
	public Properties getFinalConfig()
	{
		// return a copy so that it is read only
		Properties rv = new Properties();
		rv.putAll(m_finalConfig);
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHome()
	{
		return m_home;
	}

	/**
	 * @inheritDoc
	 */
	public String getId()
	{
		return m_id;
	}

	/**
	 * @inheritDoc
	 */
	public Set getKeywords()
	{
		return Collections.unmodifiableSet(m_keywords);
	}

	/**
	 * @inheritDoc
	 */
	public Properties getMutableConfig()
	{
		// return a copy so that it is read only
		Properties rv = new Properties();
		rv.putAll(m_mutableConfig);
		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public Properties getRegisteredConfig()
	{
		// combine the final and mutable, and return a copy so that it is read
		// only
		Properties rv = new Properties();
		rv.putAll(m_finalConfig);
		rv.putAll(m_mutableConfig);
		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public String getTitle()
	{
		return m_title;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return getId().hashCode();
	}

	/**
	 * Set the access security.
	 * 
	 * @param access
	 *        The new access security setting.
	 */
	public void setAccessSecurity(PortletTool.AccessSecurity access)
	{
		m_accessSecurity = access;
	}

	/**
	 * Set the categories.
	 * 
	 * @param categories
	 *        The new categories set (Strings).
	 */
	public void setCategories(Set categories)
	{
		m_categories = categories;
	}

	/**
	 * Set the description.
	 * 
	 * @param description
	 *        The description to set.
	 */
	public void setDescription(String description)
	{
		m_description = description;
	}

	public void setHome(String home)
	{
		m_home = home;
	}

	/**
	 * Set the id.
	 * 
	 * @param m_id
	 *        The m_id to set.
	 */
	public void setId(String id)
	{
		m_id = id;
	}

	/**
	 * Set the keywords.
	 * 
	 * @param keywords
	 *        The new keywords set (Strings).
	 */
	public void setKeywords(Set keywords)
	{
		m_keywords = keywords;
	}

	/**
	 * Set the registered configuration.
	 * 
	 * @param config
	 *        The new registered configuration Properties.
	 */
	public void setRegisteredConfig(Properties finalConfig, Properties mutableConfig)
	{
		m_finalConfig = finalConfig;
		if (m_finalConfig == null)
		{
			m_finalConfig = new Properties();
		}

		m_mutableConfig = mutableConfig;
		if (m_mutableConfig == null)
		{
			m_mutableConfig = new Properties();
		}
	}

	/**
	 * Set the title.
	 * 
	 * @param title
	 *        The title to set.
	 */
	public void setTitle(String title)
	{
		m_title = title;
	}
}
