package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;

public class SettingsCategoryPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GradebookInformation> model;

	public SettingsCategoryPanel(String id, IModel<GradebookInformation> model) {
		super(id, model);
		this.model = model;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//get categories
		List<CategoryDefinition> categories = this.businessService.getGradebookCategories();
		
		//parse the categories and see if we have any drophighest/lowest/keep highest and set some flags for the checkboxes to use
		boolean isDropHighest = false;
		boolean isDropLowest = false;
		boolean isKeepHighest = false;
		for(CategoryDefinition category: categories) {
			if(category.getDropHighest() != null) {
				isDropHighest = true;
			}
			if(category.getDrop_lowest() != null) {
				isDropLowest = true;
			}
			if(category.getKeepHighest() != null) {
				isKeepHighest = true;
			}
		}
		
		
		
		//category types
		RadioGroup<Integer> categoryType = new RadioGroup<>("categoryType", new PropertyModel<Integer>(model, "categoryType"));
		categoryType.add(new Radio<>("none", new Model<>(1)));
		categoryType.add(new Radio<>("categoriesOnly", new Model<>(2)));
		categoryType.add(new Radio<>("categoriesAndWeighting", new Model<>(2)));
		add(categoryType);
		
		//category settings
		WebMarkupContainer categorySettings = new WebMarkupContainer("categorySettings");
		
		//drop highest
        final AjaxCheckBox dropHighest = new AjaxCheckBox("dropHighest", Model.of(isDropHighest)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        dropHighest.setOutputMarkupId(true);
        categorySettings.add(dropHighest);
        
        //drop lowest
        final AjaxCheckBox dropLowest = new AjaxCheckBox("dropLowest", Model.of(isDropLowest)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        dropLowest.setOutputMarkupId(true);
        categorySettings.add(dropLowest);
        
     	//keep highest
        final AjaxCheckBox keepHighest = new AjaxCheckBox("keepHighest", Model.of(isKeepHighest)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//nothing required
			}
        };
        keepHighest.setOutputMarkupId(true);
        categorySettings.add(keepHighest);
        
        add(categorySettings);
		
	}
}
