/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.events.hibernate;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.PropertyValueException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;
import org.sakaiproject.scheduler.events.hibernate.TriggerEventHibernateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(locations={"/testApplicationContext.xml"})
public class TestTriggerEventManagerHibernateImpl extends AbstractTransactionalJUnit4SpringContextTests
{
    @Autowired
    private TriggerEventManager mgr;

    private static final Date TEST_DATE;

    static
    {
        //Need to create a time with 0 in the milliseconds field ao that equality comparisons work with values
        //  returned from the DB
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.MILLISECOND, 0);

        TEST_DATE = new Date(cal.getTimeInMillis());
    }

    private TriggerEventHibernateImpl getTestTriggerEvent()
    {
        TriggerEventHibernateImpl
            evt = new TriggerEventHibernateImpl();

        evt.setEventType(TriggerEvent.TRIGGER_EVENT_TYPE.DEBUG);
        evt.setJobName("job name");
        evt.setTriggerName("trigger name");
        evt.setMessage("message");
        evt.setTime(TEST_DATE);

        return evt;
    }

    private final void assertEquals(TriggerEventHibernateImpl evt1, TriggerEventHibernateImpl evt2)
        throws Exception
    {
        Assert.assertEquals(evt1.getEventType(), evt2.getEventType());
        Assert.assertEquals(evt1.getJobName(), evt2.getJobName());
        Assert.assertEquals(evt1.getMessage(), evt2.getMessage());
        Assert.assertEquals(evt1.getTriggerName(), evt2.getTriggerName());
        Assert.assertEquals(evt1.getTime(), evt2.getTime());
    }

    private final void generateEvents (int count)
        throws Exception
    {
        int eventTypeCount = TriggerEvent.TRIGGER_EVENT_TYPE.values().length;

        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(TEST_DATE.getTime());

        for (int i = count; i > 0; i--)
        {
            TriggerEvent.TRIGGER_EVENT_TYPE
                type = TriggerEvent.TRIGGER_EVENT_TYPE.values()[i % eventTypeCount];

            Date newDate = new Date(cal.getTimeInMillis());

            mgr.createTriggerEvent(type, JobKey.jobKey("job name " + i), TriggerKey.triggerKey("trigger name " + i),
                                   newDate, "message " + i, "server1");

            cal.add(Calendar.HOUR, -1);
        }
    }

    @After
    public final void cleanUp()
        throws Exception
    {
        mgr.purgeEvents(new Date());
    }

    @Test
    public void testPurgeEventsBeforeDate()
        throws Exception
    {
        generateEvents(5);

        Calendar
            cal = Calendar.getInstance();

        cal.setTimeInMillis(TEST_DATE.getTime());

        cal.add(Calendar.HOUR, -2);

        Date
            before = new Date (cal.getTimeInMillis());

        mgr.purgeEvents(before);

        List<TriggerEvent>
            results = mgr.getTriggerEvents();

        Assert.assertNotNull(results);
        Assert.assertEquals(3, results.size());

        for (TriggerEvent evt : results)
        {
            Date
                evtTime = evt.getTime();
            long
                evtMillis = evtTime.getTime(),
                b4Millis = before.getTime();

            Assert.assertTrue (b4Millis <= evtMillis);
        }
    }

    @Test
    public void testPurgeEventsOnEmptyTableSucceeds()
        throws Exception
    {
        cleanUp();
    }

    @Test
    public void testPurgeEventsWithNullArgumentHasNoEffect()
        throws Exception
    {
        generateEvents(5);

        mgr.purgeEvents(null);

        List<TriggerEvent>
            results = mgr.getTriggerEvents();

        Assert.assertNotNull(results);
        Assert.assertEquals (5, results.size());
    }

    @Test
    public void testCreateTriggerEventSucceds()
        throws Exception
    {
        final TriggerEventHibernateImpl
            evt = getTestTriggerEvent();

        final TriggerEventHibernateImpl
            result = (TriggerEventHibernateImpl)mgr.createTriggerEvent(evt.getEventType(),
                                                                       JobKey.jobKey(evt.getJobName()),
                                                                       TriggerKey.triggerKey(evt.getTriggerName()),
                                                                       evt.getTime(),
                                                                       evt.getMessage(),
                                                                       "server1");

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getId());

        assertEquals(evt, result);
    }

    @Test
    public void testCreateTriggerEventFailsOnInvalidInput() throws Exception
    {
        final TriggerEventHibernateImpl evt = getTestTriggerEvent();

        try
        {
            mgr.createTriggerEvent(null, JobKey.jobKey(evt.getJobName()), TriggerKey.triggerKey(evt.getTriggerName()), evt.getTime(), evt.getMessage(), "server1");
            Assert.fail("createTriggerEvent accepted null TriggerEventType");
        }
        catch (Exception e)
        {
            Assert.assertTrue(e instanceof PropertyValueException);
        }

        try
        {
            mgr.createTriggerEvent(evt.getEventType(), null, TriggerKey.triggerKey(evt.getTriggerName()), evt.getTime(), evt.getMessage(), "server1");
            Assert.fail("createTriggerEvent accepted null Job name");
        }
        catch (Exception e)
        {
            Assert.assertTrue(e instanceof NullPointerException);
        }

        try
        {
            mgr.createTriggerEvent(evt.getEventType(), JobKey.jobKey(evt.getJobName()), TriggerKey.triggerKey(evt.getTriggerName()), null, evt.getMessage(), "server1");
            Assert.fail("createTriggerEvent accepted null time");
        }
        catch (Exception e)
        {
            Assert.assertTrue(e instanceof PropertyValueException);
        }
    }

    @Test
    public void testGetTriggerEventsReturnsEmptyListWhenAppropriate()
        throws Exception
    {
        final List<TriggerEvent>
            results = mgr.getTriggerEvents();

        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void testGetTriggerEventsReturnsCompleteResults()
        throws Exception
    {
        generateEvents(100);

        List<TriggerEvent>
            result = mgr.getTriggerEvents();

        Assert.assertNotNull(result);
        Assert.assertEquals (100, result.size());
    }

    @Test
    public void testGetTriggerEventsNullArgsMatchesNoArgMethodResults ()
        throws Exception
    {
        generateEvents(5);

        List<TriggerEvent>
            noArgResult = mgr.getTriggerEvents(),
            nullArgResult = mgr.getTriggerEvents(null, null, null, null, null);

        Assert.assertNotNull(noArgResult);
        Assert.assertNotNull(nullArgResult);
        Assert.assertEquals (5, noArgResult.size());
        Assert.assertEquals (5, nullArgResult.size());

        Set<TriggerEvent>
            noArgSet = new HashSet<TriggerEvent> (noArgResult);

        for (TriggerEvent evt : nullArgResult)
        {
            Assert.assertTrue(noArgSet.contains(evt));
            noArgSet.remove(evt);
        }

        Assert.assertTrue(noArgSet.isEmpty());
    }

    @Test
    public void testGetTriggerEventsAfterDateBoundaryConditions()
        throws Exception
    {
        generateEvents(5);

        Calendar
            cal = Calendar.getInstance();

        cal.setTimeInMillis(TEST_DATE.getTime());

        List<TriggerEvent>
            results = null;

        // test 1 hour beyond last event
        cal.add(Calendar.HOUR, 1);

        for (int i = 0; i < 6; i++)
        {
            results = mgr.getTriggerEvents(new Date(cal.getTimeInMillis()), null, null, null, null);

            Assert.assertNotNull (results);
            Assert.assertEquals (i, results.size());

            cal.add(Calendar.HOUR, -1);
        }
    }

    @Test
    public void testGetTriggerEventsBeforeDateBoundaryConditions()
        throws Exception
    {
        generateEvents(5);

        Calendar
            cal = Calendar.getInstance();

        cal.setTimeInMillis(TEST_DATE.getTime());

        List<TriggerEvent>
            results = null;

        // test 1 hour beyond last event
        cal.add(Calendar.HOUR, -5);

        for (int i = 0; i < 6; i++)
        {
            results = mgr.getTriggerEvents(null, new Date(cal.getTimeInMillis()), null, null, null);

            Assert.assertNotNull (results);
            Assert.assertEquals (i, results.size());

            cal.add(Calendar.HOUR, 1);
        }
    }

    @Test
    public void testGetTriggerEventsBetweenDates()
        throws Exception
    {
        generateEvents(5);

        Calendar
            cal = Calendar.getInstance();

        cal.setTimeInMillis(TEST_DATE.getTime());
        cal.add(Calendar.HOUR, -5);

        Date
            dates[] = new Date[7];

        for (int i = 0; i < 7; i++)
        {
            dates[i] = new Date(cal.getTimeInMillis());
            cal.add(Calendar.HOUR, 1);
        }

        List<TriggerEvent>
            results = null;

        //should return empty list - can't have after less than before
        results = mgr.getTriggerEvents(dates[4], dates[2], null, null, null);

        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
        
        //should return the last event only
        results = mgr.getTriggerEvents(dates[5], dates[6], null, null, null);

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());

        TriggerEvent
            evt = results.get(0);

        Assert.assertEquals (TEST_DATE, evt.getTime());

        //should return the first event only
        results = mgr.getTriggerEvents(dates[0], dates[1], null, null, null);

        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());

        evt = results.get(0);

        Assert.assertEquals (dates[1], evt.getTime());

        //should return the all events
        results = mgr.getTriggerEvents(dates[1], dates[5], null, null, null);

        Assert.assertNotNull(results);
        Assert.assertEquals(5, results.size());
    }

    @Test
    public void testGetTriggerEventsByJobList()
        throws Exception
    {
        generateEvents(5);

        List<TriggerEvent>
            results = null;
        List<String>
            jobs = new LinkedList<String>();

        // empty job list should return an empty result list
        results = mgr.getTriggerEvents(null, null, jobs, null, null);

        Assert.assertNotNull(results);
        Assert.assertEquals(5, results.size());

        jobs.add("job name 1");
        jobs.add("job name 2");
        jobs.add("job name 3");
        jobs.add("job name 4");
        jobs.add("job name 5");

        for (TriggerEvent evt : results)
        {
            String
                jn = evt.getJobName();

            Assert.assertTrue(jobs.contains(jn));
            jobs.remove(jn);
        }

        // check getting a couple of jobs with different names
        jobs.clear();
        jobs.add("job name 1");
        jobs.add("job name 3");

        results = mgr.getTriggerEvents(null, null, jobs, null, null);

        Assert.assertNotNull(results);
        Assert.assertEquals (2, results.size());

        for (TriggerEvent evt : results)
        {
            String
                jn = evt.getJobName();

            Assert.assertTrue(jobs.contains(jn));
            jobs.remove(jn);
        }

        // check getting a couple of jobs with the same name
        jobs.clear();
        jobs.add("job name 2");
        generateEvents(5);

        results = mgr.getTriggerEvents(null, null, jobs, null, null);

        Assert.assertNotNull(results);
        Assert.assertEquals (2, results.size());

        for (TriggerEvent evt : results)
        {
            Assert.assertEquals("job name 2", evt.getJobName());
        }
    }

    @Test
    public void testGetTriggerEventsByTriggerName()
        throws Exception
    {
        generateEvents(5);

        List<TriggerEvent>
            results = null;
        String
            triggerName = null;

        // unknown trigger name should return an empty result list
        triggerName = "bogus";
        results = mgr.getTriggerEvents(null, null, null, triggerName, null);

        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());

        // check getting a couple a single event by trigger name
        triggerName = "trigger name 3";

        results = mgr.getTriggerEvents(null, null, null, triggerName, null);

        Assert.assertNotNull(results);
        Assert.assertEquals (1, results.size());
        Assert.assertEquals (triggerName, ((TriggerEvent)results.get(0)).getTriggerName());

        // check getting a couple of events with the same trigger name
        generateEvents(5);

        results = mgr.getTriggerEvents(null, null, null, triggerName, null);

        Assert.assertNotNull(results);
        Assert.assertEquals (2, results.size());

        for (TriggerEvent evt : results)
        {
            Assert.assertEquals(triggerName, evt.getTriggerName());
        }
    }

    @Test
    public void testGetTriggerEventsByEventType()
        throws Exception
    {
        TriggerEvent.TRIGGER_EVENT_TYPE[]
            values = TriggerEvent.TRIGGER_EVENT_TYPE.values(),
            searchValue = new TriggerEvent.TRIGGER_EVENT_TYPE[1];
        int
            numTypes = values.length,
            count = 3 * numTypes;
        List<TriggerEvent>
            results = null;

        generateEvents (count);

        for (int x = 0; x < numTypes; x++)
        {
            searchValue[0] = values[x];

            results = mgr.getTriggerEvents(null, null, null, null, searchValue);

            Assert.assertNotNull(results);
            Assert.assertEquals (3, results.size());

            for (TriggerEvent evt : results)
            {
                Assert.assertEquals (searchValue[0], evt.getEventType());
            }
        }
    }

}