package org.sakaiproject.profile2.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.sakaiproject.util.ResourceLoader;

public class Messages {
	private static final String BUNDLE_NAME = "org.sakaiproject.profile2.util.messages";

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return ResourceBundle.getBundle(BUNDLE_NAME, getUserPreferredLocale()).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	//helper to get the Locale from Sakai
	private static Locale getUserPreferredLocale() {
		ResourceLoader rl = new ResourceLoader();
		return rl.getLocale();
	}
}
