/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.mock;

import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigData;

/**
 * 
 *
 */
public class MockServerConfigurationService implements
		ServerConfigurationService {

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerId()
	 */
	public String getServerId() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerInstance()
	 */
	public String getServerInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerIdInstance()
	 */
	public String getServerIdInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerName()
	 */
	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerUrl()
	 */
	public String getServerUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getHelpUrl(java.lang.String)
	 */
	public String getHelpUrl(String helpContext) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getAccessUrl()
	 */
	public String getAccessUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getAccessPath()
	 */
	public String getAccessPath() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getPortalUrl()
	 */
	public String getPortalUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolUrl()
	 */
	public String getToolUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getGatewaySiteId()
	 */
	public String getGatewaySiteId() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getLoggedOutUrl()
	 */
	public String getLoggedOutUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getUserHomeUrl()
	 */
	public String getUserHomeUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getSakaiHomePath()
	 */
	public String getSakaiHomePath() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getString(java.lang.String)
	 */
	public String getString(String name) {
		return this.getString(name, null);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getString(java.lang.String, java.lang.String)
	 */
	public String getString(String name, String dflt) {
		if("vendor@org.sakaiproject.db.api.SqlService".equalsIgnoreCase(name)) {
			return "hsqldb";
		}
		return dflt;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getStrings(java.lang.String)
	 */
	public String[] getStrings(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getInt(java.lang.String, int)
	 */
	public int getInt(String name, int dflt) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getBoolean(java.lang.String, boolean)
	 */
	public boolean getBoolean(String name, boolean dflt) {
		if("auto.ddl".equalsIgnoreCase(name)) {
			return true;
		}
		return dflt;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getRawProperty(java.lang.String)
	 */
	public String getRawProperty(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolOrder(java.lang.String)
	 */
	public List<String> getToolOrder(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolsRequired(java.lang.String)
	 */
	public List<String> getToolsRequired(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getDefaultTools(java.lang.String)
	 */
	public List<String> getDefaultTools(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolCategories(java.lang.String)
	 */
	public List<String> getToolCategories(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolCategoriesAsMap(java.lang.String)
	 */
	public Map<String, List<String>> getToolCategoriesAsMap(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolToCategoryMap(java.lang.String)
	 */
	public Map<String, String> getToolToCategoryMap(String category) {
		// TODO Auto-generated method stub
		return null;
	}

    public ConfigData getConfigData() {
    	return null;
    }

	public <T> T getConfig(String arg0, T arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public ConfigItem getConfigItem(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ConfigItem registerConfigItem(ConfigItem arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerListener(ConfigurationListener arg0) {
		// TODO Auto-generated method stub
		
	}
	
	// new API introduced in KNL-1051
    public Locale getLocaleFromString(String localeString) {
    	return null;
    }
    
    // new API introduced in KNL-1051
    public Locale[] getSakaiLocales()
    {
    	return null;
    }
    
    // new API introduced in KNL-989
    public List<String> getCategoryGroups(String category)
    {
    	return null;
    }
    
    // new API introduced in KNL-989
    public boolean toolGroupIsRequired(String groupName, String toolId)
    {
    	return false;
    }
    
    // new API introduced in KNL-989
    public List getToolGroup(String category)
    {
    	return null;
    }
    
    // new API introduced in KNL-989
    public boolean toolGroupIsSelected(String groupName, String toolId)
    {
    	return false;
    }

}
