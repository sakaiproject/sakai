package org.sakaiproject.gradebookng.tool.panels;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbStudentSortType;
import org.sakaiproject.gradebookng.tool.model.StringModel;

/**
 * 
 * Header panel for the student name/eid
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

  @SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
  protected GradebookNgBusinessService businessService;

	public StudentNameColumnHeaderPanel(String id, GbStudentSortType currentSortOrder) {
		super(id);
		
		//title
		add(new Label("title", new ResourceModel("column.header.students")));
		
		//get list of sort orders
		List<GbStudentSortType> sortOrders = Arrays.asList(GbStudentSortType.values());
		
		//TODO use the list to render the dropdown, changing the text as appropriate
	}
}
