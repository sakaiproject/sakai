/**
* Copyright (c) 2023 Apereo Foundation
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

package org.sakaiproject.bulk.membership.service;

import java.util.Collection;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.bulk.membership.exception.UsersByEmailException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;

import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BulkUserMembershipToolService {

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    PreferencesService preferencesService;

    @Autowired
    private SiteService siteService;

    private final String ADD = "add";
    private final String REMOVE = "remove";
    private final String ACTIVATE = "activate";
    private final String DEACTIVATE = "deactivate";

    public User getUser(String criteria) throws UsersByEmailException {
        try {
            User user = userDirectoryService.getUserByEid(criteria);
            if (user != null) {
                return user;
            }
        } catch (UserNotDefinedException usEx) {
            Collection<User> users = userDirectoryService.findUsersByEmail(criteria);
            if (users.size() > 1) {
                throw new UsersByEmailException(criteria);
            } else if (!users.isEmpty()) {
                return users.iterator().next();
            }
        }
        return null;
    }

    public Site getSite(String id) {
        try {
            return siteService.getSite(id);
        } catch (IdUnusedException idEx) {
            return null;
        }
    }

    public void applyAction(String action, Site site, User user, String role) throws Exception {
        String userId = user.getId();
        Member member = site.getMember(userId);
        switch (action) {
            case ADD:
                if (member != null) {
                    throw new Exception("Cannot add member, member alredy exist");
                } else {
                    site.addMember(userId, role, true, false);
                }
                break;
            case REMOVE:
                if (member != null) {
                    site.removeMember(userId);
                } else {
                    throw new Exception("Cannot remove member, member alredy doesn't exist");
                }
                break;
            case ACTIVATE:
                if (member != null && !member.isActive()){
                    site.addMember(userId, member.getRole().getId(), true, false);
                } else {
                    throw new Exception("Cannot change state for user: " + userId);
                }
                break;
            case DEACTIVATE:
                if (member != null && member.isActive()){
                    site.addMember(userId, member.getRole().getId(), false, false); 
                } else {
                    throw new Exception("Cannot change state for user: " + userId);
                }
                break;
            default:
                log.error("Action: {} not found", action);
                break;
        }
        siteService.save(site);
    }

    public Locale getLocaleForCurrentUser() {
        String userId = sessionManager.getCurrentSessionUserId();
        return StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();
	}

}
