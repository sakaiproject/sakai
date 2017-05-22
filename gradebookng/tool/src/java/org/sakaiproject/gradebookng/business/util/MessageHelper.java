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
package org.sakaiproject.gradebookng.business.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.sakaiproject.util.ResourceLoader;

/**
 * Handles the retrieval of localised messages and parameter substitution outside of the Wicket context.
 *
 * Hookes into the same GradebookNgApplication.properties file
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class MessageHelper {

	private static final String BASE_NAME = "org.sakaiproject.gradebookng.GradebookNgApplication";

	/**
	 * Get a simple message from the bundle
	 *
	 * @param key
	 * @return
	 */
	public static String getString(final String key) {
		try {
			return ResourceBundle.getBundle(BASE_NAME, getUserPreferredLocale()).getString(key);
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
	public static String getString(final String key, final Object... arguments) {
		return MessageFormat.format(getString(key), arguments);
	}

	// helper to get the Locale from Sakai
	private static Locale getUserPreferredLocale() {
		final ResourceLoader rl = new ResourceLoader();
		return rl.getLocale();
	}
}
