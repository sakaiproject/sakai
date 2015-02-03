package org.sakaiproject.gradebookng.tool.panels;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.StudentSortOrder;
import org.sakaiproject.gradebookng.tool.model.StringModel;
import org.sakaiproject.gradebookng.tool.model.StudentName;

/**
 * 
 * Cell panel for the student name and eid
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameCellPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public StudentNameCellPanel(String id, String name, String eid) {
		super(id);
		
		//name
		add(new Label("name", new Model<String>(name)));
		
		//eid
		//TODO make this configurable
		add(new Label("eid", new Model<String>(eid)));

		
		
	}
	
	
	
}
