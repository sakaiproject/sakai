/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.scorm.ui.player.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.sakaiproject.scorm.exceptions.ResourceNotFoundException;
import org.sakaiproject.scorm.model.api.ContentPackageResource;

public class ContentPackageResourceStream implements IResourceStream {

	private static final long serialVersionUID = 1L;
	
	private static Log log = LogFactory.getLog(ContentPackageResourceStream.class);
	
	private ContentPackageResource resource;
	private InputStream in;
	private Locale locale;
	
	public ContentPackageResourceStream(ContentPackageResource resource) {
		this.resource = resource;
	}
	
	public void close() throws IOException {
		if (in != null)
			in.close();
	}

	public String getContentType() {
		return resource.getMimeType();
	}

	public InputStream getInputStream() throws ResourceStreamNotFoundException {
		try {
			in = resource.getInputStream();
			
			if (in == null)
				throw new ResourceNotFoundException(resource.getPath());
				
		} catch (ResourceNotFoundException rnfe) {
			log.error("Could not return input stream for resource: " + resource.getPath());
			throw new ResourceStreamNotFoundException("The requested resource was not found: " + resource.getPath());
		}
		
		return in;
	}

	public long length() {
		return resource.getLength();
	}
	
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public Time lastModifiedTime() {
		return Time.milliseconds(resource.getLastModified());
	}

}
