/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.sakaiproject.util.ResourceLoader;

/**
 * Messages.java
 * 
 * Handles the retrieval of localised messages and parameter substitution.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class Messages {
	
	private static final String BUNDLE_NAME = "org.sakaiproject.profile2.util.messages";

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
