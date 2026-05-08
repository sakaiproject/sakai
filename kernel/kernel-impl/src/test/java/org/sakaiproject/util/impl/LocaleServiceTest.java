/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.util.impl;

import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.api.LocaleService;

@Slf4j
public class LocaleServiceTest extends SakaiKernelTestBase {

    private static final Locale SPAIN = new Locale("es", "ES");

    private static final String SITE_WITH_LOCALE    = "locale-test-site-with-locale";
    private static final String SITE_WITHOUT_LOCALE = "locale-test-site-no-locale";
    private static final String USER_WITH_LOCALE    = "locale-test-user";
    private static final String LOCALE_PREFS_KEY    = "sakai:resourceloader";

    private static LocaleService localeService;

    @BeforeClass
    public static void beforeClass() throws Exception {
        try {
            oneTimeSetup();
        } catch (Exception e) {
            log.warn("Failed to start kernel for LocaleServiceTest", e);
            throw e;
        }

        SessionManager sessionManager = getService(SessionManager.class);
        Session session = sessionManager.getCurrentSession();
        session.setUserId("admin");
        session.setUserEid("admin");

        SiteService siteService = getService(SiteService.class);
        PreferencesService preferencesService = getService(PreferencesService.class);
        localeService = getService(LocaleService.class);

        // Site with France locale
        Site siteWithLocale = siteService.addSite(SITE_WITH_LOCALE, "test");
        siteWithLocale.getPropertiesEdit().addProperty(Site.PROP_SITE_LOCALE, "fr_FR");
        siteService.save(siteWithLocale);

        // Site with no locale set
        siteService.addSite(SITE_WITHOUT_LOCALE, "test");

        // User with Germany locale preference
        preferencesService.applyEditWithAutoCommit(USER_WITH_LOCALE,
                edit -> edit.getPropertiesEdit(LOCALE_PREFS_KEY).addProperty(Preferences.FIELD_LOCALE, "de_DE"));
    }

    @Test
    public void siteLocaleOverridesUserLocale() {
        Assert.assertEquals(Locale.FRANCE, localeService.getLocaleForSiteAndUser(SITE_WITH_LOCALE, USER_WITH_LOCALE));
    }

    @Test
    public void userLocaleUsedWhenSiteHasNoLocale() {
        Assert.assertEquals(Locale.GERMANY, localeService.getLocaleForSiteAndUser(SITE_WITHOUT_LOCALE, USER_WITH_LOCALE));
    }

    @Test
    public void fallsBackToDefaultWhenNeitherSiteNorUserHaveLocale() {
        Assert.assertEquals(Locale.getDefault(), localeService.getLocaleForSiteAndUser(null, null));
    }

    @Test
    public void neverReturnsNullForUnknownIds() {
        Assert.assertNotNull(localeService.getLocaleForSiteAndUser("unknown-site", "unknown-user"));
    }

    @Test
    public void getLocaleForCurrentSiteAndUserNeverReturnsNull() {
        Assert.assertNotNull(localeService.getLocaleForCurrentSiteAndUser());
    }

    @Test
    public void parseDoubleStandardDotDecimal() {
        Assert.assertEquals(4.25, localeService.parseDouble("4.25", Locale.US), 0.0001);
    }

    @Test
    public void parseDoubleLocaleCommaDecimal() {
        Assert.assertEquals(4.25, localeService.parseDouble("4,25", SPAIN), 0.0001);
    }

    @Test
    public void parseDoubleWithGroupingSeparator() {
        Assert.assertEquals(1234.56, localeService.parseDouble("1.234,56", SPAIN), 0.0001);
    }

    @Test
    public void parseDoubleLargeNumber() {
        Assert.assertEquals(1234567.89, localeService.parseDouble("1.234.567,89", SPAIN), 0.0001);
    }

    @Test
    public void parseDoubleReturnsNullForNonNumeric() {
        Assert.assertNull(localeService.parseDouble("abc", SPAIN));
    }

    @Test
    public void parseDoubleReturnsNullForBlank() {
        Assert.assertNull(localeService.parseDouble(" ", SPAIN));
    }

    @Test
    public void parseDoubleReturnsNullForNull() {
        Assert.assertNull(localeService.parseDouble(null, SPAIN));
    }

    @Test
    public void normalizeDoubleUsDotPreserved() {
        Assert.assertEquals("4.25", localeService.normalizeDouble("4.25", Locale.US));
    }

    @Test
    public void normalizeDoubleConvertsToSpainFormat() {
        Assert.assertEquals("4,25", localeService.normalizeDouble("4.25", SPAIN));
    }

    @Test
    public void normalizeDoubleStripsGroupingSeparator() {
        Assert.assertEquals("1234,56", localeService.normalizeDouble("1.234,56", SPAIN));
    }

    @Test
    public void normalizeDoubleReturnsBlankUnchanged() {
        Assert.assertEquals(" ", localeService.normalizeDouble(" ", Locale.FRANCE));
    }

    @Test
    public void normalizeDoubleReturnsNonNumericUnchanged() {
        Assert.assertEquals("abc", localeService.normalizeDouble("abc", Locale.FRANCE));
    }

    @Test
    public void normalizeDoubleReturnsNullForNull() {
        Assert.assertNull(localeService.normalizeDouble(null, Locale.FRANCE));
    }

    @Test
    public void formatDoubleUs() {
        Assert.assertEquals("4.25", localeService.formatDouble(4.25, Locale.US));
    }

    @Test
    public void formatDoubleSpain() {
        Assert.assertEquals("4,25", localeService.formatDouble(4.25, SPAIN));
    }

    @Test
    public void formatDoubleNoTrailingZeros() {
        Assert.assertEquals("4", localeService.formatDouble(4.0, Locale.US));
    }

    @Test
    public void formatDoubleBySiteAndUserUsesResolvedLocale() {
        Assert.assertEquals("4,25", localeService.formatDouble(4.25, SITE_WITH_LOCALE, USER_WITH_LOCALE));
    }

    @Test
    public void isValidDoubleInteger() {
        Assert.assertTrue(localeService.isValidDouble("42", Locale.US));
    }

    @Test
    public void isValidDoubleDecimalUs() {
        Assert.assertTrue(localeService.isValidDouble("4.25", Locale.US));
    }

    @Test
    public void isValidDoubleDecimalSpain() {
        Assert.assertTrue(localeService.isValidDouble("4,25", SPAIN));
    }

    @Test
    public void isValidDoubleAlphaIsInvalid() {
        Assert.assertFalse(localeService.isValidDouble("abc", Locale.US));
    }

    @Test
    public void isValidDoubleGroupedDecimalUs() {
        Assert.assertTrue(localeService.isValidDouble("1,234.56", Locale.US));
    }

    @Test
    public void isValidDoubleGroupedDecimalGermany() {
        Assert.assertTrue(localeService.isValidDouble("1.234,56", Locale.GERMANY));
    }

    @Test
    public void isValidDoubleGroupedIntegerUs() {
        Assert.assertTrue(localeService.isValidDouble("1,234", Locale.US));
    }

    // Regression: the original regex concatenated the decimal separator char directly into the
    // pattern without a preceding \, so for en_US the first grouped-decimal alternative was
    // \d{1,3}(\,\d{3})+.\d+ where '.' matched any character. "1,234x56" would incorrectly
    // pass. The fix escapes the separator: \d{1,3}(\,\d{3})+\.\d+
    @Test
    public void isValidDoubleGroupedWithNonSeparatorCharIsInvalid() {
        Assert.assertFalse(localeService.isValidDouble("1,234x56", Locale.US));
    }

    @Test
    public void isValidDoubleNullIsInvalid() {
        Assert.assertFalse(localeService.isValidDouble(null, Locale.US));
    }

    @Test
    public void isValidDoubleBlankIsInvalid() {
        Assert.assertFalse(localeService.isValidDouble("", Locale.US));
    }
}
