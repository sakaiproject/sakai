Autowiring Quartz Jobs
======================

To implement a quartz job now all you need todo is create a class that
implements the org.quartz.Job interface and register this class with the
scheuduler manager. The class doesn't need to be a spring bean as it
can be autowired with any services it needs. The data from the job map
is also available to the autowiring.

See: org.springframework.scheduling.quartz.SpringBeanJobFactory
See: org.sakaiproject.component.app.scheduler.jobs.AutowiredTestJob

@Inject doesn't work unless the annotation is available when the main
spring application context is setup. However @Autowired is always available
so can be used.


