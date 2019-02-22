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

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.scorm.model.api.CMIData;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.wicket.ajax.markup.html.table.SakaiDataTable;

public class CMIDataGraph extends Panel
{
	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormResultService")
	ScormResultService resultService;

	public CMIDataGraph(String id, List<CMIData> cmiData)
	{
		super(id);

		List<IColumn> columns = new LinkedList<>();
		IModel descriptionHeader = new ResourceModel("column.header.description");
		IModel nameHeader = new ResourceModel("column.header.name");
		IModel valueHeader = new ResourceModel("column.header.value");

		columns.add(new PropertyColumn(descriptionHeader, "description", "description"));
		columns.add(new PropertyColumn(nameHeader, "fieldName", "fieldName"));
		columns.add(new PropertyColumn(valueHeader, "fieldValue", "fieldValue"));

		SakaiDataTable table = new SakaiDataTable("attemptTable", columns, new CMIDataProvider(cmiData), true);
		add(table);

		this.setOutputMarkupId(true);
	}
}
