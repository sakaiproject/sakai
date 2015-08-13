package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.gradebookng.business.model.GbGroup;
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
    List<PermissionDefinition> permissions;

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
		final List<CategoryDefinition> categories = this.businessService.getGradebookCategories();
		
        final Map<Long, String> categoryMap = new HashMap<>();
        for (CategoryDefinition category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }
		
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
		
		//TA chooser
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
		
		
		/*
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
        */
        
        if(taSelected != null) {
        	permissions = businessService.getPermissionsForTeachingAssistant(taSelected.getUserUuid());
        }
        
		Form form = new Form("form", Model.ofList(permissions));
        
		//render view for list of permissions  
		ListView<PermissionDefinition> permissionsView = new ListView<PermissionDefinition>("permissions", permissions) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<PermissionDefinition> item) {
				
				PermissionDefinition permission = item.getModelObject();
				
				//action list
				
				//categories list (if present)
				DropDownChoice<Long> categoryChooser = new DropDownChoice<Long>("category", new PropertyModel<Long>(permission, "categoryId"), new ArrayList<Long>(categoryMap.keySet()), new ChoiceRenderer<Long>() {
					private static final long serialVersionUID = 1L;

					public Object getDisplayValue(Long value) {
		                return categoryMap.get(value);
		            }

		            public String getIdValue(Long object, int index) {
		                return object.toString();
		            }
		        });
		        categoryChooser.setNullValid(false);
		        if(categories.isEmpty()){
		        	categoryChooser.setVisible(false);
		        }
		        item.add(categoryChooser);
				
				//groups list if present
				
			}
			
			@Override
			public boolean isVisible() {
				return (taSelected != null);
			}
			
			
        };
        form.add(permissionsView);
        
        //'add a rule' button 
        AjaxButton addRule = new AjaxButton("addRule") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				//add a new entry
				PermissionDefinition newDef = new PermissionDefinition();
				newDef.setUserId(taSelected.getUserUuid());
				permissions.add(newDef);
				
				target.add(form);
			}
			
			@Override
			public boolean isVisible() {
				return (taSelected != null);
			}
		};
		form.add(addRule);
        
        
        add(form);
        //save changes
		
	}
	
}
