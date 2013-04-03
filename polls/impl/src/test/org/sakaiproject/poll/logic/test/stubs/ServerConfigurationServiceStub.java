/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.poll.logic.test.stubs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.sakaiproject.component.api.ServerConfigurationService;

public class ServerConfigurationServiceStub implements ServerConfigurationService {
	
	
	Map properties = new HashMap();
	
	public String getAccessPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAccessUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getBoolean(String name, boolean dflt) {
		if (properties.get(name) != null) {
			return (Boolean)properties.get(name);
		}
		
		return dflt;
	}

	public List getDefaultTools(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getGatewaySiteId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHelpUrl(String helpContext) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getInt(String name, int dflt) {
		return dflt;
	}

	public String getLoggedOutUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPortalUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSakaiHomePath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerIdInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString(String name) {
		return getString(name,"");
	}

	public String getString(String name, String dflt) {
		if (properties.get(name) != null) {
			return (String)properties.get(name);
		}
		return dflt;
	}

	public String[] getStrings(String name) {
		// TODO Auto-generated method stub
		return new String[0];
	}

	public List<String> getToolCategories(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, List<String>> getToolCategoriesAsMap(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getToolOrder(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getToolToCategoryMap(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * Returns true if selected tool is contained in pre-initialized list of selected items
	 * @parms toolId id of the selected tool
	 */
	public boolean toolGroupIsSelected(String groupName, String toolId) {
		return false;
	}

	 /*
	  * Returns true if selected tool is contained in pre-initialized list of required items
	  * @parms toolId id of the selected tool
	  */
	public boolean toolGroupIsRequired(String groupName, String toolId) {
		return false;
	}		
	
	/**
	 * Access the list of groups by category (site type)
	 * 
	 * @param category
	 *			 The tool category
	 * @return An ordered list of tool ids (String) indicating the desired tool display order, or an empty list if there are none for this category.
	 */
	public List getCategoryGroups(String category){		
		return null;
	}
	
	/**
	 * Access the list of tools by group
	 * 
	 * @param category
	 *			 The tool category
	 * @return An unordered list of tool ids (String) in selected group, or an empty list if there are none for this category.
	 */
	public List getToolGroup(String category) {
		return null;
	}

	public List getToolsRequired(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserHomeUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setProperty(String key,Object value) {
		properties.put(key,value);
	}

	public String getRawProperty(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    public <T> T getConfig(String name, T defaultValue) {
        return (T) properties.get(name);
    }

    public ConfigItem getConfigItem(String name) {
        return null;
    }

    public ConfigData getConfigData() {
        return null;
    }

    public ConfigItem registerConfigItem(ConfigItem configItem) {
        return null;
    }

    public void registerListener(ConfigurationListener configurationListener) {
        
    }

    public Locale[] getSakaiLocales() {
        return new Locale[] {Locale.getDefault()};
    }

    public Locale getLocaleFromString(String localeString) {
        return Locale.getDefault();
    }

}
