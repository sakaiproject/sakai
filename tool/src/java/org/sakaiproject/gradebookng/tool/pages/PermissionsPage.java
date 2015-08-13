package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;


/**
 * Permissions page
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class PermissionsPage extends BasePage {
	
	private static final long serialVersionUID = 1L;
	
	private GbUser taSelected;

	public PermissionsPage() {
		disableLink(this.permissionsPageLink);
	}
	
	public PermissionsPage(final GbUser taSelected) {
		disableLink(this.permissionsPageLink);
		this.taSelected = taSelected;
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
		
		
		final DropDownChoice<GbUser> taChooser = new DropDownChoice<GbUser>("ta", new Model<GbUser>(), teachingAssistants, new ChoiceRenderer<GbUser>() {
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
				
		//add the onchange to the chooser
		taChooser.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				
				//set the selection
				GbUser selection = (GbUser) taChooser.getDefaultModelObject();
				
				//refresh with the selected user
				setResponsePage(new PermissionsPage(selection));
			}
			
		});
				
		taChooser.setNullValid(false);
		taChooser.setModelObject(taSelected);

        //form.add(taChooser);
		add(taChooser);
		
		
        //wrap the loading of the permissions	
  		IModel permissionsListModel =  new LoadableDetachableModel() {
            protected Object load() {
            	if(taSelected != null) {
            		return businessService.getPermissionsForTeachingAssistant(taSelected.getUserUuid());
            	} else {
            		return new ArrayList<>();
            	}
            }
        };
        
		//render view for list of permissions  
		ListView<PermissionDefinition> permissionsView = new ListView<PermissionDefinition>("permissions", permissionsListModel) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<PermissionDefinition> item) {
				
				PermissionDefinition permissions = item.getModelObject();
				
				//action list
				
				//categories list if present
				
				//groups list if present
				
			}
			
			public boolean isVisible() {
				return (taSelected != null);
			}
			
			
        };
        add(permissionsView);
        
        //add a rule button
        /*
        AjaxButton addRule = new AjaxButton("addRule") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
			}
		};
		add(addRule);
        */
        
        //save changes
		
	}
	
}
