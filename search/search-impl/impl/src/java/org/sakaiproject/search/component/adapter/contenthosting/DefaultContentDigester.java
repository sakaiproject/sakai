/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.search.api.SearchUtils;

/**
 * @author ieb
 */
public class DefaultContentDigester implements ContentDigester
{
	private static final Log log = LogFactory
			.getLog(DefaultContentDigester.class);
	private static final int MAX_DIGEST_SIZE =  1024 * 1024 * 5;
	private Properties binaryTypes = null;
	
	public void init() {
		try
		{
		    binaryTypes = new Properties();
		    InputStream pi = getClass().getResourceAsStream("/org/sakaiproject/search/component/bundle/binarytypes.properties");
			binaryTypes.load(pi);
		    pi.close();
		}
		catch (Exception e)
		{
			log.error("Cant find binary types file /org/sakaiproject/search/component/bundle/binarytypes.properties in class path",e);
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
			
			if ( !isBinary(contentResource) && contentResource.getContentLength() < MAX_DIGEST_SIZE ) {
				try
				{
					SearchUtils.appendCleanString(new String(contentResource.getContent(),"UTF-8"), sb);
				}
				catch (Exception e)
				{
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




}
