/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.test;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;
import org.junit.Before;

@Slf4j
public abstract class SakaiTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired protected AuthzGroupService authzGroupService;
    @Autowired protected UserDirectoryService userDirectoryService;
    @Autowired protected SiteService siteService;

    public String instructor = "instructor";
    public User instructorUser = null;
    public String instructorDisplayName = "Instructor Dave";
    public String instructorSortName = "Dave, Instructor";
    public String user1 = "user1";
    public User user1User = null;
    public String user1DisplayName = "Adrian Fish";
    public String user1SortName = "Fish, Adrian";
    public String user2 = "user2";
    public User user2User = null;
    public String user2DisplayName = "Earle Nietzel";
    public String user2SortName = "Nietzel, Earle";
    public String user3 = "user3";
    public User user3User = null;
    public String user3DisplayName = "Zaphod Beeblebrox";
    public String user3SortName = "Beeblebrox, Zaphod";
    public String site1Id = "site1";
    public String site1Title = "Site 1";
    public String site1Ref = "/site/" + site1Id;
    public Site site1 = null;

    @Before
    public void setup() {

        instructorUser = mock(User.class);
        when(instructorUser.getId()).thenReturn(instructor);
        when(instructorUser.getDisplayName()).thenReturn(instructorDisplayName);
        when(instructorUser.getSortName()).thenReturn(instructorSortName);
        user1User = mock(User.class);
        when(user1User.getId()).thenReturn(user1);
        when(user1User.getDisplayName()).thenReturn(user1DisplayName);
        when(user1User.getSortName()).thenReturn(user1SortName);
        user2User = mock(User.class);
        when(user2User.getId()).thenReturn(user2);
        when(user2User.getDisplayName()).thenReturn(user2DisplayName);
        when(user2User.getSortName()).thenReturn(user2SortName);
        user3User = mock(User.class);
        when(user3User.getId()).thenReturn(user3);
        when(user3User.getDisplayName()).thenReturn(user3DisplayName);
        when(user3User.getSortName()).thenReturn(user3SortName);
        site1 = mock(Site.class);
        when(site1.getTitle()).thenReturn(site1Title);
        when(siteService.siteReference(site1Id)).thenReturn(site1Ref);

        try {
          when(siteService.getSite(site1Id)).thenReturn(site1);
          when(siteService.getSiteVisit(site1Id)).thenReturn(site1);
        } catch (Exception e) {
        }

        AuthzGroup site1Group = mock(AuthzGroup.class);
        Set<String> userIds = new HashSet<>();
        userIds.add(instructor);
        userIds.add(instructor);
        userIds.add(user1);
        userIds.add(user2);
        userIds.add(user3);
        when(site1Group.getUsers()).thenReturn(userIds);
        when(site1.getUsers()).thenReturn(userIds);

        List<User> users = new ArrayList<>();
        users.add(instructorUser);
        users.add(user1User);
        users.add(user2User);
        users.add(user3User);

        try {
            when(authzGroupService.getAuthzGroup(site1Ref)).thenReturn(site1Group);
            when(userDirectoryService.getUsers(new ArrayList(userIds))).thenReturn(users);
        } catch (Exception e) {
        }
    }
}
