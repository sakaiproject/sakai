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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * Tests the various ways of finding users in different configurations.
 */
public class UserEntityProviderFindAndCheckUserId {

    private UserEntityProvider provider;
    private UserDirectoryService uds;
    private DeveloperHelperService dhs;
    private User user;

    @Before
    public void setUp() throws UserNotDefinedException {
        provider = new UserEntityProvider();
        uds = Mockito.mock(UserDirectoryService.class);
        dhs = Mockito.mock(DeveloperHelperService.class);
        user = Mockito.mock(User.class);

        // Set the default for these methods, need to use Yoda syntax when mocking other invocations of these methods.
        Mockito.when(uds.getUserId(Mockito.anyString())).thenThrow(UserNotDefinedException.class);
        Mockito.when(uds.getUserByEid(Mockito.anyString())).thenThrow(UserNotDefinedException.class);
        Mockito.when(uds.getUserByAid(Mockito.anyString())).thenThrow(UserNotDefinedException.class);
        Mockito.when(uds.getUserEid(Mockito.anyString())).thenThrow(UserNotDefinedException.class);

        provider.setUserDirectoryService(uds);
        provider.setDeveloperHelperService(dhs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        provider.findAndCheckUserId(null, null);
    }

    @Test
    public void testResolveByIdWithFailover() throws UserNotDefinedException {
        Mockito.when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        Mockito.doReturn("eid").when(uds).getUserEid("id");
        Mockito.doReturn(user).when(uds).getUserByAid("eid");
        Mockito.when(user.getId()).thenReturn("id");

        Assert.assertEquals("id", provider.findAndCheckUserId("id", null));
        Assert.assertEquals("id", provider.findAndCheckUserId("/user/id", null));
        Assert.assertEquals("id", provider.findAndCheckUserId("eid", null));
        Assert.assertEquals("id", provider.findAndCheckUserId("/user/eid", null));
        Assert.assertNull(provider.findAndCheckUserId("missing", null));
    }

    @Test
    public void testResolveByEidWithFailover() throws UserNotDefinedException {
        Mockito.when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);
        Mockito.doReturn("id").when(uds).getUserId("eid");

        Assert.assertEquals("id", provider.findAndCheckUserId(null, "eid"));
        Assert.assertEquals("id", provider.findAndCheckUserId(null, "/user/eid"));
        Assert.assertNull(provider.findAndCheckUserId(null, "missing"));
    }

    @Test
    public void testExplicitIdOnly() throws UserNotDefinedException {
        Mockito.when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(true);
        Mockito.doReturn("eid").when(uds).getUserEid("id");
        Mockito.doReturn(user).when(uds).getUserByAid("id");
        Mockito.when(user.getId()).thenReturn("id");

        Assert.assertEquals("id", provider.findAndCheckUserId("id", null));
        Assert.assertEquals("id", provider.findAndCheckUserId("/user/id", null));
        Assert.assertNull(provider.findAndCheckUserId("missing", null));
        Assert.assertEquals("id", provider.findAndCheckUserId(null, "id"));
        Assert.assertNull(provider.findAndCheckUserId(null, "missing"));
        Assert.assertEquals("id", provider.findAndCheckUserId("id=id", null));
        Assert.assertEquals("id", provider.findAndCheckUserId(null, "id=id"));
        Assert.assertNull(provider.findAndCheckUserId("id=missing", null));
        Assert.assertNull(provider.findAndCheckUserId(null, "id=missing"));
    }
}
