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

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.util.file.Folder;

import org.sakaiproject.scorm.ui.console.pages.PackageListPage;

@Slf4j
public class ScormTool extends ScormWebApplication
{
	@Override
	public Class getHomePage()
	{
		return PackageListPage.class;
	}

	public Folder getUploadFolder()
	{
		Folder folder = new Folder(System.getProperty("java.io.tmpdir"), "scorm-uploads");

		// Make sure that this directory exists.
		if (!folder.exists())
		{
			if (!folder.mkdirs())
			{
				log.error("Cannot create temp dir: {}", folder);
			}
		}

		return folder;
	}
}
