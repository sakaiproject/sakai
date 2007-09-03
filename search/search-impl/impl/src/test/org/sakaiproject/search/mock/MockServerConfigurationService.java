/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.mock;

import java.util.List;
import java.util.Map;

import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * @author ieb
 *
 */
public class MockServerConfigurationService implements ServerConfigurationService
{

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getAccessPath()
	 */
	public String getAccessPath()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getAccessUrl()
	 */
	public String getAccessUrl()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getBoolean(java.lang.String, boolean)
	 */
	public boolean getBoolean(String name, boolean dflt)
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getDefaultTools(java.lang.String)
	 */
	public List getDefaultTools(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getGatewaySiteId()
	 */
	public String getGatewaySiteId()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getHelpUrl(java.lang.String)
	 */
	public String getHelpUrl(String helpContext)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getInt(java.lang.String, int)
	 */
	public int getInt(String name, int dflt)
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getLoggedOutUrl()
	 */
	public String getLoggedOutUrl()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getPortalUrl()
	 */
	public String getPortalUrl()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getSakaiHomePath()
	 */
	public String getSakaiHomePath()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerId()
	 */
	public String getServerId()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerIdInstance()
	 */
	public String getServerIdInstance()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerInstance()
	 */
	public String getServerInstance()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerName()
	 */
	public String getServerName()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerUrl()
	 */
	public String getServerUrl()
	{
		return "http://something:8080/";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getString(java.lang.String)
	 */
	public String getString(String name)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getString(java.lang.String, java.lang.String)
	 */
	public String getString(String name, String dflt)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getStrings(java.lang.String)
	 */
	public String[] getStrings(String name)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolCategories(java.lang.String)
	 */
	public List<String> getToolCategories(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolCategoriesAsMap(java.lang.String)
	 */
	public Map<String, List<String>> getToolCategoriesAsMap(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolOrder(java.lang.String)
	 */
	public List getToolOrder(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolToCategoryMap(java.lang.String)
	 */
	public Map<String, String> getToolToCategoryMap(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolUrl()
	 */
	public String getToolUrl()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolsRequired(java.lang.String)
	 */
	public List getToolsRequired(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getUserHomeUrl()
	 */
	public String getUserHomeUrl()
	{
		return null;
	}

}
