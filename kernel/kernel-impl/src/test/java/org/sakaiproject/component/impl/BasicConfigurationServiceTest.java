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

import junit.framework.TestCase;

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

}
