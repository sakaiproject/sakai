package org.sakaiproject.gradebookng.tool.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.component.cover.ServerConfigurationService;
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
    
    // these are magic strings we use as ids for the all groups/all categories options
    // they should not conflict with any real values that might be passed in
    // and they are parsed out on save
    private final String ALL_GROUPS = "-1";
    private final Long ALL_CATEGORIES = new Long(-1);

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
		
		//add the default 'all' category
		categories.add(0, new CategoryDefinition(ALL_CATEGORIES, getString("categories.all")));
		
        final Map<Long, String> categoryMap = new LinkedHashMap<>();
        for (CategoryDefinition category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }
		
		//get list of groups
        //note that for the permissions we need to use the group references not the ids
		final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();
		
		//add the default 'all' group
        groups.add(0, new GbGroup(ALL_GROUPS, getString("groups.all"), ALL_GROUPS, GbGroup.Type.ALL));
        
		final Map<String, String> groupMap = new LinkedHashMap<>();
        for (GbGroup group : groups) {
        	groupMap.put(group.getReference(), group.getTitle());
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
				return new StringResourceModel("permissionspage.label.tausername", null, new String[] {u.getDisplayName(), u.getDisplayId()}).getString();
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
		
		//setup the backing object
        final PermissionsPageModel pageModel = new PermissionsPageModel();
		
		//If we have chosen a user, get the permissions
		//Need to parse the permission list to process the view_course_grade permission
        if(taSelected != null) {
        	List<PermissionDefinition> permissions = businessService.getPermissionsForUser(taSelected.getUserUuid());
        	
    		Iterator<PermissionDefinition> iter = permissions.iterator();
    		while (iter.hasNext()) {
    			PermissionDefinition p = iter.next();
    			if(StringUtils.equals(p.getFunction(), GraderPermission.VIEW_COURSE_GRADE.toString())) {
    				pageModel.setViewCourseGrade(true);
    				iter.remove();
    			}
    		}
    		    		    		
    		//if we have no permissions, set the viewCourseGrade to true for a new permission set
    		//its only saved if we have permissions defined though
    		if(permissions.isEmpty()) {
    			pageModel.setViewCourseGrade(true);
    		}
    		
    		pageModel.setPermissions(permissions);
        	    		
        }
        
        //if no permissions defined yet
        Label noPermissions = new Label("noPermissions", new ResourceModel("permissionspage.instructions.norules")) {
			@Override
			public boolean isVisible() {
				return (taSelected != null && pageModel.getPermissions().isEmpty());
			}
		};
		add(noPermissions);
        
        //FORM
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Form form = new Form("form", Model.of(pageModel)) {
			@Override
			public boolean isVisible() {
				return taSelected != null;
			}
		};
		
		//submit button
		Button submit = new Button("submit") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit() {
				Form<?> form = getForm();
				
				PermissionsPageModel model = (PermissionsPageModel) form.getModelObject();
				List<PermissionDefinition> permissions = model.getPermissions();
				
				//parse out the magic strings back to nulls for persisting
				for (PermissionDefinition permission: permissions) {
					if(StringUtils.equals(permission.getGroupReference(), ALL_GROUPS)){
						permission.setGroupReference(null);
					}
					if(permission.getCategoryId().equals(ALL_CATEGORIES)){
						permission.setCategoryId(null);
					}
				}
				
				//if we have permissions AND the checkbox is ticked, create a new permission for it
				if(!permissions.isEmpty() && model.getViewCourseGrade()) {
					PermissionDefinition viewCourseGradePermission = new PermissionDefinition();
					viewCourseGradePermission.setUserId(taSelected.getUserUuid());
					viewCourseGradePermission.setGroupReference(null);
					viewCourseGradePermission.setCategoryId(null);
					viewCourseGradePermission.setFunction(GraderPermission.VIEW_COURSE_GRADE.toString());
					permissions.add(viewCourseGradePermission);
				}
				
				businessService.updatePermissionsForUser(taSelected.getUserUuid(), permissions);
				
				getSession().info(getString("permissionspage.update.success"));
				
				setResponsePage(new PermissionsPage(taSelected));
			}
			
			@Override
			public boolean isVisible() {
				return (taSelected != null);
			}
		};
		form.add(submit);
		
		//clear button
		Button clear = new Button("clear") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onSubmit() {
				setResponsePage(new PermissionsPage(taSelected));
			}
			
			@Override
			public boolean isVisible() {
				return (taSelected != null);
			}
		};
		clear.setDefaultFormProcessing(false);
		form.add(clear);
		
		//coursegrade checkbox
        form.add(new CheckBox("viewCourseGrade", new PropertyModel<Boolean>(pageModel, "viewCourseGrade")) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return (taSelected != null && !pageModel.getPermissions().isEmpty());
			}
        });
        
		//render view for list of permissions  
		ListView<PermissionDefinition> permissionsView = new ListView<PermissionDefinition>("permissions", pageModel.getPermissions()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<PermissionDefinition> item) {
				
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
				functionChooser.setNullValid(false);
				item.add(functionChooser);
				
				//categories list
				List<Long> categoryIdList = new ArrayList<Long>(categoryMap.keySet());
				DropDownChoice<Long> categoryChooser = new DropDownChoice<Long>("category", new PropertyModel<Long>(permission, "categoryId"), categoryIdList, new ChoiceRenderer<Long>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(Long l) {
		                return categoryMap.get(l);
		            }

					@Override
		            public String getIdValue(Long l, int index) {
		            	if(l == null){
		            		return ""; // to match what the service stores
		            	}
		                return l.toString();
		            }
		        });
				//set selected or first item
				categoryChooser.setModelObject((permission.getCategoryId() != null) ? permission.getCategoryId() : categoryIdList.get(0));
		        categoryChooser.setNullValid(false);
		        item.add(categoryChooser);
				
				//in
				item.add(new Label("in", new ResourceModel("permissionspage.item.in")));
				
				//groups list
				List<String> groupRefList = new ArrayList<String>(groupMap.keySet());
				DropDownChoice<String> groupChooser = new DropDownChoice<String>("group", new PropertyModel<String>(permission, "groupReference"), groupRefList, new ChoiceRenderer<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(String groupRef) {
		                return groupMap.get(groupRef);
		            }

					@Override
		            public String getIdValue(String groupRef, int index) {
		                return groupRef;
		            }
		        });
				//set selected or first item
				groupChooser.setModelObject((permission.getGroupReference() != null) ? permission.getGroupReference() : groupRefList.get(0));
				groupChooser.setNullValid(false);
		        item.add(groupChooser);
		        
		        //remove button
				AjaxButton remove = new AjaxButton("remove") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onSubmit(AjaxRequestTarget target, Form<?> form) {
						
						//remove current item
						PermissionDefinition current = item.getModelObject();
						pageModel.getPermissions().remove(current);
						
						target.add(form);
					}
				};
				remove.setDefaultFormProcessing(false);
				item.add(remove);
				
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
				
				//add a new entry with default values so the dropdowns are sane
				PermissionDefinition newDef = new PermissionDefinition();
				newDef.setUserId(taSelected.getUserUuid());
				newDef.setGroupReference(ALL_GROUPS);
				newDef.setCategoryId(ALL_CATEGORIES);
				newDef.setFunction(GraderPermission.VIEW.toString());
				pageModel.getPermissions().add(newDef);
				
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
	
	
	/**
	 * Class for wrapping up the data used by this page
	 */
	private class PermissionsPageModel implements Serializable {

		private static final long serialVersionUID = 1L;

		@Getter @Setter 
		private List<PermissionDefinition> permissions;
		
		@Getter @Setter
		private Boolean viewCourseGrade;
		
		public PermissionsPageModel() {
			this.permissions = new ArrayList<>();
		}
		
	}
	

	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		String version = ServerConfigurationService.getString("portal.cdn.version", "");

		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-permissions.css?version=%s", version)));
	}
}
