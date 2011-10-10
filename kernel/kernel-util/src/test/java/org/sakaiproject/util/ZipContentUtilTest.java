/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.content.util.ZipContentUtil;


/**
 * Testing the zip utils
 * Unfortunately, there is not really a very good way to test this stuff right now without a real CHS so there are no real helpful tests :-(
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class ZipContentUtilTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.sakaiproject.content.util.ZipContentUtil#getMaxZipExtractFiles()}.
     */
    @Test
    public void testGetMaxZipExtractSize() {
        long max = ZipContentUtil.getMaxZipExtractFiles();
        assertNotNull(max);
        assertTrue(max > 0);
    }

    /* No real way to test this without a running CHS -AZ
    @Test
    public void testCompressFolder() {
    }
    */

    /* No real way to test this without a running CHS -AZ
    @Test
    public void testExtractArchive() {
        fail("Not yet implemented");
    }
    */

    /* No real way to test this without a running CHS -AZ
    @Test
    public void testGetZipManifest() {
        fail("Not yet implemented");
    }
    */

}
