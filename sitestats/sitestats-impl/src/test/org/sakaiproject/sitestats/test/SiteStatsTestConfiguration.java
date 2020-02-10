package org.sakaiproject.sitestats.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.HSQLDialect;
import org.hsqldb.jdbcDriver;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.sitestats.test.mocks.FakeEntityManager;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class SiteStatsTestConfiguration {

    @Autowired
    private Environment environment;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings.sitestats")
    private AdditionalHibernateMappings hibernateMappings;

    private DataSource dataSource;
    private PlatformTransactionManager platformTransactionManager;
    private SessionFactory sessionFactory;

    static {
        System.setProperty("sakai.tests.enabled", "true");
    }

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public SessionFactory sessionFactory() throws IOException {
        if (sessionFactory == null) {
            LocalSessionFactoryBuilder sfb = new LocalSessionFactoryBuilder(dataSource());
            hibernateMappings.processAdditionalMappings(sfb);
            sfb.addProperties(hibernateProperties());
            sessionFactory = sfb.buildSessionFactory();
        }
        return sessionFactory;
    }

    @Bean(name = "javax.sql.DataSource")
    public DataSource dataSource() {
        if (dataSource == null) {
            DriverManagerDataSource db = new DriverManagerDataSource();
            db.setDriverClassName(environment.getProperty(org.hibernate.cfg.Environment.DRIVER, jdbcDriver.class.getName()));
            db.setUrl(environment.getProperty(org.hibernate.cfg.Environment.URL, "jdbc:hsqldb:mem:test"));
            db.setUsername(environment.getProperty(org.hibernate.cfg.Environment.USER, "sa"));
            db.setPassword(environment.getProperty(org.hibernate.cfg.Environment.PASS, ""));
            dataSource = db;
        }
        return dataSource;
    }

    @Bean
    public Properties hibernateProperties() {
        return new Properties() {
            {
                setProperty(org.hibernate.cfg.Environment.DIALECT, environment.getProperty(org.hibernate.cfg.Environment.DIALECT, HSQLDialect.class.getName()));
                setProperty(org.hibernate.cfg.Environment.HBM2DDL_AUTO, environment.getProperty(org.hibernate.cfg.Environment.HBM2DDL_AUTO));
                setProperty(org.hibernate.cfg.Environment.ENABLE_LAZY_LOAD_NO_TRANS, environment.getProperty(org.hibernate.cfg.Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true"));
            }
        };
    }

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    public PlatformTransactionManager transactionManager() throws IOException {
        if (platformTransactionManager == null) {
            if (sessionFactory == null) {
                sessionFactory();
            }
            HibernateTransactionManager txManager = new HibernateTransactionManager();
            txManager.setSessionFactory(sessionFactory);
            platformTransactionManager = txManager;
        }
        return platformTransactionManager;
    }

    @Bean(name = "org.sakaiproject.alias.api.AliasService")
    public AliasService aliasService() {
        return mock(AliasService.class);
    }

    @Bean(name = "org.sakaiproject.announcement.api.AnnouncementService")
    public AnnouncementService announcementService() {
        return mock(AnnouncementService.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.AuthzGroupService")
    public AuthzGroupService authzGroupService() {
        return mock(AuthzGroupService.class);
    }

    @Bean(name = "org.sakaiproject.calendar.api.CalendarService")
    public CalendarService calendarService() {
        return mock(CalendarService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {
        return mock(ContentHostingService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentTypeImageService")
    public ContentTypeImageService contentTypeImageService() {
        ContentTypeImageService contentTypeImageService = mock(ContentTypeImageService.class);
        when(contentTypeImageService.getContentTypeImage("folder")).thenReturn("sakai/folder.gif");
        when(contentTypeImageService.getContentTypeImage("image/png")).thenReturn("sakai/image.gif");
        return contentTypeImageService;
    }

    @Bean(name = "org.sakaiproject.sitestats.test.DB")
    public DB db() throws IOException {
        DB db = new DB();
        if (sessionFactory == null) {
            sessionFactory();
        }
        db.setSessionFactory(sessionFactory);
        return db;
    }

    @Bean(name = "org.sakaiproject.entitybroker.DeveloperHelperService")
    public DeveloperHelperService developerHelperService() {
        return mock(DeveloperHelperService.class);
    }

    @Bean(name = "org.sakaiproject.email.api.DigestService")
    public DigestService digestService() {
        return mock(DigestService.class);
    }

    @Bean(name = "org.sakaiproject.entity.api.EntityManager")
    public EntityManager entityManager() {
        return spy(FakeEntityManager.class);
    }

    @Bean(name = "org.sakaiproject.entitybroker.entityprovider.EntityProviderManager")
    public EntityProviderManager entityProviderManager() {
        return mock(EntityProviderManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.EventTrackingService")
    public EventTrackingService eventTrackingService() {
        return mock(EventTrackingService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.FormattedText")
    public FormattedText formattedText() {
        return mock(FormattedText.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.FunctionManager")
    public FunctionManager functionManager() {
        return mock(FunctionManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.LearningResourceStoreService")
    public LearningResourceStoreService learningResourceStoreService() {
        return mock(LearningResourceStoreService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.LinkMigrationHelper")
    public LinkMigrationHelper linkMigrationHelper() {
        return mock(LinkMigrationHelper.class);
    }

    @Bean(name = "org.sakaiproject.memory.api.MemoryService")
    public MemoryService memoryService() {
        MemoryService memoryService = new org.sakaiproject.memory.mock.MemoryService();
        return memoryService;
    }

    @Bean(name = "org.sakaiproject.user.api.PreferencesService")
    public PreferencesService preferencesService() {
        return mock(PreferencesService.class);
    }

    @Bean(name = "org.sakaiproject.util.ResourceLoader.sitestats")
    public ResourceLoader resourceLoader() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getString("report_content_attachments")).thenReturn("Attachments");
        when(resourceLoader.getString("th_site")).thenReturn("Site");
        when(resourceLoader.getString("th_id")).thenReturn("User ID");
        when(resourceLoader.getString("th_user")).thenReturn("name");
        when(resourceLoader.getString("th_total")).thenReturn("Total");
        return resourceLoader;
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.ScheduledInvocationManager")
    public ScheduledInvocationManager scheduledInvocationManager() {
        return mock(ScheduledInvocationManager.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.SchedulerManager")
    public SchedulerManager schedulerManager() {
        return mock(SchedulerManager.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.SecurityService")
    public SecurityService securityService() {
        return mock(SecurityService.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService scs = mock(ServerConfigurationService.class);
        when(scs.getString("sitestats.db", "internal")).thenReturn("internal");
        when(scs.getString("hibernate.dialect", "org.hibernate.dialect.HSQLDialect")).thenReturn("org.hibernate.dialect.HSQLDialect");
        when(scs.getBoolean("auto.ddl", true)).thenReturn(true);
        when(scs.getServerUrl()).thenReturn("http://localhost:8080");
        return scs;
    }

    @Bean(name = "org.sakaiproject.tool.api.SessionManager")
    public SessionManager sessionManager() {
        return mock(SessionManager.class);
    }

    @Bean(name = "org.sakaiproject.lessonbuildertool.model.SimplePageToolDao")
    public SimplePageToolDao simplePageToolDao() {
        return mock(SimplePageToolDao.class);
    }

    @Bean(name = "org.sakaiproject.db.api.SqlService")
    public SqlService sqlService() {
        return mock(SqlService.class);
    }

    @Bean(name = "org.sakaiproject.site.api.SiteService")
    public SiteService siteService() throws IdUnusedException {
        SiteService siteService = mock(SiteService.class);
        when(siteService.getSite(null)).thenThrow(new IdUnusedException("null"));
        when(siteService.getSite("non_existent_site")).thenThrow(new IdUnusedException("non_existent_site"));
        when(siteService.isUserSite("non_existent_site")).thenReturn(false);
        when(siteService.isSpecialSite("non_existent_site")).thenReturn(false);

        return siteService;
    }

    @Bean(name = "org.sakaiproject.time.api.TimeService")
    public TimeService timeService() {
        return mock(TimeService.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.ToolManager")
    public ToolManager toolManager() {
        ToolManager toolManager = mock(ToolManager.class);
        Tool chatTool = mock(Tool.class);
        when(chatTool.getId()).thenReturn(FakeData.TOOL_CHAT);
        Tool resourcesTool = mock(Tool.class);
        when(resourcesTool.getId()).thenReturn(StatsManager.RESOURCES_TOOLID);
        Set<Tool> tools = new HashSet<>(Arrays.asList(chatTool, resourcesTool));
        when(toolManager.findTools(null, null)).thenReturn(tools);
        return toolManager;
    }

    @Bean(name = "org.sakaiproject.user.api.UserDirectoryService")
    public UserDirectoryService userDirectoryService() {
        return mock(UserDirectoryService.class);
    }

    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public UsageSessionService usageSessionService() {
        return mock(UsageSessionService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return mock(UserTimeService.class);
    }

}
