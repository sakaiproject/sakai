package org.sakaiproject.signup.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/hibernate-test.xml"
})
@Slf4j
public class SignupMeetingDaoTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    @Qualifier("org.sakaiproject.signup.dao.SignupMeetingDao")
    private SignupMeetingDao dao;

    private SignupMeeting meeting(String siteId, String category, String location) {
        SignupMeeting m = new SignupMeeting();
        m.setTitle("T");
        m.setDescription("D");
        m.setLocation(location);
        m.setCategory(category);
        m.setMeetingType("once");
        m.setCreatorUserId("u1");
        Date now = new Date();
        m.setStartTime(now);
        m.setEndTime(new Date(now.getTime() + 60 * 60 * 1000));
        SignupSite ss = new SignupSite();
        ss.setSiteId(siteId);
        List<SignupSite> sites = new ArrayList<>();
        sites.add(ss);
        m.setSignupSites(sites);
        return m;
    }

    @Test
    public void testAutoReminderCountsAndList() {
        String site = "site-auto";
        Date base = new Date();
        Date startRange = base; // window starts now
        Date endRange = new Date(base.getTime() + 4 * 60 * 60 * 1000); // +4h

        // m1: inside window, autoReminder=true -> counted and listed
        SignupMeeting m1 = meeting(site, "Cat", "Loc");
        m1.setAutoReminder(true);
        m1.setStartTime(new Date(base.getTime() + 60 * 60 * 1000)); // +1h
        m1.setEndTime(new Date(base.getTime() + 2 * 60 * 60 * 1000)); // +2h
        dao.saveMeeting(m1);

        // m2: starts before window, ends inside -> listed, not counted (since count is by startTime between)
        SignupMeeting m2 = meeting(site, "Cat", "Loc");
        m2.setAutoReminder(true);
        m2.setStartTime(new Date(base.getTime() - 60 * 60 * 1000)); // -1h
        m2.setEndTime(new Date(base.getTime() + 30 * 60 * 1000)); // +30m
        dao.saveMeeting(m2);

        // m3: starts after window -> not counted, not listed
        SignupMeeting m3 = meeting(site, "Cat", "Loc");
        m3.setAutoReminder(true);
        m3.setStartTime(new Date(base.getTime() + 5 * 60 * 60 * 1000)); // +5h
        m3.setEndTime(new Date(base.getTime() + 6 * 60 * 60 * 1000)); // +6h
        dao.saveMeeting(m3);

        // m4: inside window but autoReminder=false -> ignored
        SignupMeeting m4 = meeting(site, "Cat", "Loc");
        m4.setAutoReminder(false);
        m4.setStartTime(new Date(base.getTime() + 2 * 60 * 60 * 1000)); // +2h
        m4.setEndTime(new Date(base.getTime() + 3 * 60 * 60 * 1000)); // +3h
        dao.saveMeeting(m4);

        int total = dao.getAutoReminderTotalEventCounts(startRange, endRange);
        Assert.assertEquals(1, total); // only m1 counted

        List<SignupMeeting> list = dao.getAutoReminderSignupMeetings(startRange, endRange);
        Assert.assertNotNull(list);
        // should include m1 and m2, exclude m3/m4
        Set<Long> ids = new HashSet<>();
        list.forEach(sm -> ids.add(sm.getId()));
        Assert.assertTrue(ids.contains(m1.getId()));
        Assert.assertTrue(ids.contains(m2.getId()));
        Assert.assertFalse(ids.contains(m3.getId()));
        Assert.assertFalse(ids.contains(m4.getId()));
    }

    @Test
    public void testGetSignupMeetingsInSitesEmptyList() {
        Date now = new Date();
        Date later = new Date(now.getTime() + 24 * 60 * 60 * 1000);
        List<SignupMeeting> result = dao.getSignupMeetingsInSites(Collections.emptyList(), now, later);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testCategoriesEmptyAndWithNull() {
        String site = "site-cat";
        // No meetings yet -> expect null
        Assert.assertNull(dao.getAllCategories(site));

        // Persist a meeting with null category
        SignupMeeting m1 = meeting(site, null, "Loc1");
        dao.saveMeeting(m1);

        List<String> cats1 = dao.getAllCategories(site);
        Assert.assertNotNull(cats1);
        Assert.assertTrue(cats1.contains(null));

        // Persist a meeting with a concrete category and a duplicate
        SignupMeeting m2 = meeting(site, "CatA", "Loc2");
        dao.saveMeeting(m2);
        SignupMeeting m3 = meeting(site, "CatA", "Loc3");
        dao.saveMeeting(m3);

        List<String> cats2 = dao.getAllCategories(site);
        Assert.assertNotNull(cats2);
        Assert.assertTrue(cats2.contains("CatA"));
        Assert.assertTrue(cats2.contains(null));
        // ensure distinct values (size should be 2: null and CatA)
        Set<String> distinct = new HashSet<>(cats2);
        Assert.assertEquals(2, distinct.size());
    }

    @Test
    public void testLocationsEmptyAndDistinct() {
        String site = "site-loc";
        // No meetings yet -> expect null
        Assert.assertNull(dao.getAllLocations(site));

        // Persist meetings with different locations
        SignupMeeting m1 = meeting(site, null, "LocA");
        dao.saveMeeting(m1);
        SignupMeeting m2 = meeting(site, "CatB", "LocB");
        dao.saveMeeting(m2);
        SignupMeeting m3 = meeting(site, "CatB", "LocB");
        dao.saveMeeting(m3);

        List<String> locs = dao.getAllLocations(site);
        Assert.assertNotNull(locs);
        Assert.assertTrue(locs.contains("LocA"));
        Assert.assertTrue(locs.contains("LocB"));
        Set<String> distinct = new HashSet<>(locs);
        Assert.assertEquals(2, distinct.size());
    }
}
