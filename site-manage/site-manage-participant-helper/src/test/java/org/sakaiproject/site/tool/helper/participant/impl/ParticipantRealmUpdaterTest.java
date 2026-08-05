/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.api.PasswordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** Verifies that participant side effects follow the realm-persistence result. */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ParticipantHelperTestConfiguration.class)
public class ParticipantRealmUpdaterTest {

    private static final String SITE_ID = "site-1";
    private static final String SITE_REFERENCE = "/site/" + SITE_ID;

    @Autowired private ParticipantRealmUpdater updater;
    @Autowired private AccountValidationService accountValidationService;
    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SiteService siteService;
    @Autowired private UserNotificationProvider notificationProvider;
    @Autowired private SessionManager sessionManager;
    @Autowired private UserAuditRegistration userAuditRegistration;
    @Autowired private UserAuditService userAuditService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private PasswordFactory passwordFactory;

    private Site site;
    private AuthzGroup realm;
    private Role role;
    private User user;
    private UserRoleEntry entry;

    @Before
    public void setUp() throws Exception {
        reset(accountValidationService, authzGroupService, eventTrackingService, passwordFactory,
                serverConfigurationService, siteService, notificationProvider, sessionManager, userAuditRegistration,
                userAuditService, userDirectoryService);

        site = mock(Site.class);
        realm = mock(AuthzGroup.class);
        role = mock(Role.class);
        user = mock(User.class);
        entry = new UserRoleEntry("student0011", "access");

        when(site.getId()).thenReturn(SITE_ID);
        when(site.getReference()).thenReturn(SITE_REFERENCE);
        when(site.getTitle()).thenReturn("Test Site");
        when(realm.getId()).thenReturn(SITE_REFERENCE);
        when(realm.getRole("access")).thenReturn(role);
        when(role.getId()).thenReturn("access");
        when(user.getId()).thenReturn("user-1");
        when(user.getEid()).thenReturn("student0011");
        when(authzGroupService.getAuthzGroup(SITE_REFERENCE)).thenReturn(realm);
        when(userDirectoryService.getUserByEid("student0011")).thenReturn(user);
        when(userDirectoryService.userReference("user-1")).thenReturn("/user/user-1");
    }

    @Test
    public void reportsPermissionFailureWithoutSavingOrNotifying() throws Exception {
        when(authzGroupService.allowUpdate(SITE_REFERENCE)).thenReturn(false);
        when(siteService.allowUpdateSiteMembership(SITE_ID)).thenReturn(false);
        entry = new UserRoleEntry("Guest", "User", "access", "guest@example.org");

        ParticipantRealmUpdater.Result result = addParticipant(ParticipantNotificationOption.SEND);

        assertFalse(result.committed());
        assertEquals(List.of(), result.addedEids());
        assertEquals(List.of(entry), result.rejectedEntries());
        assertTrue(messageCodes(result).contains("java.permeditsite"));
        verify(userDirectoryService, never()).addUser(any(), any());
        verify(authzGroupService, never()).save(any());
        verify(notificationProvider, never()).notifyAddedParticipant(anyBoolean(), any(), any());
    }

    @Test
    public void reportsSaveFailureWithoutClaimingSuccessOrNotifying() throws Exception {
        when(authzGroupService.allowUpdate(SITE_REFERENCE)).thenReturn(true);
        doThrow(new AuthzPermissionException("user-1", "site.upd", SITE_REFERENCE))
                .when(authzGroupService).save(realm);

        ParticipantRealmUpdater.Result result = addParticipant(ParticipantNotificationOption.SEND);

        assertFalse(result.committed());
        assertEquals(List.of(), result.addedEids());
        assertEquals(List.of(entry), result.rejectedEntries());
        assertTrue(messageCodes(result).contains("java.permeditsite"));
        verify(notificationProvider, never()).notifyAddedParticipant(anyBoolean(), any(), any());
    }

    @Test
    public void rejectsEveryParticipantAssignedARestrictedRole() throws Exception {
        when(authzGroupService.allowUpdate(SITE_REFERENCE)).thenReturn(false);
        when(siteService.allowUpdateSiteMembership(SITE_ID)).thenReturn(true);
        when(role.isAllowed("site.upd")).thenReturn(true);
        UserRoleEntry secondEntry = new UserRoleEntry("student0012", "access");

        ParticipantRealmUpdater.Result result = updater.addParticipants(site, List.of(role),
                List.of(entry, secondEntry), ParticipantStatus.ACTIVE, ParticipantNotificationOption.DO_NOT_SEND);

        assertFalse(result.committed());
        assertEquals(List.of(entry, secondEntry), result.rejectedEntries());
        assertEquals(List.of("java.roleperm"), messageCodes(result));
        verify(userDirectoryService, never()).getUserByEid(any());
        verify(authzGroupService, never()).save(any());
    }

    @Test
    public void notifiesOnlyAfterRealmPersistenceSucceeds() throws Exception {
        when(authzGroupService.allowUpdate(SITE_REFERENCE)).thenReturn(true);

        ParticipantRealmUpdater.Result result = addParticipant(ParticipantNotificationOption.SEND);

        assertTrue(result.committed());
        assertEquals(List.of("student0011"), result.addedEids());
        assertTrue(result.rejectedEntries().isEmpty());
        assertTrue(result.messages().isEmpty());
        InOrder orderedSideEffects = inOrder(authzGroupService, notificationProvider);
        orderedSideEffects.verify(authzGroupService).save(realm);
        orderedSideEffects.verify(notificationProvider).notifyAddedParticipant(false, user, site);
    }

    @Test
    public void reportsCommittedAndRejectedEntriesSeparately() throws Exception {
        when(authzGroupService.allowUpdate(SITE_REFERENCE)).thenReturn(true);
        UserRoleEntry missingEntry = new UserRoleEntry("student0012", "access");
        when(userDirectoryService.getUserByEid("student0012"))
                .thenThrow(new UserNotDefinedException("student0012"));

        ParticipantRealmUpdater.Result result = updater.addParticipants(site, List.of(role),
                List.of(entry, missingEntry), ParticipantStatus.ACTIVE, ParticipantNotificationOption.DO_NOT_SEND);

        assertTrue(result.committed());
        assertEquals(List.of("student0011"), result.addedEids());
        assertEquals(List.of(missingEntry), result.rejectedEntries());
        assertTrue(messageCodes(result).contains("java.account"));
    }

    @Test
    public void createsGuestAfterAuthorizationAndNotifiesAfterRealmSave() throws Exception {
        String guestEid = "guest@example.org";
        UserEdit guest = mock(UserEdit.class);
        entry = new UserRoleEntry("Guest", "User", "access", guestEid);
        when(authzGroupService.allowUpdate(SITE_REFERENCE)).thenReturn(true);
        when(userDirectoryService.getUserByEid(guestEid))
                .thenThrow(new UserNotDefinedException(guestEid));
        when(userDirectoryService.addUser(null, guestEid)).thenReturn(guest);
        when(guest.getId()).thenReturn("guest-1");
        when(guest.getEid()).thenReturn(guestEid);
        when(passwordFactory.generatePassword()).thenReturn("generated-password");
        when(userDirectoryService.userReference("guest-1")).thenReturn("/user/guest-1");
        when(serverConfigurationService.getBoolean("notifyNewUserEmail", true)).thenReturn(true);
        when(serverConfigurationService.getBoolean("siteManage.validateNewUsers", true)).thenReturn(false);

        ParticipantRealmUpdater.Result result = addParticipant(ParticipantNotificationOption.DO_NOT_SEND);

        assertTrue(result.committed());
        assertEquals(List.of(guestEid), result.addedEids());
        InOrder orderedSideEffects = inOrder(authzGroupService, userDirectoryService, notificationProvider);
        orderedSideEffects.verify(authzGroupService).allowUpdate(SITE_REFERENCE);
        orderedSideEffects.verify(userDirectoryService).addUser(null, guestEid);
        orderedSideEffects.verify(userDirectoryService).commitEdit(guest);
        orderedSideEffects.verify(authzGroupService).save(realm);
        orderedSideEffects.verify(notificationProvider).notifyNewUserEmail(
                guest, "generated-password", site);
    }

    private ParticipantRealmUpdater.Result addParticipant(ParticipantNotificationOption notificationOption) {
        return updater.addParticipants(site, List.of(role), List.of(entry), ParticipantStatus.ACTIVE,
                notificationOption);
    }

    private List<String> messageCodes(ParticipantRealmUpdater.Result result) {
        return result.messages().stream().map(ParticipantMessage::getCode).collect(Collectors.toList());
    }
}
