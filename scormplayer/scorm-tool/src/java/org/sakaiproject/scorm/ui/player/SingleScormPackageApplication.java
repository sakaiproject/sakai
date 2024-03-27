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
package org.sakaiproject.scorm.ui.player;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.ui.console.pages.DisplayDesignatedPackage;
import org.sakaiproject.scorm.ui.console.pages.NotConfiguredPage;
import org.sakaiproject.tool.api.ToolManager;

@Slf4j
public class SingleScormPackageApplication extends ScormWebApplication
{
	@Override
	public Class<? extends Page> getHomePage()
	{
		ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class);
		Properties cfgPlacement = toolManager.getCurrentPlacement().getPlacementConfig();
		String resourceId = cfgPlacement.getProperty(DisplayDesignatedPackage.CFG_PACKAGE_NAME);
		if (StringUtils.isNotEmpty(resourceId))
		{
			ScormContentService scormContentService = (ScormContentService) ComponentManager.get(ScormContentService.class);
			ContentPackage contentPackage = scormContentService.getContentPackageByResourceId(resourceId);
			if (contentPackage != null)
			{
				return DisplayDesignatedPackage.class;
			}
		}

		return NotConfiguredPage.class;
	}
}
