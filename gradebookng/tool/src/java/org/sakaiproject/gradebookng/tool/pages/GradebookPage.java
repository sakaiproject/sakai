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

	public static final String CREATED_ASSIGNMENT_ID_PARAM = "createdAssignmentId";

	// flag to indicate a category is uncategorised
	// doubles as a translation key
	public static final String UNCATEGORISED = "gradebookpage.uncategorised";

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

		// students cannot access this page
		if (this.role == GbRole.STUDENT) {
			throw new RestartResponseException(StudentPage.class);
		}

		final StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		Temp.time("GradebookPage init", stopwatch.getTime());

		this.form = new Form<Void>("form");
		add(this.form);

		/**
		 * Note that SEMI_TRANSPARENT has a 100% black background and TRANSPARENT is overridden to 10% opacity
		 */
		this.addOrEditGradeItemWindow = new ModalWindow("addOrEditGradeItemWindow");
		this.addOrEditGradeItemWindow.setMaskType(MaskType.TRANSPARENT);
		this.addOrEditGradeItemWindow.setResizable(false);
		this.addOrEditGradeItemWindow.setUseInitialHeight(false);
		this.addOrEditGradeItemWindow.showUnloadConfirmation(false);
		this.addOrEditGradeItemWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
			@Override
			public boolean onCloseButtonClicked(final AjaxRequestTarget target) {
				// Ensure the date picker is hidden
				target.appendJavaScript("$('#ui-datepicker-div').hide();");
				return true;
			}
		});
		this.form.add(this.addOrEditGradeItemWindow);

		this.studentGradeSummaryWindow = new ModalWindow("studentGradeSummaryWindow");
		this.studentGradeSummaryWindow.setMaskType(MaskType.TRANSPARENT);
		this.studentGradeSummaryWindow.setResizable(false);
		this.studentGradeSummaryWindow.setUseInitialHeight(false);
		this.studentGradeSummaryWindow.setWidthUnit("%");
		this.studentGradeSummaryWindow.setInitialWidth(70);
		this.studentGradeSummaryWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
			@Override
			public boolean onCloseButtonClicked(final AjaxRequestTarget target) {
				target.appendJavaScript("GradebookGradeSummaryUtils.clearBlur();");
				return true;
			}
		});
		this.form.add(this.studentGradeSummaryWindow);

		this.updateUngradedItemsWindow = new ModalWindow("updateUngradedItemsWindow");
		this.updateUngradedItemsWindow.setMaskType(MaskType.TRANSPARENT);
		this.updateUngradedItemsWindow.setResizable(false);
		this.updateUngradedItemsWindow.setUseInitialHeight(true);
		this.form.add(this.updateUngradedItemsWindow);

		this.gradeLogWindow = new ModalWindow("gradeLogWindow");
		this.gradeLogWindow.setMaskType(MaskType.TRANSPARENT);
		this.gradeLogWindow.setResizable(false);
		this.gradeLogWindow.setUseInitialHeight(false);
		this.form.add(this.gradeLogWindow);

		this.gradeCommentWindow = new ModalWindow("gradeCommentWindow");
		this.gradeCommentWindow.setMaskType(MaskType.TRANSPARENT);
		this.gradeCommentWindow.setResizable(false);
		this.gradeCommentWindow.setUseInitialHeight(false);
		this.form.add(this.gradeCommentWindow);

		this.deleteItemWindow = new ModalWindow("deleteItemWindow");
		this.deleteItemWindow.setMaskType(MaskType.TRANSPARENT);
		this.deleteItemWindow.setResizable(false);
		this.deleteItemWindow.setUseInitialHeight(false);
		this.form.add(this.deleteItemWindow);

		final AjaxButton addGradeItem = new AjaxButton("addGradeItem") {
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final ModalWindow window = getAddOrEditGradeItemWindow();
				window.setContent(new AddOrEditGradeItemPanel(window.getContentId(), window, null));
				window.show(target);
			}

			@Override
			public boolean isVisible() {
				if (GradebookPage.this.role != GbRole.INSTRUCTOR) {
					return false;
				}
				return true;
			}

		};
		addGradeItem.setDefaultFormProcessing(false);
		this.form.add(addGradeItem);

		// first get any settings data from the session
		final GradebookUiSettings settings = getUiSettings();

		// get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from
		// the map
		final List<Assignment> assignments = this.businessService.getGradebookAssignments();
		Temp.time("getGradebookAssignments", stopwatch.getTime());

		// get the grade matrix. It should be sorted if we have that info
		final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(assignments,
				settings.getAssignmentSortOrder(), settings.getNameSortOrder(), settings.getCategorySortOrder(),
				settings.getGroupFilter());

		Temp.time("buildGradeMatrix", stopwatch.getTime());

		// get assignment order
		final Map<String, List<Long>> categorizedAssignmentOrder = this.businessService.getCategorizedAssignmentsOrder();

		// get course grade visibility
		final boolean courseGradeVisible = this.businessService.isCourseGradeVisible(this.currentUserUuid);

		// categories enabled?
		final boolean categoriesEnabled = this.businessService.categoriesAreEnabled();

		// this could potentially be a sortable data provider
		final ListDataProvider<GbStudentGradeInfo> studentGradeMatrix = new ListDataProvider<GbStudentGradeInfo>(grades);
		final List<IColumn> cols = new ArrayList<IColumn>();

		// add an empty column that we can use as a handle for selecting the row
		final AbstractColumn handleColumn = new AbstractColumn(new Model("")) {

			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				cellItem.add(new EmptyPanel(componentId));
			}

			@Override
			public String getCssClass() {
				return "gb-row-selector";
			}
		};
		cols.add(handleColumn);

		// student name column
		final AbstractColumn studentNameColumn = new AbstractColumn(new Model("")) {

			@Override
			public Component getHeader(final String componentId) {
				return new StudentNameColumnHeaderPanel(componentId, Model.of(settings.getNameSortOrder())); // pass in the sort
			}

			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final GbStudentGradeInfo studentGradeInfo = (GbStudentGradeInfo) rowModel.getObject();

				final Map<String, Object> modelData = new HashMap<>();
				modelData.put("userId", studentGradeInfo.getStudentUuid());
				modelData.put("eid", studentGradeInfo.getStudentEid());
				modelData.put("firstName", studentGradeInfo.getStudentFirstName());
				modelData.put("lastName", studentGradeInfo.getStudentLastName());
				modelData.put("displayName", studentGradeInfo.getStudentDisplayName());
				modelData.put("nameSortOrder", settings.getNameSortOrder()); // pass in the sort

				cellItem.add(new StudentNameCellPanel(componentId, Model.ofMap(modelData)));
				cellItem.add(new AttributeModifier("data-studentUuid", studentGradeInfo.getStudentUuid()));

				// TODO may need a subclass of Item that does the onComponentTag override and then tag.setName("th");
			}

			@Override
			public String getCssClass() {
				return "gb-student-cell";
			}

		};
		cols.add(studentNameColumn);

		// course grade column
		final AbstractColumn courseGradeColumn = new AbstractColumn(new Model("")) {
			@Override
			public Component getHeader(final String componentId) {
				final CourseGradeColumnHeaderPanel panel = new CourseGradeColumnHeaderPanel(componentId);
				return panel;
			}

			@Override
			public String getCssClass() {
				return "gb-course-grade";
			}

			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final GbStudentGradeInfo studentGradeInfo = (GbStudentGradeInfo) rowModel.getObject();

				String courseGrade;

				if (courseGradeVisible) {
					courseGrade = studentGradeInfo.getCourseGrade();
				} else {
					courseGrade = getString("label.coursegrade.nopermission");
				}

				final Label courseGradeLabel = new Label(componentId, Model.of(courseGrade)) {
					@Override
					public void onEvent(final IEvent<?> event) {
						super.onEvent(event);
						if (event.getPayload() instanceof ScoreChangedEvent) {
							final ScoreChangedEvent scoreChangedEvent = (ScoreChangedEvent) event.getPayload();
							if (studentGradeInfo.getStudentUuid().equals(scoreChangedEvent.getStudentUuid())) {
								final CourseGrade courseGrade = GradebookPage.this.businessService
										.getCourseGrade(scoreChangedEvent.getStudentUuid());
								((Model<String>) getDefaultModel()).setObject(courseGrade.getMappedGrade());

								scoreChangedEvent.getTarget().add(this);
								scoreChangedEvent.getTarget().appendJavaScript(
										String.format("$('#%s').closest('td').addClass('gb-score-dynamically-updated');",
												this.getMarkupId()));
							}
						}
					}
				};
				courseGradeLabel.setOutputMarkupId(true);
				cellItem.add(courseGradeLabel);
			}
		};
		cols.add(courseGradeColumn);

		// build the rest of the columns based on the assignment list
		for (final Assignment assignment : assignments) {

			final AbstractColumn column = new AbstractColumn(new Model("")) {

				@Override
				public Component getHeader(final String componentId) {
					final AssignmentColumnHeaderPanel panel = new AssignmentColumnHeaderPanel(componentId,
							new Model<Assignment>(assignment));

					final String category = assignment.getCategoryName();

					int order = -1;
					if (categorizedAssignmentOrder.containsKey(category)) {
						order = categorizedAssignmentOrder.get(category).indexOf(assignment.getId());
					}

					panel.add(new AttributeModifier("data-category", category));
					panel.add(new AttributeModifier("data-categorized-order", order));

					final StringValue createdAssignmentId = getPageParameters().get(CREATED_ASSIGNMENT_ID_PARAM);
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
				public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
					final GbStudentGradeInfo studentGrades = (GbStudentGradeInfo) rowModel.getObject();

					final GbGradeInfo gradeInfo = studentGrades.getGrades().get(assignment.getId());

					final Map<String, Object> modelData = new HashMap<>();
					modelData.put("assignmentId", assignment.getId());
					modelData.put("assignmentPoints", assignment.getPoints());
					modelData.put("studentUuid", studentGrades.getStudentUuid());
					modelData.put("categoryId", assignment.getCategoryId());
					modelData.put("isExternal", assignment.isExternallyMaintained());
					modelData.put("externalAppName", assignment.getExternalAppName());
					modelData.put("gradeInfo", gradeInfo);
					modelData.put("role", GradebookPage.this.role);

					cellItem.add(new GradeItemCellPanel(componentId, Model.ofMap(modelData)));

					cellItem.setOutputMarkupId(true);
				}

			};

			cols.add(column);
		}

		// render the categories
		// Display rules:
		// 1. only show categories if the global setting is enabled
		// 2. only show categories if they have items
		// TODO may be able to pass this list into the matrix to save another lookup in there)

		List<CategoryDefinition> categories = new ArrayList<>();

		if (categoriesEnabled) {

			// only work with categories if enabled
			categories = this.businessService.getGradebookCategories();

			// remove those that have no assignments
			categories.removeIf(cat -> cat.getAssignmentList().isEmpty());

			for (final CategoryDefinition category : categories) {

				if (category.getAssignmentList().isEmpty()) {
					continue;
				}

				final AbstractColumn column = new AbstractColumn(new Model("")) {

					@Override
					public Component getHeader(final String componentId) {
						final CategoryColumnHeaderPanel panel = new CategoryColumnHeaderPanel(componentId,
								new Model<CategoryDefinition>(category));

						panel.add(new AttributeModifier("data-category", category.getName()));

						return panel;
					}

					@Override
					public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
						final GbStudentGradeInfo studentGrades = (GbStudentGradeInfo) rowModel.getObject();

						final Double score = studentGrades.getCategoryAverages().get(category.getId());

						final Map<String, Object> modelData = new HashMap<>();
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
		}

		Temp.time("all Columns added", stopwatch.getTime());

		// TODO make this AjaxFallbackDefaultDataTable
		final DataTable table = new DataTable("table", cols, studentGradeMatrix, 100);
		table.addBottomToolbar(new NavigationToolbar(table));
		table.addTopToolbar(new HeadersToolbar(table, null));
		table.add(new AttributeModifier("data-siteid", this.businessService.getCurrentSiteId()));

		final WebMarkupContainer noAssignments = new WebMarkupContainer("noAssignments");
		noAssignments.setVisible(false);
		this.form.add(noAssignments);

		final WebMarkupContainer noStudents = new WebMarkupContainer("noStudents");
		noStudents.setVisible(false);
		this.form.add(noStudents);

		this.form.add(table);

		// Populate the toolbar
		final Label gradeItemSummary = new Label("gradeItemSummary", new StringResourceModel("label.toolbar.gradeitemsummary", null,
				assignments.size() + categories.size(), assignments.size() + categories.size()));
		gradeItemSummary.setEscapeModelStrings(false);
		this.form.add(gradeItemSummary);

		final WebMarkupContainer toggleGradeItemsToolbarItem = new WebMarkupContainer("toggleGradeItemsToolbarItem");
		this.form.add(toggleGradeItemsToolbarItem);

		final AjaxButton toggleCategoriesToolbarItem = new AjaxButton("toggleCategoriesToolbarItem") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (settings.isCategoriesEnabled()) {
					add(new AttributeModifier("class", "on"));
				}
			}

			@Override
			protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
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
		this.form.add(toggleCategoriesToolbarItem);

		// section and group dropdown
		final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();

		// add the default ALL group to the list
		groups.add(0, new GbGroup(null, getString("groups.all"), null, GbGroup.Type.ALL));

		final DropDownChoice<GbGroup> groupFilter = new DropDownChoice<GbGroup>("groupFilter", new Model<GbGroup>(), groups,
				new ChoiceRenderer<GbGroup>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(final GbGroup g) {
						return g.getTitle();
					}

					@Override
					public String getIdValue(final GbGroup g, final int index) {
						return g.getId();
					}

				});

		groupFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				final GbGroup selected = (GbGroup) groupFilter.getDefaultModelObject();

				// store selected group (null ok)
				final GradebookUiSettings settings = getUiSettings();
				settings.setGroupFilter(selected);
				setUiSettings(settings);

				// refresh
				setResponsePage(new GradebookPage());
			}

		});

		// set selected group, or first item in list
		groupFilter.setModelObject((settings.getGroupFilter() != null) ? settings.getGroupFilter() : groups.get(0));
		groupFilter.setNullValid(false);
		this.form.add(groupFilter);

		final ToggleGradeItemsToolbarPanel gradeItemsTogglePanel = new ToggleGradeItemsToolbarPanel("gradeItemsTogglePanel",
				Model.ofList(assignments));
		add(gradeItemsTogglePanel);

		// hide/show components

		// no assignments, hide table, show message
		if (assignments.isEmpty()) {
			table.setVisible(false);
			toggleGradeItemsToolbarItem.setVisible(false);
			noAssignments.setVisible(true);
		}

		// no visible students, show table, show message
		// don't want two messages though, hence the else
		else if (studentGradeMatrix.size() == 0) {
			noStudents.setVisible(true);
		}

		Temp.time("Gradebook page done", stopwatch.getTime());
	}

	/**
	 * Getters for panels to get at modal windows
	 *
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

		if (settings == null) {
			settings = new GradebookUiSettings();
			settings.setCategoriesEnabled(this.businessService.categoriesAreEnabled());
		}

		return settings;
	}

	public void setUiSettings(final GradebookUiSettings settings) {
		Session.get().setAttribute("GBNG_UI_SETTINGS", settings);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		// Drag and Drop/Date Picker (requires jQueryUI)
		response.render(JavaScriptHeaderItem.forUrl(String.format("/library/js/jquery/ui/1.11.3/jquery-ui.min.js?version=%s", version)));

		// Include Sakai Date Picker
		response.render(JavaScriptHeaderItem.forUrl(String.format("/library/js/lang-datepicker/lang-datepicker.js?version=%s", version)));

		// GradebookNG Grade specific styles and behaviour
		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-grades.css?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grades.js?version=%s", version)));
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grade-summary.js?version=%s", version)));
		response.render(
				JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-update-ungraded.js?version=%s", version)));
	}
}
