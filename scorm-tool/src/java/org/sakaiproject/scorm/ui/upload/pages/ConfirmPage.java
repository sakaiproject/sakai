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
package org.sakaiproject.scorm.ui.upload.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.model.api.Archive;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.console.pages.ConsoleBasePage;

public class ConfirmPage extends ConsoleBasePage
{
	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormResourceService")
	ScormResourceService resourceService;

	public ConfirmPage(PageParameters params)
	{
		final String resourceId = params.get("resourceId").toString();
		final int status = params.get("status").toInt();
		Archive archive = resourceService.getArchive(resourceId);

		if (archive != null)
		{
			setDefaultModel(new CompoundPropertyModel(archive));
		}
	
		info(getNotification(status));
		add(new Label("title"));
	}

	private String getNotification(int status)
	{
		String resultKey = getKey(status);
		return getLocalizer().getString(resultKey, this);
	}

	private String getKey(int status)
	{
		switch (status)
		{
			case ScormConstants.VALIDATION_SUCCESS:
				return "validate.success";
			case ScormConstants.VALIDATION_WRONGMIMETYPE:
				return "validate.wrong.mime.type";
			case ScormConstants.VALIDATION_NOFILE:
				return "validate.no.file";
			case ScormConstants.VALIDATION_NOMANIFEST:
				return "validate.no.manifest";
			case ScormConstants.VALIDATION_NOTWELLFORMED:
				return "validate.not.well.formed";
			case ScormConstants.VALIDATION_NOTVALIDROOT:
				return "validate.not.valid.root";
			case ScormConstants.VALIDATION_NOTVALIDSCHEMA:
				return "validate.not.valid.schema";
			case ScormConstants.VALIDATION_NOTVALIDPROFILE:
				return "validate.not.valid.profile";
			case ScormConstants.VALIDATION_MISSINGREQUIREDFILES:
				return "validate.missing.files";
		};

		return "validate.failed";
	}
}
