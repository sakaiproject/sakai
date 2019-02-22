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

import lombok.Getter;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.time.Time;
import org.apache.wicket.util.watch.IModifiable;

import org.sakaiproject.scorm.model.api.ContentPackageResource;

public class ContentPackageWebResource extends ResourceReference implements IModifiable
{
	private static final long serialVersionUID = 1L;

	private static final String[] candidateCompressionContentTypes = { "text/html", "text/javascript", "text/css"  };

	private ContentPackageResource resource;
	private ContentPackageResourceStream resourceStream;

	public ContentPackageWebResource(ContentPackageResource resource)
	{
		super(resource.getClass(), resource.getPath());
		this.resource = resource;
		this.resourceStream = new ContentPackageResourceStream(resource);
	}

	@Override
	public IResource getResource()
	{
		return new WicketContentPackageWebResource(this);
	}

	public IResourceStream getResourceStream()
	{
		if (canCompress())
		{
			return new CompressingContentPackageResourceStream(resource);
		}

		return resourceStream;
	}

	private boolean canCompress()
	{
		return isCandidateForCompression() && supportsCompression();
	}

	private boolean isCandidateForCompression()
	{
		String contentType = resourceStream.getContentType();
		if (contentType != null)
		{
			for( String candidateCompressionContentType : candidateCompressionContentTypes )
			{
				if( contentType.startsWith( candidateCompressionContentType ) )
				{
					return true;
				}
			}
		}

		return false;
	}

	private boolean supportsCompression()
	{
		if (RequestCycle.get() == null)
		{
			return false;
		}

		ServletWebRequest request = (ServletWebRequest) RequestCycle.get().getRequest();
		String s = request.getContainerRequest().getHeader("Accept-Encoding");
		if (s == null)
		{
			return false;
		}
		else
		{
			return s.contains( "gzip" );
		}
	}

	@Override
	public Time lastModifiedTime()
	{
		return resourceStream.lastModifiedTime();
	}

	public class WicketContentPackageWebResource implements IResource
	{
		@Getter private ContentPackageWebResource resource;

		public WicketContentPackageWebResource(ContentPackageWebResource resource)
		{
			this.resource = resource;
		}

		@Override
		public void respond( Attributes atrbts )
		{
		}
	}
}
