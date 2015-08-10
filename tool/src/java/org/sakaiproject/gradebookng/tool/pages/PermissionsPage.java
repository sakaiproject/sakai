package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;


/**
 * Permissions page
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class PermissionsPage extends BasePage {
	
	private static final long serialVersionUID = 1L;

	public PermissionsPage() {
		disableLink(this.permissionsPageLink);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		//get the list of TAs
		List<GbUser> teachingAssistants = this.businessService.getTeachingAssistants();
		
		//get list of categories
		List<CategoryDefinition> categories = this.businessService.getGradebookCategories();
		
		//get list of groups
		List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();
		
		//determine text for instructions panel
		String instructions = null;
		boolean displayForm = false;
		if(teachingAssistants.isEmpty()) {
			instructions = getString("permissionspage.instructions.noteachingassistants");
		} else if(categories.isEmpty() && groups.isEmpty()) {
			instructions = getString("permissionspage.instructions.nocategoriesorsections");
		} else {
			instructions = getString("permissionspage.instructions.ok");
			displayForm = true;
		}
		
		add(new Label("instructions", instructions).setEscapeModelStrings(false));
		
		
		final DropDownChoice<GbUser> ta = new DropDownChoice<GbUser>("ta", new Model<GbUser>(), teachingAssistants, new ChoiceRenderer<GbUser>() {
			private static final long serialVersionUID = 1L;
			
				@Override
				public Object getDisplayValue(GbUser u) {
					return u.getDisplayName();
				}
				
				@Override
				public String getIdValue(GbUser u, int index) {
					return u.getUserUuid();
				}
				
			});
		
		add(ta);
		
		//add the onchange
		/*
		 * groupFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				
				GbGroup selected = (GbGroup) groupFilter.getDefaultModelObject();
				
				//store selected group (null ok)
				GradebookUiSettings settings = getUiSettings();
				settings.setGroupFilter(selected);
				setUiSettings(settings);
				
				//refresh
				setResponsePage(new GradebookPage());
			}
			
		});
				
		//set selected group, or first item in list
		groupFilter.setModelObject((settings.getGroupFilter() != null) ? settings.getGroupFilter() : groups.get(0));
        groupFilter.setNullValid(false);
        form.add(groupFilter);
        */

		
	}
	
}
