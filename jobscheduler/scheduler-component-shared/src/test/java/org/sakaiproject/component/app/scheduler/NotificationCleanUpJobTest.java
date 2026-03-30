package org.sakaiproject.component.app.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.*;
import org.quartz.impl.DirectSchedulerFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.scheduler.jobs.NotificationCleanupJob;
import org.sakaiproject.messaging.api.repository.UserNotificationRepository;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class NotificationCleanUpJobTest {


    private static final String ASSIGNMENT_TOOL_PREFIX      = "asn";
    private static final String SAMIGO_TOOL_PREFIX          = "sam";
    private static final String ANNOUNCEMENT_TOOL_PREFIX    = "annc";
    private static final String COMMONS_TOOL_PREFIX         = "commons";
    private static final String MESSAGE_TOOL_PREFIX         = "message";
    private static final String LESSONSBUILDER_TOOL_PREFIX  = "lessonbuilder";
    private final Integer defaultThreshold = 100;

    private Scheduler scheduler;


    @Mock
    private ServerConfigurationService serverConfigurationService;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @InjectMocks
    private NotificationCleanupJob job;

    private Set<String> allowedTools = Set.of(ASSIGNMENT_TOOL_PREFIX, SAMIGO_TOOL_PREFIX, ANNOUNCEMENT_TOOL_PREFIX, COMMONS_TOOL_PREFIX, MESSAGE_TOOL_PREFIX, LESSONSBUILDER_TOOL_PREFIX);

    private RepoState repoState;


    @Before
    public void setUp() throws Exception {
        repoState = new RepoState();

        DirectSchedulerFactory schedulerFactory = DirectSchedulerFactory.getInstance();
        schedulerFactory.createVolatileScheduler(1);
        scheduler = schedulerFactory.getScheduler();
        scheduler.start();


        scheduler.setJobFactory((bundle, sched) -> {
            Class<?> jobClass = bundle.getJobDetail().getJobClass();
            if (jobClass.equals(NotificationCleanupJob.class)) {
                return job;
            }
            try {
                return (Job) jobClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

        setupServerConfigurationServiceMock(defaultThreshold, null, null, null, null,null,null);
    }

    @After
    public void tearDown() throws Exception {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown(true);
        }
    }

    @Test
    public void execute_shouldRunQuartzJob() throws Exception {
        int threshold = 10;
        int userCount = 10;
        int countDown = userCount * allowedTools.size();

        CountDownLatch latch = new CountDownLatch(countDown);

        setupServerConfigurationServiceMock(10, null, null, null, null,null,null);

        setUpRandomRepoState(threshold, userCount);

        when(userNotificationRepository.findAllDistinctToUser()).thenReturn(repoState.usersWithNotifications);


        when(userNotificationRepository.countAllByToUserAndByToolAndNotDeferredOverThreshold(anyString(),argThat(allowedTools::contains), eq(threshold)))
                        .thenAnswer(inv -> {
                           String user = inv.getArgument(0);
                            String tool = inv.getArgument(1);
                            return repoState.countsByTool.get(user).get(tool);
                        });

        when(userNotificationRepository.getIdsToDeleteByUserIdAndToolPrefix(anyString(), anyInt(), argThat(allowedTools::contains)))
                .thenAnswer((inv) -> {
                            int size = inv.getArgument(1);
                            String user = inv.getArgument(0);
                            String tool = inv.getArgument(2);
                            return createRandomListofIds(size);
                        });

        when(userNotificationRepository.deleteNotificationsInList(anyList())).thenAnswer((invocation) -> {
            latch.countDown();
            List<Long> notificationsToDelete = invocation.getArgument(0);
            return notificationsToDelete.size();
        });



        JobDetail job = JobBuilder.newJob(NotificationCleanupJob.class)
                .withIdentity("testJob", "testGroup")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("testTrigger", "testGroup")
                .startNow()
                .forJob(job)
                .build();

        scheduler.scheduleJob(job, trigger);


        boolean completed = latch.await(20, TimeUnit.SECONDS);
        Assert.assertTrue("Job did not complete in time", completed);

        Assert.assertTrue("Expected at least 1 executed job",
                scheduler.getMetaData().getNumberOfJobsExecuted() >= 1);

        int numberOfExecutions = repoState.usersWithNotifications.size() * allowedTools.size();
        verify(userNotificationRepository, times(numberOfExecutions)).countAllByToUserAndByToolAndNotDeferredOverThreshold(anyString(), anyString(), anyInt());
    }

    @Test
    public void shouldSkipJob_ifGlobalThresholdIsZero() throws JobExecutionException {
        setupServerConfigurationServiceMock(0, null, null, null, null,null,null);
        JobExecutionContext context = mock(JobExecutionContext.class);

        job.execute(context);
        verify(userNotificationRepository, never()).findAllDistinctToUser();
    }

    @Test
    public void shouldSkipJob_ifToolThresholdsAreZero() throws JobExecutionException {
        setupServerConfigurationServiceMock(null, 0, 0, 0, 0,0,0);
        JobExecutionContext context = mock(JobExecutionContext.class);

        job.execute(context);
        verify(userNotificationRepository, never()).findAllDistinctToUser();
    }

    @Test
    public void shouldSkipJob_ifToolThresholdsAreZeroAndGlobalIsSet()  throws JobExecutionException {
        setupServerConfigurationServiceMock(100, 0, 0, 0, 0,0,0);
        JobExecutionContext context = mock(JobExecutionContext.class);

        job.execute(context);
        verify(userNotificationRepository, never()).findAllDistinctToUser();
    }

    @Test
    public void shouldOnlyProcessNotificationforTool_ifNonZeroPositiveThresholdIsSetForAnnouncement()  throws JobExecutionException {
        int userCount = 10;
        setUpRandomRepoState(10, userCount);
        int thresholdAnnouncement = 10;
        setupServerConfigurationServiceMock(null, 0, thresholdAnnouncement, 0, 0,0,0);

        when(userNotificationRepository.findAllDistinctToUser()).thenReturn(repoState.usersWithNotifications);
        when(userNotificationRepository.countAllByToUserAndByToolAndNotDeferredOverThreshold(anyString(), eq("annc"), eq(thresholdAnnouncement)))
                .thenAnswer(inv -> {
                    String user = inv.getArgument(0);
                    String tool = inv.getArgument(1);
                    return repoState.countsByTool.get(user).get(tool);
                });

        when(userNotificationRepository.getIdsToDeleteByUserIdAndToolPrefix(anyString(), anyInt(), eq("annc")))
                .thenAnswer((inv) -> {
                    int size = inv.getArgument(1);
                    String user = inv.getArgument(0);
                    String tool = inv.getArgument(2);
                    return createRandomListofIds(size);
                });

        when(userNotificationRepository.deleteNotificationsInList(anyList())).thenAnswer((invocation) -> {
            List<Long> notificationsToDelete = invocation.getArgument(0);
            return notificationsToDelete.size();
        });



        JobExecutionContext context = mock(JobExecutionContext.class);

        job.execute(context);

        verify(userNotificationRepository, times(1)).findAllDistinctToUser();

        int numberOfExecutions = repoState.usersWithNotifications.size();
        verify(userNotificationRepository, times(numberOfExecutions)).countAllByToUserAndByToolAndNotDeferredOverThreshold(anyString(), anyString(), anyInt());

    }







    private int calcToDelete(int threshold, String user, int size, String tool) throws ArithmeticException {
        Map<String, Long> countsByUsersForTool =  repoState.countsByTool.get(tool);
        long countByUserForTool =  countsByUsersForTool.get(user).longValue();

        /*
         *  attention: countFromDatabase is returned as long and substracting int can result still bigger than MAX_INT
         *  Math.toIntExact does throw ArithmeticException if that is the case
         */
        long toDeleteSize =   countByUserForTool -   threshold;
        return  Math.toIntExact(toDeleteSize);
    }


    private static class RepoState {
        private Map<String, Map<String, Long>> countsByTool = new HashMap<>();
        List<String> usersWithNotifications = new ArrayList<>();

        public void setCountsByTool(String tool, Map<String, Long> counts) {
            countsByTool.put(tool, counts);
        }

    }


    private List<Long> createRandomListofIds(int size) {
             return  java.util.concurrent.ThreadLocalRandom.current().longs(size)
                        .boxed()
                        .collect(Collectors.toList());
    }


    private void setUpRandomRepoState(int threshold, int userCount) {

        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();

        for (int i = 0; i < userCount; i++) {
            String user;
            do {
                user = "user-" + random.nextInt(1000);
            } while (repoState.usersWithNotifications.contains(user));


            Map<String, Long> countsByUserForTool = new HashMap<>();
            countsByUserForTool.put(ASSIGNMENT_TOOL_PREFIX, randomLongOver2TimesTheThreshold(threshold));
            countsByUserForTool.put(SAMIGO_TOOL_PREFIX, randomLongOver2TimesTheThreshold(threshold));
            countsByUserForTool.put(ANNOUNCEMENT_TOOL_PREFIX, randomLongOver2TimesTheThreshold(threshold));
            countsByUserForTool.put(COMMONS_TOOL_PREFIX, randomLongOver2TimesTheThreshold(threshold));
            countsByUserForTool.put(MESSAGE_TOOL_PREFIX, randomLongOver2TimesTheThreshold(threshold));
            countsByUserForTool.put(LESSONSBUILDER_TOOL_PREFIX, randomLongOver2TimesTheThreshold(threshold));

            repoState.setCountsByTool(user, countsByUserForTool);
            repoState.usersWithNotifications.add(user);
        }
    }

    private long randomLongOver2TimesTheThreshold(long max) {
        return java.util.concurrent.ThreadLocalRandom.current().nextLong(max + 1, (max * 2));
    }


    private void setupServerConfigurationServiceMock(Integer global, Integer samigo, Integer announcement, Integer assignment , Integer commons, Integer message, Integer lessonbuilder) {
        if (global == null) global = defaultThreshold;
        when(serverConfigurationService.getInt("notification.cleanup.threshold.per.tool", defaultThreshold)).thenReturn(global.intValue());

        if (samigo == null) samigo = global;
        when(serverConfigurationService.getInt(("notification.cleanup.threshold.per.tool." + SAMIGO_TOOL_PREFIX), global)).thenReturn(samigo.intValue());

        if (announcement == null) announcement = global;
       when(serverConfigurationService.getInt(("notification.cleanup.threshold.per.tool." + ANNOUNCEMENT_TOOL_PREFIX), global)).thenReturn(announcement.intValue());

        if (assignment == null) assignment = global;
        when(serverConfigurationService.getInt(("notification.cleanup.threshold.per.tool." + ASSIGNMENT_TOOL_PREFIX), global)).thenReturn(assignment.intValue());

        if (commons == null)  commons = global;
        when(serverConfigurationService.getInt(("notification.cleanup.threshold.per.tool." + COMMONS_TOOL_PREFIX), global)).thenReturn(commons.intValue());

        if (message == null)  message = global;
        when(serverConfigurationService.getInt(("notification.cleanup.threshold.per.tool." + MESSAGE_TOOL_PREFIX), global)).thenReturn(message.intValue());

        if (lessonbuilder == null)  lessonbuilder = global;
        when(serverConfigurationService.getInt(("notification.cleanup.threshold.per.tool." + LESSONSBUILDER_TOOL_PREFIX), global)).thenReturn(lessonbuilder.intValue());
    }





}