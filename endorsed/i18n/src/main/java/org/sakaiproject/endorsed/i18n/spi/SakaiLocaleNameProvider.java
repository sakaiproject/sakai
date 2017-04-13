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

import java.util.Locale;
import java.util.spi.LocaleNameProvider;

/**
 * An implementation class for {@link LocaleNameProvider} which provides localized
 * names for {@link Locale}.
 *
 * @author Yuki Yamada
 *
 */
public class SakaiLocaleNameProvider extends LocaleNameProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Locale[] getAvailableLocales() {
		return SakaiLocaleServiceProviderUtil.getAvailableLocales();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayCountry(final String countryCode, final Locale locale) throws IllegalArgumentException,
			NullPointerException {
		if (countryCode == null) {
			throw new NullPointerException("countryCode:null");
		} else if (!countryCode.matches("^[A-Z][A-Z]$")) {
			// the country code string should be in the form of two upper-case letters.
			throw new IllegalArgumentException("countryCode:" + countryCode);
		} else if (locale == null) {
			throw new NullPointerException("locale:null");
		} else if (!SakaiLocaleServiceProviderUtil.isAvailableLocale(locale)) {
			throw new IllegalArgumentException("locale:" + locale.toString());
		}

		String displayCountry = null;
		String key = "Country." + countryCode;

		if (SakaiLocaleServiceProviderUtil.containsKey(key, locale)) {
			displayCountry = SakaiLocaleServiceProviderUtil.getString(key, locale);
		}

		return displayCountry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayLanguage(final String languageCode, final Locale locale) throws IllegalArgumentException,
			NullPointerException {
		if (languageCode == null) {
			throw new NullPointerException("languageCode:null");
		} else if (!languageCode.matches("^[a-z][a-z]$")) {
			// the language code string should be in the form of two lower-case letters.
			throw new IllegalArgumentException("languageCode:" + languageCode);
		} else if (locale == null) {
			throw new NullPointerException("locale:null");
		} else if (!SakaiLocaleServiceProviderUtil.isAvailableLocale(locale)) {
			throw new IllegalArgumentException("locale:" + locale.toString());
		}

		String displayLanguage = null;
		String key = "Language." + languageCode;

		if (SakaiLocaleServiceProviderUtil.containsKey(key, locale)) {
			displayLanguage = SakaiLocaleServiceProviderUtil.getString(key, locale);
		}

		return displayLanguage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayVariant(final String variant, final Locale locale) throws IllegalArgumentException,
			NullPointerException {
		if (variant == null) {
			throw new NullPointerException("variant:null");
		} else if (locale == null) {
			throw new NullPointerException("locale:null");
		} else if (!SakaiLocaleServiceProviderUtil.isAvailableLocale(locale)) {
			throw new IllegalArgumentException("locale:" + locale.toString());
		}

		String displayVariant = null;
		String key = "Variant." + variant;

		if (SakaiLocaleServiceProviderUtil.containsKey(key, locale)) {
			displayVariant = SakaiLocaleServiceProviderUtil.getString(key, locale);
		}

		return displayVariant;
	}
}
