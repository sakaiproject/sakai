/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitemanage.impl;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteGroupHelper;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Keeps the membership of role based groups (groups created in Site Info with a role as member,
 * marked with {@link SiteConstants#GROUP_PROP_ROLE_PROVIDERID}) in sync with the site membership
 * by watching site realm updates. Originally added for SAK-16362, restored for SAK-46387.
 */
@Slf4j
public class RoleGroupEventWatcher implements Observer {

    private static final String CURRENT_EVENT_RESOURCE_REF = "current.event.resource.ref";
    private static final String GROUP_REF_SEGMENT = Entity.SEPARATOR + SiteService.GROUP_SUBTYPE;
    private static final String SITE_REF_PREFIX = SiteService.REFERENCE_ROOT + Entity.SEPARATOR;

    @Setter private AuthzGroupService authzGroupService;
    @Setter private EntityManager entityManager;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private SecurityService securityService;
    @Setter private SiteService siteService;
    @Setter private ThreadLocalManager threadLocalManager;

    public void init() {
        eventTrackingService.addLocalObserver(this);
    }

    public void destroy() {
        eventTrackingService.deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof Event)) return;

        Event event = (Event) arg;
        String function = event.getEvent();
        if (!AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP.equals(function)
                && !AuthzGroupService.SECURE_JOIN_AUTHZ_GROUP.equals(function)
                && !AuthzGroupService.SECURE_UNJOIN_AUTHZ_GROUP.equals(function)) {
            return;
        }

        // for a group realm event, work with the containing site realm instead
        String realmId = entityManager.newReference(event.getResource()).getId();
        if (realmId == null) return;
        if (realmId.contains(GROUP_REF_SEGMENT)) {
            realmId = realmId.substring(0, realmId.indexOf(GROUP_REF_SEGMENT));
        }
        if (!realmId.startsWith(SITE_REF_PREFIX)) return;

        // guard against the events fired by our own saveGroupMembership below
        if (threadLocalManager.get(CURRENT_EVENT_RESOURCE_REF) != null) return;
        threadLocalManager.set(CURRENT_EVENT_RESOURCE_REF, realmId);

        // the sync only propagates changes already made to the site realm, bypass permission checks
        SecurityAdvisor advisor = (userId, sf, reference) -> SecurityAdvisor.SecurityAdvice.ALLOWED;
        securityService.pushAdvisor(advisor);
        try {
            AuthzGroup realm = authzGroupService.getAuthzGroup(realmId);
            Site site = siteService.getSite(realmId.substring(SITE_REF_PREFIX.length()));

            boolean needSave = false;
            for (Group group : site.getGroups()) {
                if (group.getProperties().getProperty(Group.GROUP_PROP_WSETUP_CREATED) == null) continue;
                String roleString = group.getProperties().getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID);
                if (StringUtils.isBlank(roleString)) continue;

                needSave = true;
                for (String role : SiteGroupHelper.unpack(roleString)) {
                    try {
                        // replace the members holding this role with the current site users of the role
                        for (Member member : group.getMembers()) {
                            if (role.equals(member.getRole().getId())) {
                                group.deleteMember(member.getUserId());
                            }
                        }
                        for (String userId : realm.getUsersHasRole(role)) {
                            group.insertMember(userId, role, true, false);
                        }
                    } catch (AuthzRealmLockException e) {
                        log.warn("Could not sync role {} in locked group {}, {}", role, group.getId(), e.toString());
                    }
                }
            }
            if (needSave) {
                siteService.saveGroupMembership(site);
            }
        } catch (Exception e) {
            log.warn("Could not sync role based groups for {}, {}", event.getResource(), e.toString());
        } finally {
            securityService.popAdvisor(advisor);
            threadLocalManager.set(CURRENT_EVENT_RESOURCE_REF, null);
        }
    }
}
