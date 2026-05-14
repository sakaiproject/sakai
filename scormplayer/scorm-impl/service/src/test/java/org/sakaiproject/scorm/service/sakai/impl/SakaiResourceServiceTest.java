package org.sakaiproject.scorm.service.sakai.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.exceptions.ResourceStorageException;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

public class SakaiResourceServiceTest
{
	private Path tempRoot;
	private ServerConfigurationService configurationService;
	private ToolManager toolManager;
	private Placement placement;
	private SakaiResourceService service;

	@Before
	public void setUp() throws Exception
	{
		tempRoot = Files.createTempDirectory("scorm-temp-tests-" + UUID.randomUUID());
		configurationService = mock(ServerConfigurationService.class);
		toolManager = mock(ToolManager.class);
		placement = mock(Placement.class);

		when(configurationService.getSakaiHomePath()).thenReturn(tempRoot.toString());
		when(toolManager.getCurrentPlacement()).thenReturn(placement);
		when(placement.getContext()).thenReturn("site-a");

		service = new TestSakaiResourceService(configurationService, toolManager);
	}

	@After
	public void tearDown() throws Exception
	{
		if (tempRoot != null && Files.exists(tempRoot))
		{
			Files.walk(tempRoot)
				.sorted((a, b) -> b.getNameCount() - a.getNameCount())
				.forEach(path -> path.toFile().delete());
		}
	}

	@Test
	public void temporaryArchiveCanBeReadAndRemovedAfterPlacementContextChanges() throws Exception
	{
		byte[] expected = "zip-content".getBytes(StandardCharsets.UTF_8);
		String resourceId = service.putArchive(new ByteArrayInputStream(expected), "test.zip", "application/zip", false, 0);
		assertTrue(resourceId.startsWith("tmp:site-a:"));

		when(placement.getContext()).thenReturn("site-b");

		try (InputStream stream = service.getArchiveStream(resourceId))
		{
			assertArrayEquals(expected, stream.readAllBytes());
		}

		service.removeArchive(resourceId);

		assertThrows(ResourceStorageException.class, () -> service.getArchive(resourceId));
	}

	private static final class TestSakaiResourceService extends SakaiResourceService
	{
		private final ServerConfigurationService configurationService;
		private final ToolManager toolManager;
		private final ContentHostingService contentHostingService = mock(ContentHostingService.class);
		private final SecurityService securityService = mock(SecurityService.class);
		private final SiteService siteService = mock(SiteService.class);

		private TestSakaiResourceService(ServerConfigurationService configurationService, ToolManager toolManager)
		{
			this.configurationService = configurationService;
			this.toolManager = toolManager;
		}

		@Override
		protected ServerConfigurationService configurationService()
		{
			return configurationService;
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
		protected String newFolder(String uuid, ZipEntry entry) throws ResourceStorageException
		{
			return uuid;
		}

		@Override
		protected String newItem(String uuid, ZipInputStream zipStream, ZipEntry entry) throws ResourceStorageException
		{
			return uuid;
		}

		@Override
		public List<ContentPackageResource> getResources(String uuid) throws ResourceStorageException
		{
			return Collections.emptyList();
		}

		@Override
		public List<Archive> getUnvalidatedArchives() throws ResourceStorageException
		{
			return Collections.emptyList();
		}

		@Override
		public String putArchive(InputStream stream, String name, String mimeType, boolean isHidden, int priority) throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, IdUnusedException, OverQuotaException, ServerOverloadException
		{
			return super.putArchive(stream, name, mimeType, isHidden, priority);
		}

		@Override
		public void removeResources(String uuid) throws ResourceNotDeletedException
		{
			throw new UnsupportedOperationException();
		}
	}
}
