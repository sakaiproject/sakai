/**
 * Copyright (c) 2019 The Apereo Foundation
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
package org.sakaiproject.scorm.ui;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.HttpHeaderCollection;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PartWriterCallback;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;

import org.sakaiproject.scorm.service.sakai.impl.ContentPackageSakaiResource;
import org.sakaiproject.scorm.ui.player.util.CompressingContentPackageResourceStream;
import org.sakaiproject.scorm.ui.player.util.ContentPackageWebResource;
import org.sakaiproject.scorm.ui.player.util.ContentPackageWebResource.WicketContentPackageWebResource;

import static org.apache.wicket.request.resource.AbstractResource.CONTENT_RANGE_ENDBYTE;
import static org.apache.wicket.request.resource.AbstractResource.CONTENT_RANGE_STARTBYTE;

import static org.sakaiproject.scorm.api.ScormConstants.ROOT_DIRECTORY;

/**
 *
 * @author bjones86
 */
@Slf4j
public class ContentPackageResourceReference extends ResourceReference
{
    public ContentPackageResourceReference()
    {
        super( ContentPackageResourceReference.class, "contentPackages" );
    }

    @Override
    public IResource getResource()
    {
        return new ContentPackageResource();
    }

    public class ContentPackageResource extends AbstractResource
    {
        @Override
        protected ResourceResponse newResourceResponse( Attributes attributes )
        {
            StringBuilder b = new StringBuilder( ROOT_DIRECTORY + attributes.getParameters().get( "resourceID" ).toString() + "/" + attributes.getParameters().get( "resourceName" ).toString() );
            for( int i = 0; i < attributes.getParameters().getIndexedCount(); i++ )
            {
                if (attributes.getParameters().get( i ).toString().equals( "contentpackages"))
                {
                    break;
                }
                b.append( "/" ).append( attributes.getParameters().get( i ) );
            }
            String resourceName = b.toString();

            ContentPackageWebResource resource = ((WicketContentPackageWebResource) Application.get().getSharedResources()
                    .get( ContentPackageSakaiResource.class, resourceName, null, null, null, false ).getResource()).getResource();
            IResourceStream stream = resource.getResourceStream();

            try
            {
                long size = stream.length().bytes();
                final boolean compressed = stream instanceof CompressingContentPackageResourceStream;
                ResourceResponse resourceResponse = new ResourceResponse()
                {
                    @Override
                    public HttpHeaderCollection getHeaders()
                    {
                        HttpHeaderCollection headers = super.getHeaders();
                        if (compressed)
                        {
                            if (headers == null)
                            {
                                headers = new HttpHeaderCollection();
                            }
                            headers.addHeader("Content-Encoding", "gzip");
                        }
                        return headers;
                    }
                };
                resourceResponse.setContentLength( size );
                resourceResponse.setContentType( stream.getContentType() );
                resourceResponse.setTextEncoding( "utf-8" );
                resourceResponse.setAcceptRange( ContentRangeType.BYTES );
                resourceResponse.setFileName( resource.getName() );

                RequestCycle cycle = RequestCycle.get();
                Long startbyte = cycle.getMetaData( CONTENT_RANGE_STARTBYTE );
                Long endbyte = cycle.getMetaData( CONTENT_RANGE_ENDBYTE );
                resourceResponse.setWriteCallback( new PartWriterCallback( stream.getInputStream(), size, startbyte, endbyte ).setClose( true ) );
                return resourceResponse;
            }
            catch( Exception ex )
            {
                log.error( "Error returning response for stream", ex );
                throw new WicketRuntimeException( "An error occurred while processing the media resource response", ex );
            }
        }
    }
}
