/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;

@Slf4j
public abstract class FakeServerConfigurationService implements ServerConfigurationService {
	private Map<String,String> m = new HashMap<String,String>();
	
	public FakeServerConfigurationService() {
		m.put("sitestats.db", "internal");
		m.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
	}
	
	public void printAll() {
		log.debug("-- Start ----------------------------");
		for(Object key : m.keySet()) {
			log.debug("["+key+"] : "+m.get((String) key));
		}	
		log.debug("-- End ------------------------------");
	}
	
	public void setProperty(String key, String value) {
		m.put(key, value);
	}
	
	public void removeProperty(String key) {
		m.remove(key);
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		return Boolean.parseBoolean(getString(key, Boolean.toString(defaultValue)));
	}

	public int getInt(String key, int defaultValue) {
		return Integer.parseInt(getString(key, Integer.toString(defaultValue)));
	}

	@Override
	public Collection<String> getServerNameAliases() {
		return Collections.emptyList();
	}

	public String getServerUrl() {
		return "http://localhost:8080";
	}

	public String getRawProperty(String key) {
		return m.get(key);
	}

	public String getString(String key) {
		return m.get(key);
	}

	public String getString(String key, String defaultValue) {
		String v = m.get(key);
		if(v == null) {
			v = defaultValue;
		}
		return v;
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


    public <T> T getConfig(String name, T defaultValue) {
        return (T) m.get(name);
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
