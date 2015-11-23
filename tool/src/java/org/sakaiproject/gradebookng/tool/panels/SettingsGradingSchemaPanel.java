package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.model.GbGradingSchemaEntry;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;
import org.sakaiproject.tool.gradebook.GradingScale;

public class SettingsGradingSchemaPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GradebookInformation> model;
	
	WebMarkupContainer schemaWrap;

	public SettingsGradingSchemaPanel(String id, IModel<GradebookInformation> model) {
		super(id, model);
		this.model = model;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//get all known scales
		List<GradingScaleDefinition> gradingScales = this.businessService.getGradingScales();
		
		//get current one
		String selectedGradingScaleUid = this.model.getObject().getSelectedGradingScaleUid();

		//create map of grading scales
		final Map<String, String> gradingScaleMap = new LinkedHashMap<>();
        for (GradingScaleDefinition gradingScale : gradingScales) {
        	gradingScaleMap.put(gradingScale.getUid(), gradingScale.getName());
        }
		
		//grading scale type chooser
		List<String> gradingSchemaList = new ArrayList<String>(gradingScaleMap.keySet());
		DropDownChoice<String> typeChooser = new DropDownChoice<String>("type", new PropertyModel<String>(model, "gradeScale"), gradingSchemaList, new ChoiceRenderer<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String gradingScaleUid) {
                return gradingScaleMap.get(gradingScaleUid);
            }

			@Override
            public String getIdValue(String gradingScaleUid, int index) {
                return gradingScaleUid;
            }
        });
		
		//TODO add ajax listener onto this so we can repaint just the table underneath with the DEFAULT set, but if its changed back to the configured mappings in gradebookinformation, use that preferentially
        
		typeChooser.setNullValid(false);
		typeChooser.setModelObject(selectedGradingScaleUid);
		add(typeChooser);
		
		
		
		IModel<List<GbGradingSchemaEntry>> gradingSchemaEntriesModel = new LoadableDetachableModel<List<GbGradingSchemaEntry>>() {
	        @Override
	        protected List<GbGradingSchemaEntry> load() {
	            
	        	//TODO this needs to get the grading scale UID and if it is not the one that is
	        	// currently in use, get the default bottom percents from the gradebook service
	        	// if it is, the use the configured ones for this gradebook
	        	
	        	
	        	//get the bottom percents from the configured grading scale in THIS gradebook, not the defaults
	        	//convert map into list of objects which is easier to work with in the views
	    		Map<String,Double> bottomPercents = model.getObject().getSelectedGradingScaleBottomPercents();

	    		List<GbGradingSchemaEntry> rval = new ArrayList<>();
	    		for(Map.Entry<String, Double> entry: bottomPercents.entrySet()) {
	    			rval.add(new GbGradingSchemaEntry(entry.getKey(), entry.getValue()));
	    		}
	        	
	    		return rval;
	        }
	    };
		
		//render the grading schema table
		schemaWrap = new WebMarkupContainer("schemaWrap");
    	ListView<GbGradingSchemaEntry> schemaView = new ListView<GbGradingSchemaEntry>("schemaView", gradingSchemaEntriesModel) {

  			private static final long serialVersionUID = 1L;

  			@Override
  			protected void populateItem(final ListItem<GbGradingSchemaEntry> item) {
  				System.out.println("asdf");
  				
  				GbGradingSchemaEntry entry = item.getModelObject();
  				
  				//grade
  				Label grade = new Label("grade", new PropertyModel<String>(entry, "grade"));
  				item.add(grade);
  				
  				//minpercent
  				TextField<Double> minPercent = new TextField<Double>("minPercent", new PropertyModel<Double>(entry, "minPercent"));
  				item.add(minPercent);
  
  			}
  		};
  		schemaWrap.add(schemaView);
  		schemaWrap.setOutputMarkupId(true);
  		add(schemaWrap);
		
  		
  		//handle updates on the schema type chooser, to repaint the table
  		typeChooser.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(schemaWrap);
			}
  		});
	}
}
