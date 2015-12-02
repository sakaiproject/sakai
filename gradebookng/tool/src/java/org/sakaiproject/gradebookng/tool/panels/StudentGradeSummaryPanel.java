package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		this.setOutputMarkupId(true);
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
				target.appendJavaScript("GradebookGradeSummaryUtils.clearBlur();");
			}
		});

		final WebMarkupContainer studentNavigation = new WebMarkupContainer("studentNavigation");
		studentNavigation.setOutputMarkupPlaceholderTag(true);
		add (studentNavigation);
		
		List tabs=new ArrayList();

		tabs.add(new AbstractTab(new Model<String>(getString("label.studentsummary.instructorviewtab"))) {
			public Panel getPanel(String panelId) { return new InstructorGradeSummaryGradesPanel(panelId, (IModel<Map<String,Object>>) getDefaultModel()); }
		});
		tabs.add(new AbstractTab(new Model<String>(getString("label.studentsummary.studentviewtab"))) {
			public Panel getPanel(String panelId) { return new StudentGradeSummaryGradesPanel(panelId, (IModel<Map<String,Object>>) getDefaultModel()); }
		});

		add(new AjaxBootstrapTabbedPanel("tabs", tabs) {
			@Override
			protected String getTabContainerCssClass() {
				return "nav nav-pills";
			}

			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				super.onAjaxUpdate(target);

				boolean showingInstructorView = (getSelectedTab() == 0);
				boolean showingStudentView = (getSelectedTab() == 1);

				studentNavigation.setVisible(showingInstructorView);
				target.add(studentNavigation);

				target.appendJavaScript("new GradebookGradeSummary($(\"#"+getParent().getMarkupId()+"\"), " + showingStudentView + ");");
			}
		});

		add(new Label("heading", new StringResourceModel("heading.studentsummary", null, new Object[]{ displayName, eid })));
	}

}
