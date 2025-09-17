/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class AjaxBootstrapTabbedPanel<T extends ITab> extends AjaxTabbedPanel<T> {

	private static final long serialVersionUID = 1L;

	public AjaxBootstrapTabbedPanel(final String id, final List<T> tabs) {
		super(id, tabs);
	}

	public AjaxBootstrapTabbedPanel(final String id, final List<T> tabs, final IModel<Integer> model) {
		super(id, tabs, model);
	}



	@Override
	protected String getTabContainerCssClass() {
		return "navIntraTool actionToolbar";
	}
	
	@Override
	protected WebMarkupContainer newLink(final String linkId, final int index) {
		WebMarkupContainer link = super.newLink(linkId, index);
		
		if (index == getSelectedTab()) {
			link.add(new AttributeAppender("class", Model.of("current"), " "));
		}
		
		return link;
	}
}