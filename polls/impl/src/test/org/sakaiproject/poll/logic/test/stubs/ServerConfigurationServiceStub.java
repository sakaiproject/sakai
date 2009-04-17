/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.poll.logic.test.stubs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}
