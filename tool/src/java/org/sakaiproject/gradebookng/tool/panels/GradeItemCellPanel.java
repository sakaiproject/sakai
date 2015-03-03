package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GradeInfo;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;

/**
 * The panel for the cell of a grade item
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradeItemCellPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	//todo wrap this in a model as per http://wicket.apache.org/guide/guide/bestpractices.html#bestpractices_6
	public GradeItemCellPanel(String id, final Long assignmentId, final StudentGradeInfo studentGrades) {
		super(id);
		
		//TODO harden this
		GradeInfo gradeInfo = studentGrades.getGrades().get(assignmentId);
						
		AjaxEditableLabel<String> grade = new AjaxEditableLabel<String>("grade", new Model<String>(gradeInfo.getGrade())) {
			
			private static final long serialVersionUID = 1L;
			

			@Override
			protected String defaultNullLabel() {
				return "-"; //TODO this is temporary, they need something to click
			}
			
			@Override
			protected void onSubmit(final AjaxRequestTarget target) {
				super.onSubmit(target);
				String newGrade = this.getEditor().getValue();
				
				
				
				System.out.println("newGrade: " + newGrade);
				
				boolean result = businessService.saveGrade(assignmentId, studentGrades.getStudentUuid(), newGrade);
				System.out.println("result: " + result);

			}
			
			@Override
			protected void updateLabelAjaxAttributes(AjaxRequestAttributes attributes) {
				//when switching from editor to label
				//attributes.getExtraParameters();
				
			}
			
			@Override
			protected void updateEditorAjaxAttributes(AjaxRequestAttributes attributes) {
				//when switching from label to editor
				//attributes.getExtraParameters();
			}
			
			
		};
		
		
		add(grade);
		
		//TODO since we are using a custom column panel here we still need this to be editable
		
		//menu
		

	}

}
