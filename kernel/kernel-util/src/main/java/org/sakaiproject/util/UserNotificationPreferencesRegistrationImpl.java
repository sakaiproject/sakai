/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation
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

package org.sakaiproject.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistration;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistrationService;

public abstract class UserNotificationPreferencesRegistrationImpl implements UserNotificationPreferencesRegistration {

	private UserNotificationPreferencesRegistrationService userNotificationPreferencesRegistrationService;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private String sectionTitle = "";
	private String sectionDescription = "";
	private String sectionTitleBundleKey = "";
	private String sectionDescriptionBundleKey = "";
	private String overrideSectionTitleBundleKey = "";
	private String sectionTitleOverride = "";
	private String defaultValue = "0";
	private String prefix = "";
	private String type = "";
	private String toolId = "";
	private Map<String, String> rawOptions = new HashMap<String, String>();
	private Map<String, String> options = new HashMap<String, String>();
	private boolean overrideBySite = false;
	private boolean expandByDefault = true;
	
	private String bundleLocation = "";
	private ResourceLoader rl = null;
	
	public UserNotificationPreferencesRegistrationImpl() {
		;
	}

	public UserNotificationPreferencesRegistrationImpl(String sectionTitle, String sectionDescription, String sectionTitleOverride, String defaultValue, 
			String prefix, String type, String toolId, Map<String, String> options, boolean overrideBySite, boolean expandByDefault) {
		this.sectionTitle = sectionTitle;
		this.sectionDescription = sectionDescription;
		this.sectionTitleOverride = sectionTitleOverride;
		this.defaultValue = defaultValue;
		this.prefix = prefix;
		this.type = type;
		this.options = options;
		this.overrideBySite = overrideBySite;
		this.expandByDefault = expandByDefault;
		this.toolId = toolId;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, String> getOptions() {
		return this.options;
	}
	
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}
	
	public Map<String, String> getRawOptions() {
		return this.rawOptions;
	}
	
	public void setRawOptions(Map<String, String> rawOptions) {
		this.rawOptions = rawOptions;
	}

	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}

	public void setSectionDescription(String sectionDescription) {
		this.sectionDescription = sectionDescription;
	}

	public String getSectionDescription() {
		return sectionDescription;
	}

	public String getSectionTitleOverride() {
		return sectionTitleOverride;
	}

	public void setSectionTitleOverride(String sectionTitleOverride) {
		this.sectionTitleOverride = sectionTitleOverride;
	}

	public boolean isOverrideBySite() {
		return overrideBySite;
	}

	public void setOverrideBySite(boolean overrideBySite) {
		this.overrideBySite = overrideBySite;
	}
	
	public boolean isExpandByDefault() {
		return expandByDefault;
	}

	public void setExpandByDefault(boolean expandByDefault) {
		this.expandByDefault = expandByDefault;
	}
	
	public void setSectionTitleBundleKey(String sectionTitleBundleKey) {
		this.sectionTitleBundleKey = sectionTitleBundleKey;
	}
	public String getSectionTitleBundleKey() {
		return sectionTitleBundleKey;
	}
	
	public void setSectionDescriptionBundleKey(
			String sectionDescriptionBundleKey) {
		this.sectionDescriptionBundleKey = sectionDescriptionBundleKey;
	}

	public String getSectionDescriptionBundleKey() {
		return sectionDescriptionBundleKey;
	}

	public void setOverrideSectionTitleBundleKey(
			String overrideSectionTitleBundleKey) {
		this.overrideSectionTitleBundleKey = overrideSectionTitleBundleKey;
	}
	public String getOverrideSectionTitleBundleKey() {
		return overrideSectionTitleBundleKey;
	}
	public void setBundleLocation(String bundleLocation) {
		this.bundleLocation = bundleLocation;
	}
	
	public String getBundleLocation() {
		return bundleLocation;
	}
	
	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	public String getToolId() {
		return toolId;
	}

	private ResourceLoader getLocalResourceLoader() {
		if (rl == null) {
			rl = (ResourceLoader)getResourceLoader(getBundleLocation());
		}
		return rl;
	}
	
	private Map<String, String> processOptionsMap(Map<String, String> optionsMap) {
		//Look up the bundle file
		Map<String, String> processedOptions = new HashMap<String, String>();
		for (String i : optionsMap.keySet()) {
			ResourceLoader loader = getLocalResourceLoader();
			if (loader != null) {
				String value = loader.getString(optionsMap.get(i));
				processedOptions.put(i, value);
			}
			
		}
		return processedOptions;
	}
	
	public void init() {
		logger.info("UserPreferencesRegistrationImpl.init()");
		
		Map<String, String> processedOptions = processOptionsMap(getRawOptions());
		ResourceLoader loader = getLocalResourceLoader();
		if (loader != null) {
			this.sectionTitle = loader.getString(getSectionTitleBundleKey());
			this.sectionDescription = loader.getString(getSectionDescriptionBundleKey());
			this.sectionTitleOverride = loader.getString(getOverrideSectionTitleBundleKey());
		}
		this.options = processedOptions;
		getUserNotificationPreferencesRegistrationService().register(this);
	}

	public void setUserNotificationPreferencesRegistrationService(
			UserNotificationPreferencesRegistrationService userNotificationPreferencesRegistrationService) {
		this.userNotificationPreferencesRegistrationService = userNotificationPreferencesRegistrationService;
	}

	public UserNotificationPreferencesRegistrationService getUserNotificationPreferencesRegistrationService() {
		return userNotificationPreferencesRegistrationService;
	}
}
