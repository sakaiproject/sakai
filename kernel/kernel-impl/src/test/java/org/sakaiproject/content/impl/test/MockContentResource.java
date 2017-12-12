/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;

/**
 * MockContentResource
 *
 */
@Slf4j
public class MockContentResource extends MockContentEntity implements ContentResourceEdit
{
	public static final int STREAM_BUFFER_SIZE = 4096;

	protected String collectionId;
	protected byte[] content;

	protected long contentLength;
	protected String contentType;
	protected String resourceId;

	/**
	 * @param collectionId
	 * @param resourceId
	 */
	public MockContentResource(String collectionId, String resourceId)
	{
	    super();
	    this.collectionId = collectionId;
	    this.resourceId = resourceId;
	    //Setup this property for sorting purposes
	    this.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, resourceId); 
	    this.getPropertiesEdit().addProperty(ResourceProperties.PROP_CONTENT_LENGTH, "0"); 
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentResource#getContent()
	 */
	public byte[] getContent() throws ServerOverloadException
	{
		if(this.content == null)
		{
			throw new ServerOverloadException(" ServerOverloadException");
			
		}
		return this.content;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentResource#getContentLength()
	 */
	public long getContentLength()
	{
		return this.contentLength;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentResource#getContentType()
	 */
	public String getContentType()
	{
		return this.contentType;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentResourceEdit#setContent(byte[])
	 */
	public void setContent(byte[] content)
	{
		this.content = content;
		this.contentLength = this.content.length;

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentResourceEdit#setContent(java.io.InputStream)
	 */
	public void setContent(InputStream stream)
	{
		// Do not create the files for resources with zero length bodies
		if ((stream == null)) return;
   	
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		
		int byteCount = 0;

		// chunk
		byte[] chunk = new byte[STREAM_BUFFER_SIZE];
		int lenRead;
		try
        {
            while ((lenRead = stream.read(chunk)) != -1)
            {
            	bstream.write(chunk, 0, lenRead);
            	byteCount += lenRead;
            }
            
            this.contentLength = byteCount;
			resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, Long.toString(byteCount));
			
			this.content = bstream.toByteArray();
       }
        catch (IOException e)
        {
            log.warn("IOException ", e);
        }
        finally
        {
        	if(stream != null)
        	{
        		try
                {
                    stream.close();
                }
                catch (IOException e)
                {
                	log.warn("IOException ", e);
                }
        	}
        }


	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentResourceEdit#setContentLength(int)
	 */
	public void setContentLength(long length)
	{
		this.contentLength = length;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentResourceEdit#setContentType(java.lang.String)
	 */
	public void setContentType(String type)
	{
		this.contentType = type;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentResource#streamContent()
	 */
	public InputStream streamContent() throws ServerOverloadException
	{
		if(this.content == null)
		{
			throw new ServerOverloadException(" ServerOverloadException");
			
		}
		
		ByteArrayInputStream stream = new ByteArrayInputStream(this.content);
		
		return stream;
	}

}
