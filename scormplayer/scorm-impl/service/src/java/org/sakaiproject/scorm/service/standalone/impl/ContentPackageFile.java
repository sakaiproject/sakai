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
package org.sakaiproject.scorm.service.standalone.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.scorm.exceptions.ResourceNotFoundException;
import org.sakaiproject.scorm.model.api.ContentPackageResource;

@Slf4j
public class ContentPackageFile extends ContentPackageResource
{
	private static final long serialVersionUID = 1L;

	private String archiveResourceId;
	private File file;

	public ContentPackageFile(String archiveResourceId, String path, File file)
	{
		super(path);
		this.archiveResourceId = archiveResourceId;
		this.file = file;
	}

	@Override
	public InputStream getInputStream() throws ResourceNotFoundException
	{
		InputStream inputStream = null;

		try
		{
			inputStream = new FileInputStream(file);
		}
		catch (FileNotFoundException e)
		{
			log.error("Failed to retrieve file at {}", file, e);
			throw new ResourceNotFoundException(getPath());
		}

		return inputStream;
	}

	@Override
	public String getMimeType()
	{
		String mimeType = new MimetypesFileTypeMap().getContentType(file);

		if (file.getName().endsWith(".css"))
		{
			mimeType = "text/css";
		}
		else if (file.getName().endsWith(".swf"))
		{
			mimeType = "application/x-Shockwave-Flash";
		}

		return mimeType;
	}

	@Override
	public long getLastModified()
	{
		return file.lastModified();
	}

	@Override
	public long getLength()
	{
		return file.length();
	}
}
