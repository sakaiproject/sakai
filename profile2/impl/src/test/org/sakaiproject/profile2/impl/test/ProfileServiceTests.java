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
package org.sakaiproject.profile2.impl.test;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.profile2.api.ProfileService;
import org.sakaiproject.profile2.api.ProfileTransferBean;
import org.sakaiproject.profile2.api.repository.SocialNetworkingInfoRepository;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProfileServiceTestConfiguration.class })
public class ProfileServiceTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private ProfileService profileService;
    @Autowired private SakaiPersonManager sakaiPersonManager;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SocialNetworkingInfoRepository socialNetworkingInfoRepository;
    @Autowired private UserDirectoryService userDirectoryService;

    private String user1Id = UUID.randomUUID().toString();
    private String site1Id = UUID.randomUUID().toString();
    private User user1;

    @Before
    public void setup() {

      reset(userDirectoryService);

      user1 = mock(User.class);
      when(user1.getCreatedBy()).thenReturn(user1);
      when(user1.getModifiedBy()).thenReturn(user1);
      when(user1.getId()).thenReturn(user1Id);
      when(user1.getDisplayName()).thenReturn("User 1");
      when(user1.getEmail()).thenReturn("user1@mailinator.com");
      when(user1.getFirstName()).thenReturn("User");
      when(user1.getLastName()).thenReturn("1");
      when(user1.getEid()).thenReturn("user1");
    }

    @Test
    public void getUsersOwnProfile() {

        Exception exception = assertThrows(SecurityException.class, () -> profileService.getUserProfile(user1Id));
		    assertEquals("Must be logged in to get a UserProfile.", exception.getMessage());

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1Id);
        ProfileTransferBean userProfile = profileService.getUserProfile(user1Id);
        assertNull(userProfile);

        try {
          when(userDirectoryService.getUser(user1Id)).thenReturn(user1);
        } catch (UserNotDefinedException e) {
        }

        Optional<SakaiPerson> person = Optional.ofNullable(mock(SakaiPerson.class));
			  when(sakaiPersonManager.getSakaiPerson(any(), any())).thenReturn(person);

        userProfile = profileService.getUserProfile(user1Id);
        assertNotNull(userProfile);

        assertEquals(user1Id, userProfile.id);
        assertEquals(user1.getDisplayName(), userProfile.displayName);
    }

    @Test
    public void getOtherUsersProfile() {

        String viewerId = UUID.randomUUID().toString();

        User viewer = mock(User.class);
        when(viewer.getType()).thenReturn("user");

        try {
          when(userDirectoryService.getUser(user1Id)).thenReturn(user1);
          when(userDirectoryService.getUser(viewerId)).thenReturn(viewer);
        } catch (UserNotDefinedException e) {
        }

        when(sessionManager.getCurrentSessionUserId()).thenReturn(viewerId);
        ProfileTransferBean userProfile = profileService.getUserProfile(user1Id);
        assertNotNull(userProfile);

        Optional<SakaiPerson> person = Optional.ofNullable(mock(SakaiPerson.class));
			  when(sakaiPersonManager.getSakaiPerson(any(), any())).thenReturn(person);

        // This is not our profile, and we are not a super user.
        assertNull(userProfile.email);

        // Super users or the owner can view the email
        when(securityService.isSuperUser()).thenReturn(true);
        userProfile = profileService.getUserProfile(user1Id);
        assertNotNull(userProfile.email);
    }

    @Test
    public void saveUserProfile() {

        when(sessionManager.getCurrentSessionUserId()).thenReturn(user1Id);

        try {
          when(userDirectoryService.getUser(user1Id)).thenReturn(user1);
        } catch (UserNotDefinedException e) {
        }

        Optional<SakaiPerson> person = Optional.ofNullable(mock(SakaiPerson.class));
			  when(sakaiPersonManager.getSakaiPerson(any(), any())).thenReturn(person);

        ProfileTransferBean userProfile = profileService.getUserProfile(user1Id);
        assertNotNull(userProfile);

        String newEmail = "user1@example.com";
        userProfile.email = newEmail;

        String facebookUrl = "https://www.facebook.com/user1";
        String instagramUrl = "https://www.instagram.com/user1";
        String linkedinUrl = "https://www.linkedin.com/user1";
        String nickname = "Khyber pass";

        userProfile.facebookUrl = facebookUrl;
        userProfile.instagramUrl = instagramUrl;
        userProfile.linkedinUrl = linkedinUrl;
        userProfile.nickname = nickname;

        profileService.saveUserProfile(userProfile);

        when(person.get().getNickname()).thenReturn(nickname);

        userProfile = profileService.getUserProfile(user1Id);

        assertEquals(nickname, userProfile.nickname);
        assertEquals(facebookUrl, userProfile.facebookUrl);
        assertEquals(instagramUrl, userProfile.instagramUrl);
        assertEquals(linkedinUrl, userProfile.linkedinUrl);
    }
}
