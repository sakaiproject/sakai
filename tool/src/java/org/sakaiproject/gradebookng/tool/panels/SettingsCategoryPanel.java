package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;

public class SettingsCategoryPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GradebookInformation> model;
	
	DataTable table;
	
	
	@Setter @Getter
	boolean isDropHighest = false;
	
	@Setter @Getter
	boolean isDropLowest = false;
	
	@Setter @Getter
	boolean isKeepHighest = false;

	public SettingsCategoryPanel(String id, IModel<GradebookInformation> model) {
		super(id, model);
		this.model = model;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//get categories
		final List<CategoryDefinition> categories = this.model.getObject().getCategories();
		
		//parse the categories and see if we have any drophighest/lowest/keep highest and set the flags for the checkboxes to use
		for(CategoryDefinition category: categories) {
			if(category.getDropHighest() != null && category.getDropHighest() > 0) {
				isDropHighest = true;
			}
			if(category.getDrop_lowest() != null && category.getDrop_lowest() > 0) {
				isDropLowest = true;
			}
			if(category.getKeepHighest() != null && category.getKeepHighest() > 0) {
				isKeepHighest = true;
			}
		}		
		
		//category types
		RadioGroup<Integer> categoryType = new RadioGroup<>("categoryType", new PropertyModel<Integer>(model, "categoryType"));
		categoryType.add(new Radio<>("none", new Model<>(1)));
		categoryType.add(new Radio<>("categoriesOnly", new Model<>(2)));
		categoryType.add(new Radio<>("categoriesAndWeighting", new Model<>(3)));
		
		categoryType.setRequired(true);
		add(categoryType);
		
		//category settings
		WebMarkupContainer categorySettings = new WebMarkupContainer("categorySettings");
		
		//enable drop highest
        final AjaxCheckBox dropHighest = new AjaxCheckBox("dropHighest", Model.of(isDropHighest())) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setDropHighest(this.getModelObject());
				target.add(table);
			}
        };
        dropHighest.setOutputMarkupId(true);
        categorySettings.add(dropHighest);
        
        //enable drop lowest
        final AjaxCheckBox dropLowest = new AjaxCheckBox("dropLowest", Model.of(isDropLowest())) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setDropLowest(this.getModelObject());
				target.add(table);
			}
        };
        dropLowest.setOutputMarkupId(true);
        categorySettings.add(dropLowest);
        
     	//enable keep highest
        final AjaxCheckBox keepHighest = new AjaxCheckBox("keepHighest", Model.of(isKeepHighest())) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setKeepHighest(this.getModelObject());
				target.add(table);
			}
        };
        keepHighest.setOutputMarkupId(true);
        categorySettings.add(keepHighest);
        
        add(categorySettings);
        
        //render table of categories
    	ListView<CategoryDefinition> categoriesView = new ListView<CategoryDefinition>("categoriesView", categories) {

  			private static final long serialVersionUID = 1L;

  			@Override
  			protected void populateItem(final ListItem<CategoryDefinition> item) {
  				
  				CategoryDefinition category = item.getModelObject();
  				
  				//name
  				TextField<String> name = new TextField<String>("name", new PropertyModel<String>(category, "name"));
  				item.add(name);
  				
  				//weight
  				TextField<Double> weight = new TextField<Double>("weight", new PropertyModel<Double>(category, "weight"));
  				item.add(weight);
  				
  				//num assignments
  				Label numItems = new Label("numItems", new StringResourceModel("settingspage.categories.items", null, new Object[] {category.getAssignmentList().size()}));
  				item.add(numItems);
  				
  				//extra credit
  		        final CheckBox extraCredit = new CheckBox("extraCredit", new PropertyModel<Boolean>(category, "extraCredit"));
  		        extraCredit.setOutputMarkupId(true);
  		        item.add(extraCredit);
  				
  				//drop highest
  		        final CheckBox categoryDropHighest = new CheckBox("categoryDropHighest", new PropertyModel<Boolean>(category, "dropHighest"));
  		        categoryDropHighest.setOutputMarkupId(true);
  		        item.add(categoryDropHighest);
  		        
  		        //drop lowest
  		        final CheckBox categoryDropLowest = new CheckBox("categoryDropLowest", new PropertyModel<Boolean>(category, "drop_lowest"));
  		        categoryDropLowest.setOutputMarkupId(true);
  		        item.add(categoryDropLowest);
  		        
  		     	//keep highest
  		        final CheckBox categoryKeepHighest = new CheckBox("categoryKeepHighest", new PropertyModel<Boolean>(category, "keepHighest"));
  		        categoryKeepHighest.setOutputMarkupId(true);
  		        item.add(categoryKeepHighest);
  				
  				//remove button
				AjaxButton remove = new AjaxButton("remove") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onSubmit(AjaxRequestTarget target, Form<?> form) {
						
						//remove current item
						CategoryDefinition current = item.getModelObject();
						categories.remove(current);
					}
				};
				remove.setDefaultFormProcessing(false);
				item.add(remove);
  				
  			}
  		};
  		add(categoriesView);
        
        //add category button
        AjaxButton addCategory = new AjaxButton("addCategory") {
			private static final long serialVersionUID = 1L;
        	
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
				
				CategoryDefinition cd = new CategoryDefinition();
				cd.setAssignmentList(Collections.<Assignment> emptyList());
				categories.add(cd);
			}
        };
        addCategory.setDefaultFormProcessing(false);
        add(addCategory);
		
	}
	
	
	
}
