/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.scorm.service.sakai.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import org.junit.Test;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SakaiResourceServiceTest
{
	@Test
	public void putArchiveUsesSiteScopedUploadCollection() throws Exception
	{
		String siteId = "course-1";
		String uploadRoot = "/private/scorm/uploads/";
		String siteUploadRoot = uploadRoot + siteId + "/";

		ContentHostingService contentHostingService = mock(ContentHostingService.class);
		ContentCollectionEdit uploadsEdit = mock(ContentCollectionEdit.class);
		ContentCollectionEdit siteUploadsEdit = mock(ContentCollectionEdit.class);
		ContentResourceEdit resourceEdit = mock(ContentResourceEdit.class);
		ResourcePropertiesEdit collectionProperties = mock(ResourcePropertiesEdit.class);
		ResourcePropertiesEdit resourceProperties = mock(ResourcePropertiesEdit.class);

		doThrow(new IdUnusedException(uploadRoot)).when(contentHostingService).checkCollection(uploadRoot);
		doThrow(new IdUnusedException(siteUploadRoot)).when(contentHostingService).checkCollection(siteUploadRoot);
		when(contentHostingService.addCollection(uploadRoot)).thenReturn(uploadsEdit);
		when(contentHostingService.addCollection(siteUploadRoot)).thenReturn(siteUploadsEdit);
		when(uploadsEdit.getPropertiesEdit()).thenReturn(collectionProperties);
		when(siteUploadsEdit.getPropertiesEdit()).thenReturn(collectionProperties);
		when(contentHostingService.addResource(eq(siteUploadRoot), eq("lesson"), eq(".zip"), eq(100))).thenReturn(resourceEdit);
		when(resourceEdit.getPropertiesEdit()).thenReturn(resourceProperties);
		when(resourceEdit.getId()).thenReturn(siteUploadRoot + "lesson.zip");

		TestSakaiResourceService service = new TestSakaiResourceService();
		service.setContentHostingService(contentHostingService);
		service.setSecurityService(mock(SecurityService.class));
		service.setSiteService(mock(SiteService.class));
		service.setToolManager(mockToolManager(siteId));
		service.setServerConfigurationService(mock(ServerConfigurationService.class));

		service.putArchive(new ByteArrayInputStream("zip".getBytes()), "lesson.zip", "application/zip", false, NotificationService.NOTI_NONE);

		verify(contentHostingService).addResource(siteUploadRoot, "lesson", ".zip", 100);
	}

	@Test
	public void convertArchiveRemovesTemporaryUploadAfterSuccess() throws Exception
	{
		String resourceId = "/private/scorm/uploads/course-1/lesson.zip";
		String packageUuid = "package-uuid";
		String packageCollection = "/private/scorm/" + packageUuid + "/";

		ContentHostingService contentHostingService = mock(ContentHostingService.class);
		ContentCollection rootCollection = mock(ContentCollection.class);
		ContentCollectionEdit packageEdit = mock(ContentCollectionEdit.class);
		ResourcePropertiesEdit propertiesEdit = mock(ResourcePropertiesEdit.class);

		when(contentHostingService.getCollection("/private/scorm/")).thenReturn(rootCollection);
		when(rootCollection.getMemberResources()).thenReturn(Collections.emptyList());
		when(contentHostingService.editCollection(packageCollection)).thenReturn(packageEdit);
		when(packageEdit.getPropertiesEdit()).thenReturn(propertiesEdit);

		TestSakaiResourceService service = new TestSakaiResourceService();
		service.setContentHostingService(contentHostingService);
		service.setSecurityService(mock(SecurityService.class));
		service.setSiteService(mock(SiteService.class));
		service.setToolManager(mock(ToolManager.class));
		service.setServerConfigurationService(mock(ServerConfigurationService.class));
		service.setUnpackResult(packageUuid);

		service.convertArchive(resourceId, "Lesson");

		verify(contentHostingService).removeResource(resourceId);
	}

	private ToolManager mockToolManager(String siteId)
	{
		ToolManager toolManager = mock(ToolManager.class);
		Placement placement = mock(Placement.class);
		when(toolManager.getCurrentPlacement()).thenReturn(placement);
		when(placement.getContext()).thenReturn(siteId);
		return toolManager;
	}

	private static class TestSakaiResourceService extends SakaiResourceService
	{
		private ServerConfigurationService serverConfigurationService;
		private ContentHostingService contentHostingService;
		private SecurityService securityService;
		private SiteService siteService;
		private ToolManager toolManager;
		private String unpackResult;

		void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
		{
			this.serverConfigurationService = serverConfigurationService;
		}

		void setContentHostingService(ContentHostingService contentHostingService)
		{
			this.contentHostingService = contentHostingService;
		}

		void setSecurityService(SecurityService securityService)
		{
			this.securityService = securityService;
		}

		void setSiteService(SiteService siteService)
		{
			this.siteService = siteService;
		}

		void setToolManager(ToolManager toolManager)
		{
			this.toolManager = toolManager;
		}

		void setUnpackResult(String unpackResult)
		{
			this.unpackResult = unpackResult;
		}

		@Override
		protected ServerConfigurationService configurationService()
		{
			return serverConfigurationService;
		}

		@Override
		protected ContentHostingService contentService()
		{
			return contentHostingService;
		}

		@Override
		protected SecurityService securityService()
		{
			return securityService;
		}

		@Override
		protected SiteService siteService()
		{
			return siteService;
		}

		@Override
		protected ToolManager toolManager()
		{
			return toolManager;
		}

		@Override
		protected String unpack(String resourceId)
		{
			return unpackResult;
		}

		@Override
		public InputStream getArchiveStream(String resourceId)
		{
			return new ByteArrayInputStream(new byte[0]);
		}
	}
}
