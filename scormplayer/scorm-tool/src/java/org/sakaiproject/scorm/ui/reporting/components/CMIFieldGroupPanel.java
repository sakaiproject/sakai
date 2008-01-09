/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.ui.reporting.components;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.sakaiproject.scorm.model.api.CMIField;
import org.sakaiproject.scorm.model.api.CMIFieldGroup;

public class CMIFieldGroupPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final RepeatingView fields;
	
	public CMIFieldGroupPanel(String id, CMIFieldGroup fieldGroup) {
		super(id);

		add(fields = new RepeatingView("fields"));
		
		List<CMIField> list = fieldGroup.getList();
		
		for (CMIField field : list) {
			addField(field, fields);
		}
		
	}

	
	/*
	 * Copied the basic organization of this method from an Apache Wicket class
	 * 	org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
	 * originally authored by Igor Vaynberg (ivaynberg)
	 */
	private void addField(CMIField field, RepeatingView container)
	{
		CMIFieldPanel fieldComponent = new CMIFieldPanel("field", field, field.getFieldValue());
		fieldComponent.setRenderBodyOnly(true);

		WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
		item.setRenderBodyOnly(true);
		item.add(fieldComponent);

		container.add(item);
	}
	
	
	
}
