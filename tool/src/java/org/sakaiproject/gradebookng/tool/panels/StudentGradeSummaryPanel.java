package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * 
 * Cell panel for the student grade summary
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentGradeSummaryPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private StudentGradeInfo gradeInfo;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	public StudentGradeSummaryPanel(String id, IModel<Map<String,String>> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,String> modelData = (Map<String,String>) this.getDefaultModelObject();
		String userId = modelData.get("userId");

		//build the grade matrix for the user then iterate over it
        final List<Assignment> assignments = this.businessService.getGradebookAssignments();
        
		//TODO catch if this is null, the get(0) will throw an exception
		this.gradeInfo = this.businessService.buildGradeMatrix(assignments, Collections.singletonList(userId)).get(0);
		
		//final ListDataProvider<StudentGradeInfo> studentGradeMatrix = new ListDataProvider<StudentGradeInfo>(grades);
        //List<IColumn> cols = new ArrayList<IColumn>();
		
		//add components
				System.out.println(gradeInfo.getStudentName());
				
	}
	
	
	
}
