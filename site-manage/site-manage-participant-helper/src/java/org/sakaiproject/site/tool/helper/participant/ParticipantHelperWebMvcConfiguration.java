/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant;

import java.nio.charset.StandardCharsets;

import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.participant.impl.SiteAddParticipantHandler;
import org.sakaiproject.site.tool.helper.participant.impl.ParticipantRealmUpdater;
import org.sakaiproject.site.tool.helper.participant.impl.ParticipantAccountParser;
import org.sakaiproject.site.util.SecFetchSiteCsrfInterceptor;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.ResourceLoaderMessageSource;
import org.sakaiproject.util.api.LocaleService;
import org.sakaiproject.util.api.PasswordFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.ISpringTemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
@EnableWebMvc
@ComponentScan("org.sakaiproject.site.tool.helper.participant")
public class ParticipantHelperWebMvcConfiguration implements WebMvcConfigurer, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceLoaderMessageSource messages = new ResourceLoaderMessageSource();
        messages.setBasename("classpath:org/sakaiproject/site/tool/participant/bundle/sitesetupgeneric");
        return messages;
    }

    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return viewResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/content/**").addResourceLocations("/content/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SecFetchSiteCsrfInterceptor());
    }

    @Bean
    public LocaleResolver localeResolver(LocaleService localeService) {
        return new ParticipantLocaleResolver(localeService);
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public SiteAddParticipantHandler siteAddParticipantHandler(AccountValidationService accountValidationService,
            AuthzGroupService authzGroupService, EventTrackingService eventTrackingService,
            CourseManagementService courseManagementService,
            PasswordFactory passwordFactory, ServerConfigurationService serverConfigurationService,
            SessionManager sessionManager, SiteService siteService, ToolManager toolManager,
            @Qualifier("org.sakaiproject.userauditservice.api.UserAuditRegistration.sitemanage") UserAuditRegistration userAuditRegistration,
            UserAuditService userAuditService, UserDirectoryService userDirectoryService,
            UserNotificationProvider userNotificationProvider, ParticipantRealmUpdater participantRealmUpdater,
            ParticipantAccountParser participantAccountParser) {
        SiteAddParticipantHandler handler = new SiteAddParticipantHandler();
        handler.setAccountValidationService(accountValidationService);
        handler.setAuthzGroupService(authzGroupService);
        handler.setCourseManagementService(courseManagementService);
        handler.setEventTrackingService(eventTrackingService);
        handler.setPasswordFactory(passwordFactory);
        handler.setParticipantAccountParser(participantAccountParser);
        handler.setParticipantRealmUpdater(participantRealmUpdater);
        handler.setServerConfigurationService(serverConfigurationService);
        handler.setSessionManager(sessionManager);
        handler.setSiteService(siteService);
        handler.setToolManager(toolManager);
        handler.setUserAuditRegistration(userAuditRegistration);
        handler.setUserAuditService(userAuditService);
        handler.setUserDirectoryService(userDirectoryService);
        handler.setNotiProvider(userNotificationProvider);
        return handler;
    }

    @Bean
    public ParticipantRealmUpdater participantRealmUpdater(AuthzGroupService authzGroupService,
            EventTrackingService eventTrackingService, ServerConfigurationService serverConfigurationService,
            SiteService siteService, UserNotificationProvider userNotificationProvider, SessionManager sessionManager,
            @Qualifier("org.sakaiproject.userauditservice.api.UserAuditRegistration.sitemanage") UserAuditRegistration userAuditRegistration,
            UserAuditService userAuditService, UserDirectoryService userDirectoryService) {
        return new ParticipantRealmUpdater(authzGroupService, eventTrackingService, serverConfigurationService, siteService,
                userNotificationProvider, sessionManager, userAuditRegistration, userAuditService, userDirectoryService);
    }

    private ISpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setEnableSpringELCompiler(true);
        templateEngine.setMessageSource(messageSource());
        templateEngine.setTemplateResolver(templateResolver());
        return templateEngine;
    }

    private ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        return templateResolver;
    }
}
