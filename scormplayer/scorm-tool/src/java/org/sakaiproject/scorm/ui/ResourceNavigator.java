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
package org.sakaiproject.scorm.ui;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.ScormResourceService;

public abstract class ResourceNavigator implements INavigable, Serializable {

	private static Log log = LogFactory.getLog(ResourceNavigator.class);
	
	protected abstract ScormResourceService resourceService();
	
	public abstract Object getApplication();
		
	public boolean useLocationRedirect() {
		return true;
	}
	
	public void displayResource(final SessionBean sessionBean, Object target) {
		if (null == sessionBean)
			return;
		
		if (sessionBean.isEnded() && target != null) {		
			((AjaxRequestTarget)target).appendJavascript("window.location.href='" + sessionBean.getCompletionUrl() + "';");
		}
		
		String url = getUrl(sessionBean);
		
		
		// Don't bother to display anything if a null url is returned. 
		if (null == url)
			return;

		if (log.isDebugEnabled())
			log.debug("Going to " + url);
		
		Component component = getFrameComponent();
		
		WebRequest webRequest = (WebRequest)component.getRequest();
		HttpServletRequest servletRequest = webRequest.getHttpServletRequest();

		String fullUrl = new StringBuilder(servletRequest.getContextPath()).append("/").append(url).toString();
		
		if (useLocationRedirect()) {
			component.add(new AttributeModifier("src", new Model(fullUrl)));
			
			if (target != null)
				((AjaxRequestTarget)target).addComponent(component);
		} else if (target != null) {
			// It's critical to the proper functioning of the tool that this logic be maintained for SjaxCall 
			// This is due to a bug in Firefox's handling of Javascript when an iframe has control of the XMLHttpRequest
			((AjaxRequestTarget)target).appendJavascript("parent.scormContent.location.href='" + fullUrl + "'");
		}

	}
	
	public String getUrl(final SessionBean sessionBean) {
		
		if (sessionBean.getLaunchData() == null)
			return null;
		
		String resourceId = sessionBean.getContentPackage().getResourceId();		
		String launchLine = sessionBean.getLaunchData().getLaunchLine();
		
		if (launchLine == null || launchLine.trim().length() == 0)
			return null;
		
		if (launchLine.startsWith("/"))
			launchLine = launchLine.substring(1);
		if (resourceId.startsWith("/"))
			resourceId = resourceId.substring(1);
		
		try {
	        launchLine = URLDecoder.decode(launchLine, "UTF-8");
        } catch (UnsupportedEncodingException e) {
	        // Very unlikely, but report anyway.
        	log.error("Error while URL decoding: '"+launchLine+"'", e);
        }
		/*StringBuilder nameBuilder = new StringBuilder(resourceId);
		
		if (!resourceId.endsWith("/") && !launchLine.startsWith("/")) 
			nameBuilder.append("/");
		
		nameBuilder.append(launchLine);
		
		String resourceName = nameBuilder.toString();*/
		
		String resourceName = resourceService().getResourcePath(resourceId, launchLine);
	
		
		
		//ResourceReference reference = new ResourceReference(PlayerPage.class, resourceName);
		
		//Resource r = reference.getResource();
		
		//String url = "org.sakaiproject.scorm.ui.player.pages.PlayerPage/" + resourceName;
			//RequestCycle.get().urlFor(reference).toString();
			
		//resourceService().getUrl(sessionBean);
		
		
		String url = null;
		
		if (launchLine != null) {
			ContentPackageResourceRequestTarget requestTarget = new ContentPackageResourceRequestTarget(resourceName);
			ContentPackageResourceMountStrategy strategy = new ContentPackageResourceMountStrategy("contentpackages");
			
			//String resourceKey = Application.get().getSharedResources().resourceKey(PlayerPage.class, resourceName, null, null);
			
			//String resourceKey = Application.get().getSharedResources().resourceKey(PlayerPage.class, resourceName, null, null);
			
			//ResourceReference reference = new ResourceReference(resourceKey);
			
			//url = RequestCycle.get().urlFor(reference).toString();
			
			//Resource r = Application.get().getSharedResources().resourceKey(PlayerPage.class, resourceName, locale, style)
			
			url = strategy.encode(requestTarget).toString();
			
			//url = "contentPackages/resourceName/" + resourceName;
		}
			
		
		return url;
	}
	
	
	public Component getFrameComponent() {
		return null;
	}

}
