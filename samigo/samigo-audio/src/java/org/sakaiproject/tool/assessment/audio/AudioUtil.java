package org.sakaiproject.tool.assessment.audio;

import java.util.Locale;
import java.util.ResourceBundle;

public class AudioUtil {

	private static final String RESOURCE_PACKAGE = "org.sakaiproject.tool.assessment.bundle";

	private static final String RESOURCE_NAME = "AudioResources";

	private static AudioUtil INSTANCE;

	private String localeLanguage;

	private String localeCountry;

	public static AudioUtil getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AudioUtil();
		}
		return INSTANCE;
	}

	public void setLocaleLanguage(String localeLanguage) {
		this.localeLanguage = localeLanguage;
	}

	public String getLocaleLanguage() {
		return localeLanguage;
	}

	public void setLocaleCountry(String localeCountry) {
		this.localeCountry = localeCountry;
	}

	public String getLocaleCountry() {
		return localeCountry;
	}

	public ResourceBundle getResourceBundle() {
		Locale locale = Locale.getDefault();
		if (localeLanguage != null && !"".equals(localeLanguage)) {
			if (this.localeCountry != null && !"".equals(this.localeCountry)) {
				locale = new Locale(localeLanguage, localeCountry);
			} else {
				locale = new Locale(localeLanguage);
			}
		}
		ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PACKAGE + "."
				+ RESOURCE_NAME, locale);
		return res;
	}

}
