/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.util;

import org.junit.Assert;
import org.junit.Test;

public class StorageUtilsTest {

    @Test
    public void testEscapeSql() {
        String siteId = "Advisor's Site";
        String escapedSite = "Advisor''s Site";
        Assert.assertEquals(escapedSite, StorageUtils.escapeSql(siteId));
    }

    @Test
    public void testEscapeSqlLike() {
        String siteId = "ENGL_101_FA%21";
        String escapedSite = "ENGL\\_101\\_FA\\%21";
        Assert.assertEquals(escapedSite, StorageUtils.escapeSqlLike(siteId));
    }
}
