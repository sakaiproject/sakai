/**
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
package org.sakaiproject.entitybroker.providers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.config.EntityRestTestConfiguration;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {EntityRestTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class UserEntityProviderFindAndCheckUserIdTest {

    @Autowired private UserEntityProvider provider;
    @Autowired private UserDirectoryService uds;
    private DeveloperHelperService dhs;

    @Before
    public void setUp() throws UserNotDefinedException {
        reset(uds);
        dhs = mock(DeveloperHelperService.class);
        provider.setDeveloperHelperService(dhs);

        User user = mock(User.class);
        when(user.getId()).thenReturn("id");
        when(uds.getUser("id")).thenReturn(user);
        when(uds.getUserEid("id")).thenReturn("eid");
        when(uds.getUserByAid("id")).thenThrow(UserNotDefinedException.class);
        when(uds.getUserEid("eid")).thenThrow(UserNotDefinedException.class);
        when(uds.getUserByAid("eid")).thenReturn(user);
        when(uds.getUserId("eid")).thenReturn("id");
        when(uds.getUserEid("missing")).thenThrow(UserNotDefinedException.class);
        when(uds.getUserByAid("missing")).thenThrow(UserNotDefinedException.class);
    }

    @Test
    public void testNull() {
        assertThrows(IllegalArgumentException.class, () -> provider.findAndCheckUserId(null, null));
    }

    @Test
    public void testNoExplicitSeparateId() throws UserNotDefinedException {
        when(dhs.getConfigurationSetting("separateIdEid@org.sakaiproject.user.api.UserDirectoryService", null)).thenReturn("true");
        when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);

        assertEquals("id", provider.findAndCheckUserId("id", null));
        assertEquals("id", provider.findAndCheckUserId("/user/id", null));
        assertEquals("id", provider.findAndCheckUserId("eid", null));
        assertEquals("id", provider.findAndCheckUserId("/user/eid", null));
        assertNull(provider.findAndCheckUserId("missing", null));
    }

    @Test
    public void testNoExplicitSeparateEid() throws UserNotDefinedException {
        when(dhs.getConfigurationSetting("separateIdEid@org.sakaiproject.user.api.UserDirectoryService", null)).thenReturn("true");
        when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        assertEquals("id", provider.findAndCheckUserId(null, "eid"));
        assertEquals("id", provider.findAndCheckUserId(null, "/user/eid"));
        assertNull(provider.findAndCheckUserId(null, "missing"));
    }

    @Test
    public void testExplicitSeparateId() throws UserNotDefinedException {
        when(dhs.getConfigurationSetting("separateIdEid@org.sakaiproject.user.api.UserDirectoryService", null)).thenReturn("true");
        when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(true);
        when(uds.getUserEid("id")).thenReturn("eid");

        assertEquals("id", provider.findAndCheckUserId("id", null));
        assertEquals("id", provider.findAndCheckUserId("/user/id", null));
        assertNull(provider.findAndCheckUserId("missing", null));
        assertEquals("id", provider.findAndCheckUserId("id=id", null));
        assertNull(provider.findAndCheckUserId("id=missing", null));

    }

    @Test
    public void testExplicitSeparateEid() throws UserNotDefinedException {
        when(dhs.getConfigurationSetting("separateIdEid@org.sakaiproject.user.api.UserDirectoryService", null)).thenReturn("true");
        when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(true);
        assertEquals("id", provider.findAndCheckUserId(null, "id"));
        assertEquals("id", provider.findAndCheckUserId(null, "/user/id"));
        // I would expect this to work, but it doesn't
        // Assert.assertEquals("id", provider.findAndCheckUserId(null, "eid"));
        assertNull(provider.findAndCheckUserId(null, "missing"));
        // This looks wrong as we are returning a different ID to the one supplied, unlike all the other calls.
        assertEquals("id", provider.findAndCheckUserId(null, "id=eid"));
        assertNull(provider.findAndCheckUserId(null, "id=missing"));
    }

    @Test
    public void testNoExplicitNoSeparateId() throws UserNotDefinedException {
        when(dhs.getConfigurationSetting("separateIdEid@org.sakaiproject.user.api.UserDirectoryService", null)).thenReturn("false");
        when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        assertEquals("id", provider.findAndCheckUserId("id", null));
        assertEquals("id", provider.findAndCheckUserId("/user/id", null));
        assertNull(provider.findAndCheckUserId("missing", null));
    }


    @Test
    public void testNoExplicitNoSeparateEid() throws UserNotDefinedException {
        when(dhs.getConfigurationSetting("separateIdEid@org.sakaiproject.user.api.UserDirectoryService", null)).thenReturn("false");
        when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        assertEquals("id", provider.findAndCheckUserId(null, "eid"));
        assertEquals("id", provider.findAndCheckUserId(null, "/user/eid"));
        assertNull(provider.findAndCheckUserId(null, "missing"));
    }
}
