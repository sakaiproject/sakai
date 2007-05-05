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

package org.sakaiproject.scorm.client.pages;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.scorm.client.Clock;
import org.sakaiproject.scorm.client.ScormTool;
import org.sakaiproject.scorm.client.api.ScormClientFacade;

import wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import wicket.markup.html.BodyContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.ExternalLink;
import wicket.markup.html.link.Link;
import wicket.util.time.Duration;

public class Index extends WebPage 
{
	private static final String BODY_ONLOAD_START="setMainFrameHeight('Main";
	private static final String BODY_ONLOAD_END="');setFocus(focus_path);";
		
	/**
	 * Constructor.
	 */
	public Index()
	{
		ScormClientFacade clientService = ((ScormTool)getApplication()).getClientFacade();
		String placementId = ""; //clientService.getPlacementId();
		
		String bodyOnloadText = new StringBuffer().append(BODY_ONLOAD_START).append(placementId).append(BODY_ONLOAD_END).toString();
		if (null != getBodyContainer())
			getBodyContainer().addOnLoadModifier(bodyOnloadText, this);
		
		// add the clock component
		Clock clock = new Clock("clock", TimeZone.getTimeZone("America/Los_Angeles"));
		add(clock);
		clock.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)));		
	
		String userName = clientService.getUserName();
		
		add(new Label("user", userName));
	
		ResourceToolActionPipe pipe = clientService.getResourceToolActionPipe();		
	
		String rtLabel = "";
		
		if (null != pipe) {
			byte[] contentBuffer = pipe.getContent();
			
			//pipe.setRevisedResourceProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, org.sakaiproject.scorm.client.api.ScormClientService.REFERENCE_ROOT);

			//rtLabel = (String)pipe.getRevisedResourceProperties().get(ContentHostingService.PROP_ALTERNATE_REFERENCE);
			
			ContentResource contentResource = (ContentResource)pipe.getContentEntity();
			
			//clientService.grantAlternativeRef(contentResource.getId());
			
			add(new ExternalLink("resourceUrl", contentResource.getUrl()));
			
			try {
		        StringBuffer rtLabelBuffer = new StringBuffer();
		        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(contentBuffer));
				ZipEntry zipe;
				while( (zipe = zip.getNextEntry()) != null )
				{
					rtLabelBuffer.append(zipe.getName()).append(" ");
				}
		        rtLabel = rtLabelBuffer.toString();
				zip.close();
			} catch (IOException e) {
		          System.err.println(e);     
		    }
			
			clientService.closePipe(pipe);
			
		}
		add(new Label("resourceLabel", rtLabel));	
	}
	
	
	
}
