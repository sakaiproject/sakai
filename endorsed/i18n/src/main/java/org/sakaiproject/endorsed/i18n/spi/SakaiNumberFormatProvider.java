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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.spi.NumberFormatProvider;
import java.util.Locale;

/**
 * An implementation class for {@link NumberFormatProvider} which provides a
 * localized {@link NumberFormat}.
 *
 * @author Yuki Yamada
 *
 */
public class SakaiNumberFormatProvider extends NumberFormatProvider {

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
	public NumberFormat getCurrencyInstance(final Locale locale) throws IllegalArgumentException, NullPointerException {
		return getInstance("CurrencyPattern", locale);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NumberFormat getIntegerInstance(final Locale locale) throws IllegalArgumentException, NullPointerException {
		NumberFormat format = getInstance("IntegerPattern", locale);
		format.setParseIntegerOnly(true);
		return format;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NumberFormat getNumberInstance(final Locale locale) throws IllegalArgumentException, NullPointerException {
		return getInstance("NumberPattern", locale);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NumberFormat getPercentInstance(final Locale locale) throws IllegalArgumentException, NullPointerException {
		return getInstance("PercentPattern", locale);
	}

	/**
	 * Returns a new NumberFormat instance for the specified key and locale.
	 *
	 * @param key the property key.
	 * @param locale the desired locale.
	 * @return a number formatter.
	 * @throws IllegalArgumentException if <code>locale</code> isn't available.
	 * @throws NullPointerException if <code>locale</code> is <code>null</code>.
	 */
	protected NumberFormat getInstance(final String key, final Locale locale) throws IllegalArgumentException,
			NullPointerException {
		if (locale == null) {
			throw new NullPointerException("locale:null");
		} else if (!SakaiLocaleServiceProviderUtil.isAvailableLocale(locale)) {
			throw new IllegalArgumentException("locale:" + locale.toString());
		}

		DecimalFormat format = new DecimalFormat();
		format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(locale));

		String pattern = SakaiLocaleServiceProviderUtil.getString(key, locale);
		format.applyLocalizedPattern(pattern);

		return format;
	}
}
