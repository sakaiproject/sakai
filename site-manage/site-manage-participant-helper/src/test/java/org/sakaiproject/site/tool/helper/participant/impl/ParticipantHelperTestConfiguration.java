/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import org.mockito.Mockito;
import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.api.PasswordFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/** Test wiring for participant-helper behavior at kernel-service boundaries. */
@Configuration
@ComponentScan(basePackageClasses = {ParticipantAccountParser.class, ParticipantDisplayResolver.class,
        ParticipantRealmUpdater.class}, useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {ParticipantAccountParser.class, ParticipantDisplayResolver.class,
                        ParticipantRealmUpdater.class}))
public class ParticipantHelperTestConfiguration {

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        return Mockito.mock(ServerConfigurationService.class);
    }

    @Bean(name = "org.sakaiproject.user.api.UserDirectoryService")
    public UserDirectoryService userDirectoryService() {
        return Mockito.mock(UserDirectoryService.class);
    }

    @Bean
    public AuthzGroupService authzGroupService() {
        return Mockito.mock(AuthzGroupService.class);
    }

    @Bean
    public EventTrackingService eventTrackingService() {
        return Mockito.mock(EventTrackingService.class);
    }

    @Bean
    public SiteService siteService() {
        return Mockito.mock(SiteService.class);
    }

    @Bean
    public UserNotificationProvider userNotificationProvider() {
        return Mockito.mock(UserNotificationProvider.class);
    }

    @Bean
    public SessionManager sessionManager() {
        return Mockito.mock(SessionManager.class);
    }

    @Bean(name = "org.sakaiproject.userauditservice.api.UserAuditRegistration.sitemanage")
    public UserAuditRegistration userAuditRegistration() {
        return Mockito.mock(UserAuditRegistration.class);
    }

    @Bean
    public UserAuditService userAuditService() {
        return Mockito.mock(UserAuditService.class);
    }

    @Bean
    public AccountValidationService accountValidationService() {
        return Mockito.mock(AccountValidationService.class);
    }

    @Bean
    public PasswordFactory passwordFactory() {
        return Mockito.mock(PasswordFactory.class);
    }
}
