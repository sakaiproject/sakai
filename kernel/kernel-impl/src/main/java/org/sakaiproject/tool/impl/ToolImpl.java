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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * Tool is a utility class that implements the Tool interface.
 * </p>
 */
public class ToolImpl implements Tool, Comparable
{
	/** The access security. */
	protected Tool.AccessSecurity m_accessSecurity = Tool.AccessSecurity.PORTAL;

	/** The tool Manager that possesses the RessourceBundle. */
	private ToolManager m_toolManager;

	/** The set of categories. */
	protected Set m_categories = new HashSet();

	/** The description string. */
	protected String m_description = null;

	/** The configuration properties that are set by registration and may not be changed by confguration. */
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

	/** Localization data.  */
	public ResourceLoader m_title_local = null;
	public Map m_title_bundle = null;

	/**
	 * Construct
	 */
	public ToolImpl(ToolManager activeToolManager)
	{
		m_toolManager = activeToolManager;
	}

	/**
	 * @inheritDoc
	 */
	public int compareTo(Object obj)
	{
		// let it throw a class case exception if the obj is not some sort of Tool
		org.sakaiproject.tool.api.Tool tool = (org.sakaiproject.tool.api.Tool) obj;

		// do an id based
		return getId().compareTo(tool.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ToolImpl))
		{
			return false;
		}

		return ((ToolImpl) obj).getId().equals(getId());
	}

	/**
	 * @inheritDoc
	 */
	public Tool.AccessSecurity getAccessSecurity()
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
		final String localizedToolDescription = m_toolManager.getLocalizedToolProperty(this.getId(), "description");

		if(localizedToolDescription == null)
		{
			return m_description;
		}
		else
		{
			return localizedToolDescription;
		}
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
		// combine the final and mutable, and return a copy so that it is read only
		Properties rv = new Properties();
		rv.putAll(m_finalConfig);
		rv.putAll(m_mutableConfig);
		return rv;
	}

	/**
	 * @inheritDoc
	 *
	 *	Modified to fix SAK-8908 by Mark Norton.
	 *	This implementation of getTitle() uses a three tier lookup strategy:
	 *	<OL>
	 *	<LI>If the title is present in a central tool bundle, use it.</LI>
	 *	<LI>If there is a tool title resource bundle in the tool package, use it.</LI>
	 *	<LI>Otherwise default to the title registered in the tool registration file.</LI>
	 *	</OL>
	 */
	public String getTitle()
	{
		final String centralToolTitle = m_toolManager.getLocalizedToolProperty(this.getId(), "title");
		if (centralToolTitle != null)
			return centralToolTitle;

		String localizedToolTitle = null;
		// Titles have extra logic that isn't present for descriptions (WHY WHY WHY)
		if (m_title_bundle != null)
		{
			//	Get the user's current locale preference.
			ResourceLoader rl = new ResourceLoader();
			String loc =rl.getLocale().toString();
			//	Attempt to get the properties corresponding to that locale.
			Properties props = (Properties) m_title_bundle.get(loc);
			//	If a localized set doesn't exist, try for a default set.
			if (props == null)
				props = (Properties) m_title_bundle.get("DEFAULT");
			//	Get the localized tool title.
			if (props != null)
				localizedToolTitle = (String) props.get ("title");
		}
		if (localizedToolTitle != null)
			return localizedToolTitle;

		//	Use the the default tool title from tool definition file.
		return m_title;
	}

	/**
	 * {@inheritDoc}
	 */
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
	public void setAccessSecurity(Tool.AccessSecurity access)
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
