package au.edu.anu.portal.portlets.rss.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Messages.java
 * 
 * Handles the retrieval of localised messages and parameter substitution.
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class Messages {
	private static final String BUNDLE_NAME = "au.edu.anu.portal.portlets.rss.utils.messages";
	
	/**
	 * Get a simple message from the bundle
	 * 
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		return getMessage(key);
	}
	
	/**
	 * Get a parameterised message from the bundle and perform the parameter substitution on it
	 * 
	 * @param key
	 * @return
	 */
	public static String getString(String key, Object[] arguments) {
        return MessageFormat.format(getMessage(key), arguments);
    }
	
	// helper to get the message from the bundle
	private static String getMessage(String key) {
		try {
			return ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
}
