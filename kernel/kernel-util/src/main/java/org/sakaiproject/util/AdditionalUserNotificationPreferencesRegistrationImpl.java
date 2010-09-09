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
import org.sakaiproject.user.api.AdditionalUserNotificationPreferencesRegistration;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistrationService;

public abstract class AdditionalUserNotificationPreferencesRegistrationImpl extends UserNotificationPreferencesRegistrationImpl implements
		AdditionalUserNotificationPreferencesRegistration {

	private UserNotificationPreferencesRegistrationService userPreferencesRegistrationService;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private String sectionTitleBundleKey = "";
	private String sectionDescriptionBundleKey = "";
	private String overrideSectionTitleBundleKey = "";
	private String bundleLocation = "";
	private ResourceLoader rl = null;

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
			String value = getLocalResourceLoader().getString(optionsMap.get(i));
			processedOptions.put(i, value);
		}
		return processedOptions;
	}
	
	public void setUserPreferencesRegistrationService(
			UserNotificationPreferencesRegistrationService userPreferencesRegistrationService) {
		this.userPreferencesRegistrationService = userPreferencesRegistrationService;
	}

	public UserNotificationPreferencesRegistrationService getUserPreferencesRegistrationService() {
		return userPreferencesRegistrationService;
	}

}
