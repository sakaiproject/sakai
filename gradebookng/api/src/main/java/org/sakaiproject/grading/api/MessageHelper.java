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
package org.sakaiproject.grading.api;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Handles the retrieval of localised messages and parameter substitution outside of the Wicket context.
 *
 * Hookes into the same GradebookNgApplication.properties file
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class MessageHelper {

	/**
	 * Get a simple message from the bundle
	 *
	 * @param key
	 * @return
	 */
	public static String getString(final String key, final Locale userPreferredLocale) {
		try {
			return ResourceBundle.getBundle("gradebookng", userPreferredLocale).getString(key);
		} catch (final MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Get a parameterised message from the bundle and perform the parameter substitution on it
	 *
	 * @param key
	 * @return
	 */
	public static String getString(final String key, final Locale userPreferredLocale, final Object... arguments) {
		return MessageFormat.format(getString(key, userPreferredLocale), arguments);
	}

}
