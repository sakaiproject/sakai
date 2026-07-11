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

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.userauditservice.api.model.UserAuditEntry;
import lombok.extern.slf4j.Slf4j;

/** Applies validated participant roles to a site's authorization realm. */
@Slf4j
public class ParticipantRealmUpdater {

    private final AuthzGroupService authzGroupService;
    private final EventTrackingService eventTrackingService;
    private final ServerConfigurationService serverConfigurationService;
    private final SiteService siteService;
    private final UserNotificationProvider notificationProvider;
    private final SessionManager sessionManager;
    private final UserAuditRegistration userAuditRegistration;
    private final UserAuditService userAuditService;
    private final UserDirectoryService userDirectoryService;

    public ParticipantRealmUpdater(AuthzGroupService authzGroupService, EventTrackingService eventTrackingService,
            ServerConfigurationService serverConfigurationService, SiteService siteService,
            UserNotificationProvider notificationProvider, SessionManager sessionManager,
            UserAuditRegistration userAuditRegistration, UserAuditService userAuditService,
            UserDirectoryService userDirectoryService) {
        this.authzGroupService = authzGroupService;
        this.eventTrackingService = eventTrackingService;
        this.serverConfigurationService = serverConfigurationService;
        this.siteService = siteService;
        this.notificationProvider = notificationProvider;
        this.sessionManager = sessionManager;
        this.userAuditRegistration = userAuditRegistration;
        this.userAuditService = userAuditService;
        this.userDirectoryService = userDirectoryService;
    }

    public List<String> addParticipants(Site site, AuthzGroup realm, List<UserRoleEntry> entries, ParticipantStatus status,
            ParticipantNotificationOption notificationOption, List<ParticipantMessage> messages) {
        List<String> addedEids = new ArrayList<>();
        if (entries.isEmpty()) return addedEids;

        String realmId = site.getReference();
        try {
            AuthzGroup realmEdit = authzGroupService.getAuthzGroup(realmId);
            boolean mayUpdateRealm = authzGroupService.allowUpdate(realmId);
            Set<String> checkedRoles = new HashSet<>();
            List<UserAuditEntry> auditEntries = new ArrayList<>();
            List<String> addedUserReferences = new ArrayList<>();
            String auditorId = sessionManager.getCurrentSessionUserId();

            for (UserRoleEntry entry : entries) {
                String eid = entry.getEid();
                String roleName = entry.getRole();
                if (!mayUpdateRealm && checkedRoles.add(roleName)) {
                    Role role = realmEdit.getRole(roleName);
                    if (role != null && role.isAllowed("site.upd")) {
                        messages.add(new ParticipantMessage("java.roleperm", new Object[] {roleName},
                                ParticipantMessage.Severity.ERROR));
                        continue;
                    }
                }

                Role role = realmEdit.getRole(roleName);
                if (!SiteParticipantHelper.getAllowedRoles(site.getType(), realm.getRoles()).contains(role)) {
                    messages.add(new ParticipantMessage("java.roleperm", new Object[] {roleName},
                            ParticipantMessage.Severity.ERROR));
                    continue;
                }

                addParticipant(site, realmEdit, entry, status, notificationOption, mayUpdateRealm, auditorId, auditEntries,
                        addedUserReferences, addedEids, messages);
            }

            saveRealm(realmEdit, realmId, auditEntries, addedUserReferences, messages);
        } catch (GroupNotDefinedException e) {
            messages.add(new ParticipantMessage("java.realm", new Object[] {realmId}, ParticipantMessage.Severity.INFO));
            log.warn("cannot find realm for {}", realmId, e);
        } catch (Exception e) {
            log.warn("could not update participant realm {}", realmId, e);
        }
        return addedEids;
    }

    private void addParticipant(Site site, AuthzGroup realmEdit, UserRoleEntry entry, ParticipantStatus status,
            ParticipantNotificationOption notificationOption,
            boolean mayUpdateRealm, String auditorId, List<UserAuditEntry> auditEntries, List<String> userReferences,
            List<String> addedEids, List<ParticipantMessage> messages) {
        try {
            User user = userDirectoryService.getUserByEid(entry.getEid());
            if (!mayUpdateRealm && !siteService.allowUpdateSiteMembership(site.getId())) return;

            realmEdit.addMember(user.getId(), entry.getRole(), status.isActive(), false);
            addedEids.add(entry.getEid());
            userReferences.add(userDirectoryService.userReference(user.getId()));
            if (auditorId != null) {
                auditEntries.add(UserAuditEntry.of(site.getId(), user.getId(), entry.getRole(),
                        UserAuditService.USER_AUDIT_ACTION_ADD, userAuditRegistration.getDatabaseSourceKey(), auditorId));
            }
            if (notificationOption.sendsNotification()) {
                notificationProvider.notifyAddedParticipant(entry.getEid().contains(SiteAddParticipantHandler.EMAIL_CHAR), user, site);
            }
        } catch (UserNotDefinedException e) {
            messages.add(new ParticipantMessage("java.account", new Object[] {entry.getEid()},
                    ParticipantMessage.Severity.INFO));
            log.debug("cannot find user with eid={}", entry.getEid(), e);
        }
    }

    private void saveRealm(AuthzGroup realmEdit, String realmId, List<UserAuditEntry> auditEntries,
            List<String> userReferences, List<ParticipantMessage> messages) {
        try {
            authzGroupService.save(realmEdit);
            if (!auditEntries.isEmpty()) userAuditService.addToUserAuditing(auditEntries);
            eventTrackingService.post(eventTrackingService.newEvent(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP,
                    realmEdit.getId(), false));
            if (serverConfigurationService.getBoolean(SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, true)) {
                for (String userReference : userReferences) {
                    eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD,
                            userReference, true));
                }
            }
        } catch (GroupNotDefinedException e) {
            messages.add(new ParticipantMessage("java.realm", new Object[] {realmId}, ParticipantMessage.Severity.INFO));
            log.warn("cannot find realm for {}", realmId, e);
        } catch (AuthzPermissionException e) {
            messages.add(new ParticipantMessage("java.permeditsite", new Object[] {realmId},
                    ParticipantMessage.Severity.INFO));
            log.warn("don't have permission to edit realm {}", realmId, e);
        }
    }
}
