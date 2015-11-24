package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbGradingSchemaEntry;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;

public class SettingsGradingSchemaPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GbSettings> model;
	
	WebMarkupContainer schemaWrap;
	
	/**
	 * This is the currently PERSISTED grading scale that is persisted for this gradebook
	 */
	String configuredGradingScaleUid;
	
	/**
	 * This is the currently SELECTED grading scale, from the dropdown
	 */
	String currentGradingScaleUid;

	public SettingsGradingSchemaPanel(String id, IModel<GbSettings> model) {
		super(id, model);
		this.model = model;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//get all known scales
		final List<GradingScaleDefinition> gradingScales = this.businessService.getGradingScales();
		
		//get current one
		configuredGradingScaleUid = this.model.getObject().getGradebookInformation().getSelectedGradingScaleUid();

		//set the value for the dropdown
		currentGradingScaleUid = configuredGradingScaleUid;
		
		//create map of grading scales to use for the dropdown
		final Map<String, String> gradingScaleMap = new LinkedHashMap<>();
        for (GradingScaleDefinition gradingScale : gradingScales) {
        	gradingScaleMap.put(gradingScale.getUid(), gradingScale.getName());
        }
        		
		//grading scale type chooser
		List<String> gradingSchemaList = new ArrayList<String>(gradingScaleMap.keySet());
		final DropDownChoice<String> typeChooser = new DropDownChoice<String>("type", new PropertyModel<String>(model, "gradebookInformation.gradeScale"), gradingSchemaList, new ChoiceRenderer<String>() {
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
		typeChooser.setNullValid(false);
		typeChooser.setModelObject(currentGradingScaleUid);
		add(typeChooser);
		
		
		// wrap the bottom percent values in a model that can be refetch on each call
		IModel<List<GbGradingSchemaEntry>> gradingSchemaEntriesModel = new LoadableDetachableModel<List<GbGradingSchemaEntry>>() {
	        @Override
	        protected List<GbGradingSchemaEntry> load() {
	            
	        	//get configured values or defaults
	        	//need to retain insertion order
	        	Map<String,Double> bottomPercents = new LinkedHashMap<>();
	        	
	        	if(StringUtils.equals(currentGradingScaleUid, configuredGradingScaleUid)) {
	        		
	        		//get the values from the configured grading scale in this gradebook and sort accordingly
	        		bottomPercents = sortBottomPercents(configuredGradingScaleUid, model.getObject().getGradebookInformation().getSelectedGradingScaleBottomPercents());
	        		

	        		
	        	} else {
	        		//get the default values for the chosen grading scale and sort accordingly
	        		for (GradingScaleDefinition gradingScale : gradingScales) {
	        			
	        			if(StringUtils.equals(currentGradingScaleUid, gradingScale.getUid())) {
	    	        		bottomPercents = sortBottomPercents(currentGradingScaleUid, gradingScale.getDefaultBottomPercents());
	        			}
	        		}
	        	}
	        	
	        	//convert map into list of objects which is easier to work with in the views 
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
  				
  				GbGradingSchemaEntry entry = item.getModelObject();
  				
  				//grade
  				Label grade = new Label("grade", new PropertyModel<String>(entry, "grade"));
  				item.add(grade);
  				
  				//minpercent
  				TextField<Double> minPercent = new TextField<Double>("minPercent", new PropertyModel<Double>(entry, "minPercent"));
  				
  				//if grade is F, set disabled
  				if(StringUtils.equals(entry.getGrade(), "F")) {
  					minPercent.setEnabled(false);
  				}
  				
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
				
				//set current selection and call for a repaint
				currentGradingScaleUid = (String) typeChooser.getDefaultModelObject();
				target.add(schemaWrap);
			}
  		});
	}
	
	/**
	 * Helper to sort the bottom percents maps. Caters for both letter grade and P/NP types
	 * @param uid UID of the grading schema so we know how to sort
	 * @param percents
	 * @return
	 */
	private Map<String,Double> sortBottomPercents(String uid, Map<String,Double> percents) {
		
		Map<String, Double> rval = null;
				
		if(StringUtils.equals(uid, "PassNotPassMapping")) {
			rval = new TreeMap<>(Collections.reverseOrder()); //P before NP.
		} else {
			rval = new TreeMap<>(new LetterGradeComparator()); //letter grade mappings
		}
		rval.putAll(percents);
				
		return rval;
	}
	
}


/**
 * Comparator to ensure correct ordering of letter grades, catering for + and - in the grade
 * Copied from GradebookService and made Serializable as we use it in a TreeMap
 * Also has the fix from SAK-30094.
 */
class LetterGradeComparator implements Comparator<String>, Serializable {

	private static final long serialVersionUID = 1L;

	public int compare(String o1, String o2){
		if(o1.toLowerCase().charAt(0) == o2.toLowerCase().charAt(0)) {
			if(o1.length() == 2 && o2.length() == 2) {
				if(o1.charAt(1) == '+') {
					return 0;
				} else {
					return 1;
				}
			}
			if(o1.length() == 1 && o2.length() == 2) {
				if(o2.charAt(1) == '+') {
					return 1;
				} else {
					return 0;
				}
			}
			if(o1.length() == 2 && o2.length() == 1) {
				if(o1.charAt(1) == '+') {
					return -1;
				} else {
					return 1;
				}
			}
			return 0;
		}
		else {
			return o1.toLowerCase().compareTo(o2.toLowerCase());
		}
	}
};
