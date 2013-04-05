/**********************************************************************************
 *
 * $Id: BasicConfigurationServiceTest.java 107118 2012-04-16 14:41:07Z azeckoski@unicon.net $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.component.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigData;

/**
 * Used for testing protected methods in the BasicConfigurationService
 */
public class BasicConfigurationServiceTest extends TestCase {

    private static Log log = LogFactory.getLog(BasicConfigurationServiceTest.class);

    private BasicConfigurationService basicConfigurationService;
    private String SOURCE = "TEST";

    public void setUp() throws Exception {
        basicConfigurationService = new BasicConfigurationService();
        basicConfigurationService.addConfigItem( new ConfigItemImpl("name", "Aaron"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("AZ", "Aaron Zeckoski"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("testKeyEmpty", ""), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("testKeyInvalid", "testing invalid=${invalid} testing"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("testKeyNested", "testing name=${name} testing"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("testKeyNestedMulti", "testing az=${AZ} nested=${testKeyNested} invalid=${invalid}"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("test1", "test1"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("test2", "test2"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("test3", "test3"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("test4", "test4"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("test5", "test5"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("test6", "test6"), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("test7", "${AZ}"), SOURCE);
        log.info(basicConfigurationService.getConfigData());
    }

    public void testDereferenceString() throws Exception {
        // https://jira.sakaiproject.org/browse/SAK-22148
        String value = null;
        // testing for - String dereferenceValue(String value)
        value = basicConfigurationService.dereferenceValue(null);
        assertNull(value);
        value = basicConfigurationService.dereferenceValue("");
        assertNotNull(value);
        assertEquals("", value);
        value = basicConfigurationService.dereferenceValue("  ");
        assertNotNull(value);
        assertEquals("  ", value);
        value = basicConfigurationService.dereferenceValue("hello world");
        assertNotNull(value);
        assertEquals("hello world", value);
        value = basicConfigurationService.dereferenceValue("hello ${name}");
        assertNotNull(value);
        assertEquals("hello Aaron", value);
        value = basicConfigurationService.dereferenceValue("hello ${testKeyInvalid}");
        assertNotNull(value);
        assertEquals("hello testing invalid=${invalid} testing", value);
        value = basicConfigurationService.dereferenceValue("hello ${testKeyEmpty}");
        assertNotNull(value);
        assertEquals("hello ", value);
        value = basicConfigurationService.dereferenceValue("hello ${testKeyNested}");
        assertNotNull(value);
        assertEquals("hello testing name=Aaron testing", value);
        value = basicConfigurationService.dereferenceValue("hello ${testKeyNestedMulti}");
        assertNotNull(value);
        assertEquals("hello testing az=Aaron Zeckoski nested=testing name=Aaron testing invalid=${invalid}", value);
    }

    public void testDereferenceAll() throws Exception {
        // https://jira.sakaiproject.org/browse/SAK-22148
        int changed = basicConfigurationService.dereferenceConfig();
        ConfigData cd = basicConfigurationService.getConfigData();
        assertEquals(14, cd.getTotalConfigItems());
        assertEquals(3, changed); // 4 of them have keys but 1 key is invalid so it will not be replaced
        assertEquals("Aaron", basicConfigurationService.getConfig("name", "default") );
        assertEquals("testing name=Aaron testing", basicConfigurationService.getConfig("testKeyNested", "default") );
        assertEquals("testing az=Aaron Zeckoski nested=testing name=Aaron testing invalid=${invalid}", basicConfigurationService.getConfig("testKeyNestedMulti", "default") );
        assertEquals("Aaron Zeckoski", basicConfigurationService.getConfig("test7", "default") );
    }

    public void testKNL1038() throws Exception {
        /* KNL-1038 - "${sakai.home}/samigo/answerUploadRepositoryPath"
         * This is basically testing whether replacements work in default values
         * (as they should)
         */
        String val = null;
        val = basicConfigurationService.getString("name");
        assertNotSame("", val);
        assertEquals("Aaron", val);

        // namePlusLast should NOT exist
        val = basicConfigurationService.getString("namePlusLast");
        assertEquals("", val);
        val = basicConfigurationService.getString("namePlusLast", "${name} Zeckoski");
        assertNotSame("", val);
        assertEquals("Aaron Zeckoski", val);
    }

    public void testKNL_1052() {
        String val;
        val = basicConfigurationService.getString("xxxxxxxAAAZZZZ");
        assertEquals("", val);
        val = basicConfigurationService.getString("xxxxxxxAAAZZZZ","DEFAULT");
        assertEquals("DEFAULT", val);

        basicConfigurationService.addConfigItem( new ConfigItemImpl("xxxxxxxAAAZZZZ", "AZ"), SOURCE);
        val = basicConfigurationService.getString("xxxxxxxAAAZZZZ");
        assertEquals("AZ", val);
    }

    public void testLocales() {
        Locale locale;
        Locale defaultLocale = Locale.getDefault();
        Locale[] locales;
        int lsize = 0;

        locale = basicConfigurationService.getLocaleFromString("az");
        assertNotNull(locale);
        assertNotSame(defaultLocale, locale);
        assertEquals(new Locale("az"), locale);

        locale = basicConfigurationService.getLocaleFromString("az_JP");
        assertNotNull(locale);
        assertNotSame(defaultLocale, locale);
        assertEquals(new Locale("az","JP"), locale);

        locale = basicConfigurationService.getLocaleFromString("az-JP");
        assertNotNull(locale);
        assertNotSame(defaultLocale, locale);
        assertEquals(new Locale("az","JP"), locale);

        // blank should become Default Locale to match existing behavior
        locale = basicConfigurationService.getLocaleFromString("");
        assertNotNull(locale);
        assertEquals(defaultLocale, locale);

        // invalid format should become Default to match existing behaviors
        locale = basicConfigurationService.getLocaleFromString("_");
        assertNotNull(locale);
        assertEquals(defaultLocale, locale);

        locale = basicConfigurationService.getLocaleFromString("__");
        assertNotNull(locale);
        assertEquals(defaultLocale, locale);

        // null stays as null
        locale = basicConfigurationService.getLocaleFromString(null);
        assertNull(locale);


        // check the basic retrieval
        locales = basicConfigurationService.getSakaiLocales();
        assertNotNull(locales);
        lsize = locales.length;
        assertTrue( lsize > 0 );
        assertFalse( hasDuplicate(Arrays.asList(locales)) );
        assertTrue( ArrayUtils.contains(locales, Locale.getDefault()) );

        // test limited set
        basicConfigurationService.addConfigItem( new ConfigItemImpl("locales", "az, az_JP, az_ZW"), SOURCE);
        locales = basicConfigurationService.getSakaiLocales();
        assertNotNull(locales);
        lsize = locales.length;
        assertTrue( lsize > 0 );
        assertFalse( hasDuplicate(Arrays.asList(locales)) );
        assertTrue( ArrayUtils.contains(locales, Locale.getDefault()) );
        assertEquals(4, lsize);
        assertTrue( ArrayUtils.contains(locales, new Locale("az")) );
        assertTrue( ArrayUtils.contains(locales, new Locale("az","JP")) );
        assertTrue( ArrayUtils.contains(locales, new Locale("az","ZW")) );

        // test dupes and empty entries (which should just become the default)
        basicConfigurationService.addConfigItem( new ConfigItemImpl("locales", "az, az, az, az, az_JP, az_JP, az_ZW, , "), SOURCE);
        locales = basicConfigurationService.getSakaiLocales();
        assertNotNull(locales);
        lsize = locales.length;
        assertTrue( lsize > 0 );
        assertFalse( hasDuplicate(Arrays.asList(locales)) );
        assertTrue( ArrayUtils.contains(locales, Locale.getDefault()) );
        assertEquals(4, lsize);
        assertTrue( ArrayUtils.contains(locales, new Locale("az")) );
        assertTrue( ArrayUtils.contains(locales, new Locale("az","JP")) );
        assertTrue( ArrayUtils.contains(locales, new Locale("az","ZW")) );

        // test empty has at least the default one
        basicConfigurationService.addConfigItem( new ConfigItemImpl("locales", ""), SOURCE);
        locales = basicConfigurationService.getSakaiLocales();
        assertNotNull(locales);
        lsize = locales.length;
        assertTrue( lsize > 0 );
        assertFalse( hasDuplicate(Arrays.asList(locales)) );
        assertTrue( ArrayUtils.contains(locales, Locale.getDefault()) );
        assertEquals(1, lsize);
}

    public static <T> boolean hasDuplicate(Collection<T> list) {
        HashSet<T> set = new HashSet<T>();
        // Set#add returns false if the set does not change, which indicates that a duplicate element has been added.
        for (T each: list) {
            if (!set.add(each)) {
                return true;
            }
        }
        return false;
    }

}
