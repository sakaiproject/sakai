package org.sakaiproject.component.app.scheduler.events.hibernate;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.scheduler.events.hibernate.TriggerEventHibernateImpl;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Oct 7, 2010
 * Time: 2:32:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestTriggerEventManagerHibernateImpl
{
    private ClassPathXmlApplicationContext
        context = null;
    private TriggerEventManagerHibernateImpl
        temhi = null;
    private static final Date
        TEST_DATE;

    static
    {
        //Need to create a time with 0 in the milliseconds field ao that equality comparisons work with values
        //  returned from the DB
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.MILLISECOND, 0);

        TEST_DATE = new Date(cal.getTimeInMillis());
    }

    public TestTriggerEventManagerHibernateImpl()
    {
        context = new ClassPathXmlApplicationContext("testApplicationContext.xml");

        temhi = (TriggerEventManagerHibernateImpl)
            context.getBean("org.sakaiproject.component.app.scheduler.events.hibernate.TriggerEventManagerHibernateImpl");
    }

    private TriggerEventManagerHibernateImpl getEventManager()
    {
        return temhi;
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
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();
        TriggerEventHibernateImpl
            evt = null;
        int
            eventTypeCount = TriggerEvent.TRIGGER_EVENT_TYPE.values().length;

        Calendar
            cal = Calendar.getInstance();

        cal.setTimeInMillis(TEST_DATE.getTime());

        for (int i = count; i > 0; i--)
        {
            TriggerEvent.TRIGGER_EVENT_TYPE
                type = TriggerEvent.TRIGGER_EVENT_TYPE.values()[i % eventTypeCount];

            Date newDate = new Date(cal.getTimeInMillis());

            mgr.createTriggerEvent(type, "job name " + i, "trigger name " + i,
                                   newDate, "message " + i);

            cal.add(Calendar.HOUR, -1);
        }
    }

    @After
    public final void cleanUp()
        throws Exception
    {
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

        mgr.purgeEvents(new Date());
    }

    @Test
    public void testPurgeEventsBeforeDate()
        throws Exception
    {
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

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
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

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
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();
        final TriggerEventHibernateImpl
            evt = getTestTriggerEvent();

        final TriggerEventHibernateImpl
            result = (TriggerEventHibernateImpl)mgr.createTriggerEvent(evt.getEventType(),
                                                                       evt.getJobName(),
                                                                       evt.getTriggerName(),
                                                                       evt.getTime(),
                                                                       evt.getMessage());

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getId());

        assertEquals(evt, result);
    }

    @Test
    public void testCreateTriggerEventFailsOnInvalidInput()
        throws Exception
    {
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();
        final TriggerEventHibernateImpl
            evt = getTestTriggerEvent();

        boolean
            fail = true;

        try
        {
            mgr.createTriggerEvent(null, evt.getJobName(), evt.getTriggerName(), evt.getTime(), evt.getMessage());
        }
        catch (Exception e)
        {
            fail = false;
        }

        if (fail)
        {
            Assert.fail("createTriggerEvent accepted null TriggerEventType");
        }
        else
        {
            fail = true;
        }

        try
        {
            mgr.createTriggerEvent(evt.getEventType(), null, evt.getTriggerName(), evt.getTime(), evt.getMessage());
        }
        catch (Exception e)
        {
            fail = false;
        }

        if (fail)
        {
            Assert.fail("createTriggerEvent accepted null Job name");
        }
        else
        {
            fail = true;
        }
        
        try
        {
            mgr.createTriggerEvent(evt.getEventType(), evt.getJobName(), evt.getTriggerName(), null, evt.getMessage());
        }
        catch (Exception e)
        {
            fail = false;
        }

        if (fail)
        {
            Assert.fail("createTriggerEvent accepted null time");
        }
        else
        {
            fail = true;
        }
    }

    @Test
    public void testGetTriggerEventsReturnsEmptyListWhenAppropriate()
        throws Exception
    {
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

        final List<TriggerEvent>
            results = mgr.getTriggerEvents();

        Assert.assertNotNull(results);
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void testGetTriggerEventsReturnsCompleteResults()
        throws Exception
    {
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

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
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

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
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

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
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

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
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

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
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

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
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();

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
        final TriggerEventManagerHibernateImpl
            mgr = getEventManager();
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