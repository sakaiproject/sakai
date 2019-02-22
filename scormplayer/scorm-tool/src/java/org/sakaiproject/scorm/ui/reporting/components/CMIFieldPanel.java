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
package org.sakaiproject.scorm.ui.reporting.components;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;

import org.sakaiproject.scorm.model.api.CMIField;

public class CMIFieldPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private final RepeatingView children;

	public CMIFieldPanel(String id, CMIField field, String value)
	{
		super(id);

		add(new Label("description", new Model(field.getDescription())));
		add(new Label("fieldValue", new Model(value)));
		add(children = new RepeatingView("children"));

		if (field.isParent())
		{
			for (CMIField child : field.getChildren())
			{
				for (String fieldValue : field.getFieldValues())
				{
					addChild(child, fieldValue, children);
				}
			}
		}
	}

	/*
	 * Copied the basic organization of this method from an Apache Wicket class
	 * 	org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
	 * originally authored by Igor Vaynberg (ivaynberg)
	 */
	private void addChild(CMIField field, String value, RepeatingView container)
	{
		CMIFieldPanel fieldComponent = new CMIFieldPanel("child", field, value);
		fieldComponent.setRenderBodyOnly(true);

		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(fieldComponent);

		container.add(item);
	}
}
