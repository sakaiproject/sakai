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
import java.util.ResourceBundle;

import org.sakaiproject.component.api.ServerConfigurationService;

public class CopyrightManager implements org.sakaiproject.content.copyright.api.CopyrightManager {
	
	protected boolean active = true;
	
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
	
	public org.sakaiproject.content.copyright.api.CopyrightInfo getCopyrightInfo(Locale locale, String [] rights, URL serverURL){
		String baseURL = getBaseURL(serverURL.getFile());
		CopyrightInfo copyrightInfo = new CopyrightInfo();
		String[] copyright_types = m_serverConfigurationService.getStrings("copyright.types");
		if (copyright_types==null) {
			active = false;
			copyright_types = (rights == null)?new String[]{}:rights;
		}
		ResourceBundle rb = ResourceBundle.getBundle("org.sakaiproject.content.copyright.copyright",locale);
		String language = locale.getLanguage();
		for (String copyrightType:copyright_types){
			CopyrightItem item = new CopyrightItem();
			if (active) {
				item.setType(copyrightType);
				item.setText(rb.getString(copyrightType));
				if (existsFile("/library/content/copyright/" + copyrightType + "_" + language + ".html",baseURL)) {
					item.setLicenseUrl("/library/content/copyright/" + copyrightType + "_" + language + ".html");
				} else if (existsFile("/library/content/copyright/" + copyrightType + ".html",baseURL)) {
					item.setLicenseUrl("/library/content/copyright/" + copyrightType + ".html");
				}
			} else {
				item.setType(copyrightType);
				item.setText(copyrightType);
			}
			copyrightInfo.add(item);
		}
		return copyrightInfo;
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
