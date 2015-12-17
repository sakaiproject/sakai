Upgrading Spring to 4.1.x Guide for Contrib tools

In order to make it easy to upgrade contrib tools here is a list of things that you will need to address in the contrib tool.
You can use this this PR https://github.com/sakaiproject/evaluation/pull/25/files as a reference.

- Web.xml
  You must update the sakai spring ContextLoaderListener as this has changed to
  <listener-class>org.sakaiproject.util.SakaiContextLoaderListener</listener-class>

- Update spring definitions to use the XSD over traditional DTD
  (this is not required but the IDE will not beable to validate xml files and there have been elements that have changed,
   so its a good idea)
  <beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    
- Update spring definitions with some of the following changes:
  - BeanReferenceFactoryBean has been removed and you should switch to using "alias"
    (in rsf a copy of BeanReferenceFactoryBean has been included uk.org.ponder.springutil.BeanReferenceFactoryBean
     as alias don't work for rsf)
  - remove "singleton=true" as this is the default, you could also use "scope"
  - remove "local" and use "bean"

- a number of classes in org.springframework.web.servlet.mvc were removed in Spring 4.0. The best known is SimpleFormController, but several others were also involved. I was able to simply pull them from the Spring 3 source and use them. For an example (which actually has most of those that you're likely to need) see metaobj/metaobj-util/tool-lib/src/java/org/springframework/web/servlet/mvc in clhedrick/sakai-contrib
  
- generic-dao has had its groupId changed as we forked the original code hosted on github 
  and didn't want any confusion with the original code.
  <groupId>org.sakaiproject.genericdao</groupId>
  
- rsf also changed recently (unrelated to the spring upgrade)
  please see https://github.com/sakaiproject/evaluation/pull/23/files for reference
  
- HibernateTemplate changes 
  - removal saveOrUpdateAll, will need to loop and call saveOrUpdate on the persistence object
  - HibernateTemplate.find needs an explicit cast to List<?>

- Quartz upgraded from 1.8 to 2.2
  Quartz changes here that require some refactoring for any tool that uses quartz 
  see the following http://quartz-scheduler.org/documentation/quartz-2.x/migration-guide
  
- Unit Tests
  - Tests should be updated to jUnit4 style of tests as spring has removed AbstractSpringContextTests, AbstractTransactionalSpringContextTests 
    in favor of junit style AbstractJUnit4SpringContextTests, AbstractTransactionalJUnit4SpringContextTests
  - Use @ContextConfiguration to load test bean definitions
  - Use junit @Before and @After
  - Use springs @BeforeTransaction when necessary
  - Remove TestCase and use @Test annotation
  - use junit4 Assert
  - One last important thing to note is that Spring caches the test application contexts to speed up tests 
    so if you have a test that changes the context (typically integration tests using a datasource) and therefore
    fails you will probably need to use the @DirtiesContext annotation to tell spring not to cache this application context.
  
  
