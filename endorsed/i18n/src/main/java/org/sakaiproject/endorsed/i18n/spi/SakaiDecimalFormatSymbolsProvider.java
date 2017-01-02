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

import java.text.DecimalFormatSymbols;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.util.Locale;

/**
 * An implementation class for {@link DecimalFormatSymbolsProvider} which provides
 * a localized {@link DecimalFormatSymbols}.
 *
 * @author Yuki Yamada
 *
 */
public class SakaiDecimalFormatSymbolsProvider extends DecimalFormatSymbolsProvider {

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
	public DecimalFormatSymbols getInstance(final Locale locale) throws IllegalArgumentException, NullPointerException {
		if (locale == null) {
			throw new NullPointerException("locale:null");
		} else if (!SakaiLocaleServiceProviderUtil.isAvailableLocale(locale)) {
			throw new IllegalArgumentException("locale:" + locale.toString());
		}

		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(
				SakaiLocaleServiceProviderUtil.getChar("DecimalSeparator", locale));
		symbols.setDigit(
				SakaiLocaleServiceProviderUtil.getChar("Digit", locale));
		symbols.setExponentSeparator(
				SakaiLocaleServiceProviderUtil.getString("ExponentSeparator", locale));
		symbols.setGroupingSeparator(
				SakaiLocaleServiceProviderUtil.getChar("GroupingSeparator", locale));
		symbols.setInfinity(
				SakaiLocaleServiceProviderUtil.getString("Infinity", locale));
		symbols.setInternationalCurrencySymbol(
				SakaiLocaleServiceProviderUtil.getString("InternationalCurrencySymbol", locale));
		symbols.setCurrencySymbol(
				SakaiLocaleServiceProviderUtil.getString("CurrencySymbol", locale));
		symbols.setMinusSign(
				SakaiLocaleServiceProviderUtil.getChar("MinusSign", locale));
		symbols.setMonetaryDecimalSeparator(
				SakaiLocaleServiceProviderUtil.getChar("MonetaryDecimalSeparator", locale));
		symbols.setNaN(
				SakaiLocaleServiceProviderUtil.getString("NaN", locale));
		symbols.setPatternSeparator(
				SakaiLocaleServiceProviderUtil.getChar("PatternSeparator", locale));
		symbols.setPercent(
				SakaiLocaleServiceProviderUtil.getChar("Percent", locale));
		symbols.setPerMill(
				SakaiLocaleServiceProviderUtil.getChar("PerMill", locale));
		symbols.setZeroDigit(
				SakaiLocaleServiceProviderUtil.getChar("ZeroDigit", locale));

		return symbols;
	}
}
