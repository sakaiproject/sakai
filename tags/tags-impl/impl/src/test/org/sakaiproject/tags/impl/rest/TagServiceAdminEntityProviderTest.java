/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tags.impl.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.impl.TagServiceImpl;
import org.sakaiproject.tags.impl.storage.BaseStorageTest;
import org.sakaiproject.tool.api.Session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class TagServiceAdminEntityProviderTest extends BaseStorageTest {

    private TagServiceAdminEntityProvider provider;
    private SecurityService securityService;
    private Session session;
    private Map<String, Object> params;

    @Before
    public void setUpProvider() {
        // Reuse the module's database-backed storage harness; only kernel boundaries are mocked.
        TagServiceImpl tagService = new TagServiceImpl();
        tagService.setTags(tagStorage);
        tagService.setTagCollections(tagCollectionStorage);
        securityService = mock(SecurityService.class);
        session = mock(Session.class);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(securityService.unlock("tagservice.manage", "/site/!admin")).thenReturn(true);
        when(session.getAttribute("sakai.tagservice-admin.token")).thenReturn("valid-token");
        params = new HashMap<>();
        params.put("session", "valid-token");
        provider = new TagServiceAdminEntityProvider();
        provider.setSessionManager(sessionManager);
        provider.setSecurityService(securityService);
        provider.setTagService(tagService);
    }

    @Test
    public void anonymousDownloadIsDeniedBeforeCheckingPermissionsOrToken() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn(null);
        assertThrows(SecurityException.class, () -> provider.downloadCollection(null, params));
        verifyNoInteractions(securityService, session);
    }

    @Test
    public void downloadRequiresManagePermissionEvenWithValidToken() {
        when(securityService.unlock("tagservice.manage", "/site/!admin")).thenReturn(false);
        assertThrows(SecurityException.class, () -> provider.downloadCollection(null, params));
        verifyNoInteractions(session);
    }

    @Test
    public void downloadRequiresTokenInCurrentSession() {
        when(session.getAttribute("sakai.tagservice-admin.token")).thenReturn(null);
        assertThrows(SecurityException.class, () -> provider.downloadCollection(null, params));
    }

    @Test
    public void downloadRequiresRequestToken() {
        params.remove("session");
        assertThrows(SecurityException.class, () -> provider.downloadCollection(null, params));
    }

    @Test
    public void downloadRejectsMismatchedToken() {
        params.put("session", "wrong-token");
        assertThrows(SecurityException.class, () -> provider.downloadCollection(null, params));
    }

    @Test
    public void permittedDownloadReturnsStoredTags() {
        TagCollection collection = newPersistentTagCollection("download");
        Tag tag = newTag("download", collection);
        String tagId = tagStorage.createTag(tag);
        params.put("tagcollectionid", collection.getTagCollectionId());
        List<Tag> downloaded = provider.downloadCollection(null, params);
        assertEquals(1, downloaded.size());
        assertEquals(tagId, downloaded.get(0).getTagId());
        assertEquals(tag.getTagLabel(), downloaded.get(0).getTagLabel());
    }

    @Test
    public void anonymousSessionCreationPropagatesAccessDenial() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn(null);
        assertThrows(SecurityException.class, () -> provider.startSession(null, params));
    }

    @Test
    public void tagMutationPropagatesInvalidTokenDenial() {
        params.put("session", "wrong-token");
        assertThrows(SecurityException.class, () -> provider.createTag(null, params));
    }
}
