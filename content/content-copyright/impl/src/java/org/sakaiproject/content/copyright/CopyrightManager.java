/**
 * $Id: ValidationLogicDao.java 81430 2010-08-18 14:12:46Z david.horwitz@uct.ac.za $
 * $URL: https://source.sakaiproject.org/svn/reset-pass/trunk/account-validator-impl/src/java/org/sakaiproject/accountvalidator/dao/impl/ValidationLogicDao.java $
 *
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */
package org.sakaiproject.content.copyright;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;

@Slf4j
public class CopyrightManager implements org.sakaiproject.content.copyright.api.CopyrightManager {

	private static final String SAK_PROP_COPYRIGHT_TYPES = "copyright.types";
	private static final String SAK_PROP_USE_CUSTOM_COPYRIGHT = "copyright.useCustom";
	private static final String SAK_PROP_COPYRIGHT_REQ_CHOICE = "copyright.requireChoice";
	private static final Boolean SAK_PROP_USE_CUSTOM_COPYRIGHT_DEFAULT = false;
	private static final Boolean SAK_PROP_COPYRIGHT_REQ_CHOICE_DEFAULT = false;

	private static final String MSG_KEY_CUSTOM_COPYRIGHT_PREFIX = "custom.copyright.";
	private static final String MSG_KEY_COPYRIGHT_REQ_CHOICE_KEY = "copyright.requireChoice";
	private static final String COPYRIGHT_MSG_BUNDLE = "org.sakaiproject.content.copyright.copyright";

	private static final String COPYRIGHT_LICENSE_FILE_LOCATION = "/library/content/copyright/";
	private static final String COPYRIGHT_LICENSE_FILE_EXT = ".html";

	protected boolean active = true;

	private static ResourceBundle rb = null;

	private static Locale locale = null;

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService m_serverConfigurationService = null;
	/**
	 * Dependency: ServerConfigurationService.
	 * 
	 * @param service
	 *        The ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service) {
		m_serverConfigurationService = service;
	}

	@Override
	public org.sakaiproject.content.copyright.api.CopyrightInfo getCopyrightInfo(Locale locale, String[] rights, URL serverURL){
		TreeMap<String, String> copyrightMap = new TreeMap<>();
		rb = ResourceBundle.getBundle(COPYRIGHT_MSG_BUNDLE, locale);

		// If sakai.properties says to use custom copyright, grab all key/values from copyright.properties that start with the defined prefix 'custom.copyright.'
		boolean useCustomCopyright = m_serverConfigurationService.getBoolean(SAK_PROP_USE_CUSTOM_COPYRIGHT, SAK_PROP_USE_CUSTOM_COPYRIGHT_DEFAULT);
		if (useCustomCopyright) {
			for (String key : rb.keySet()) {
				if (StringUtils.startsWith(key, MSG_KEY_CUSTOM_COPYRIGHT_PREFIX)) {
					copyrightMap.put(key, rb.getString(key));
				}
			}
		} else {
			// Otherwise, get the standard copyright types (keys) defined by the sakai.property 'copyright.types'
			String[] copyrightTypes = m_serverConfigurationService.getStrings(SAK_PROP_COPYRIGHT_TYPES);
			if (copyrightTypes != null) {
				for (String key : copyrightTypes) {
					copyrightMap.put(key, rb.getString(key));
				}
			}
		}

		// If the map is still empty at this point, fall back to the default copyright values passed into this function (provided the array is not null)
		if (copyrightMap.isEmpty()) {
			active = false;
			if (rights != null) {
				for (int i = 0; i < rights.length; i++) {
					copyrightMap.put(i + "", rights[i]);
				}
			}
		}

		String baseURL = getBaseURL(serverURL.getFile());
		CopyrightInfo copyrightInfo = new CopyrightInfo();
		CopyrightManager.locale = locale;
		String language = locale.getLanguage();

		// Loop through the map to build the copyright options
		for (String key : copyrightMap.keySet()) {
			copyrightInfo.add(buildCopyrightItem(key, language, baseURL, copyrightMap));
		}

		// If the copyright options are greater than zero, and copyright require choice (sakai.property) is set, add the 'Please select...' option to the beginning of the list
		if (!copyrightInfo.getItems().isEmpty()) {
			boolean customCopyrightRequireChoice = m_serverConfigurationService.getBoolean(SAK_PROP_COPYRIGHT_REQ_CHOICE, SAK_PROP_COPYRIGHT_REQ_CHOICE_DEFAULT);
			if (customCopyrightRequireChoice) {
				copyrightMap.put(MSG_KEY_COPYRIGHT_REQ_CHOICE_KEY, rb.getString(MSG_KEY_COPYRIGHT_REQ_CHOICE_KEY));
				copyrightInfo.addToBeginning(buildCopyrightItem(MSG_KEY_COPYRIGHT_REQ_CHOICE_KEY, language, baseURL, copyrightMap));
			}
		}

		return copyrightInfo;
	}

	/**
	 * Utility method to build CopyrightItem objects to reduce code duplication.
	 * @param key the key in the map that corresponds to the correct user facing text
	 * @param language the language of the user
	 * @param baseURL the base URL where the HTML file licenses are stored
	 * @param copyrightMap the map of keys to user facing messages
	 * @return a built CopyrightItem object
	 */
	private CopyrightItem buildCopyrightItem(String key, String language, String baseURL, TreeMap<String, String> copyrightMap) {
		CopyrightItem item = new CopyrightItem();

		// If custom copyright options are 'active', try to find the corresponding HTML license file on the file system
		if (active) {
			item.setType(key);
			item.setText(copyrightMap.get(key));

			String location1 = COPYRIGHT_LICENSE_FILE_LOCATION + key + "_" + language + COPYRIGHT_LICENSE_FILE_EXT;
			String location2 = COPYRIGHT_LICENSE_FILE_LOCATION + key + COPYRIGHT_LICENSE_FILE_EXT;
			if (existsFile(location1, baseURL)) {
				item.setLicenseUrl(location1);
			} else if (existsFile(location2, baseURL)) {
				item.setLicenseUrl(location2);
			}
		} else {
			String copyrightText = copyrightMap.get(key);
			item.setType(copyrightText);
			item.setText(copyrightText);
		}

		return item;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getCopyrightString(String messageKey) {
		if ((rb == null && locale == null) || StringUtils.isBlank( messageKey )) {
			return "";
		}

		String copyright = "";
		try {
			if (rb == null && locale != null) {
				rb = ResourceBundle.getBundle(COPYRIGHT_MSG_BUNDLE, locale);
			}

			copyright = rb.getString(messageKey);
		} catch (MissingResourceException | ClassCastException ex) {
			// no copyright bundle or no message found for key, log and continue, will return empty string, OR
			// object found for key was not a string, log and continue, will return empty string
			log.debug(ex.getMessage(), ex);
		}

		return copyright;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void setLocale(Locale locale) {
		CopyrightManager.locale = locale;
	}

	public String getUseThisCopyright(String [] rights) {
		if (active) {
			return CopyrightManager.USE_THIS_COPYRIGHT;
		} else {
			if (rights == null || rights.length == 0) {
				return null;
			} else {
				return rights[rights.length-1];
			}
		}
	}

	private String getBaseURL(String serverURL) {
		return serverURL.substring(0,serverURL.indexOf("WEB-INF"))+"..";
	}
	
	private boolean existsFile(String file,String baseURL) {
		File f = new File(baseURL+file);
		return f.exists();
	}
	
}
