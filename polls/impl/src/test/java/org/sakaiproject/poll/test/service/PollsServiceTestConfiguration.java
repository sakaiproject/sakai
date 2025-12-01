package org.sakaiproject.poll.test.service;

import org.mockito.Mockito;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class PollsServiceTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings.poll")
    private AdditionalHibernateMappings additionalHibernateMappings;

    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        return additionalHibernateMappings;
    }

    @Bean(name = "org.sakaiproject.util.api.FormattedText")
    public FormattedText formattedText() {
        return Mockito.mock(FormattedText.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.SchedulerManager")
    public SchedulerManager schedulerManager() {
        return Mockito.mock(SchedulerManager.class);
    }

    @Bean(name = "org.sakaiproject.email.api.EmailService")
    public EmailService emailService() {
        return Mockito.mock(EmailService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return Mockito.mock(UserTimeService.class);
    }

    @Bean(name = "org.sakaiproject.entitybroker.DeveloperHelperService")
    public DeveloperHelperService developerHelperService() {
        return Mockito.mock(DeveloperHelperService.class);
    }

    @Bean(name = "org.sakaiproject.event.api.LearningResourceStoreService")
    public LearningResourceStoreService learningResourceStoreService() {
        return Mockito.mock(LearningResourceStoreService.class);
    }

    @Bean(name = "org.sakaiproject.emailtemplateservice.api.EmailTemplateService")
    public EmailTemplateService emailTemplateService() {
        return Mockito.mock(EmailTemplateService.class);
    }

    @Bean(name = "org.sakaiproject.lti.api.LTIService")
    public LTIService ltiService() {
        return Mockito.mock(LTIService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.LinkMigrationHelper")
    public LinkMigrationHelper linkMigrationHelper() {
        return Mockito.mock(LinkMigrationHelper.class);
    }

    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public UsageSessionService usageSessionService() {
        return Mockito.mock(UsageSessionService.class);
    }

    //         <property name="eventTrackingService" ref="org.sakaiproject.event.api.EventTrackingService"/>
    //         <property name="functionManager" ref="org.sakaiproject.authz.api.FunctionManager"/>
    //         <property name="learningResourceStoreService" ref="org.sakaiproject.event.api.LearningResourceStoreService"/>
    //         <property name="linkMigrationHelper" ref="org.sakaiproject.util.api.LinkMigrationHelper"/>
    //         <property name="ltiService" ref="org.sakaiproject.lti.api.LTIService"/>
    //         <property name="pollRepository" ref="org.sakaiproject.poll.api.repository.PollRepository"/>
    //         <property name="securityService" ref="org.sakaiproject.authz.api.SecurityService"/>
    //         <property name="serverConfigurationService" ref="org.sakaiproject.component.api.ServerConfigurationService"/>
    //         <property name="sessionManager" ref="org.sakaiproject.tool.api.SessionManager"/>
    //         <property name="siteService" ref="org.sakaiproject.site.api.SiteService"/>
    //         <property name="toolManager" ref="org.sakaiproject.tool.api.ToolManager"/>
    //         <property name="usageSessionService" ref="org.sakaiproject.event.api.UsageSessionService"/>
    //         <property name="userDirectoryService" ref="org.sakaiproject.user.api.UserDirectoryService"/>
    //         <property name="voteRepository" ref="org.sakaiproject.poll.api.repository.VoteRepository"/>
}
