/**
 * Copyright (c) 2007-2017 The Apereo Foundation
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

public class UserEntityProviderGetUserByIdEidTest {

    private UserEntityProvider provider;
    private UserDirectoryService uds;
    private DeveloperHelperService dhs;
    private User user;

    @Before
    public void setUp() {
        provider = new UserEntityProvider();
        uds = Mockito.mock(UserDirectoryService.class);
        dhs = Mockito.mock(DeveloperHelperService.class);
        user = Mockito.mock(User.class);

        provider.setUserDirectoryService(uds);
        provider.setDeveloperHelperService(dhs);
    }

    @Test
    public void testNotFound() throws UserNotDefinedException {
        Mockito.when(uds.getUserByAid(Mockito.anyString())).thenThrow(UserNotDefinedException.class);
        Mockito.when(uds.getUser(Mockito.anyString())).thenThrow(UserNotDefinedException.class);
        Mockito.when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);

        Assert.assertNull(provider.getUserByIdEid("anything"));
    }

    @Test
    public void testNotFoundId() throws UserNotDefinedException {
        Mockito.when(uds.getUser(Mockito.anyString())).thenThrow(UserNotDefinedException.class);
        Mockito.when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);

        Assert.assertNull(provider.getUserByIdEid("id=1234"));
        Mockito.verify(uds, Mockito.never()).getUserByEid(Mockito.anyString());
    }

    @Test
    public void testEidFallthrough() throws UserNotDefinedException {
        Mockito.when(uds.getUserByAid(Mockito.anyString())).thenThrow(UserNotDefinedException.class);
        Mockito.when(uds.getUser("1234")).thenReturn(user);
        Mockito.when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);

        Assert.assertEquals(user, provider.getUserByIdEid("1234"));
    }

    @Test
    public void testEidFallthroughId() throws UserNotDefinedException {
        Mockito.when(uds.getUserByAid(Mockito.anyString())).thenThrow(UserNotDefinedException.class);
        Mockito.when(uds.getUser("1234")).thenReturn(user);
        Mockito.when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);

        Assert.assertEquals(user, provider.getUserByIdEid("id=1234"));
    }

    @Test
    public void testEidFound() throws UserNotDefinedException {
        Mockito.when(uds.getUserByAid("1234")).thenReturn(user);
        Mockito.when(uds.getUser(Mockito.anyString())).thenThrow(UserNotDefinedException.class);
        Mockito.when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(false);

        Assert.assertEquals(user, provider.getUserByIdEid("1234"));
    }

    @Test
    public void testEidOnly() throws UserNotDefinedException {
        Mockito.when(uds.getUserByAid("1234")).thenThrow(UserNotDefinedException.class);
        Mockito.when(uds.getUser(Mockito.anyString())).thenReturn(user);
        Mockito.when(dhs.getConfigurationSetting("user.explicit.id.only", false)).thenReturn(true);

        // Check we don't fallthrough to searching by ID.
        // This seems strange as the original bug was that EID searching was slow so why you would want
        // to stop ID searching I'm not sure.
        Assert.assertNull(provider.getUserByIdEid("1234"));
    }
}
