package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.model.GradeInfo;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;

/**
 * The panel for the cell of
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradeItemCellPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public GradeItemCellPanel(String id, Long assignmentId, StudentGradeInfo studentGrades) {
		super(id);
		
		//TODO harden this
		GradeInfo gradeInfo = studentGrades.getGrades().get(assignmentId);
		
		add(new Label("grade", new Model(gradeInfo.getGrade())));
		
		//TODO since we are using a custom column panel here we still need this to be editable
		
		
		//menu
		

	}

}
