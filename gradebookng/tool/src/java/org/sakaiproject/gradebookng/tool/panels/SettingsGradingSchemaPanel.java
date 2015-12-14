package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbGradingSchemaEntry;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.service.gradebook.shared.GradeMappingDefinition;

public class SettingsGradingSchemaPanel extends Panel implements IFormModelUpdateListener {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GbSettings> model;
	
	WebMarkupContainer schemaWrap;
	ListView<GbGradingSchemaEntry> schemaView;
	List<GradeMappingDefinition> gradeMappings;
	
	/**
	 * This is the currently PERSISTED grade mapping id that is persisted for this gradebook
	 */
	String configuredGradeMappingId;
	
	/**
	 * This is the currently SELECTED grade mapping, from the dropdown
	 */
	String currentGradeMappingId;

	public SettingsGradingSchemaPanel(String id, IModel<GbSettings> model) {
		super(id, model);
		this.model = model;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//get all mappings available for this gradebook
		gradeMappings = this.model.getObject().getGradebookInformation().getGradeMappings();
		
		//get current one
		configuredGradeMappingId = this.model.getObject().getGradebookInformation().getSelectedGradeMappingId();
		
		//set the value for the dropdown
		currentGradeMappingId = configuredGradeMappingId;
				
		//setup the grading scale schema entries
		model.getObject().setGradingSchemaEntries(setupGradingSchemaEntries());
		
		//create map of grading scales to use for the dropdown
		final Map<String, String> gradeMappingMap = new LinkedHashMap<>();
        for (GradeMappingDefinition gradeMapping : this.gradeMappings) {
        	gradeMappingMap.put(gradeMapping.getId(), gradeMapping.getName());
        }
        		
		//grading scale type chooser
		List<String> gradingSchemaList = new ArrayList<String>(gradeMappingMap.keySet());
		final DropDownChoice<String> typeChooser = new DropDownChoice<String>("type", new PropertyModel<String>(model, "gradebookInformation.selectedGradeMappingId"), gradingSchemaList, new ChoiceRenderer<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(String gradeMappingId) {
                return gradeMappingMap.get(gradeMappingId);
            }

			@Override
            public String getIdValue(String gradeMappingId, int index) {
                return gradeMappingId;
            }
        });        
		typeChooser.setNullValid(false);
		typeChooser.setModelObject(currentGradeMappingId);
		add(typeChooser);
		
		//render the grading schema table
		schemaWrap = new WebMarkupContainer("schemaWrap");
    	schemaView = new ListView<GbGradingSchemaEntry>("schemaView", new PropertyModel<List<GbGradingSchemaEntry>>(model, "gradingSchemaEntries")) {

  			private static final long serialVersionUID = 1L;

  			@Override
  			protected void populateItem(final ListItem<GbGradingSchemaEntry> item) {
  				
  				GbGradingSchemaEntry entry = item.getModelObject();
  				
  				//grade
  				Label grade = new Label("grade", new PropertyModel<String>(entry, "grade"));
  				item.add(grade);
  				
  				//minpercent
  				TextField<Double> minPercent = new TextField<Double>("minPercent", new PropertyModel<Double>(entry, "minPercent"));
  				
  				//if grade is F or NP, set disabled
  				if(ArrayUtils.contains(new String[]{"F", "NP"}, entry.getGrade())) {
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
				
				//set current selection
				currentGradeMappingId = (String) typeChooser.getDefaultModelObject();
								
				//refresh data
	    		model.getObject().setGradingSchemaEntries(setupGradingSchemaEntries());
				
	    		//repaint
				target.add(schemaWrap);
			}
  		});
	}
	
	/**
	 * Helper to sort the bottom percents maps. Caters for both letter grade and P/NP types
	 * @param gradingScaleName name of the grading schema so we know how to sort.
	 * @param percents
	 * @return
	 */
	private Map<String,Double> sortBottomPercents(String gradingScaleName, Map<String,Double> percents) {
		
		Map<String, Double> rval = null;
						
		if(StringUtils.equals(gradingScaleName, "Pass / Not Pass")) {
			rval = new TreeMap<>(Collections.reverseOrder()); //P before NP.
		} else {
			rval = new TreeMap<>(new LetterGradeComparator()); //letter grade mappings
		}
		rval.putAll(percents);
				
		return rval;
	}

	/**
	 * Sync up the custom list we are using for the list view, back into the GrdebookInformation object
	 */
	@Override
	public void updateModel() {
		
		List<GbGradingSchemaEntry> schemaEntries = schemaView.getModelObject();
		
		Map<String, Double> bottomPercents = new HashMap<>();
		for(GbGradingSchemaEntry schemaEntry: schemaEntries) {
			bottomPercents.put(schemaEntry.getGrade(), schemaEntry.getMinPercent());
		}
		
		model.getObject().getGradebookInformation().setSelectedGradingScaleBottomPercents(bottomPercents);
	}
	
	/**
	 * Helper to setup the applicable grading schema entries, depending on current state
	 * @return
	 */
	private List<GbGradingSchemaEntry> setupGradingSchemaEntries() {
				
		//get configured values or defaults
    	//need to retain insertion order
    	Map<String,Double> bottomPercents = new LinkedHashMap<>();
    	
    	//note that we sort based on name so we need to pull the right name out	of the list of mappings, for both cases
		String gradingSchemaName = this.gradeMappings.stream()
				.filter(gradeMapping -> StringUtils.equals(gradeMapping.getId(), currentGradeMappingId))
				.findFirst()
				.get()
				.getName();
		
		if(StringUtils.equals(currentGradeMappingId, configuredGradeMappingId)) {
    		//get the values from the configured grading scale in this gradebook and sort accordingly
    		bottomPercents = sortBottomPercents(gradingSchemaName, model.getObject().getGradebookInformation().getSelectedGradingScaleBottomPercents());
    	} else {
    		//get the default values for the chosen grading scale and sort accordingly
    		bottomPercents = sortBottomPercents(gradingSchemaName,
	    		this.gradeMappings.stream()
					.filter(gradeMapping -> StringUtils.equals(gradeMapping.getId(), currentGradeMappingId))
					.findFirst()
					.get()
					.getDefaultBottomPercents()
    		);
    	}
    	
    	//convert map into list of objects which is easier to work with in the views 
		List<GbGradingSchemaEntry> rval = new ArrayList<>();
		for(Map.Entry<String, Double> entry: bottomPercents.entrySet()) {
			rval.add(new GbGradingSchemaEntry(entry.getKey(), entry.getValue()));
		}
		
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
					return -1; //SAK-30094
				} else {
					return 1;
				}
			}
			if(o1.length() == 1 && o2.length() == 2) {
				if(o2.charAt(1) == '+') {
					return 1; //SAK-30094
				} else {
					return -1;
				}
			}
			if(o1.length() == 2 && o2.length() == 1) {
				if(o1.charAt(1) == '+') {
					return -1; //SAK-30094
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
