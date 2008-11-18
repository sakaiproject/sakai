/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.test.mock;

import java.util.List;
import java.util.Map;

import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * @author ieb
 *
 */
public class MockServerConfiguratonService implements ServerConfigurationService
{

	private Map<String, String> values;

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getAccessPath()
	 */
	public String getAccessPath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getAccessUrl()
	 */
	public String getAccessUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getBoolean(java.lang.String, boolean)
	 */
	public boolean getBoolean(String arg0, boolean arg1)
	{
		return "true".equals(values.get(arg0));
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getDefaultTools(java.lang.String)
	 */
	public List getDefaultTools(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getGatewaySiteId()
	 */
	public String getGatewaySiteId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getHelpUrl(java.lang.String)
	 */
	public String getHelpUrl(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getInt(java.lang.String, int)
	 */
	public int getInt(String arg0, int arg1)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getLoggedOutUrl()
	 */
	public String getLoggedOutUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getPortalUrl()
	 */
	public String getPortalUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getSakaiHomePath()
	 */
	public String getSakaiHomePath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerId()
	 */
	public String getServerId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerIdInstance()
	 */
	public String getServerIdInstance()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerInstance()
	 */
	public String getServerInstance()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerName()
	 */
	public String getServerName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerUrl()
	 */
	public String getServerUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getRawProperty(java.lang.String)
	 */
	public String getRawProperty(String name) {
		return getString(name);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getString(java.lang.String)
	 */
	public String getString(String arg0)
	{
		return values.get(arg0);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getString(java.lang.String, java.lang.String)
	 */
	public String getString(String arg0, String arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getStrings(java.lang.String)
	 */
	public String[] getStrings(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolOrder(java.lang.String)
	 */
	public List getToolOrder(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolUrl()
	 */
	public String getToolUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolsRequired(java.lang.String)
	 */
	public List getToolsRequired(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getUserHomeUrl()
	 */
	public String getUserHomeUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getToolCategories(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, List<String>> getToolCategoriesAsMap(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getToolToCategoryMap(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the values
	 */
	public Map<String, String> getValues()
	{
		return values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(Map<String, String> values)
	{
		this.values = values;
	}

}
