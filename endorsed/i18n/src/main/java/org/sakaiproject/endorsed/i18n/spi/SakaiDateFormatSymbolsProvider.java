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

import java.text.DateFormatSymbols;
import java.text.spi.DateFormatSymbolsProvider;
import java.util.Locale;

/**
 * An implementation class for {@link DateFormatSymbolsProvider} which provides a
 * localized {@link DateFormatSymbols}.
 *
 * @author Yuki Yamada
 *
 */
public class SakaiDateFormatSymbolsProvider extends DateFormatSymbolsProvider {

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
	public DateFormatSymbols getInstance(final Locale locale) throws IllegalArgumentException, NullPointerException {
		if (locale == null) {
			throw new NullPointerException("locale:null");
		} else if (!SakaiLocaleServiceProviderUtil.isAvailableLocale(locale)) {
			throw new IllegalArgumentException("locale:" + locale.toString());
		}

		DateFormatSymbols symbols = new DateFormatSymbols();
		symbols.setEras(new String[] {
				SakaiLocaleServiceProviderUtil.getString("Eras.BC", locale),
				SakaiLocaleServiceProviderUtil.getString("Eras.AD", locale) });
		symbols.setMonths(new String[] {
				SakaiLocaleServiceProviderUtil.getString("Months.JAN", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.FEB", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.MAR", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.APR", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.MAY", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.JUN", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.JUL", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.AUG", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.SEP", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.OCT", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.NOV", locale),
				SakaiLocaleServiceProviderUtil.getString("Months.DEC", locale) });
		symbols.setShortMonths(new String[] {
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.JAN", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.FEB", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.MAR", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.APR", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.MAY", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.JUN", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.JUL", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.AUG", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.SEP", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.OCT", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.NOV", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortMonths.DEC", locale) });
		symbols.setWeekdays(new String[] {"",
				SakaiLocaleServiceProviderUtil.getString("Weekdays.SUN", locale),
				SakaiLocaleServiceProviderUtil.getString("Weekdays.MON", locale),
				SakaiLocaleServiceProviderUtil.getString("Weekdays.TUE", locale),
				SakaiLocaleServiceProviderUtil.getString("Weekdays.WED", locale),
				SakaiLocaleServiceProviderUtil.getString("Weekdays.THU", locale),
				SakaiLocaleServiceProviderUtil.getString("Weekdays.FRI", locale),
				SakaiLocaleServiceProviderUtil.getString("Weekdays.SAT", locale) });
		symbols.setShortWeekdays(new String[] {"",
				SakaiLocaleServiceProviderUtil.getString("ShortWeekdays.SUN", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortWeekdays.MON", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortWeekdays.TUE", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortWeekdays.WED", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortWeekdays.THU", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortWeekdays.FRI", locale),
				SakaiLocaleServiceProviderUtil.getString("ShortWeekdays.SAT", locale) });
		symbols.setAmPmStrings(new String[] {
				SakaiLocaleServiceProviderUtil.getString("AmPmStrings.AM", locale),
				SakaiLocaleServiceProviderUtil.getString("AmPmStrings.PM", locale) });
		symbols.setLocalPatternChars(SakaiLocaleServiceProviderUtil.getString(
				"LocalPatternChars", locale));

		// Not support Zone Strings

		return symbols;
	}
}
