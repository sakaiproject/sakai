/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.search.api.SearchUtils;

/**
 * @author ieb
 */
@Slf4j
public class DefaultContentDigester implements ContentDigester
{
	private int maxDigestSize =  1024 * 1024 * 20;
	private Properties binaryTypes = null;

	public void init() {
		try
		{
		    binaryTypes = new Properties();
		    InputStream pi = getClass().getResourceAsStream("/org/sakaiproject/search/component/bundle/binarytypes.config");
			binaryTypes.load(pi);
		    pi.close();
		}
		catch (Exception e)
		{
			log.error("Cant find binary types file /org/sakaiproject/search/component/bundle/binarytypes.config in class path",e);
			System.exit(-1);
		}
	    
	}
	

	public String getContent(ContentResource contentResource)
	{
		try
		{
			ResourceProperties  rp  = contentResource.getProperties();
			StringBuilder sb = new StringBuilder();
			sb.append(rp.getProperty(ResourceProperties.PROP_DISPLAY_NAME)).append(" ");
			sb.append(rp.getProperty(ResourceProperties.PROP_DESCRIPTION)).append(" ");
			
			if ( !isBinary(contentResource) && contentResource.getContentLength() < maxDigestSize ) {
				try
				{
					SearchUtils.appendCleanString(new String(contentResource.getContent(),"UTF-8"), sb);
				}
				catch (Exception e)
				{
					log.debug(e.getMessage());
				}
			} 
			return sb.toString();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to get content", e);
		}
	}

	/**
	 * @param contentResource
	 * @return
	 */
	public boolean isBinary(ContentResource contentResource)
	{
		String mimeType = contentResource.getContentType();
		return "true".equals(binaryTypes.get(mimeType));
	}

	

	public boolean accept(String mimeType)
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.component.adapter.contenthosting.ContentDigester#getContentReader(org.sakaiproject.content.api.ContentResource)
	 */
	public Reader getContentReader(ContentResource contentResource)
	{
		return new StringReader(getContent(contentResource));
	}

	/**
	 * @return the maxDigestSize
	 */
	public int getMaxDigestSize()
	{
		return maxDigestSize;
	}

	/**
	 * @param maxDigestSize the maxDigestSize to set
	 */
	public void setMaxDigestSize(int maxDigestSize)
	{
		this.maxDigestSize = maxDigestSize;
	}

}
