/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.userauditservice.api.model.UserAuditEntry;
import org.sakaiproject.util.api.PasswordFactory;
import lombok.extern.slf4j.Slf4j;

/** Owns participant provisioning and membership updates for one site operation. */
@Slf4j
public class ParticipantRealmUpdater {

    private final AccountValidationService accountValidationService;
    private final AuthzGroupService authzGroupService;
    private final EventTrackingService eventTrackingService;
    private final PasswordFactory passwordFactory;
    private final ServerConfigurationService serverConfigurationService;
    private final SiteService siteService;
    private final UserNotificationProvider notificationProvider;
    private final SessionManager sessionManager;
    private final UserAuditRegistration userAuditRegistration;
    private final UserAuditService userAuditService;
    private final UserDirectoryService userDirectoryService;

    public ParticipantRealmUpdater(AccountValidationService accountValidationService,
            AuthzGroupService authzGroupService, EventTrackingService eventTrackingService,
            PasswordFactory passwordFactory, ServerConfigurationService serverConfigurationService,
            SiteService siteService, UserNotificationProvider notificationProvider, SessionManager sessionManager,
            UserAuditRegistration userAuditRegistration, UserAuditService userAuditService,
            UserDirectoryService userDirectoryService) {
        this.accountValidationService = accountValidationService;
        this.authzGroupService = authzGroupService;
        this.eventTrackingService = eventTrackingService;
        this.passwordFactory = passwordFactory;
        this.serverConfigurationService = serverConfigurationService;
        this.siteService = siteService;
        this.notificationProvider = notificationProvider;
        this.sessionManager = sessionManager;
        this.userAuditRegistration = userAuditRegistration;
        this.userAuditService = userAuditService;
        this.userDirectoryService = userDirectoryService;
    }

    public Result addParticipants(Site site, List<Role> allowedRoles, List<UserRoleEntry> entries,
            ParticipantStatus status, ParticipantNotificationOption notificationOption) {
        List<ParticipantMessage> messages = new ArrayList<>();
        if (entries.isEmpty()) {
            messages.add(new ParticipantMessage("java.guest", null, ParticipantMessage.Severity.ERROR));
            return Result.failure(entries, messages);
        }

        String realmId = site.getReference();
        try {
            AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realmId);
            boolean mayUpdateRealm = authzGroupService.allowUpdate(realmId);
            if (!mayUpdateRealm && !siteService.allowUpdateSiteMembership(site.getId())) {
                messages.add(new ParticipantMessage("java.permeditsite", new Object[] {site.getTitle()},
                        ParticipantMessage.Severity.ERROR));
                return Result.failure(entries, messages);
            }

            Set<String> allowedRoleNames = allowedRoles.stream().map(Role::getId).collect(Collectors.toSet());
            Set<String> reportedRestrictedRoles = new HashSet<>();
            List<UserRoleEntry> acceptedEntries = new ArrayList<>();
            List<UserRoleEntry> rejectedEntries = new ArrayList<>();

            for (UserRoleEntry entry : entries) {
                String roleName = entry.getRole();
                Role role = realmEdit.getRole(roleName);
                if (role == null || !allowedRoleNames.contains(roleName)) {
                    messages.add(new ParticipantMessage("java.roleperm", new Object[] {roleName},
                            ParticipantMessage.Severity.ERROR));
                    rejectedEntries.add(entry);
                } else if (!mayUpdateRealm && role.isAllowed("site.upd")) {
                    if (reportedRestrictedRoles.add(roleName)) {
                        messages.add(new ParticipantMessage("java.roleperm", new Object[] {roleName},
                                ParticipantMessage.Severity.ERROR));
                    }
                    rejectedEntries.add(entry);
                } else {
                    acceptedEntries.add(entry);
                }
            }

            if (acceptedEntries.isEmpty()) {
                return Result.failure(rejectedEntries, messages);
            }

            List<PendingParticipant> pendingParticipants = new ArrayList<>();
            List<CreatedGuestAccount> createdGuestAccounts = new ArrayList<>();
            List<UserAuditEntry> auditEntries = new ArrayList<>();
            List<String> addedUserReferences = new ArrayList<>();
            String auditorId = sessionManager.getCurrentSessionUserId();

            for (UserRoleEntry entry : acceptedEntries) {
                User user = resolveUser(entry, createdGuestAccounts, messages);
                if (user != null) {
                    pendingParticipants.add(new PendingParticipant(entry, user));
                } else {
                    rejectedEntries.add(entry);
                }
            }

            if (pendingParticipants.isEmpty()) {
                return Result.failure(rejectedEntries, messages);
            }

            for (PendingParticipant pending : pendingParticipants) {
                UserRoleEntry entry = pending.entry();
                User user = pending.user();
                realmEdit.addMember(user.getId(), entry.getRole(), status.isActive(), false);
                addedUserReferences.add(userDirectoryService.userReference(user.getId()));
                if (auditorId != null) {
                    auditEntries.add(UserAuditEntry.of(site.getId(), user.getId(), entry.getRole(),
                            UserAuditService.USER_AUDIT_ACTION_ADD, userAuditRegistration.getDatabaseSourceKey(), auditorId));
                }
            }

            try {
                authzGroupService.save(realmEdit);
            } catch (GroupNotDefinedException e) {
                messages.add(new ParticipantMessage("java.realm", null, ParticipantMessage.Severity.ERROR));
                log.warn("cannot find realm for {}", realmId, e);
                return Result.failure(entries, messages);
            } catch (AuthzPermissionException e) {
                messages.add(new ParticipantMessage("java.permeditsite", new Object[] {site.getTitle()},
                        ParticipantMessage.Severity.ERROR));
                log.warn("don't have permission to edit realm {}", realmId, e);
                return Result.failure(entries, messages);
            }

            postCommit(site, realmEdit, auditEntries, addedUserReferences);
            if (notificationOption.sendsNotification()) {
                notifyParticipants(site, pendingParticipants);
            }
            notifyCreatedGuestAccounts(site, createdGuestAccounts);

            List<String> addedEids = pendingParticipants.stream()
                    .map(pending -> pending.entry().getEid())
                    .toList();
            return Result.committed(addedEids, rejectedEntries, messages);
        } catch (GroupNotDefinedException e) {
            messages.add(new ParticipantMessage("java.realm", null, ParticipantMessage.Severity.ERROR));
            log.warn("cannot find realm for {}", realmId, e);
        } catch (Exception e) {
            messages.add(new ParticipantMessage("java.realm", null, ParticipantMessage.Severity.ERROR));
            log.warn("could not update participant realm {}", realmId, e);
        }
        return Result.failure(entries, messages);
    }

    private User resolveUser(UserRoleEntry entry, List<CreatedGuestAccount> createdGuestAccounts,
            List<ParticipantMessage> messages) {
        try {
            return userDirectoryService.getUserByEid(entry.getEid());
        } catch (UserNotDefinedException e) {
            if (!entry.getEid().contains(SiteAddParticipantHandler.EMAIL_CHAR)) {
                messages.add(new ParticipantMessage("java.account", new Object[] {entry.getEid()},
                        ParticipantMessage.Severity.INFO));
                log.debug("cannot find user with eid={}", entry.getEid(), e);
                return null;
            }
            return createGuestAccount(entry, createdGuestAccounts, messages);
        }
    }

    private User createGuestAccount(UserRoleEntry entry, List<CreatedGuestAccount> createdGuestAccounts,
            List<ParticipantMessage> messages) {
        String eid = entry.getEid();
        UserEdit userEdit = null;
        boolean committed = false;
        try {
            userEdit = userDirectoryService.addUser(null, eid);
            userEdit.setEmail(eid);
            userEdit.setType("guest");
            if (StringUtils.isNotBlank(entry.getFirstName())) userEdit.setFirstName(entry.getFirstName());
            if (StringUtils.isNotBlank(entry.getLastName())) userEdit.setLastName(entry.getLastName());
            String password = passwordFactory.generatePassword();
            userEdit.setPassword(password);
            userDirectoryService.commitEdit(userEdit);
            committed = true;
            createdGuestAccounts.add(new CreatedGuestAccount(userEdit, password));
            return userEdit;
        } catch (UserIdInvalidException e) {
            messages.add(new ParticipantMessage("java.isinval", new Object[] {eid}, ParticipantMessage.Severity.INFO));
            log.warn("Guest account id [{}] is invalid", eid, e);
        } catch (UserAlreadyDefinedException e) {
            messages.add(new ParticipantMessage("java.beenused", new Object[] {eid}, ParticipantMessage.Severity.INFO));
            log.warn("Guest account id [{}] has already been used", eid, e);
        } catch (UserPermissionException e) {
            messages.add(new ParticipantMessage("java.haveadd", new Object[] {eid}, ParticipantMessage.Severity.INFO));
            log.warn("User does not have permission to create guest account [{}]", eid, e);
        } finally {
            if (userEdit != null && !committed && userEdit.isActiveEdit()) {
                userDirectoryService.cancelEdit(userEdit);
            }
        }
        return null;
    }

    private void postCommit(Site site, AuthzGroup realmEdit, List<UserAuditEntry> auditEntries,
            List<String> userReferences) {
        try {
            if (!auditEntries.isEmpty()) userAuditService.addToUserAuditing(auditEntries);
        } catch (RuntimeException e) {
            log.warn("Could not record participant audit entries for site {}", site.getId(), e);
        }

        try {
            eventTrackingService.post(eventTrackingService.newEvent(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP,
                    realmEdit.getId(), false));
            if (serverConfigurationService.getBoolean(SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, true)) {
                for (String userReference : userReferences) {
                    eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD,
                            userReference, true));
                }
            }
        } catch (RuntimeException e) {
            log.warn("Could not post participant membership events for site {}", site.getId(), e);
        }
    }

    private void notifyParticipants(Site site, List<PendingParticipant> pendingParticipants) {
        for (PendingParticipant pending : pendingParticipants) {
            try {
                notificationProvider.notifyAddedParticipant(
                        pending.entry().getEid().contains(SiteAddParticipantHandler.EMAIL_CHAR), pending.user(), site);
            } catch (RuntimeException e) {
                log.warn("Could not notify added participant {} for site {}", pending.entry().getEid(), site.getId(), e);
            }
        }
    }

    private void notifyCreatedGuestAccounts(Site site, List<CreatedGuestAccount> createdGuestAccounts) {
        if (!serverConfigurationService.getBoolean("notifyNewUserEmail", true)) return;

        boolean validateUsers = serverConfigurationService.getBoolean("siteManage.validateNewUsers", true);
        for (CreatedGuestAccount account : createdGuestAccounts) {
            try {
                if (validateUsers) {
                    accountValidationService.createValidationAccount(account.user().getId(), true);
                } else {
                    notificationProvider.notifyNewUserEmail(account.user(), account.password(), site);
                }
            } catch (RuntimeException e) {
                log.warn("Could not notify newly created guest {} for site {}", account.user().getEid(), site.getId(), e);
            }
        }
    }

    private record PendingParticipant(UserRoleEntry entry, User user) {
    }

    private record CreatedGuestAccount(User user, String password) {
    }

    public record Result(boolean committed, List<String> addedEids, List<UserRoleEntry> rejectedEntries,
            List<ParticipantMessage> messages) {

        public Result {
            addedEids = List.copyOf(addedEids);
            rejectedEntries = List.copyOf(rejectedEntries);
            messages = List.copyOf(messages);
        }

        private static Result committed(List<String> addedEids, List<UserRoleEntry> rejectedEntries,
                List<ParticipantMessage> messages) {
            return new Result(true, addedEids, rejectedEntries, messages);
        }

        private static Result failure(List<UserRoleEntry> rejectedEntries, List<ParticipantMessage> messages) {
            return new Result(false, List.of(), rejectedEntries, messages);
        }
    }
}
