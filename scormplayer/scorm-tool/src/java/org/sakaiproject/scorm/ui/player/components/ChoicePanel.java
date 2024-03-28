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

import org.adl.sequencer.SeqNavRequests;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.sakaiproject.scorm.ui.player.pages.ScormPlayerPage;

public class ChoicePanel extends Panel
{
	private static final long serialVersionUID = 1L;

	public ChoicePanel(String id, long contentPackageId, String resourceId, String error)
	{
		super(id);

		add(new Label("error", error));

		addChoice("navStart", SeqNavRequests.NAV_START, contentPackageId, resourceId);
		addChoice("navAbandon", SeqNavRequests.NAV_ABANDON, contentPackageId, resourceId);
		addChoice("navAbandonAll", SeqNavRequests.NAV_ABANDONALL, contentPackageId, resourceId);
		addChoice("navNone", SeqNavRequests.NAV_NONE, contentPackageId, resourceId);
		addChoice("navResumeAll", SeqNavRequests.NAV_RESUMEALL, contentPackageId, resourceId);
		addChoice("navExit", SeqNavRequests.NAV_EXIT, contentPackageId, resourceId);
		addChoice("navExitAll", SeqNavRequests.NAV_EXITALL, contentPackageId, resourceId);
	}

	private void addChoice(String requestId, int request, long id, String resourceId)
	{
		final PageParameters params = new PageParameters();
		params.add("contentPackageId", "" + id);
		params.add("resourceId", resourceId);
		params.add("navRequest", "" + request);

		add(new BookmarkablePageLink(requestId, ScormPlayerPage.class, params));
	}
}
