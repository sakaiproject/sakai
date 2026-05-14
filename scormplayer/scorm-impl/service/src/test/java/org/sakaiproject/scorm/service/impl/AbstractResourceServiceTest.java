package org.sakaiproject.scorm.service.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

public class AbstractResourceServiceTest
{
	@Test
	public void shouldSkipEntrySkipsKnownJunk()
	{
		RecordingResourceService service = new RecordingResourceService();
		assertTrue(service.shouldSkip(new ZipEntry("__MACOSX/._manifest.xml")));
		assertTrue(service.shouldSkip(new ZipEntry("folder/.DS_Store")));
		assertTrue(service.shouldSkip(new ZipEntry("folder/Thumbs.db")));
		assertTrue(service.shouldSkip(new ZipEntry("folder/desktop.ini")));
		assertTrue(service.shouldSkip(new ZipEntry("folder/._index.html")));
		assertTrue(service.shouldSkip(new ZipEntry("../imsmanifest.xml")));
		assertTrue(service.shouldSkip(new ZipEntry("content/../../index.html")));
	}

	@Test
	public void shouldSkipEntryAllowsNormalFiles()
	{
		RecordingResourceService service = new RecordingResourceService();
		assertFalse(service.shouldSkip(new ZipEntry("imsmanifest.xml")));
		assertFalse(service.shouldSkip(new ZipEntry("content/index.html")));
	}

	@Test
	public void unpackEntrySkipsJunkWithoutCreatingFolderOrItem() throws Exception
	{
		RecordingResourceService service = new RecordingResourceService();
		service.unpackEntryForTest("folder/.DS_Store", false);
		assertTrue(service.folderCalls.get() == 0);
		assertTrue(service.itemCalls.get() == 0);
	}

	@Test
	public void unpackEntryRoutesDirectoryToNewFolder() throws Exception
	{
		RecordingResourceService service = new RecordingResourceService();
		service.unpackEntryForTest("content/", true);
		assertTrue(service.folderCalls.get() == 1);
		assertTrue(service.itemCalls.get() == 0);
	}

	@Test
	public void unpackEntryRoutesFileToNewItem() throws Exception
	{
		RecordingResourceService service = new RecordingResourceService();
		service.unpackEntryForTest("content/index.html", false);
		assertTrue(service.folderCalls.get() == 0);
		assertTrue(service.itemCalls.get() == 1);
	}

	private static final class RecordingResourceService extends AbstractResourceService
	{
		private final AtomicInteger folderCalls = new AtomicInteger();
		private final AtomicInteger itemCalls = new AtomicInteger();

		boolean shouldSkip(ZipEntry entry)
		{
			return shouldSkipEntry(entry);
		}

		void unpackEntryForTest(String entryName, boolean directory) throws ResourceStorageException
		{
			ZipEntry entry = new ZipEntry(entryName);
			unpackEntry("uuid", null, entry);
		}

		@Override
		protected String getContentPackageDirectoryPath(String uuid)
		{
			return uuid;
		}

		@Override
		protected String getRootDirectoryPath()
		{
			return "/";
		}

		@Override
		protected String newFolder(String uuid, ZipEntry entry) throws ResourceStorageException
		{
			folderCalls.incrementAndGet();
			return uuid;
		}

		@Override
		protected String newItem(String uuid, ZipInputStream zipStream, ZipEntry entry) throws ResourceStorageException
		{
			itemCalls.incrementAndGet();
			return uuid;
		}

		@Override
		public Archive getArchive(String resourceId) throws ResourceStorageException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public InputStream getArchiveStream(String resourceId) throws ResourceStorageException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int getMaximumUploadFileSize()
		{
			return 0;
		}

		@Override
		public List<ContentPackageResource> getResources(String archiveResourceId) throws ResourceStorageException
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
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeArchive(String resourceId)
		{
		}

		@Override
		public void removeResources(String collectionId) throws ResourceNotDeletedException
		{
			throw new UnsupportedOperationException();
		}
	}
}
