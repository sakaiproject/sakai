package org.sakaiproject.gradebookng.tool.panels;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

public class SettingsCategoryPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GbSettings> model;	
	
	boolean isDropHighest = false;
	boolean isDropLowest = false;
	boolean isKeepHighest = false;
	
	public SettingsCategoryPanel(String id, IModel<GbSettings> model) {
		super(id, model);
		this.model = model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//get categories, passed in
		final List<CategoryDefinition> categories = this.model.getObject().getGradebookInformation().getCategories();
				
		//parse the categories and see if we have any drophighest/lowest/keep highest and set the flags for the checkboxes to use
		//also build a map that we can use to add/remove from
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
		RadioGroup<Integer> categoryType = new RadioGroup<>("categoryType", new PropertyModel<Integer>(model, "gradebookInformation.categoryType"));
		categoryType.add(new Radio<>("none", new Model<>(1)));
		categoryType.add(new Radio<>("categoriesOnly", new Model<>(2)));
		categoryType.add(new Radio<>("categoriesAndWeighting", new Model<>(3)));
		categoryType.setRequired(true);
		add(categoryType);

		//render category related form fields
		final WebMarkupContainer categoriesWrap = new WebMarkupContainer("categoriesWrap") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean isVisible() {
				//don't show if 'no categories'
				int categoryType = model.getObject().getGradebookInformation().getCategoryType();
				return (categoryType != 1);
			}
			
		};
		categoriesWrap.setOutputMarkupPlaceholderTag(true);
		
		//wrapper for the options
		final WebMarkupContainer categoriesOptionsWrap = new WebMarkupContainer("categoriesOptionsWrap");
		
		//enable drop highest
		final AjaxCheckBox dropHighest = new AjaxCheckBox("dropHighest", Model.of(isDropHighest)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				isDropHighest = this.getModelObject();
				
				//reset
				if(!isDropHighest) {
					for(CategoryDefinition c: model.getObject().getGradebookInformation().getCategories()) {
						c.setDropHighest(0);
					}
					target.appendJavaScript("$('.gb-category-drophighest').hide();");
				}
				
				target.add(categoriesWrap);
			}
		};

		dropHighest.setOutputMarkupId(true);
		categoriesOptionsWrap.add(dropHighest);

		//enable drop lowest
		final AjaxCheckBox dropLowest = new AjaxCheckBox("dropLowest", Model.of(isDropLowest)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				isDropLowest = this.getModelObject();
				
				//reset
				if(!isDropLowest) {
					for(CategoryDefinition c: model.getObject().getGradebookInformation().getCategories()) {
						c.setDrop_lowest(0);
					}
					target.appendJavaScript("$('.gb-category-droplowest').hide();");
				}
				
				target.add(categoriesWrap);
			}
		};
		dropLowest.setOutputMarkupId(true);
		categoriesOptionsWrap.add(dropLowest);

		//enable keep highest
		final AjaxCheckBox keepHighest = new AjaxCheckBox("keepHighest", Model.of(isKeepHighest)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				isKeepHighest = this.getModelObject();
				
				//reset
				if(!isKeepHighest) {
					for(CategoryDefinition c: model.getObject().getGradebookInformation().getCategories()) {
						c.setKeepHighest(0);
					}
					target.appendJavaScript("$('.gb-category-keephighest').hide();");
				}
				
				target.add(categoriesWrap);
			}
		};
		keepHighest.setOutputMarkupId(true);
		categoriesOptionsWrap.add(keepHighest);
		
		//add the options wrapper
		categoriesOptionsWrap.setOutputMarkupPlaceholderTag(true);
		categoriesWrap.add(categoriesOptionsWrap);

		//When category type changes, ensure form is updated to reflect new value
		categoryType.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				
				//adjust visibility of items depending on category type
				int categoryType = model.getObject().getGradebookInformation().getCategoryType();
				categoriesWrap.setVisible(categoryType != 1);
				categoriesOptionsWrap.setVisible(categoryType != 1);
				
				//if categories only (2), the categories table will be visible but the weighting column will not
				if(categoryType == 2) {
					target.appendJavaScript("$('.gb-category-weight').hide();");
				}
				
				target.add(categoriesWrap);
			}
		});

		final Label runningTotal = new Label("runningTotal", FormatHelper.formatDoubleAsPercentage(calculateCategoryWeightTotal(categories) * 100));
		runningTotal.setOutputMarkupId(true);
		categoriesWrap.add(runningTotal);

    	ListView<CategoryDefinition> categoriesView = new ListView<CategoryDefinition>("categoriesView", model.getObject().getGradebookInformation().getCategories()) {

  			private static final long serialVersionUID = 1L;

  			@Override
  			protected void populateItem(final ListItem<CategoryDefinition> item) {
  				
  				CategoryDefinition category = item.getModelObject();
  				
  				//name
  				TextField<String> name = new TextField<String>("name", new PropertyModel<String>(category, "name"));
  				item.add(name);
  				
  				//weight
  				TextField<Double> weight = new TextField<Double>("weight", new PropertyModel<Double>(category, "weight")) {
  					
  					@Override
					public <C> IConverter<C> getConverter(Class<C> type) {
					    return (IConverter<C>) new PercentConverter();
					}
  					
  				};
  				
  				//onchange, update the running total
				weight.add(new OnChangeAjaxBehavior() {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						Double newTotal = calculateCategoryWeightTotal(categories);

						if (newTotal.equals(new Double(1))) {
							runningTotal.add(new AttributeModifier("class", "text-success"));
						} else {
							runningTotal.add(new AttributeModifier("class", "text-danger"));
						}

						runningTotal.setDefaultModel(Model.of(FormatHelper.formatDoubleAsPercentage(newTotal * 100)));
						target.add(runningTotal);
					}
				});
  				item.add(weight);
  				
  				//num assignments
  				Label numItems = new Label("numItems", new StringResourceModel("settingspage.categories.items", null, new Object[] {category.getAssignmentList().size()}));
  				item.add(numItems);
  				
  				//extra credit
  		        final CheckBox extraCredit = new CheckBox("extraCredit", new PropertyModel<Boolean>(category, "extraCredit"));
  		        extraCredit.setOutputMarkupId(true);
  		        item.add(extraCredit);
  				
  				//drop highest
  				final TextField<Integer> categoryDropHighest = new TextField<Integer>("categoryDropHighest", new PropertyModel<Integer>(category, "dropHighest"));
  		        categoryDropHighest.setOutputMarkupId(true);
  		        item.add(categoryDropHighest);
  		        
  		        //drop lowest
  				final TextField<Integer> categoryDropLowest = new TextField<Integer>("categoryDropLowest", new PropertyModel<Integer>(category, "drop_lowest"));
  		        categoryDropLowest.setOutputMarkupId(true);
  		        item.add(categoryDropLowest);
  		        
  		     	//keep highest
  				final TextField<Integer> categoryKeepHighest = new TextField<Integer>("categoryKeepHighest", new PropertyModel<Integer>(category, "keepHighest"));
  		        categoryKeepHighest.setOutputMarkupId(true);
  		        item.add(categoryKeepHighest);
  				
  				//remove button
				AjaxButton remove = new AjaxButton("remove") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onSubmit(AjaxRequestTarget target, Form<?> form) {
						
						//remove this category from the model
						CategoryDefinition current = item.getModelObject();
						
						model.getObject().getGradebookInformation().getCategories().remove(current);
						target.add(categoriesWrap);
					}
				};
				remove.setDefaultFormProcessing(false);
				item.add(remove);
  			}
  			
  			@Override
  			public void renderHead(IHeaderResponse response) {
  			    super.renderHead(response);

  			    int categoryType = model.getObject().getGradebookInformation().getCategoryType();
  			    if(categoryType == 2) {
  			    	response.render(OnDomReadyHeaderItem.forScript("$('.gb-category-weight').hide();"));
  			    }
  			    
  			    if(!isDropHighest) {
  			    	response.render(OnDomReadyHeaderItem.forScript("$('.gb-category-drophighest').hide();"));
  			    }
  			    if(!isDropLowest) {
			    	response.render(OnDomReadyHeaderItem.forScript("$('.gb-category-droplowest').hide();"));
			    }
  			    if(!isKeepHighest) {
			    	response.render(OnDomReadyHeaderItem.forScript("$('.gb-category-keephighest').hide();"));
			    }
  			}
  		};
  		categoriesView.setReuseItems(true);
  		categoriesWrap.add(categoriesView);
  		categoriesWrap.setOutputMarkupId(true);
  		add(categoriesWrap);
  		
        
        //add category button
        AjaxButton addCategory = new AjaxButton("addCategory") {
			private static final long serialVersionUID = 1L;
        	
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
				
				//add a new empty category to the model
				CategoryDefinition cd = new CategoryDefinition();
				cd.setExtraCredit(false);
				cd.setWeight(new Double(0));
				cd.setAssignmentList(Collections.<Assignment> emptyList());
				
				model.getObject().getGradebookInformation().getCategories().add(cd);
				
				target.add(categoriesWrap);
			}
        };
        addCategory.setDefaultFormProcessing(false);
        categoriesWrap.add(addCategory);
        
        
		
	}

	private Double calculateCategoryWeightTotal(List<CategoryDefinition> categories) {
		Double total = new Double(0);
		for (CategoryDefinition categoryDefinition : categories) {
			
			Double weight = categoryDefinition.getWeight();
			if(weight == null) {
				weight = new Double(0);
			}
			
			if (!categoryDefinition.isExtraCredit()) {
				total += weight;
			}
		}
		return total;
	}
	
	/**
	 * Custom converter to flip between percentage for display and double for model object storage
	 * Trims off any trailing .0
	 *
	 */
	class PercentConverter implements IConverter<Double> {

		private static final long serialVersionUID = 1L;

		/**
		 * Back to object for the model.
		 * 
		 * this is persisted as a fraction between 0 and 1 so for two decimal place precision in the UI we need 4 here.
		 * 
		 * String will be a percentage, get it back to a decimal fraction.
		 */
		@Override
		public Double convertToObject(String value, Locale locale) throws ConversionException {
			
			//want this truncated to four decimal places, or less
			NumberFormat df = DecimalFormat.getInstance();
			df.setMinimumFractionDigits(0);
			df.setMaximumFractionDigits(4);
			df.setRoundingMode(RoundingMode.DOWN);
			
			//convert
			Double d;
			try {
				d = Double.valueOf(value) / 100;
			} catch (NumberFormatException e) {
				throw new ConversionException(e);
			}

			//to string for the rounding/truncation
			String s = df.format(d);
			
			//back to double
			return Double.valueOf(s);
		}

		/**
		 * Convert to percentage for display
		 * 
		 * Double will be a decimal fraction between 0 and 1 inclusive.
		 */
		@Override
		public String convertToString(Double value, Locale locale) {
			
			//set the decimal precision
			NumberFormat df = DecimalFormat.getInstance();
			df.setMinimumFractionDigits(0);
			df.setMaximumFractionDigits(2);
			df.setRoundingMode(RoundingMode.DOWN);
			
			//convert to percentage representation
			value = value * 100;
			
			return df.format(value);
		}
		
	}
}
