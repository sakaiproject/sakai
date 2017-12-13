/**********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.endorsed.i18n.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import java.util.logging.Logger;

/**
 * An utility class to get a value from the resource bundles.
 *
 * @author Yuki Yamada
 *
 */
public final class SakaiLocaleServiceProviderUtil {

	/**
	 * A global logger.
	 */
	private static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	/**
	 * A set of all locales for the resource bundles.
	 */
	private static final Set<Locale> LOCALES = new HashSet<Locale>();

	private SakaiLocaleServiceProviderUtil() {
	}

	static {
		// Load the locale config.
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream inStream = loader.getResourceAsStream("SakaiLocaleServiceProvider.config");

		Properties props = new Properties();
		if (inStream != null) {
			try {
				props.load(inStream);
			} catch (IOException e) {
				log.warning(e.getMessage());
			} finally {
				try {
					inStream.close();
				} catch (IOException e) {
					log.warning(e.getMessage());
				}
			}
		}

		String value = props.getProperty("locales");
		if (value != null) {
			String[] locales = value.split(",");
			for (String localeStr : locales) {
				String[] params = localeStr.trim().split("_", 3);
				Locale locale = null;
				if (params.length == 1) {
					locale = new Locale(params[0]);
				} else if (params.length == 2) {
					locale = new Locale(params[0], params[1]);
				} else if (params.length == 3) {
					locale = new Locale(params[0], params[1], params[2]);
				}
				LOCALES.add(locale);
			}
		}
	}

	/**
	 * Returns an array of all locales for the resource bundles.
	 *
	 * @return an array of locales.
	 */
	public static Locale[] getAvailableLocales() {
		return LOCALES.toArray(new Locale[0]);
	}

	/**
	 * Determines whether the given <code>locale</code> is available
	 * for the resource bundles.
	 *
	 * @param locale possible locale.
	 * @return <code>true</code> if the given <code>locale</code> is available for the
	 *     resource bundles; <code>false</code> otherwise.
	 */
	public static boolean isAvailableLocale(final Locale locale) {
		boolean isAvailable = false;

		if (getBundle(locale) != null) {
			isAvailable = true;
		}

		return isAvailable;
	}

	/**
	 * Returns a string for the given <code>key</code> from the resource bundle.
	 *
	 * @param key the key for the desired string.
	 * @param locale the desired locale.
	 * @return the string for the given <code>key</code> and <code>locale</code>.
	 */
	public static String getString(final String key, final Locale locale) {
		String value = null;

		if (key != null) {
			ResourceBundle rb = getBundle(locale);
			if (rb != null) {
				value = rb.getString(key);
			}
		}

		return value;
	}

	/**
	 * Returns a <code>char</code> value for the given <code>key</code> from the
	 * resource bundle.
	 *
	 * @param key the key for the desired <code>char</code> value.
	 * @param locale the desired locale.
	 * @return the <code>char</code> value for the given <code>key</code> and
	 *     <code>locale</code>.
	 */
	public static char getChar(final String key, final Locale locale) {
		char value = 0;

		String str = getString(key, locale);
		if (str != null && !str.isEmpty()) {
			value = str.charAt(0);
		}

		return value;
	}

	/**
	 * Determines whether the given <code>key</code> is contained in the resource
	 * bundle.
	 *
	 * @param key possible key.
	 * @param locale the desired locale.
	 * @return <code>true</code> if the given <code>key</code> is contained in the
	 *     resource bundle; <code>false</code> otherwise.
	 */
	public static boolean containsKey(final String key, final Locale locale) {
		boolean contain = false;

		if (key != null) {
			ResourceBundle rb = getBundle(locale);
			if (rb != null) {
				contain = rb.containsKey(key);
			}
		}

		return contain;
	}

	/**
	 * Get a resource bundle using the specified locale.
	 *
	 * @param locale the desired locale.
	 * @return the resource bundle for the given locale, or <code>null</code> if the
	 *     resource bundle could not be found.
	 */
	private static ResourceBundle getBundle(final Locale locale) {
		ResourceBundle rb = null;

		if (locale != null) {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();

			try {
				rb = ResourceBundle.getBundle("SakaiLocaleServiceProvider", locale, loader);
			} catch (MissingResourceException e) {
				log.warning(e.getMessage());
			}
		}

		return rb;
	}
}
