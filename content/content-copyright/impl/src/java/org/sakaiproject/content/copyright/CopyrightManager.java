package org.sakaiproject.content.copyright;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;

public class CopyrightManager implements org.sakaiproject.content.copyright.api.CopyrightManager {

	static final Log logger = LogFactory.getLog(CopyrightManager.class);
	
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
			copyright_types = rights;
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
		return active?org.sakaiproject.content.copyright.api.CopyrightManager.USE_THIS_COPYRIGHT:rights[rights.length-1];
	}

	private String getBaseURL(String serverURL) {
		return serverURL.substring(0,serverURL.indexOf("WEB-INF"))+"..";
	}
	
	private boolean existsFile(String file,String baseURL) {
		File f = new File(baseURL+file);
		return f.exists();
	}
	
}