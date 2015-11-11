package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.tool.gradebook.GradingScale;

public class SettingsGradingSchemaPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	IModel<GradebookInformation> model;

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
		
		//TODO set the selected one
		
		//type chooser
		final DropDownChoice<GradingScale> typeChooser = new DropDownChoice<GradingScale>("type", new Model<GradingScale>(), gradingScales, new ChoiceRenderer<GradingScale>() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Object getDisplayValue(GradingScale g) {
				return g.getName();
				//return new StringResourceModel("permissionspage.label.tausername", null, new String[] {u.getDisplayName(), u.getDisplayId()}).getString();
			}
			
			@Override
			public String getIdValue(GradingScale gs, int index) {
				return gs.getUid();
			}
				
		});
		
		typeChooser.setNullValid(false);
		
		//set selected
		for(GradingScale gs: gradingScales) {
			if(StringUtils.equals(gs.getUid(), selectedGradingScaleUid)) {
				typeChooser.setModelObject(gs);
			}
		}
		
		
		add(typeChooser);
				
		//points/percentage entry
		//RadioGroup<Integer> gradeEntry = new RadioGroup<>("gradeEntry", new PropertyModel<Integer>(model, "gradeType"));
		//gradeEntry.add(new Radio<>("points", new Model<>(1)));
		//gradeEntry.add(new Radio<>("percentages", new Model<>(2)));
		//add(gradeEntry);
		
	}
}
