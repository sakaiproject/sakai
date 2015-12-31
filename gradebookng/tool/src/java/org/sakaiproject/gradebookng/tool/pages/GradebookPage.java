package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.MaskType;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.Temp;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.model.ScoreChangedEvent;
import org.sakaiproject.gradebookng.tool.panels.AddOrEditGradeItemPanel;
import org.sakaiproject.gradebookng.tool.panels.AssignmentColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.CategoryColumnCellPanel;
import org.sakaiproject.gradebookng.tool.panels.CategoryColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.GradeItemCellPanel;
import org.sakaiproject.gradebookng.tool.panels.StudentNameCellPanel;
import org.sakaiproject.gradebookng.tool.panels.StudentNameColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.ToggleGradeItemsToolbarPanel;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CourseGrade;


/**
 * Grades page. Instructors and TAs see this one. Students see the {@link StudentPage}.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradebookPage extends BasePage {
	
	private static final long serialVersionUID = 1L;

	public static final String UNCATEGORIZED = "Uncategorized";
	public static final String CREATED_ASSIGNMENT_ID_PARAM = "createdAssignmentId";

	ModalWindow addOrEditGradeItemWindow;
	ModalWindow studentGradeSummaryWindow;
	ModalWindow updateUngradedItemsWindow;
	ModalWindow gradeLogWindow;
	ModalWindow gradeCommentWindow;
	ModalWindow deleteItemWindow;

	Form<Void> form;
	
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public GradebookPage() {
		disableLink(this.gradebookPageLink);
		
		//students cannot access this page
		if(role == GbRole.STUDENT) {
			throw new RestartResponseException(StudentPage.class);
		}
		
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		Temp.time("GradebookPage init", stopwatch.getTime());

		form = new Form<Void>("form");
		add(form);
		
		/**
		 * Note that SEMI_TRANSPARENT has a 100% black background and TRANSPARENT is overridden to 10% opacity
		 */
		addOrEditGradeItemWindow = new ModalWindow("addOrEditGradeItemWindow");
		addOrEditGradeItemWindow.setMaskType(MaskType.TRANSPARENT);
		addOrEditGradeItemWindow.setResizable(false);
		addOrEditGradeItemWindow.setUseInitialHeight(false);
		addOrEditGradeItemWindow.showUnloadConfirmation(false);
		form.add(addOrEditGradeItemWindow);
		
		studentGradeSummaryWindow = new ModalWindow("studentGradeSummaryWindow");
		studentGradeSummaryWindow.setMaskType(MaskType.TRANSPARENT);
		studentGradeSummaryWindow.setResizable(false);
		studentGradeSummaryWindow.setUseInitialHeight(false);
		studentGradeSummaryWindow.setWidthUnit("%");
		studentGradeSummaryWindow.setInitialWidth(70);
		studentGradeSummaryWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
			@Override
			public boolean onCloseButtonClicked(AjaxRequestTarget target) {
				target.appendJavaScript("GradebookGradeSummaryUtils.clearBlur();");
				return true;
			}
		});
		form.add(studentGradeSummaryWindow);
		
		updateUngradedItemsWindow = new ModalWindow("updateUngradedItemsWindow");
		updateUngradedItemsWindow.setMaskType(MaskType.TRANSPARENT);
		updateUngradedItemsWindow.setResizable(false);
		updateUngradedItemsWindow.setUseInitialHeight(true);
		form.add(updateUngradedItemsWindow);
		
		gradeLogWindow = new ModalWindow("gradeLogWindow");
		gradeLogWindow.setMaskType(MaskType.TRANSPARENT);
		gradeLogWindow.setResizable(false);
		gradeLogWindow.setUseInitialHeight(false);
		form.add(gradeLogWindow);
		
		gradeCommentWindow = new ModalWindow("gradeCommentWindow");
		gradeCommentWindow.setMaskType(MaskType.TRANSPARENT);
		gradeCommentWindow.setResizable(false);
		gradeCommentWindow.setUseInitialHeight(false);
		form.add(gradeCommentWindow);
		
		deleteItemWindow = new ModalWindow("deleteItemWindow");
		deleteItemWindow.setMaskType(MaskType.TRANSPARENT);
		deleteItemWindow.setResizable(false);
		deleteItemWindow.setUseInitialHeight(false);
		form.add(deleteItemWindow);
		
		AjaxButton addGradeItem = new AjaxButton("addGradeItem") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				ModalWindow window = getAddOrEditGradeItemWindow();
				window.setContent(new AddOrEditGradeItemPanel(window.getContentId(), window, null));
				window.show(target);
			}
			
			@Override
			public boolean isVisible() {
				if(role != GbRole.INSTRUCTOR){
					return false;
				}
				return true;
			}
			
		};
		addGradeItem.setDefaultFormProcessing(false);
		form.add(addGradeItem);
		
		//first get any settings data from the session
		final GradebookUiSettings settings = this.getUiSettings();
				
        //get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
        final List<Assignment> assignments = this.businessService.getGradebookAssignments();
		Temp.time("getGradebookAssignments", stopwatch.getTime());
        
        //get the grade matrix. It should be sorted if we have that info
        final List<GbStudentGradeInfo> grades = businessService.buildGradeMatrix(assignments, settings.getAssignmentSortOrder(), settings.getNameSortOrder(), settings.getGroupFilter());
        
		Temp.time("buildGradeMatrix", stopwatch.getTime());

		//get assignment order
        final Map<String, List<Long>> categorizedAssignmentOrder = businessService.getCategorizedAssignmentsOrder();

        //get course grade visibility
        final boolean courseGradeVisible = businessService.isCourseGradeVisible(currentUserUuid);
        
        //categories enabled?
        final boolean categoriesEnabled = businessService.categoriesAreEnabled();
                
        //this could potentially be a sortable data provider
        final ListDataProvider<GbStudentGradeInfo> studentGradeMatrix = new ListDataProvider<GbStudentGradeInfo>(grades);
        List<IColumn> cols = new ArrayList<IColumn>();
        
        //add an empty column that we can use as a handle for selecting the row
        AbstractColumn handleColumn = new AbstractColumn(new Model("")){

			@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				cellItem.add(new EmptyPanel(componentId));
			}
			
			@Override
			public String getCssClass() {
				return "gb-row-selector";
			}
        };
        cols.add(handleColumn);
        
        //student name column
        AbstractColumn studentNameColumn = new AbstractColumn(new Model("")) {

        	@Override
        	public Component getHeader(String componentId) {
        		return new StudentNameColumnHeaderPanel(componentId, Model.of(settings.getNameSortOrder())); //pass in the sort
        	}
        	
        	@Override
			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
				GbStudentGradeInfo studentGradeInfo = (GbStudentGradeInfo) rowModel.getObject();
				
				Map<String,Object> modelData = new HashMap<>();
				modelData.put("userId", studentGradeInfo.getStudentUuid());
				modelData.put("eid", studentGradeInfo.getStudentEid());
				modelData.put("firstName", studentGradeInfo.getStudentFirstName());
				modelData.put("lastName", studentGradeInfo.getStudentLastName());
				modelData.put("displayName", studentGradeInfo.getStudentDisplayName());
				modelData.put("nameSortOrder", settings.getNameSortOrder()); //pass in the sort
				
				cellItem.add(new StudentNameCellPanel(componentId, Model.ofMap(modelData)));
				cellItem.add(new AttributeModifier("data-studentUuid", studentGradeInfo.getStudentUuid()));
				
				//TODO may need a subclass of Item that does the onComponentTag override and then tag.setName("th");
			}
        	
        	@Override
			public String getCssClass() {
				return "gb-student-cell";
			}

        };
        cols.add(studentNameColumn);
        
        // course grade column
        AbstractColumn courseGradeColumn = new AbstractColumn(new Model("")) {
            @Override
            public Component getHeader(String componentId) {
                CourseGradeColumnHeaderPanel panel = new CourseGradeColumnHeaderPanel(componentId);
                return panel;
            }

            @Override
            public String getCssClass() {
                return "gb-course-grade";
            }

            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                final GbStudentGradeInfo studentGradeInfo = (GbStudentGradeInfo) rowModel.getObject();

                String courseGrade;
                
                if(courseGradeVisible) {
                	courseGrade = studentGradeInfo.getCourseGrade();
                } else {
                	courseGrade = getString("label.coursegrade.nopermission");
                }

                Label courseGradeLabel = new Label(componentId, Model.of(courseGrade)) {
                    @Override
                    public void onEvent(IEvent<?> event) {
                        super.onEvent(event);
                        if (event.getPayload() instanceof ScoreChangedEvent) {
                            ScoreChangedEvent scoreChangedEvent = (ScoreChangedEvent) event.getPayload();
                            if (studentGradeInfo.getStudentUuid().equals(scoreChangedEvent.getStudentUuid())) {
                                CourseGrade courseGrade = businessService.getCourseGrade(scoreChangedEvent.getStudentUuid());
                                ((Model<String>)getDefaultModel()).setObject(courseGrade.getMappedGrade());
                                scoreChangedEvent.getTarget().add(this);
                            }
                        }
                    }
                };
                courseGradeLabel.setOutputMarkupId(true);
                cellItem.add(courseGradeLabel);
            }
        };
        cols.add(courseGradeColumn);
        
        //build the rest of the columns based on the assignment list       
        for(final Assignment assignment: assignments) {
        	
        	AbstractColumn column = new AbstractColumn(new Model("")) {

            	@Override
            	public Component getHeader(String componentId) {
            		AssignmentColumnHeaderPanel panel = new AssignmentColumnHeaderPanel(componentId, new Model<Assignment>(assignment));
            		
            		String category = assignment.getCategoryName();
            		
            		int order = -1;
            		if (categorizedAssignmentOrder.containsKey(category)) {
            			order = categorizedAssignmentOrder.get(category).indexOf(assignment.getId());
            		}
            		
            		panel.add(new AttributeModifier("data-category", category));
            		panel.add(new AttributeModifier("data-categorized-order", order));

            		StringValue createdAssignmentId = getPageParameters().get(CREATED_ASSIGNMENT_ID_PARAM);
            		if (!createdAssignmentId.isNull() && assignment.getId().equals(createdAssignmentId.toLong())) {
            			panel.add(new AttributeModifier("class", "gb-just-created"));
            			getPageParameters().remove(CREATED_ASSIGNMENT_ID_PARAM);
            		}

    				return panel;
            	}

				@Override
				public String getCssClass() {
					return "gb-grade-item-column-cell";
				}
            	
            	@Override
				public void populateItem(Item cellItem, String componentId, IModel rowModel) {
            		GbStudentGradeInfo studentGrades = (GbStudentGradeInfo) rowModel.getObject();
            		
            		GbGradeInfo gradeInfo = studentGrades.getGrades().get(assignment.getId());
            		
            		Map<String,Object> modelData = new HashMap<>();
    				modelData.put("assignmentId", assignment.getId());
    				modelData.put("assignmentPoints", assignment.getPoints());
    				modelData.put("studentUuid", studentGrades.getStudentUuid());
    				modelData.put("categoryId", assignment.getCategoryId());
    				modelData.put("isExternal", assignment.isExternallyMaintained());
    				modelData.put("gradeInfo", gradeInfo);
    				modelData.put("role", role);
    				
    				cellItem.add(new GradeItemCellPanel(componentId, Model.ofMap(modelData)));
    				
    				cellItem.setOutputMarkupId(true);
				}   
            	
            	
            };
                                   
            cols.add(column);
        }
        
        //render the categories (TODO may be able to pass this list into the matrix to save another lookup in there)
        final List<CategoryDefinition> categories = this.businessService.getGradebookCategories();
        
        for(final CategoryDefinition category: categories) {
        	AbstractColumn column = new AbstractColumn(new Model("")) {

        		@Override
            	public Component getHeader(String componentId) {
            		CategoryColumnHeaderPanel panel = new CategoryColumnHeaderPanel(componentId, new Model<CategoryDefinition>(category));

            		panel.add(new AttributeModifier("data-category", category.getName()));
            		
            		return panel;
        		}
            	
            	@Override
    			public void populateItem(Item cellItem, String componentId, IModel rowModel) {
    				GbStudentGradeInfo studentGrades = (GbStudentGradeInfo) rowModel.getObject();
    				
            		Double score = studentGrades.getCategoryAverages().get(category.getId());
            		
            		Map<String,Object> modelData = new HashMap<>();
    				modelData.put("score", score);
    				modelData.put("studentUuid", studentGrades.getStudentUuid());
    				modelData.put("categoryId", category.getId());
    				
    				cellItem.add(new CategoryColumnCellPanel(componentId, Model.ofMap(modelData)));
    				cellItem.setOutputMarkupId(true);
    			}
            	
            	@Override
    			public String getCssClass() {
    				return "gb-category-item-column-cell";
    			} 
        		
        	};
             
             cols.add(column);
         }
       
		Temp.time("all Columns added", stopwatch.getTime());
        
        //TODO make this AjaxFallbackDefaultDataTable
        DataTable table = new DataTable("table", cols, studentGradeMatrix, 100);
        table.addBottomToolbar(new NavigationToolbar(table));
        table.addTopToolbar(new HeadersToolbar(table, null));
        table.add(new AttributeModifier("data-siteid", this.businessService.getCurrentSiteId()));
        
        WebMarkupContainer noAssignments = new WebMarkupContainer("noAssignments");
        noAssignments.setVisible(false);
        form.add(noAssignments);
        
        WebMarkupContainer noStudents = new WebMarkupContainer("noStudents");
        noStudents.setVisible(false);
        form.add(noStudents);
        
        form.add(table);

        // Populate the toolbar 
        Label gradeItemSummary = new Label("gradeItemSummary", new StringResourceModel("label.toolbar.gradeitemsummary", null, assignments.size() + categories.size(), assignments.size() + categories.size()));
        gradeItemSummary.setEscapeModelStrings(false);
        form.add(gradeItemSummary);
        
        WebMarkupContainer toggleGradeItemsToolbarItem = new WebMarkupContainer("toggleGradeItemsToolbarItem");
        form.add(toggleGradeItemsToolbarItem);
        
        AjaxButton toggleCategoriesToolbarItem = new AjaxButton("toggleCategoriesToolbarItem") {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                if (settings.isCategoriesEnabled()) {
                    add(new AttributeModifier("class", "on"));
                }
            }
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                settings.setCategoriesEnabled(!settings.isCategoriesEnabled());
                setUiSettings(settings);

                if (settings.isCategoriesEnabled()) {
                    add(new AttributeModifier("class", "on"));
                } else {
                    add(new AttributeModifier("class", ""));
                }
                target.add(this);
                target.appendJavaScript("sakai.gradebookng.spreadsheet.toggleCategories();");
            }
            @Override
            public boolean isVisible() {
                return categoriesEnabled && !assignments.isEmpty();
            }
        };
        form.add(toggleCategoriesToolbarItem);

        //section and group dropdown
        List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();
        
        //add the default ALL group to the list
        groups.add(0, new GbGroup(null, getString("groups.all"), null, GbGroup.Type.ALL));
            
		final DropDownChoice<GbGroup> groupFilter = new DropDownChoice<GbGroup>("groupFilter", new Model<GbGroup>(), groups, new ChoiceRenderer<GbGroup>() {
		private static final long serialVersionUID = 1L;
		
			@Override
			public Object getDisplayValue(GbGroup g) {
				return g.getTitle();
			}
			
			@Override
			public String getIdValue(GbGroup g, int index) {
				return g.getId();
			}
			
		});
		
		groupFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {

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

        ToggleGradeItemsToolbarPanel gradeItemsTogglePanel = new ToggleGradeItemsToolbarPanel("gradeItemsTogglePanel", assignments);
        add(gradeItemsTogglePanel);
        
        //hide/show components 
        
        //no assignments, hide table, show message
        if(assignments.isEmpty()) {
        	table.setVisible(false);
        	toggleGradeItemsToolbarItem.setVisible(false);
        	noAssignments.setVisible(true);
        }
        
        //no visible students, show table, show message
        //don't want two messages though, hence the else
        else if(studentGradeMatrix.size() == 0) {
        	noStudents.setVisible(true);
        }
        
		Temp.time("Gradebook page done", stopwatch.getTime());
	}
	
	
	/**
	 * Getters for panels to get at modal windows
	 * @return
	 */
	public ModalWindow getAddOrEditGradeItemWindow() {
		return this.addOrEditGradeItemWindow;
	}
	
	public ModalWindow getStudentGradeSummaryWindow() {
		return this.studentGradeSummaryWindow;
	}
	
	public ModalWindow getUpdateUngradedItemsWindow() {
		return this.updateUngradedItemsWindow;
	}
	
	public ModalWindow getGradeLogWindow() {
		return this.gradeLogWindow;
	}
	
	public ModalWindow getGradeCommentWindow() {
		return this.gradeCommentWindow;
	}

	public ModalWindow getDeleteItemWindow() {
		return this.deleteItemWindow;
	}

	/**
	 * Getter for the GradebookUiSettings. Used to store a few UI related settings for the current session only.
	 * 
	 * TODO move this to a helper
	 */
	public GradebookUiSettings getUiSettings() {
		
		GradebookUiSettings settings = (GradebookUiSettings) Session.get().getAttribute("GBNG_UI_SETTINGS");
		
		if(settings == null) {
			settings = new GradebookUiSettings();
			settings.setCategoriesEnabled(businessService.categoriesAreEnabled());
		}
		
		return settings;
	}
	
	public void setUiSettings(GradebookUiSettings settings) {
		Session.get().setAttribute("GBNG_UI_SETTINGS", settings);
	}


	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		String version = ServerConfigurationService.getString("portal.cdn.version", "");

		//Drag and Drop/Date Picker (requires jQueryUI)
		response.render(JavaScriptHeaderItem.forUrl(String.format("/library/js/jquery/ui/1.11.3/jquery-ui.min.js?version=%s", version)));

		//Include Sakai Date Picker
		response.render(JavaScriptHeaderItem.forUrl(String.format("/library/js/lang-datepicker/lang-datepicker.js?version=%s", version)));

		//GradebookNG Grade specific styles and behaviour
		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-grades.css?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grades.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grade-summary.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-update-ungraded.js?version=%s", version)));
	}
}
