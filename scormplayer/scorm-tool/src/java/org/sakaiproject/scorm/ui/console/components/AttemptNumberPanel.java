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
package org.sakaiproject.scorm.ui.console.components;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;

import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;

public class AttemptNumberPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private final RepeatingView attemptNumberLinks;
	@Getter @Setter private long attemptNumber;

	public AttemptNumberPanel(String id, long numberOfAttempts, Class<?> pageClass, PageParameters pageParams)
	{
		super(id);

		this.attemptNumberLinks = new RepeatingView("attemptNumberLinks");
		add(attemptNumberLinks);

		attemptNumber = -1;

		for (long i = 1; i <= numberOfAttempts; i++)
		{
			this.addAttemptNumberLink(i, pageClass, pageParams, attemptNumberLinks, attemptNumber);
		}
	}

	
	/*
	 * Copied the basic organization of this method from an Apache Wicket class
	 * 	org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
	 * originally authored by Igor Vaynberg (ivaynberg)
	 */
	protected void addAttemptNumberLink(long i, Class<?> pageClass, PageParameters params, RepeatingView container, long current)
	{
		params.set("attemptNumber", i);

		BookmarkablePageLabeledLink link = new BookmarkablePageLabeledLink("attemptNumberLink", new Model("" + i), pageClass, params);

		if (i == current)
		{
			link.setEnabled(false);
		}

		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(link);

		container.add(item);
	}
}
