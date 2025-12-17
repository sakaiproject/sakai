package org.sakaiproject.entitybroker.config;

import org.apache.ignite.IgniteSpringBean;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.rest.EntityActionsManager;
import org.sakaiproject.entitybroker.rest.EntityBatchHandler;
import org.sakaiproject.entitybroker.rest.EntityDescriptionManager;
import org.sakaiproject.entitybroker.rest.EntityEncodingManager;
import org.sakaiproject.entitybroker.rest.EntityHandlerImpl;
import org.sakaiproject.entitybroker.rest.EntityRESTProviderBase;
import org.sakaiproject.entitybroker.rest.EntityRedirectsManager;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.profile2.api.ProfileService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistration;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistrationService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.mockito.Mockito.mock;

@Configuration
@EnableTransactionManagement
@ImportResource({"classpath:/WEB-INF/components.xml", "classpath:/WEB-INF/applicationContext.xml"})
@PropertySource("classpath:/hibernate.properties")
public class EntityRestTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings.entitybroker")
    private AdditionalHibernateMappings additionalHibernateMappings;

    @Autowired
    private EntityBrokerManager entityBrokerManager;

    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        return additionalHibernateMappings;
    }

    @Bean(name = "org.sakaiproject.email.api.EmailService")
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean(name = "org.sakaiproject.event.api.LearningResourceStoreService")
    public LearningResourceStoreService learningResourceStoreService() {
        return mock(LearningResourceStoreService.class);
    }

    @Bean(name = "org.sakaiproject.thread_local.api.ThreadLocalManager")
    public ThreadLocalManager threadLocalManager() {
        return mock(ThreadLocalManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public UsageSessionService usageSessionService() {
        return mock(UsageSessionService.class);
    }

    @Bean
    public EntityActionsManager entityActionsManager() {
        return new EntityActionsManager(entityBrokerManager.getEntityProviderMethodStore());
    }

    @Bean
    public EntityEncodingManager entityEncodingManager() {
        return new EntityEncodingManager(entityBrokerManager.getEntityProviderManager(), entityBrokerManager);
    }

    @Bean
    public EntityDescriptionManager entityDescriptionManager() {
        return new EntityDescriptionManager(
                entityBrokerManager.getEntityViewAccessProviderManager(),
                entityBrokerManager.getEntityProviderManager(),
                entityBrokerManager.getEntityPropertiesService(),
                entityBrokerManager,
                entityBrokerManager.getEntityProviderMethodStore());
    }

    @Bean
    public EntityRedirectsManager entityRedirectsManager() {
        return new EntityRedirectsManager(entityBrokerManager,
                entityBrokerManager.getEntityProviderMethodStore(),
                entityBrokerManager.getRequestStorage());
    }

    @Bean
    public EntityBatchHandler entityBatchHandler(EntityEncodingManager entityEncodingManager) {
        return new EntityBatchHandler(entityBrokerManager, entityEncodingManager,
                entityBrokerManager.getExternalIntegrationProvider());
    }

    @Bean
    public EntityHandlerImpl entityRequestHandler(
            EntityActionsManager entityActionsManager,
            EntityRedirectsManager entityRedirectsManager,
            EntityEncodingManager entityEncodingManager,
            EntityDescriptionManager entityDescriptionManager,
            EntityBatchHandler entityBatchHandler) {
        EntityHandlerImpl handler = new EntityHandlerImpl(
                entityBrokerManager.getEntityProviderManager(),
                entityBrokerManager,
                entityEncodingManager,
                entityDescriptionManager,
                entityBrokerManager.getEntityViewAccessProviderManager(),
                entityBrokerManager.getRequestGetter(),
                entityActionsManager,
                entityRedirectsManager,
                entityBatchHandler,
                entityBrokerManager.getRequestStorage());
        return handler;
    }

    @Bean(destroyMethod = "destroy")
    public EntityRESTProviderBase entityRESTProvider(
            EntityActionsManager entityActionsManager,
            EntityEncodingManager entityEncodingManager,
            EntityHandlerImpl entityRequestHandler) {
        return new EntityRESTProviderBase(entityBrokerManager, entityActionsManager,
                entityEncodingManager, entityRequestHandler);
    }

    @Bean(name = "org.sakaiproject.user.api.UserNotificationPreferencesRegistrationService")
    public UserNotificationPreferencesRegistrationService userNotificationPreferencesRegistrationService() {
        return mock(UserNotificationPreferencesRegistrationService.class);
    }

    @Bean(name = "org.sakaiproject.userauditservice.api.UserAuditRegistration.direct")
    public UserAuditRegistration userAuditRegistration() {
        return mock(UserAuditRegistration.class);
    }

    @Bean(name = "org.sakaiproject.coursemanagement.api.CourseManagementService")
    public CourseManagementService courseManagementService() {
        return mock(CourseManagementService.class);
    }

    @Bean(name = "org.sakaiproject.coursemanagement.api.CourseManagementAdministration")
    public CourseManagementAdministration courseManagementAdministration() {
        return mock(CourseManagementAdministration.class);
    }

    @Bean(name = "org.sakaiproject.profile2.api.ProfileService")
    public ProfileService profileService() {
        return mock(ProfileService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.FormattedText")
    public FormattedText formattedText() {
        return mock(FormattedText.class);
    }

    @Bean(name = "org.sakaiproject.id.api.IdManager")
    public IdManager idManager() {
        return mock(IdManager.class);
    }

    @Bean(name = "org.sakaiproject.user.api.AuthenticationManager")
    public AuthenticationManager authenticationManager() {
        return mock(AuthenticationManager.class);
    }

    @Bean(name = "org.sakaiproject.cluster.api.ClusterService")
    public ClusterService clusterService() {
        return mock(ClusterService.class);
    }

    @Bean(name = "org.sakaiproject.api.privacy.PrivacyManager")
    public PrivacyManager privacyManager() {
        return mock(PrivacyManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.NotificationService")
    public NotificationService notificationService() {
        return mock(NotificationService.class);
    }

    @Bean(name = "org.sakaiproject.ignite.SakaiIgnite")
    public IgniteSpringBean sakaiIgnite() {
        return mock(IgniteSpringBean.class);
    }

}
