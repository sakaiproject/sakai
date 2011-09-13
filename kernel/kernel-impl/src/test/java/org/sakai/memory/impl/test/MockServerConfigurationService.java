package org.sakai.memory.impl.test;

import java.util.List;
import java.util.Map;

import org.sakaiproject.component.api.ServerConfigurationService;

public class MockServerConfigurationService implements
		ServerConfigurationService {

	public String getAccessPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAccessUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getBoolean(String name, boolean dflt) {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

	public String getString(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString(String name, String dflt) {
		// TODO Auto-generated method stub
		return null;
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

	public String getRawProperty(String name) {
		// TODO Auto-generated method stub
		return null;
	}

    public <T> T getConfig(String name, T defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    public ConfigItem getConfigItem(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public ConfigData getConfigData() {
        // TODO Auto-generated method stub
        return null;
    }

    public ConfigItem registerConfigItem(ConfigItem configItem) {
        // TODO Auto-generated method stub
        return null;
    }

    public void registerListener(ConfigurationListener configurationListener) {
        // TODO Auto-generated method stub
        
    }

}
