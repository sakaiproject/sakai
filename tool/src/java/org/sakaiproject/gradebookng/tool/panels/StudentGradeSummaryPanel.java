package org.sakaiproject.gradebookng.tool.panels;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 
 * Cell panel for the student grade summary
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentGradeSummaryPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private ModalWindow window;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	public StudentGradeSummaryPanel(String id, IModel<Map<String,Object>> model, ModalWindow window) {
		super(id, model);
		
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		Map<String,Object> modelData = (Map<String,Object>) this.getDefaultModelObject();
		String eid = (String) modelData.get("eid");
		String displayName = (String) modelData.get("displayName");

		//done button
		add(new AjaxLink<Void>("done") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target){
				window.close(target);
			}
		});

		add(new StudentGradeSummaryGradesPanel("instructorView", (IModel<Map<String,Object>>) getDefaultModel(), StudentGradeSummaryGradesPanel.VIEW.INSTRUCTOR));
		add(new StudentGradeSummaryGradesPanel("studentView", (IModel<Map<String,Object>>) getDefaultModel(), StudentGradeSummaryGradesPanel.VIEW.STUDENT));

		add(new Label("heading", new StringResourceModel("heading.studentsummary", null, new Object[]{ displayName, eid })));
	}

	/**
	 * Format a due date
	 * 
	 * @param date
	 * @return
	 */
	private String formatDueDate(Date date) {
		//TODO locale formatting via ResourceLoader
		
		if(date == null) {
			return getString("label.studentsummary.noduedate");
		}
		
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    	return df.format(date);
	}

}
