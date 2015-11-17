package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.model.GbGradingSchemaEntry;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.tool.gradebook.GradingScale;

public class SettingsGradingSchemaPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GradebookInformation> model;
	
	WebMarkupContainer schemaWrap;
	GradingScale selectedGradingScale;

	public SettingsGradingSchemaPanel(String id, IModel<GradebookInformation> model) {
		super(id, model);
		this.model = model;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//get from service
		List<GradingScale> gradingScales = this.businessService.getGradingScales();
		
		//get current one
		String selectedGradingScaleUid = this.model.getObject().getSelectedGradingScaleUid();
		for(GradingScale gs: gradingScales) {
			if(StringUtils.equals(gs.getUid(), selectedGradingScaleUid)) {
				selectedGradingScale = gs;
			}
		}
		
		//get grademappings
		//this.model.getObject().getGradeMap();
		
		//HERE
		//we need to be able to get the gradeMapping list for a given gradebook id and gradingScaleUid
		//so that we can look it up. and if it switches we can run the query again to select he latest data.
		
		//get the grade mappings for the current one
		//TODO turn this into a model that can refresh
		
		
		//type chooser
		final DropDownChoice<GradingScale> typeChooser = new DropDownChoice<GradingScale>("type", new Model<GradingScale>(), gradingScales, new ChoiceRenderer<GradingScale>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Object getDisplayValue(GradingScale g) {
				return g.getName();
			}
			
			@Override
			public String getIdValue(GradingScale gs, int index) {
				return gs.getUid();
			}
				
		});
		
		typeChooser.setNullValid(false);
		typeChooser.setModelObject(selectedGradingScale);
		add(typeChooser);
		
		//convert map into list of objects which is easier to work with in the views
		//note that this is the list of bottom percents from the configured grading scale in THIS gradebook, not the defaults
		Map<String,Double> bottomPercents = this.model.getObject().getSelectedGradingScaleBottomPercents();

		List<GbGradingSchemaEntry> gradingSchemaEntries = new ArrayList<>();
		for(Map.Entry<String, Double> entry: bottomPercents.entrySet()) {
			gradingSchemaEntries.add(new GbGradingSchemaEntry(entry.getKey(), entry.getValue()));
		}
		
		//render the grading schema table
		schemaWrap = new WebMarkupContainer("schemaWrap");
    	ListView<GbGradingSchemaEntry> schemaView = new ListView<GbGradingSchemaEntry>("schemaView", Model.ofList(gradingSchemaEntries)) {

  			private static final long serialVersionUID = 1L;

  			@Override
  			protected void populateItem(final ListItem<GbGradingSchemaEntry> item) {
  				
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
				
		
	}
}
