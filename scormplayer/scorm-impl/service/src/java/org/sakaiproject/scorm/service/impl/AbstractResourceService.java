/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.service.impl;

import java.io.IOException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.MimetypesFileTypeMap;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.scorm.exceptions.InvalidArchiveException;
import org.sakaiproject.scorm.exceptions.ResourceStorageException;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.service.api.ScormResourceService;

@Slf4j
public abstract class AbstractResourceService implements ScormResourceService
{
	@Override
	public String convertArchive(String resourceId, String title) throws InvalidArchiveException, ResourceStorageException
	{
		return unpack(resourceId);
	}

	protected abstract String getContentPackageDirectoryPath(String uuid);
	protected abstract String getRootDirectoryPath();
	protected abstract String newFolder(String uuid, ZipEntry entry) throws ResourceStorageException;
	protected abstract String newItem(String uuid, ZipInputStream zipStream, ZipEntry entry) throws ResourceStorageException;

	protected String getMimeType(String name)
	{
		String mimeType = new MimetypesFileTypeMap().getContentType(name);

		if (name.endsWith(".css;charset=UTF-8"))
		{
			mimeType = "text/css";
		}
		else if (name.endsWith(".swf"))
		{
			mimeType = "application/x-Shockwave-Flash";
		}
		else if (name.endsWith(".js"))
		{
			mimeType = "text/javascript;charset=UTF-8";
		}

		return mimeType;
	}

	@Override
	public String getResourcePath(String resourceId, String launchLine)
	{
		StringBuilder pathBuilder = new StringBuilder();

		if (launchLine.startsWith("/"))
		{
			launchLine = launchLine.substring(1);
		}

		pathBuilder.append(getContentPackageDirectoryPath(resourceId));
		pathBuilder.append(launchLine);
		return pathBuilder.toString().replace(" ", "%20");
	}

	protected boolean isValidArchive(String mimeType)
	{
		return (mimeType != null && (mimeType.startsWith("application/zip") || mimeType.startsWith("application/x-download")));
	}

	protected String stripSuffix(String name)
	{
		int indexOf = name.lastIndexOf('.');
		if (indexOf == -1)
		{
			return name;
		}

		return name.substring(0, indexOf);
	}

	protected String unpack(String resourceId) throws InvalidArchiveException, ResourceStorageException
	{
		Archive archive = getArchive(resourceId);
		String uuid = UUID.randomUUID().toString();

		if (!isValidArchive(archive.getMimeType()))
		{
			log.warn("Invalid mime type = {}", archive.getMimeType());
		}

		if (true)
		{
			unzip(uuid, new ZipInputStream(getArchiveStream(resourceId)));
		}
		else
		{
			throw new InvalidArchiveException(archive.getMimeType());
		}

		return uuid;
	}

	protected void unpackEntry(String parentPath, ZipInputStream stream, ZipEntry entry) throws ResourceStorageException
	{
		if (entry.isDirectory())
		{
			newFolder(parentPath, entry);
		}
		else
		{
			newItem(parentPath, stream, entry);
		}
	}

	protected void unzip(String parentPath, ZipInputStream zipStream) throws ResourceStorageException
	{
		ZipEntry entry;
		try
		{
			entry = zipStream.getNextEntry();
			while (entry != null)
			{
				unpackEntry(parentPath, zipStream, entry);
				entry = zipStream.getNextEntry();
			}
		}
		catch (IOException ioe)
		{
			log.error("Caught an io exception reading from zip stream", ioe);
		}
		finally
		{
			try
			{
				if (null != zipStream)
				{
					zipStream.close();
				}
			}
			catch (IOException noie)
			{
				log.info("Caught an io exception closing streams!", noie);
			}
		}
	}
}
