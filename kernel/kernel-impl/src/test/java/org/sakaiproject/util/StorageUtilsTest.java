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
