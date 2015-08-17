package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GraderPermission;
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
		
		//add the default 'all'
		categories.add(0, new CategoryDefinition(null, getString("categories.all")));
		
        final Map<Long, String> categoryMap = new HashMap<>();
        for (CategoryDefinition category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }
		
		//get list of groups
		final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();
		
		//add the default 'all'
        groups.add(0, new GbGroup(null, getString("groups.all"), GbGroup.Type.ALL));
		
		final Map<String, String> groupMap = new HashMap<>();
        for (GbGroup group : groups) {
        	groupMap.put(group.getId(), group.getTitle());
        }
		
		//get list of permissions that can be assigned (skip the course grade permissions as handle it differently)
        final List<String> assignablePermissions = new ArrayList<>();
        assignablePermissions.add(GraderPermission.VIEW.toString());
        assignablePermissions.add(GraderPermission.GRADE.toString());
		
		//determine text for instructions panel
		String instructions = null;
		if(teachingAssistants.isEmpty()) {
			instructions = getString("permissionspage.instructions.noteachingassistants");
		} else if(categories.isEmpty() && groups.isEmpty()) {
			instructions = getString("permissionspage.instructions.nocategoriesorsections");
		} else {
			instructions = getString("permissionspage.instructions.ok");
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

		add(taChooser);
		
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
				
				//can
				item.add(new Label("can", new ResourceModel("permissionspage.item.can")));
				
				//function list
				final DropDownChoice<String> functionChooser = new DropDownChoice<String>("function", new PropertyModel<String>(permission, "function"), assignablePermissions, new ChoiceRenderer<String>() {
					private static final long serialVersionUID = 1L;
					
					@Override
					public Object getDisplayValue(String function) {
						return getString("permissionspage.function." + function);
					}
					
					@Override
					public String getIdValue(String function, int index) {
						return function;
					}
						
				});
				item.add(functionChooser);
				
				//categories list
				DropDownChoice<Long> categoryChooser = new DropDownChoice<Long>("category", new PropertyModel<Long>(permission, "categoryId"), new ArrayList<Long>(categoryMap.keySet()), new ChoiceRenderer<Long>() {
					private static final long serialVersionUID = 1L;

					public Object getDisplayValue(Long l) {
		                return categoryMap.get(l);
		            }

		            public String getIdValue(Long l, int index) {
		                return l.toString();
		            }
		        });
		        categoryChooser.setNullValid(false);
		        if(categories.isEmpty()){
		        	categoryChooser.setVisible(false);
		        }
		        item.add(categoryChooser);
				
				//in
				item.add(new Label("in", new ResourceModel("permissionspage.item.in")));
				
				//groups list
				DropDownChoice<String> groupChooser = new DropDownChoice<String>("group", new PropertyModel<String>(permission, "groupId"), new ArrayList<String>(groupMap.keySet()), new ChoiceRenderer<String>() {
					private static final long serialVersionUID = 1L;

					public Object getDisplayValue(String gId) {
		                return groupMap.get(gId);
		            }

		            public String getIdValue(String gId, int index) {
		                return gId;
		            }
		        });
				groupChooser.setNullValid(false);
		        item.add(groupChooser);
				
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
