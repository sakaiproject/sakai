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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.ArrayUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigData;
import org.sakaiproject.util.BasicConfigItem;

/**
 * Used for testing protected methods in the BasicConfigurationService
 */
@Slf4j
public class BasicConfigurationServiceTest {
    private BasicConfigurationService basicConfigurationService;
    private String SOURCE = "TEST";

    @Before
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
        basicConfigurationService.addConfigItem( new ConfigItemImpl("intVal", 11), SOURCE);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("booleanVal", true), SOURCE);
        log.info(basicConfigurationService.getConfigData().toString());
    }

    @Test
    public void testDereferenceString() throws Exception {
        // https://jira.sakaiproject.org/browse/SAK-22148
        String value = null;
        // testing for - String dereferenceValue(String value)
        value = basicConfigurationService.dereferenceValue(null);
        Assert.assertNull(value);
        value = basicConfigurationService.dereferenceValue("");
        Assert.assertNotNull(value);
        Assert.assertEquals("", value);
        value = basicConfigurationService.dereferenceValue("  ");
        Assert.assertNotNull(value);
        Assert.assertEquals("  ", value);
        value = basicConfigurationService.dereferenceValue("hello world");
        Assert.assertNotNull(value);
        Assert.assertEquals("hello world", value);
        value = basicConfigurationService.dereferenceValue("hello ${name}");
        Assert.assertNotNull(value);
        Assert.assertEquals("hello Aaron", value);
        value = basicConfigurationService.dereferenceValue("hello ${testKeyInvalid}");
        Assert.assertNotNull(value);
        Assert.assertEquals("hello testing invalid=${invalid} testing", value);
        value = basicConfigurationService.dereferenceValue("hello ${testKeyEmpty}");
        Assert.assertNotNull(value);
        Assert.assertEquals("hello ", value);
        value = basicConfigurationService.dereferenceValue("hello ${testKeyNested}");
        Assert.assertNotNull(value);
        Assert.assertEquals("hello testing name=Aaron testing", value);
        value = basicConfigurationService.dereferenceValue("hello ${testKeyNestedMulti}");
        Assert.assertNotNull(value);
        Assert.assertEquals("hello testing az=Aaron Zeckoski nested=testing name=Aaron testing invalid=${invalid}", value);
    }

    @Test
    public void testDereferenceAll() throws Exception {
        // https://jira.sakaiproject.org/browse/SAK-22148
        int changed = basicConfigurationService.dereferenceConfig();
        ConfigData cd = basicConfigurationService.getConfigData();
        Assert.assertEquals(16, cd.getTotalConfigItems());
        Assert.assertEquals(3, changed); // 4 of them have keys but 1 key is invalid so it will not be replaced
        Assert.assertEquals("Aaron", basicConfigurationService.getConfig("name", "default") );
        Assert.assertEquals("testing name=Aaron testing", basicConfigurationService.getConfig("testKeyNested", "default") );
        Assert.assertEquals("testing az=Aaron Zeckoski nested=testing name=Aaron testing invalid=${invalid}", basicConfigurationService.getConfig("testKeyNestedMulti", "default") );
        Assert.assertEquals("Aaron Zeckoski", basicConfigurationService.getConfig("test7", "default") );
    }

    @Test
    public void testKNL1038() throws Exception {
        /* KNL-1038 - "${sakai.home}/samigo/answerUploadRepositoryPath"
         * This is basically testing whether replacements work in default values
         * (as they should)
         */
        String val = null;
        val = basicConfigurationService.getString("name");
        Assert.assertNotSame("", val);
        Assert.assertEquals("Aaron", val);

        // namePlusLast should NOT exist
        val = basicConfigurationService.getString("namePlusLast");
        Assert.assertEquals("", val);
        val = basicConfigurationService.getString("namePlusLast", "${name} Zeckoski");
        Assert.assertNotSame("", val);
        Assert.assertEquals("Aaron Zeckoski", val);
    }

    @Test
    public void testKNL_1052() {
        String val;
        val = basicConfigurationService.getString("xxxxxxxAAAZZZZ");
        Assert.assertEquals("", val);
        val = basicConfigurationService.getString("xxxxxxxAAAZZZZ","DEFAULT");
        Assert.assertEquals("DEFAULT", val);

        basicConfigurationService.addConfigItem( new ConfigItemImpl("xxxxxxxAAAZZZZ", "AZ"), SOURCE);
        val = basicConfigurationService.getString("xxxxxxxAAAZZZZ");
        Assert.assertEquals("AZ", val);
    }

    @Test
    public void testLocales() {
        Locale locale;
        Locale defaultLocale = Locale.getDefault();
        Locale[] locales;
        int lsize = 0;

        locale = basicConfigurationService.getLocaleFromString("az");
        Assert.assertNotNull(locale);
        Assert.assertNotSame(defaultLocale, locale);
        Assert.assertEquals(new Locale("az"), locale);

        locale = basicConfigurationService.getLocaleFromString("az_JP");
        Assert.assertNotNull(locale);
        Assert.assertNotSame(defaultLocale, locale);
        Assert.assertEquals(new Locale("az","JP"), locale);

        locale = basicConfigurationService.getLocaleFromString("az-JP");
        Assert.assertNotNull(locale);
        Assert.assertNotSame(defaultLocale, locale);
        Assert.assertEquals(new Locale("az","JP"), locale);

        // blank should become Default Locale to match existing behavior
        locale = basicConfigurationService.getLocaleFromString("");
        Assert.assertNotNull(locale);
        Assert.assertEquals(defaultLocale, locale);

        // invalid format should become Default to match existing behaviors
        locale = basicConfigurationService.getLocaleFromString("_");
        Assert.assertNotNull(locale);
        Assert.assertEquals(defaultLocale, locale);

        locale = basicConfigurationService.getLocaleFromString("__");
        Assert.assertNotNull(locale);
        Assert.assertEquals(defaultLocale, locale);

        // null stays as null
        locale = basicConfigurationService.getLocaleFromString(null);
        Assert.assertNull(locale);


        // check the basic retrieval
        locales = basicConfigurationService.getSakaiLocales();
        Assert.assertNotNull(locales);
        lsize = locales.length;
        Assert.assertTrue( lsize > 0 );
        Assert.assertFalse( hasDuplicate(Arrays.asList(locales)) );
        Assert.assertTrue( ArrayUtils.contains(locales, Locale.getDefault()) );

        // test limited set
        basicConfigurationService.addConfigItem( new ConfigItemImpl("locales", "az, az_JP, az_ZW"), SOURCE);
        locales = basicConfigurationService.getSakaiLocales();
        Assert.assertNotNull(locales);
        lsize = locales.length;
        Assert.assertTrue( lsize > 0 );
        Assert.assertFalse( hasDuplicate(Arrays.asList(locales)) );
        Assert.assertTrue( ArrayUtils.contains(locales, Locale.getDefault()) );
        Assert.assertEquals(4, lsize);
        Assert.assertTrue( ArrayUtils.contains(locales, new Locale("az")) );
        Assert.assertTrue( ArrayUtils.contains(locales, new Locale("az","JP")) );
        Assert.assertTrue( ArrayUtils.contains(locales, new Locale("az","ZW")) );

        // test dupes and empty entries (which should just become the default)
        basicConfigurationService.addConfigItem( new ConfigItemImpl("locales", "az, az, az, az, az_JP, az_JP, az_ZW, , "), SOURCE);
        locales = basicConfigurationService.getSakaiLocales();
        Assert.assertNotNull(locales);
        lsize = locales.length;
        Assert.assertTrue( lsize > 0 );
        Assert.assertFalse( hasDuplicate(Arrays.asList(locales)) );
        Assert.assertTrue( ArrayUtils.contains(locales, Locale.getDefault()) );
        Assert.assertEquals(4, lsize);
        Assert.assertTrue( ArrayUtils.contains(locales, new Locale("az")) );
        Assert.assertTrue( ArrayUtils.contains(locales, new Locale("az","JP")) );
        Assert.assertTrue( ArrayUtils.contains(locales, new Locale("az","ZW")) );

        // test empty has at least the default one
        basicConfigurationService.addConfigItem( new ConfigItemImpl("locales", ""), SOURCE);
        locales = basicConfigurationService.getSakaiLocales();
        Assert.assertNotNull(locales);
        lsize = locales.length;
        Assert.assertTrue( lsize > 0 );
        Assert.assertFalse( hasDuplicate(Arrays.asList(locales)) );
        Assert.assertTrue( ArrayUtils.contains(locales, Locale.getDefault()) );
        Assert.assertEquals(1, lsize);
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

    @Test
    public void testKNL_1132() {
        // testing integer and boolean handling
        int intVal = basicConfigurationService.getInt("intVal", -1);
        Assert.assertEquals(11, intVal);
        intVal = basicConfigurationService.getInt("intVal2", 12); // doesn't exist
        Assert.assertEquals(12, intVal);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("intVal3", null), SOURCE);
        intVal = basicConfigurationService.getInt("intVal3", 13); // value is null
        Assert.assertEquals(13, intVal);

        boolean booleanValue = basicConfigurationService.getBoolean("booleanVal", false);
        Assert.assertEquals(true, booleanValue);
        booleanValue = basicConfigurationService.getBoolean("booleanVal2", true); // doesn't exist
        Assert.assertEquals(true, booleanValue);
        basicConfigurationService.addConfigItem( new ConfigItemImpl("booleanVal3", null), SOURCE);
        booleanValue = basicConfigurationService.getBoolean("booleanVal3", true); // value is null
        Assert.assertEquals(true, booleanValue);

        // NOTE: this is internal only (i.e. no one outside the kernel could encounter this)
        ConfigItemImpl booleanVal4 = new ConfigItemImpl("booleanVal4", null);
        booleanVal4.setDefaultValue(""); // causes an NPE
        basicConfigurationService.addConfigItem( booleanVal4, SOURCE);
        boolean booleanValue4 = basicConfigurationService.getBoolean("booleanVal4", false);
        Assert.assertEquals(false, booleanValue4);    
    }

    @Test
    public void testKNL_1137() {
        // verify that types are handled correctly for 
        basicConfigurationService.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("defaultedVal1", "string", SOURCE));
        Assert.assertEquals(ServerConfigurationService.TYPE_STRING, basicConfigurationService.getConfigItem("defaultedVal1").getType());

        basicConfigurationService.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("defaultedVal2", 123, SOURCE));
        Assert.assertEquals(ServerConfigurationService.TYPE_INT, basicConfigurationService.getConfigItem("defaultedVal2").getType());

        basicConfigurationService.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("defaultedVal3", new String[]{"AZ","BZ"}, SOURCE));
        Assert.assertEquals(ServerConfigurationService.TYPE_ARRAY, basicConfigurationService.getConfigItem("defaultedVal3").getType());

        basicConfigurationService.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("defaultedVal4", true, SOURCE));
        Assert.assertEquals(ServerConfigurationService.TYPE_BOOLEAN, basicConfigurationService.getConfigItem("defaultedVal4").getType());
    }

}
