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
		final List<CategoryDefinition> categories = this.businessService.getGradebookCategories();
		
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
		categoryType.add(new Radio<>("categoriesAndWeighting", new Model<>(2)));
		add(categoryType);
		
		//category settings
		WebMarkupContainer categorySettings = new WebMarkupContainer("categorySettings");
		
		//drop highest
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
        
        //drop lowest
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
        
     	//keep highest
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
        
        //categories
        final ListDataProvider<CategoryDefinition> categoryProvider = new ListDataProvider<CategoryDefinition>(categories);
        List<IColumn> cols = new ArrayList<IColumn>();
        
        //category name column
        @SuppressWarnings({ "rawtypes", "unchecked" })
        AbstractColumn categoryNameColumn = new AbstractColumn(new Model("Category")){
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				CategoryDefinition category = (CategoryDefinition) rowModel.getObject();
				cellItem.add(new Label(componentId, category.getName()));
			}
        };
        cols.add(categoryNameColumn);
        
        //category % column
        @SuppressWarnings({ "rawtypes", "unchecked" })
		AbstractColumn categoryPercentColumn = new AbstractColumn(new Model("%")){
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				CategoryDefinition category = (CategoryDefinition) rowModel.getObject();
				cellItem.add(new Label(componentId, category.getWeight()));
			}
        };
        cols.add(categoryPercentColumn);
        
        //category extra credit column
        @SuppressWarnings({ "rawtypes", "unchecked" })
		AbstractColumn categoryExtraCreditColumn = new AbstractColumn(new Model("Extra Credit")){
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				CategoryDefinition category = (CategoryDefinition) rowModel.getObject();
				cellItem.add(new Label(componentId, category.isExtraCredit()));
			}
        };
        cols.add(categoryExtraCreditColumn);
        
        //category items column
        @SuppressWarnings({ "rawtypes", "unchecked" })
		AbstractColumn categoryItemsColumn = new AbstractColumn(new Model("Gradebook Items")){
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				CategoryDefinition category = (CategoryDefinition) rowModel.getObject();
				cellItem.add(new Label(componentId, category.getAssignmentList().size()));
			}
        };
        cols.add(categoryItemsColumn);
        
        //category drop highest column
    	@SuppressWarnings({ "rawtypes", "unchecked" })
		AbstractColumn categoryDropHighestColumn = new AbstractColumn(new ResourceModel("settingspage.categories.drophighestcolhead")){
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				CategoryDefinition category = (CategoryDefinition) rowModel.getObject();
				cellItem.add(new Label(componentId, category.getDropHighest()));
			}
        };
        cols.add(categoryDropHighestColumn);
        
        //category drop lowest column
        @SuppressWarnings({ "rawtypes", "unchecked" })
		AbstractColumn categoryDropLowestColumn = new AbstractColumn(new ResourceModel("settingspage.categories.droplowestcolhead")){
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				CategoryDefinition category = (CategoryDefinition) rowModel.getObject();
				cellItem.add(new Label(componentId, category.getDrop_lowest()));
			}
        };
        cols.add(categoryDropLowestColumn);
        
        
        //category keep highest column
        @SuppressWarnings({ "rawtypes", "unchecked" })
		AbstractColumn categoryKeepHighestColumn = new AbstractColumn(new ResourceModel("settingspage.categories.keephighestcolhead")){
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				CategoryDefinition category = (CategoryDefinition) rowModel.getObject();
				cellItem.add(new Label(componentId, category.getKeepHighest()));
			}
        };
        cols.add(categoryKeepHighestColumn);
        
        //category remove column
        @SuppressWarnings({ "rawtypes", "unchecked" })
		AbstractColumn categoryRemoveColumn = new AbstractColumn(new Model("Remove")){
			private static final long serialVersionUID = 1L;
			@Override
			public void populateItem(final Item cellItem, String componentId, IModel rowModel) {
				final CategoryDefinition category = (CategoryDefinition) rowModel.getObject();				
				AjaxButton removeCategory = new AjaxButton(componentId) {
					private static final long serialVersionUID = 1L;
					protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
						categories.remove(category);
						target.add(table);
					}
				};
				removeCategory.setModel(new ResourceModel("button.remove"));
				removeCategory.add(new AttributeModifier("class", "btn btn-link"));
				removeCategory.setOutputMarkupId(true);
				removeCategory.setDefaultFormProcessing(false);
				cellItem.add(removeCategory);
			}
        };
        cols.add(categoryRemoveColumn);
        
        //table
        table = new DataTable("categories", cols, categoryProvider, 100);
        table.addTopToolbar(new HeadersToolbar(table, null));
        table.setOutputMarkupId(true);
        add(table);
        
        
        //render table of categories
        final WebMarkupContainer categoriesContainer = new WebMarkupContainer("categoriesContainer");
    	ListView<CategoryDefinition> categoriesView = new ListView<CategoryDefinition>("categories2", categories) {

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
  				
  				//remove button
				AjaxButton remove = new AjaxButton("remove") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onSubmit(AjaxRequestTarget target, Form<?> form) {
						
						//remove current item
						CategoryDefinition current = item.getModelObject();
						categories.remove(current);
						
						target.add(categoriesContainer);
					}
				};
				remove.setDefaultFormProcessing(false);
				item.add(remove);
  				
  			}
  		};
  		categoriesContainer.setOutputMarkupId(true);
  		categoriesContainer.add(categoriesView);
  		add(categoriesContainer);
        
        //add category button
        AjaxButton addCategory = new AjaxButton("addCategory") {
			private static final long serialVersionUID = 1L;
        	
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
				
				CategoryDefinition cd = new CategoryDefinition();
				cd.setAssignmentList(Collections.<Assignment> emptyList());
				categories.add(cd);
				target.add(categoriesContainer);
			}
        };
        addCategory.setDefaultFormProcessing(false);
        add(addCategory);
		
	}
	
	
	
}
