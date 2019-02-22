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
package org.sakaiproject.scorm.ui.player.components;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.PackageResourceReference;

import org.sakaiproject.scorm.model.api.SessionBean;

public class CommunicationPanel extends Panel
{
	public static final PackageResourceReference API = new PackageResourceReference(CommunicationPanel.class, "res/API.js");
	public static final PackageResourceReference API_WRAPPER = new PackageResourceReference(CommunicationPanel.class, "res/APIWrapper.js");

	private static final long serialVersionUID = 1L;

	public CommunicationPanel(String id, final SessionBean sessionBean, final LaunchPanel launchPanel)
	{
		super(id);

		SjaxContainer container = new SjaxContainer("sjaxContainer", sessionBean, launchPanel);
		add(container);
	}

	protected String getFirstArg(List<String> argumentValues)
	{
		if (CollectionUtils.isEmpty(argumentValues))
		{
			return "";
		}

		return argumentValues.get(0);
	}

	protected String getSecondArg(List<String> argumentValues)
	{
		if (null == argumentValues || argumentValues.size() <= 1)
		{
			return "";
		}

		return argumentValues.get(1);
	}
}
