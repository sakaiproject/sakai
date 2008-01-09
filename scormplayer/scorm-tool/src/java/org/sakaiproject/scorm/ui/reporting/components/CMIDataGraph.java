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
import org.sakaiproject.wicket.markup.html.repeater.data.table.BasicDataTable;

public class CMIDataGraph extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean
	ScormResultService resultService;
	
	public CMIDataGraph(String id, List<CMIData> cmiData) {
		super(id);

		List<IColumn> columns = new LinkedList<IColumn>();
		
		IModel descriptionHeader = new ResourceModel("column.header.description");
		IModel nameHeader = new ResourceModel("column.header.name");
		IModel valueHeader = new ResourceModel("column.header.value");
		
		columns.add(new PropertyColumn(descriptionHeader, "description", "description"));
		columns.add(new PropertyColumn(nameHeader, "fieldName", "fieldName"));
		columns.add(new PropertyColumn(valueHeader, "fieldValue", "fieldValue"));
		
		BasicDataTable table = new BasicDataTable("attemptTable", columns, cmiData);
		add(table);
		
		this.setOutputMarkupId(true);
	}
	
}
