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

import org.junit.Assert;
import org.junit.Ignore;
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
     * Test method for {@link org.sakaiproject.content.util.ZipContentUtil#getMaxZipExtractFiles()}.
     * 
     * Ignored since this test requires a running CHS
     */
	@Ignore
    @Test
    public void testGetMaxZipExtractSize() {
        long max = ZipContentUtil.getMaxZipExtractFiles();
        Assert.assertNotNull(max);
        Assert.assertTrue(max > 0);
    }

    /* No real way to test this without a running CHS -AZ */
    @Ignore
    @Test
    public void testCompressFolder() {
    }

    /* No real way to test this without a running CHS -AZ */
    @Ignore
    @Test
    public void testExtractArchive() {
        Assert.fail("Not yet implemented");
    }

    /* No real way to test this without a running CHS -AZ */
    @Ignore
    @Test
    public void testGetZipManifest() {
        Assert.fail("Not yet implemented");
    }

}
