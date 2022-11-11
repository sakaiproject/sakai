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

package org.sakaiproject.dav;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.anyString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class DavTest {
    private static final String ADJUST_ID_NOT_VALID = "ID is not valid";
    private static final String USER_1_EID = "e123456";
    private static final String USER_1_ID = USER_1_EID;
    private static final String USER_2_EID = "y123456";
    private static final String USER_2_ID = "p123456";

    private static ContentHostingService contentHostingService;
    private static UserDirectoryService userDirectoryService;
    private static DavServlet davServlet;

    private static final List<User> users = new ArrayList<>();

    @Before
    /**
     * When and thenAnswer needs to be reinitialized after each
     */
    public void beforeEach() {
        User user1 = mock(User.class);
        Mockito.lenient().when(user1.getEid()).thenReturn(USER_1_EID);
        Mockito.lenient().when(user1.getId()).thenReturn(USER_1_ID);
        users.add(user1);

        User user2 = mock(User.class);
        Mockito.lenient().when(user2.getEid()).thenReturn(USER_2_EID);
        Mockito.lenient().when(user2.getId()).thenReturn(USER_2_ID);
        users.add(user2);

        contentHostingService = mock(ContentHostingService.class);
        when(contentHostingService.isShortRefs()).thenReturn(false);

        userDirectoryService = mock(UserDirectoryService.class);
        try {
            Mockito.lenient().when(userDirectoryService.getUser(anyString()))
                    .thenAnswer(invocation -> this.getUser(invocation.getArgument(0)))
                    .thenThrow(UserNotDefinedException.class);

        } catch (UserNotDefinedException ex) { }

        try {
            Mockito.lenient().when(userDirectoryService.getUserByEid(anyString()))
                    .thenAnswer(invocation -> this.getUserByEid(invocation.getArgument(0)))
                    .thenThrow(UserNotDefinedException.class);

        } catch (UserNotDefinedException ex) { }

        davServlet = new DavServlet();
        davServlet.setContentHostingService(contentHostingService);
        davServlet.setUserDirectoryService(userDirectoryService);
    }

    @Test
    /**
     * Tests for adjustId method from DavServlet class
     * Should return valid IDs
     */
    public void testIdIsValid() throws Exception {
        Assert.assertEquals(ADJUST_ID_NOT_VALID, "/user/e123456", davServlet.adjustId("/user/E123456"));
        Assert.assertEquals(ADJUST_ID_NOT_VALID, "/user/e123456", davServlet.adjustId("/user/e123456"));
        Assert.assertEquals(ADJUST_ID_NOT_VALID, "/group-user/RANDOM_SITE/e123456", davServlet.adjustId("/group-user/RANDOM_SITE/E123456"));
        Assert.assertEquals(ADJUST_ID_NOT_VALID, "/group-user/RANDOM_SITE/e123456", davServlet.adjustId("/group-user/RANDOM_SITE/e123456"));
        Assert.assertEquals(ADJUST_ID_NOT_VALID, "/group-user/RANDOM_SITE/p123456", davServlet.adjustId("/group-user/RANDOM_SITE/Y123456"));
        Assert.assertEquals(ADJUST_ID_NOT_VALID, "/group-user/RANDOM_SITE/p123456", davServlet.adjustId("/group-user/RANDOM_SITE/y123456"));
        Assert.assertEquals(ADJUST_ID_NOT_VALID, "/group-user/RANDOM_SITE/p123456", davServlet.adjustId("/group-user/RANDOM_SITE/P123456"));
        Assert.assertEquals(ADJUST_ID_NOT_VALID, "/group-user/RANDOM_SITE/p123456", davServlet.adjustId("/group-user/RANDOM_SITE/p123456"));
    }

    /**
     * Replacement for UserDirectoryService getUser method
     * @param id the user id
     * @return a user
     * @throws UserNotDefinedException if the user doesn't exist
     */
    private User getUser(String id) throws UserNotDefinedException {
        Optional<User> theUser = users.stream()
                .filter(user -> id.toLowerCase().equals(user.getId().toLowerCase()))
                .findFirst();
        this.beforeEach(); // Clean methods
        if (!theUser.isPresent()) throw new UserNotDefinedException("null");
        return theUser.get();
    }

    /**
     * Replacement for UserDirectoryService getUserByEid method
     * @param eid the user eid
     * @return a user
     * @throws UserNotDefinedException if the user doesn't exist
     */
    private User getUserByEid(String eid) throws UserNotDefinedException {
        Optional<User> theUser = users.stream()
                .filter(user -> eid.toLowerCase().equals(user.getEid().toLowerCase()))
                .findFirst();
        this.beforeEach(); // Clean methods
        if (!theUser.isPresent()) throw new UserNotDefinedException("null");
        return theUser.get();
    }
}
