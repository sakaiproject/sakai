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
package org.sakaiproject.scorm.ui;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.util.string.AppendingStringBuffer;

import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.ScormResourceService;

@Slf4j
public abstract class ResourceNavigator implements INavigable, Serializable
{
	private static final long serialVersionUID = 1L;

	protected abstract ScormResourceService resourceService();

	public boolean useLocationRedirect()
	{
		return true;
	}

	@Override
	public void displayResource(final SessionBean sessionBean, Object target)
	{
		if (null == sessionBean)
		{
			return;
		}

		if (sessionBean.isEnded() && target != null)
		{
			((AjaxRequestTarget) target).appendJavaScript("window.location.href='" + sessionBean.getCompletionUrl() + "';initResizing();");
		}

		String url = getUrl(sessionBean);

		// Don't bother to display anything if a null url is returned. 
		if (null == url)
		{
			return;
		}

		log.debug("Going to {}", url);

		Component component = getFrameComponent();
		ServletWebRequest webRequest = (ServletWebRequest) component.getRequest();
		HttpServletRequest servletRequest = webRequest.getContainerRequest();
		String fullUrl = new StringBuilder(servletRequest.getContextPath()).append("/").append(url).toString();

		if (useLocationRedirect())
		{
			component.add(new AttributeModifier("src", new Model(fullUrl)));

			if (target != null)
			{
				((AjaxRequestTarget) target).add(component);
				((AjaxRequestTarget) target).appendJavaScript("initResizing();");
			}
		}
		else if (target != null)
		{
			// It's critical to the proper functioning of the tool that this logic be maintained for SjaxCall 
			// This is due to a bug in Firefox's handling of Javascript when an iframe has control of the XMLHttpRequest
			((AjaxRequestTarget) target).appendJavaScript("parent.scormContent.location.href='" + fullUrl + "';initResizing();");
		}
	}

	public String getUrl(final SessionBean sessionBean)
	{
		if (sessionBean.getLaunchData() == null)
		{
			return null;
		}

		String resourceId = sessionBean.getContentPackage().getResourceId();
		String launchLine = sessionBean.getLaunchData().getLaunchLine();

		if (StringUtils.isBlank(launchLine))
		{
			return null;
		}

		if (launchLine.startsWith("/"))
		{
			launchLine = launchLine.substring(1);
		}
		if (resourceId.startsWith("/"))
		{
			resourceId = resourceId.substring(1);
		}

		try
		{
			launchLine = URLDecoder.decode(launchLine, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			// Very unlikely, but report anyway.
			log.error("Error while URL decoding: '{}'", launchLine, e);
		}

		String resourceName = resourceService().getResourcePath(resourceId, launchLine);
		final AppendingStringBuffer url = new AppendingStringBuffer(40);

		if (launchLine != null)
		{
			url.append("contentpackages");
			if (StringUtils.isNotBlank(resourceName))
			{
				if (!url.endsWith("/"))
				{
					url.append("/");
				}

				try
				{
					resourceName = URLDecoder.decode(resourceName, "UTF-8");
				}
				catch (UnsupportedEncodingException e)
				{
					// Very unlikely, but report anyway.
					log.error("Error while URL decoding: '{}'", resourceName, e);
				}

				url.append("resourceName");
				if (!resourceName.startsWith("/"))
				{
					url.append("/");
				}

				url.append(resourceName);
			}

			if (log.isDebugEnabled())
			{
				log.debug("encode -----------> URL: {}", url);
			}
		}

		return url.toString();
	}

	public Component getFrameComponent()
	{
		return null;
	}
}
