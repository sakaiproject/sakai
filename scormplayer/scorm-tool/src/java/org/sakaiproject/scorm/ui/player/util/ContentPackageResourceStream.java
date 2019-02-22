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
package org.sakaiproject.scorm.ui.player.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;

import org.sakaiproject.scorm.exceptions.ResourceNotFoundException;
import org.sakaiproject.scorm.model.api.ContentPackageResource;

@Slf4j
public class ContentPackageResourceStream implements IResourceStream
{
	private static final long serialVersionUID = 1L;

	private ContentPackageResource resource;
	private InputStream in;
	@Getter @Setter private Locale locale;

	public ContentPackageResourceStream(ContentPackageResource resource)
	{
		this.resource = resource;
	}

	@Override
	public void close() throws IOException
	{
		if (in != null)
		{
			in.close();
		}
	}

	@Override
	public String getContentType()
	{
		return resource.getMimeType();
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		try
		{
			in = resource.getInputStream();
			if (in == null)
			{
				throw new ResourceNotFoundException(resource.getPath());
			}
		}
		catch (ResourceNotFoundException rnfe)
		{
			log.error("Could not return input stream for resource: {}", resource.getPath());
			throw new ResourceStreamNotFoundException("The requested resource was not found: " + resource.getPath());
		}

		return in;
	}

	@Override
	public String getStyle()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public void setStyle( String style )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public String getVariation()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public void setVariation( String variation )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public Bytes length()
	{
		return Bytes.bytes(resource.getLength());
	}

	@Override
	public Time lastModifiedTime()
	{
		return Time.millis(resource.getLastModified());
	}
}
