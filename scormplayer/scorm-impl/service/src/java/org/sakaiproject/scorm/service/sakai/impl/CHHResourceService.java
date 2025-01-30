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
package org.sakaiproject.scorm.service.sakai.impl;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.scorm.content.impl.ScormCHH;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.tool.api.ToolManager;

@Slf4j
public abstract class CHHResourceService extends SakaiResourceService
{
	@Override
	protected abstract ServerConfigurationService configurationService();

	@Override
	protected abstract ContentHostingService contentService();

	@Override
	protected abstract ToolManager toolManager();

	protected abstract ScormCHH scormCHH();

	public String convertArchive(String resourceId)
	{
		try
		{
			ContentResourceEdit modify = this.contentService().editResource(resourceId);

			modify.setContentHandler(scormCHH());
			modify.setResourceType("org.sakaiproject.content.types.scormContentPackage");

			ResourcePropertiesEdit props = modify.getPropertiesEdit();
			props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, "org.sakaiproject.scorm.content.api.ScormCHH");

			int noti = NotificationService.NOTI_NONE;
			this.contentService().commitResource(modify, noti);
		}
		catch (Exception e)
		{
			log.error("Unable to convert archive to a Scorm content package", e);
		}

		return resourceId;
	}

	public ContentPackageResource getResource(String resourceId, String path)
	{
		String fullResourceId = new StringBuilder(resourceId).append("/").append(path).toString();
		try
		{
			ContentResource resource = this.contentService().getResource(fullResourceId);
			return new ContentPackageSakaiResource(path, resource);
		}
		catch (Exception e)
		{
			log.error("Failed to retrieve resource from content hosting {}", e);
		}

		return null;
	}

	public String getUrl(SessionBean sessionBean)
	{
		if (null != sessionBean.getLaunchData())
		{
			String launchLine = sessionBean.getLaunchData().getLaunchLine();
			String baseUrl = sessionBean.getBaseUrl();
			StringBuffer fullPath = new StringBuffer().append(baseUrl);

			if (!baseUrl.endsWith(Entity.SEPARATOR) && !launchLine.startsWith(Entity.SEPARATOR))
			{
				fullPath.append(Entity.SEPARATOR);
			}

			fullPath.append(launchLine);
			return fullPath.toString();
		}

		return null;
	}
}
