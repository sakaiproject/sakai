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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.user.api.UserNotificationPreferencesRegistration;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistrationService;

/**
 * Here's an example of what the xml bean would look like:
 * <pre>
 * {@code
 * 
 *     <bean id="org.sakaiproject.user.api.UserNotificationPreferencesRegistration.content"
 *        parent="org.sakaiproject.user.api.UserNotificationPreferencesRegistration"
 *        class="org.sakaiproject.content.user.prefs.ContentUserNotificationPreferencesRegistrationImpl"
 *        init-method="init" singleton="true">
 *     <property name="serverConfigurationService" ref="org.sakaiproject.component.api.ServerConfigurationService"/>
 *     <property name="bundleLocation"><value>org.sakaiproject.localization.bundle.content.content</value></property>
 *     <property name="sectionTitleBundleKey"><value>prefs_title</value></property>
 *     <property name="sectionDescriptionBundleKey"><value>prefs_description</value></property>
 *     <property name="overrideSectionTitleBundleKey"><value>prefs_title_override</value></property>
 *     <property name="defaultValue"><value>3</value></property>
 *     <property name="type"><value>sakai:content</value></property>
 *     <property name="prefix"><value>rsrc</value></property>
 *     <property name="toolId"><value>sakai.resources</value></property>
 *     <property name="rawOptions">
 *        <map>
 *           <entry key="1"><value>prefs_opt1</value></entry>
 *           <entry key="2"><value>prefs_opt2</value></entry>
 *           <entry key="3"><value>prefs_opt3</value></entry>
 *        </map>
 *     </property>
 *    <property name="overrideBySite"><value>false</value></property>
 *    <property name="expandByDefault"><value>true</value></property>
 *  </bean> 
 * }
 * </pre>
 * 
 * @author chrismaurer
 *
 */
@Slf4j
public abstract class UserNotificationPreferencesRegistrationImpl implements UserNotificationPreferencesRegistration {

	private UserNotificationPreferencesRegistrationService userNotificationPreferencesRegistrationService;

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

	/**
	 * Empty defaut constructor
	 */
	public UserNotificationPreferencesRegistrationImpl() {
		;
	}

	/**
	 * Full constructor.  Shouldn't need to be called directly as it'll most likely be done with a bean in a components.xml.
	 * @param sectionTitle
	 * @param sectionDescription
	 * @param sectionTitleOverride
	 * @param defaultValue
	 * @param prefix
	 * @param type
	 * @param toolId
	 * @param options
	 * @param overrideBySite
	 * @param expandByDefault
	 */
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

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Setter
	 * @param defaultValue
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPrefix() {
		return prefix;
	}
	
	/**
	 * Setter
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getOptions() {
		ResourceLoader loader = getLocalResourceLoader();
		if (loader == null || loader.getLocale().equals(Locale.getDefault())) {
			return options;
		}

		Map<String, String> optionsMap = getRawOptions();
		Map<String, String> processedOptions = new HashMap<String, String>();
		for (Entry<String, String> entry : optionsMap.entrySet()) {
			String value = loader.getString(entry.getValue());
			processedOptions.put(entry.getKey(), value);
		}
		return processedOptions;
	}

	/**
	 * Setter
	 * @param options
	 */
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	/**
	 * Gets the raw options that have been configured in the xml bean.
	 * Would look something like this:
	 * <pre>
	 * {@code
	 *  <property name="rawOptions">
     *    <map>
     *       <entry key="1"><value>prefs_opt1</value></entry>
     *       <entry key="2"><value>prefs_opt2</value></entry>
     *       <entry key="3"><value>prefs_opt3</value></entry>
     *    </map>
     * </property>
     * }
	 * </pre>
	 * Where the value is a key that will be looked up in a message bundle
	 * @return
	 */
	public Map<String, String> getRawOptions() {
		return this.rawOptions;
	}

	/**
	 * Setter
	 * @param rawOptions
	 */
	public void setRawOptions(Map<String, String> rawOptions) {
		this.rawOptions = rawOptions;
	}

	/**
	 * Setter
	 * @param sectionTitle
	 */
	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSectionTitle() {
		ResourceLoader loader = getLocalResourceLoader();
		if (loader == null || loader.getLocale().equals(Locale.getDefault())) {
			return sectionTitle;
		}
		return loader.getString(getSectionTitleBundleKey());
	}

	/**
	 * Setter
	 * @param sectionDescription
	 */
	public void setSectionDescription(String sectionDescription) {
		this.sectionDescription = sectionDescription;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSectionDescription() {
		ResourceLoader loader = getLocalResourceLoader();
		if (loader == null || loader.getLocale().equals(Locale.getDefault())) {
			return sectionDescription;
		}
		return loader.getString(getSectionDescriptionBundleKey());
	}

	/**
	 * Gets the display text for the Site Override Section
	 */
	public String getSectionTitleOverride() {
		ResourceLoader loader = getLocalResourceLoader();
		if (loader == null || loader.getLocale().equals(Locale.getDefault())) {
			return sectionTitleOverride;
		}
		return loader.getString(getOverrideSectionTitleBundleKey());
	}

	/**
	 * Setter
	 * @param sectionTitleOverride
	 */
	public void setSectionTitleOverride(String sectionTitleOverride) {
		this.sectionTitleOverride = sectionTitleOverride;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isOverrideBySite() {
		return overrideBySite;
	}

	/**
	 * Setter
	 * @param overrideBySite
	 */
	public void setOverrideBySite(boolean overrideBySite) {
		this.overrideBySite = overrideBySite;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isExpandByDefault() {
		return expandByDefault;
	}

	/**
	 * Setter
	 * @param expandByDefault
	 */
	public void setExpandByDefault(boolean expandByDefault) {
		this.expandByDefault = expandByDefault;
	}

	/**
	 * Setter
	 * @param sectionTitleBundleKey
	 */
	public void setSectionTitleBundleKey(String sectionTitleBundleKey) {
		this.sectionTitleBundleKey = sectionTitleBundleKey;
	}
	public String getSectionTitleBundleKey() {
		return sectionTitleBundleKey;
	}

	/**
	 * Setter
	 * @param sectionDescriptionBundleKey
	 */
	public void setSectionDescriptionBundleKey(
			String sectionDescriptionBundleKey) {
		this.sectionDescriptionBundleKey = sectionDescriptionBundleKey;
	}

	/**
	 * Get the key used to look up the text in the bundle for the section description.
	 * @return
	 */
	public String getSectionDescriptionBundleKey() {
		return sectionDescriptionBundleKey;
	}

	/**
	 * Setter
	 * @param overrideSectionTitleBundleKey
	 */
	public void setOverrideSectionTitleBundleKey(
			String overrideSectionTitleBundleKey) {
		this.overrideSectionTitleBundleKey = overrideSectionTitleBundleKey;
	}
	
	/**
	 * Get the key used to look up the Site Override section title in the bundle
	 * @return
	 */
	public String getOverrideSectionTitleBundleKey() {
		return overrideSectionTitleBundleKey;
	}
	
	/**
	 * Setter
	 * @param bundleLocation
	 */
	public void setBundleLocation(String bundleLocation) {
		this.bundleLocation = bundleLocation;
	}

	/**
	 * Get the fully qualified package of where the message bundle is located.
	 * @return
	 */
	public String getBundleLocation() {
		return bundleLocation;
	}

	/**
	 * Setter for the tool id
	 * @param toolId
	 */
	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getToolId() {
		return toolId;
	}

	/**
	 * Gets the ResourceLoader specified by the bundleLocation.
	 * @return
	 */
	private ResourceLoader getLocalResourceLoader() {
		if (rl == null) {
			rl = (ResourceLoader)getResourceLoader(getBundleLocation());
		}
		return rl;
	}

	/**
	 * Go through the optionsMap (as defined by rawOptions) and get the display texts for the specified keys.
	 * @param optionsMap
	 * @return
	 */
	private Map<String, String> processOptionsMap(Map<String, String> optionsMap) {
		//Look up the bundle file
		Map<String, String> processedOptions = new HashMap<String, String>();
		for (Entry<String, String> entry : optionsMap.entrySet()) {
			ResourceLoader loader = getLocalResourceLoader();
			if (loader != null) {
				String value = loader.getString(entry.getValue());
				processedOptions.put(entry.getKey(), value);
			}

		}
		return processedOptions;
	}

	/**
	 * Init method which will go through all the properties that had bundle keys and look up the actual texts, then register the object.
	 */
	public void init() {
		log.info("UserPreferencesRegistrationImpl.init()");

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

	/**
	 * Setter for the UserNotificationPreferencesRegistrationService
	 * @param userNotificationPreferencesRegistrationService
	 */
	public void setUserNotificationPreferencesRegistrationService(
			UserNotificationPreferencesRegistrationService userNotificationPreferencesRegistrationService) {
		this.userNotificationPreferencesRegistrationService = userNotificationPreferencesRegistrationService;
	}

	/**
	 * Gets the UserNotificationPreferencesRegistrationService
	 * @return
	 */
	public UserNotificationPreferencesRegistrationService getUserNotificationPreferencesRegistrationService() {
		return userNotificationPreferencesRegistrationService;
	}
}
