package org.sakaiproject.content.copyright.api;

import java.net.URL;
import java.util.Locale;

public interface CopyrightManager {

	/** Type designed to be the custom copyright type */
	public final static String USE_THIS_COPYRIGHT = "use_below"; 
	
	public CopyrightInfo getCopyrightInfo(Locale locale, String [] rights, URL serverURL);
	
	public String getUseThisCopyright(String [] rights);
	
}