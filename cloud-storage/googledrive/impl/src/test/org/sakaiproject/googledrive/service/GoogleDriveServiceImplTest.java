/**
 * Copyright (c) 2003-2022 The Apereo Foundation
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

package org.sakaiproject.googledrive.service;

import static org.mockito.Mockito.*;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.Get;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.googledrive.repository.GoogleDriveUserRepository;
import org.sakaiproject.googledrive.model.GoogleDriveItem;
import org.sakaiproject.googledrive.model.GoogleDriveUser;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {GoogleDriveServiceImplTestConfiguration.class})
public class GoogleDriveServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private SessionManager sessionManager;

    @Autowired private GoogleDriveService googleDriveService;

    private String userId;
    private Drive drive;
    private GoogleDriveUserRepository googledriveRepo;
    private Cache<String, Drive> googledriveUserCache;
    private Cache<String, List<GoogleDriveItem>> driveRootItemsCache;
    private Cache<String, List<GoogleDriveItem>> driveChildrenItemsCache;
    private Cache<String, GoogleDriveItem> driveItemsCache;

    @Before
    public void setUp() {

        userId = UUID.randomUUID().toString();
        googledriveUserCache = mock(Cache.class);
        drive = mock(Drive.class);
        driveRootItemsCache = mock(Cache.class);
        driveChildrenItemsCache = mock(Cache.class);
        driveItemsCache = mock(Cache.class);
        googledriveRepo = mock(GoogleDriveUserRepository.class);

        when(sessionManager.getCurrentSessionUserId()).thenReturn(userId);
        when(googledriveUserCache.get(userId)).thenReturn(drive);

        ReflectionTestUtils.setField(googleDriveService, "googledriveUserCache", googledriveUserCache);
        ReflectionTestUtils.setField(googleDriveService, "driveRootItemsCache", driveRootItemsCache);
        ReflectionTestUtils.setField(googleDriveService, "driveChildrenItemsCache", driveChildrenItemsCache);
        ReflectionTestUtils.setField(googleDriveService, "driveItemsCache", driveItemsCache);
        ReflectionTestUtils.setField(googleDriveService, "googledriveRepo", googledriveRepo);

    }

    @Test
    public void testGoogleDriveServiceIsValid() {
        Assert.assertNotNull(googleDriveService);
    }

    @Test
    public void testGoogleDriveEnabledForDefaultOrganization() throws Exception {
        User u = mock(User.class);
        // It's an invented organization and the default is enabled.
        when(u.getEid()).thenReturn("eee@inventedorganization.com");
        when(userDirectoryService.getUser(any())).thenReturn(u);
        Assert.assertTrue(googleDriveService.isGoogleDriveEnabledForUser());
    }

    @Test
    public void testGoogleDriveEnabledForOrganization() throws Exception {
        User u = mock(User.class);
        when(u.getEid()).thenReturn("eee@org");
        when(userDirectoryService.getUser(any())).thenReturn(u);
        Assert.assertTrue(googleDriveService.isGoogleDriveEnabledForUser());
    }


    @Test
    public void testFormAuthenticationUrl() throws Exception {
        User u = mock(User.class);
        when(u.getEid()).thenReturn("eee@org");
        when(userDirectoryService.getUser(any())).thenReturn(u);
        String url = googleDriveService.formAuthenticationUrl();
        Assert.assertNotNull(url);
    }

    @Test
    public void testGetGoogleDriveUser() throws Exception {
        GoogleDriveUser gdu = new GoogleDriveUser();
        gdu.setSakaiUserId(userId);
        Mockito.doReturn(gdu).when(googledriveRepo).findBySakaiId(userId);
        GoogleDriveUser gduReturned = googleDriveService.getGoogleDriveUser(userId);
        Assert.assertNotNull(gduReturned);
        Assert.assertEquals(gduReturned, gdu);
        Assert.assertNull(googleDriveService.getGoogleDriveUser(null));
    }

    @Test
    public void testGetGoogleDriveUserNotFound() throws Exception {
        GoogleDriveUser gduReturned = googleDriveService.getGoogleDriveUser(userId);
        Assert.assertNull(gduReturned);
    }

    @Test
    public void testCleanGoogleDriveCacheForUser() throws Exception {
        googleDriveService.cleanGoogleDriveCacheForUser(userId);
        verify(googledriveUserCache, times(1)).remove(userId);
        verify(driveRootItemsCache, times(1)).remove(userId);
        verify(driveChildrenItemsCache, times(1)).clear();
        verify(driveItemsCache, times(1)).clear();
        googleDriveService.cleanGoogleDriveCacheForUser(null);
    }

    @Test
    public void testRevokeGoogleDriveConfiguration() throws Exception {
        googleDriveService.revokeGoogleDriveConfiguration(userId);
        verify(googledriveUserCache, times(1)).remove(userId);
        verify(driveRootItemsCache, times(1)).remove(userId);
        verify(driveChildrenItemsCache, times(1)).clear();
        verify(driveItemsCache, times(1)).clear();
        googleDriveService.revokeGoogleDriveConfiguration(null);
    }

    @Test
    public void testDownloadDriveFile() throws Exception {
        Files files = mock(Files.class);
        Get get = mock(Get.class);
        when(drive.files()).thenReturn(files);
        when(files.get(any())).thenReturn(get);
        String itemId = "whatever";
        Assert.assertNotNull(googleDriveService.downloadDriveFile(userId, itemId));
        Assert.assertNotNull(googleDriveService.downloadDriveFile(userId, null));
    }

    @Test
    public void testGetDriveItem() throws Exception {
        String itemId = "randomitem";
        Assert.assertNotNull(googleDriveService.getDriveItem(userId, itemId));
        Assert.assertNotNull(googleDriveService.getDriveItem(userId, null));
    }

    @Test
    public void testGetDriveChildrenItems() throws Exception {
        String itemId = "randomitem";
        Assert.assertNotNull(googleDriveService.getDriveChildrenItems(userId, itemId, 0));
        Assert.assertNotNull(googleDriveService.getDriveChildrenItems(userId, null, 0));
    }

    @Test
    public void testGetDriveRootItems() throws Exception {
        Assert.assertNotNull(googleDriveService.getDriveRootItems(userId));
    }

    @Test
    public void testToken() throws Exception {
        String code = "randomcode";
        Assert.assertFalse(googleDriveService.token(userId, code));
    }

}
