/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2017 Sakai Foundation
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

package org.sakaiproject.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Testing the NumberUtilTest class
 */
@Slf4j
public class NumberUtilTest {

    @Test
    public void testIsValidLocaleDouble() {
        /*
         * There are 4 different ways to use decimal and group separator
         * https://en.wikipedia.org/wiki/Decimal_separator
         * comma, dot, both (Canada but are en-CA, fr-CA so it belongs to dot and comma), Arabic format
         *
         */
        // Group 1 : comma on decimal separator, dot on group separator.
        final Locale commaGroupLocale = new Locale("es", "ES");
        setResourceLoaderLocale(commaGroupLocale);

        Assert.assertTrue(NumberUtil.isValidLocaleDouble("1.234,00"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("3,00"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("456457546"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("3524,055"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("2.300"));
        // No integer part
        Assert.assertTrue(NumberUtil.isValidLocaleDouble(",01"));
        // Longer numbers with separators
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("1.234.456,00"));

        Assert.assertFalse(NumberUtil.isValidLocaleDouble("2.00"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("3,520.55"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("3.4561,00"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("1234.567,00"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble(".02"));

        Assert.assertFalse(NumberUtil.isValidLocaleDouble("A4FC9"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("0x42"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("1.2345678E7"));

        // Group 2 : dot on decimal separator, comma on group separator.
        final Locale dotGroupLocale = Locale.US;
        setResourceLoaderLocale(dotGroupLocale);

        Assert.assertTrue(NumberUtil.isValidLocaleDouble("1,234.00"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("3.00"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("456457546"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("3524.055"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("2,300"));
        // No integer part
        Assert.assertTrue(NumberUtil.isValidLocaleDouble(".01"));
        // Longer numbers with separators
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("1,234,567.00"));

        Assert.assertFalse(NumberUtil.isValidLocaleDouble("2,00"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("3.520,55"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("3,4561.00"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("1234,567.00"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble(",02"));

        Assert.assertFalse(NumberUtil.isValidLocaleDouble("A4FC9"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("0x42"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("1.2345678E7"));

        // Group 3 : arabic
        final Locale arabicLocale = new Locale("ar", "DZ");
        setResourceLoaderLocale(arabicLocale);

        // Arabic separators are special unicode chars so we get it from locale NumberFormat configuration.
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(arabicLocale);
        final char arabicGroup = df.getDecimalFormatSymbols().getGroupingSeparator();
        final char arabicDecimal = df.getDecimalFormatSymbols().getDecimalSeparator();

        Assert.assertTrue(NumberUtil.isValidLocaleDouble("1" + arabicGroup + "234" + arabicDecimal + "00"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("3" + arabicDecimal + "00"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("456457546"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("3524" + arabicDecimal + "055"));
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("2" + arabicGroup + "300"));
        // No integer part
        Assert.assertTrue(NumberUtil.isValidLocaleDouble(arabicDecimal + "02"));

        Assert.assertFalse(NumberUtil.isValidLocaleDouble("3" + arabicDecimal + "520" + arabicGroup + "55"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("3" + arabicDecimal + "4561" + arabicGroup + "00"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble(arabicGroup + "01"));

        Assert.assertFalse(NumberUtil.isValidLocaleDouble("A4FC9"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("0x42"));
        Assert.assertFalse(NumberUtil.isValidLocaleDouble("1.2345678E7"));

        // Just tests function call
        Assert.assertTrue(NumberUtil.isValidLocaleDouble("123"));
    }

    @Test
    public void testNormalizeLocaleDoubleWithLocale() {
        Locale spain = new Locale("es", "ES");

        Assert.assertEquals("4.25", NumberUtil.normalizeLocaleDouble("4.25", Locale.US));
        Assert.assertEquals("4,25", NumberUtil.normalizeLocaleDouble("4.25", spain));
        Assert.assertEquals("4,25", NumberUtil.normalizeLocaleDouble("4,25", spain));
        Assert.assertEquals("4", NumberUtil.normalizeLocaleDouble("4", spain));
        Assert.assertEquals("1234,56", NumberUtil.normalizeLocaleDouble("1.234,56", spain));
        Assert.assertEquals("1234567,89", NumberUtil.normalizeLocaleDouble("1.234.567,89", spain));
        Assert.assertEquals(" ", NumberUtil.normalizeLocaleDouble(" ", Locale.FRANCE));
        Assert.assertEquals("abc", NumberUtil.normalizeLocaleDouble("abc", Locale.FRANCE));
        Assert.assertNull(NumberUtil.normalizeLocaleDouble(null, Locale.FRANCE));
    }

    @Test
    public void testParseLocaleDoubleWithLocale() {
        Locale spain = new Locale("es", "ES");

        Assert.assertEquals(4.25d, NumberUtil.parseLocaleDouble("4.25", Locale.US), 0.0001);
        Assert.assertEquals(4.25d, NumberUtil.parseLocaleDouble("4,25", spain), 0.0001);
        Assert.assertEquals(1234.56d, NumberUtil.parseLocaleDouble("1.234,56", spain), 0.0001);
        Assert.assertEquals(1234567.89d, NumberUtil.parseLocaleDouble("1.234.567,89", spain), 0.0001);
        Assert.assertNull(NumberUtil.parseLocaleDouble("abc", spain));
        Assert.assertNull(NumberUtil.parseLocaleDouble(" ", spain));
        Assert.assertNull(NumberUtil.parseLocaleDouble(null, spain));
    }

    @Test
    public void testNormalizeLocaleDoubleDefaultLocale() {
        Locale spain = new Locale("es", "ES");
        setResourceLoaderLocale(spain);

        Assert.assertEquals("4,25", NumberUtil.normalizeLocaleDouble("4.25"));
        Assert.assertEquals("4", NumberUtil.normalizeLocaleDouble("4"));
        Assert.assertEquals("1234,56", NumberUtil.normalizeLocaleDouble("1.234,56"));
    }

    private static void setResourceLoaderLocale(Locale locale) {
        NumberUtil.setResourceLoader(new FixedLocaleResourceLoader(locale));
    }

    private static class FixedLocaleResourceLoader extends ResourceLoader {
        private final Locale locale;

        FixedLocaleResourceLoader(Locale locale) {
            this.locale = locale;
        }

        @Override
        public Locale getLocale() {
            return locale;
        }
    }

    @After
    public void resetLocale() {
        NumberUtil.setResourceLoader(new ResourceLoader());
    }
}
