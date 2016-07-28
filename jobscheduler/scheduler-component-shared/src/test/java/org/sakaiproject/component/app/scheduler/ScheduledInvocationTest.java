package org.sakaiproject.component.app.scheduler;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.DirectSchedulerFactory;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.time.api.Time;

import java.util.UUID;

/**
 * Check it's working as expected.
 */
public class ScheduledInvocationTest {

    public static final String COMPONENT_ID = "componentId";
    public static final String ALL = "";
    public static final String CONTEXT = "opaqueContext";
    private ScheduledInvocationManagerImpl manager;
    private Scheduler scheduler;

    @Before
    public void setUp() throws SchedulerException {
        DirectSchedulerFactory schedulerFactory = DirectSchedulerFactory.getInstance();
        schedulerFactory.createVolatileScheduler(1);
        scheduler = schedulerFactory.getScheduler();
        scheduler.start();
        Assert.assertNotNull(scheduler);


        manager = new ScheduledInvocationManagerImpl();
        manager.setIdManager(() -> UUID.randomUUID().toString());
        manager.setSchedulerFactory(schedulerFactory);
    }

    @After
    public void tearDown() throws SchedulerException {
        scheduler.clear();
        scheduler.shutdown();
    }

    @Test
    public void testFindEmpty() {
        DelayedInvocation[] delayedInvocations = manager.findDelayedInvocations(ALL, ALL);
        Assert.assertEquals(0, delayedInvocations.length);
    }

    @Test
    public void testCreateAndFind() {
        Time time = Mockito.mock(Time.class);
        Mockito.when(time.getTime()).thenReturn(0L);
        String uuid = manager.createDelayedInvocation(time, COMPONENT_ID, CONTEXT);
        // Check we can find it.
        DelayedInvocation[] addedInvocations = manager.findDelayedInvocations(ALL, ALL);
        Assert.assertNotNull(addedInvocations);
        Assert.assertEquals(1, addedInvocations.length);
        Assert.assertEquals(uuid, addedInvocations[0].uuid);
        Assert.assertEquals(uuid, addedInvocations[0].uuid);
    }

    @Test
    public void testCreateAndDelete() {
        Time time = Mockito.mock(Time.class);
        Mockito.when(time.getTime()).thenReturn(0L);
        String uuid = manager.createDelayedInvocation(time, COMPONENT_ID, CONTEXT);
        manager.deleteDelayedInvocation(uuid);
        DelayedInvocation[] empty = manager.findDelayedInvocations(ALL, ALL);
        Assert.assertArrayEquals(new DelayedInvocation[0], empty);
    }

    @Test
    public void testCreateAndDeleteBySearch() {
        Time time = Mockito.mock(Time.class);
        Mockito.when(time.getTime()).thenReturn(0L);
        String uuid = manager.createDelayedInvocation(time, COMPONENT_ID, CONTEXT);
        manager.deleteDelayedInvocation(COMPONENT_ID, CONTEXT);
        DelayedInvocation[] empty = manager.findDelayedInvocations(ALL, ALL);
//        Assert.assertArrayEquals(new DelayedInvocation[0], empty);
    }

    @Test
    public void testCreateMultiple() {
        Time time = Mockito.mock(Time.class);
        Mockito.when(time.getTime()).thenReturn(0L);
        String first = manager.createDelayedInvocation(time, COMPONENT_ID, CONTEXT);
        String second = manager.createDelayedInvocation(time, COMPONENT_ID, CONTEXT);
        Assert.assertNotEquals(first, second);
    }

    @Test
    public void testCreateMultipleContexts() {
        Time time = Mockito.mock(Time.class);
        Mockito.when(time.getTime()).thenReturn(0L);
        String first = manager.createDelayedInvocation(time, COMPONENT_ID, CONTEXT + 1);
        String second = manager.createDelayedInvocation(time, COMPONENT_ID, CONTEXT + 2);
        Assert.assertNotEquals(first, second);
    }

}
