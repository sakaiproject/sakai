package org.sakaiproject.sitestats.test.mocks;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sakaiproject.component.api.ServerConfigurationService;

public class FakeServerConfigurationService implements ServerConfigurationService {
	private Properties p = new Properties();
	
	public FakeServerConfigurationService() {
		p.put("sitestats.db", "internal");
		p.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
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
		return Boolean.getBoolean(p.getProperty(key, Boolean.toString(defaultValue)));
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

	public int getInt(String arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
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

	public String getString(String key) {
		return p.getProperty(key);
	}

	public String getString(String key, String defaultValue) {
		return p.getProperty(key, defaultValue);
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
