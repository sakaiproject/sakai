package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GradeInfo;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * 
 * Panel for the modal window that allows an instructor to update the ungraded items
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class UpdateUngradedItemsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
		
	private ModalWindow window;
	
	private IModel<Long> model;
	
	//private transient GradeOverride override;
		
	private static final double DEFAULT_GRADE = 0;

	public UpdateUngradedItemsPanel(String id, IModel<Long> model, ModalWindow window) {
		super(id);
		this.model = model;
		this.window = window;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		final Long assignmentId = this.model.getObject();
		
		//form model
		GradeOverride override = new GradeOverride();
		override.setGrade(DEFAULT_GRADE);
		
		CompoundPropertyModel<GradeOverride> formModel = new CompoundPropertyModel<GradeOverride>(override);
		
		//build form
		Form<GradeOverride> form = new Form<GradeOverride>("form", formModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				GradeOverride override = this.getModelObject();
				
				//TODO validation of the fields here
				
				//TODO send an ajax request to do the work and add a throbber
				//close the window when it's finished.
				//if(businessService.updateUngradedItems(assignmentId, override.getGrade())) {
					//GradebookPage rval = new GradebookPage();
					//rval.info(getString("message.updateungradeditems.success"));
					//setResponsePage(rval);
				//} else {
					//error(getString("message.edititem.error"));
				//}
				
			}
		};
		
		//Button cancel = new Button("cancel") {
		//	private static final long serialVersionUID = 1L;
		//	@Override
		//	public void onSubmit() {
				//window.close(target);
		//	}
		//};
		//cancel.setDefaultFormProcessing(false);
        //form.add(cancel);
		
		form.add(new TextField<Double>("grade").setRequired(true));

		add(form);
	}
	
	/**
	 * Model for this form
	 */
	
	class GradeOverride implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		@Getter
		@Setter
		private double grade;
		
	}
	
	
	
}
