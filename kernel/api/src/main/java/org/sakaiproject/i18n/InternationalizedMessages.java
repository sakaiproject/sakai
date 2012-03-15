/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.i18n;

import java.util.Locale;
import java.util.Map;

/**
 * InternationalizedMessages is a bundle of message implementations for many locales.
 */
public interface InternationalizedMessages extends Map
{
	/** The type string for this "application": should not change over time as it may be stored in various parts of persistent entities. */
	String APPLICATION_ID = "sakai:resourceloader";

	/** Preferences key for user's regional language locale */
	String LOCALE_KEY = "locale";

	/**
	 * Return formatted message based on locale-specific pattern
	 * 
	 * @param key
	 *        maps to locale-specific pattern in properties file
	 * @param args
	 *        parameters to format and insert according to above pattern
	 * @return formatted message
	 */
	String getFormattedMessage(String key, Object... args);

	/**
	 * Return user's prefered locale
	 * 
	 * @return user's Locale object
	 */
	Locale getLocale();

	/**
	 * Return string value for specified property in current locale specific ResourceBundle
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle * *
	 * @return String value for specified property key
	 */
	String getString(String key);

	/**
	 * Return string value for specified property in current locale specific ResourceBundle
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle
	 * @param dflt
	 *        the default value to be returned in case the property is missing
	 * @return String value for specified property key
	 */
	String getString(String key, String dflt);
}
