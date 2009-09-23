/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.component.api.ServerConfigurationService;

public class FakeServerConfigurationService implements ServerConfigurationService {
	private Map<String,String> m = new HashMap<String,String>();
	
	public FakeServerConfigurationService() {
		m.put("sitestats.db", "internal");
		m.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
	}
	
	public void printAll() {
		System.out.println("-- Start ----------------------------");
		for(Object key : m.keySet()) {
			System.out.println("["+key+"] : "+m.get((String) key));
		}	
		System.out.println("-- End ------------------------------");
	}
	
	public void setProperty(String key, String value) {
		m.put(key, value);
	}
	
	public void removeProperty(String key) {
		m.remove(key);
	}
	
	public String getAccessPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAccessUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return Boolean.parseBoolean(getString(key, Boolean.toString(defaultValue)));
	}

	public List getDefaultTools(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getGatewaySiteId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHelpUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getInt(String key, int defaultValue) {
		return Integer.parseInt(getString(key, Integer.toString(defaultValue)));
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

	public String[] getStrings(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getToolCategories(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, List<String>> getToolCategoriesAsMap(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getToolOrder(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getToolToCategoryMap(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public List getToolsRequired(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserHomeUrl() {
		// TODO Auto-generated method stub
		return null;
	}

}
