package org.sakaiproject.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

class StorageUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testEscapeSql() {
    }

    @Test
    public void testEscapeSqlLike() {
        String siteId = "ENGL_101_FA21";
        String escapeSql = "ENGL\\_101\\_FA21";
        Assert.assertEquals(escapeSql, StorageUtils.escapeSqlLike(siteId));
    }
}