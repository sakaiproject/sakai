package org.sakaiproject.sitestats.test;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.sitestats.api.LessonBuilderStat;
import org.sakaiproject.sitestats.impl.LessonBuilderStatImpl;

public class LessonBuilderStatTest {
    final static int BEFORE = -1;
    final static int SAME = 0;
    final static int AFTER = 1;

    @Test
    public void testCompareTo() {
        LessonBuilderStat lbsOne = new LessonBuilderStatImpl();
        LessonBuilderStat lbsTwo = new LessonBuilderStatImpl();

        Assert.assertEquals(SAME, lbsOne.compareTo(lbsTwo));

        Date now = new Date();
        LessonBuilderStat lbsThree = new LessonBuilderStatImpl();
        lbsThree.setId(1L);
        lbsThree.setUserId("*SOME-USER*");
        lbsThree.setSiteId("*SOME-SITE*");
        lbsThree.setPageRef("*SOME-PAGE_REF*");
        lbsThree.setPageAction("*SOME-PAGE_ACTION*");
        lbsThree.setPageTitle("*SOME-PAGE_TITLE*");
        lbsThree.setPageId(100);
        lbsThree.setCount(10);
        lbsThree.setDate(now);

        Assert.assertEquals(SAME, lbsThree.compareTo(lbsThree));
        Assert.assertEquals(BEFORE, lbsOne.compareTo(lbsThree));

        LessonBuilderStat lbsFour = new LessonBuilderStatImpl();
        lbsFour.setId(1L);
        lbsFour.setUserId("*SOME-USER*");
        lbsFour.setSiteId("*SOME-SITE*");
        lbsFour.setPageRef("*SOME-PAGE_REF*");
        lbsFour.setPageAction("*SOME-PAGE_ACTION*");
        lbsFour.setPageTitle("*SOME-PAGE_TITLE*");
        lbsFour.setPageId(100);
        lbsFour.setCount(10);
        lbsFour.setDate(now);

        Assert.assertEquals(SAME, lbsFour.compareTo(lbsThree));

        lbsFour.setDate(Date.from(now.toInstant().plusSeconds(10)));
        Assert.assertEquals(AFTER, lbsFour.compareTo(lbsThree));

        lbsThree.setUserId("*SOME-USER-1*");
        Assert.assertTrue(lbsFour.compareTo(lbsThree) <= BEFORE);

        lbsFour.setSiteId("*SOME-SITE-1*");
        Assert.assertTrue(lbsFour.compareTo(lbsThree) >= AFTER);
    }
}
