/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.util.GbStopWatch;
import org.sakaiproject.gradebookng.tool.actions.DeleteAssignmentAction;
import org.sakaiproject.gradebookng.tool.actions.EditAssignmentAction;
import org.sakaiproject.gradebookng.tool.actions.EditCommentAction;
import org.sakaiproject.gradebookng.tool.actions.EditSettingsAction;
import org.sakaiproject.gradebookng.tool.actions.ExcuseGradeAction;
import org.sakaiproject.gradebookng.tool.actions.GradeUpdateAction;
import org.sakaiproject.gradebookng.tool.actions.MoveAssignmentLeftAction;
import org.sakaiproject.gradebookng.tool.actions.MoveAssignmentRightAction;
import org.sakaiproject.gradebookng.tool.actions.OverrideCourseGradeAction;
import org.sakaiproject.gradebookng.tool.actions.SetScoreForUngradedAction;
import org.sakaiproject.gradebookng.tool.actions.SetStudentNameOrderAction;
import org.sakaiproject.gradebookng.tool.actions.SetZeroScoreAction;
import org.sakaiproject.gradebookng.tool.actions.ToggleCourseGradePoints;
import org.sakaiproject.gradebookng.tool.actions.ViewAssignmentStatisticsAction;
import org.sakaiproject.gradebookng.tool.actions.ViewCourseGradeLogAction;
import org.sakaiproject.gradebookng.tool.actions.ViewCourseGradeStatisticsAction;
import org.sakaiproject.gradebookng.tool.actions.ViewGradeLogAction;
import org.sakaiproject.gradebookng.tool.actions.ViewGradeSummaryAction;
import org.sakaiproject.gradebookng.tool.actions.ViewRubricGradeAction;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.component.GbGradeTable;
import org.sakaiproject.gradebookng.tool.model.GbGradeTableData;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.panels.AddOrEditGradeItemPanel;
import org.sakaiproject.gradebookng.tool.panels.BulkEditItemsPanel;
import org.sakaiproject.gradebookng.tool.panels.SortGradeItemsPanel;
import org.sakaiproject.gradebookng.tool.panels.ToggleGradeItemsToolbarPanel;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.rubrics.logic.RubricsConstants;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GraderPermission;
import org.sakaiproject.service.gradebook.shared.GradingType;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Grades page. Instructors and TAs see this one. Students see the {@link StudentPage}.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradebookPage extends BasePage {
	private static final long serialVersionUID = 1L;

	public static final String FOCUS_ASSIGNMENT_ID_PARAM = "focusAssignmentId";
	public static final String NEW_GBITEM_POPOVER_PARAM = "newItem";

	// flag to indicate a category is uncategorised
	// doubles as a translation key
	public static final String UNCATEGORISED = "gradebookpage.uncategorised";

	GbModalWindow addOrEditGradeItemWindow;
	GbModalWindow studentGradeSummaryWindow;
	GbModalWindow updateUngradedItemsWindow;
	GbModalWindow rubricGradeWindow;
	GbModalWindow gradeLogWindow;
	GbModalWindow gradeCommentWindow;
	GbModalWindow deleteItemWindow;
	GbModalWindow assignmentStatisticsWindow;
	GbModalWindow updateCourseGradeDisplayWindow;
	GbModalWindow sortGradeItemsWindow;
	GbModalWindow courseGradeStatisticsWindow;
	GbModalWindow bulkEditItemsWindow;

	Label liveGradingFeedback;
	boolean hasGradebookItems, hasStudents;
	private static final AttributeModifier DISPLAY_NONE = new AttributeModifier("style", "display: none");

	Form<Void> form;

	List<PermissionDefinition> permissions = new ArrayList<>();
	boolean showGroupFilter = true;
	private GbGradeTable gradeTable;

	private final WebMarkupContainer tableArea;

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public GradebookPage() {
		disableLink(this.gradebookPageLink);

		if (this.role == GbRole.NONE) {
			sendToAccessDeniedPage(getString("error.role"));
		}

		// get Gradebook to save additional calls later
		final Gradebook gradebook = this.businessService.getGradebook();

		// students cannot access this page, they have their own
		if (this.role == GbRole.STUDENT) {
			throw new RestartResponseException(StudentPage.class);
		}

		// TAs with no permissions or in a roleswap situation
		if (this.role == GbRole.TA) {

			// roleswapped?
			if (this.businessService.isUserRoleSwapped()) {
				sendToAccessDeniedPage(getString("ta.roleswapped"));
			}

			// no perms
			this.permissions = this.businessService.getPermissionsForUser(this.currentUserUuid);
			if (this.permissions.isEmpty()
					|| (this.permissions.size() == 1 && StringUtils.equals(((PermissionDefinition) this.permissions.get(0)).getFunction(), GraderPermission.NONE.toString()))) {
				sendToAccessDeniedPage(getString("ta.nopermission"));
			}
		}

		final GbStopWatch stopwatch = new GbStopWatch();
		stopwatch.start();
		stopwatch.time("GradebookPage init", stopwatch.getTime());

		this.form = new Form<>("form");
		add(this.form);

		this.form.add(new AttributeModifier("data-siteid", this.businessService.getCurrentSiteId()));
		this.form.add(new AttributeModifier("data-gradestimestamp", new Date().getTime()));

		/**
		 * Note that SEMI_TRANSPARENT has a 100% black background and TRANSPARENT is overridden to 10% opacity
		 */
		this.addOrEditGradeItemWindow = new GbModalWindow("addOrEditGradeItemWindow");
		this.addOrEditGradeItemWindow.showUnloadConfirmation(false);
		this.form.add(this.addOrEditGradeItemWindow);

		this.studentGradeSummaryWindow = new GbModalWindow("studentGradeSummaryWindow");
		this.studentGradeSummaryWindow.setWidthUnit("%");
		this.studentGradeSummaryWindow.setInitialWidth(70);
		this.form.add(this.studentGradeSummaryWindow);

		this.updateUngradedItemsWindow = new GbModalWindow("updateUngradedItemsWindow");
		this.form.add(this.updateUngradedItemsWindow);

		this.rubricGradeWindow = new GbModalWindow("rubricGradeWindow");
		this.rubricGradeWindow.setPositionAtTop(true);
		this.form.add(this.rubricGradeWindow);

		this.gradeLogWindow = new GbModalWindow("gradeLogWindow");
		this.form.add(this.gradeLogWindow);

		this.gradeCommentWindow = new GbModalWindow("gradeCommentWindow");
		this.form.add(this.gradeCommentWindow);

		this.sortGradeItemsWindow = new GbModalWindow("sortGradeItemsWindow");
		this.sortGradeItemsWindow.showUnloadConfirmation(false);
		this.form.add(this.sortGradeItemsWindow);

		this.deleteItemWindow = new GbModalWindow("deleteItemWindow");
		this.form.add(this.deleteItemWindow);

		this.assignmentStatisticsWindow = new GbModalWindow("gradeStatisticsWindow");
		this.assignmentStatisticsWindow.setPositionAtTop(true);
		this.form.add(this.assignmentStatisticsWindow);

		this.updateCourseGradeDisplayWindow = new GbModalWindow("updateCourseGradeDisplayWindow");
		this.form.add(this.updateCourseGradeDisplayWindow);

		this.courseGradeStatisticsWindow = new GbModalWindow("courseGradeStatisticsWindow");
		this.courseGradeStatisticsWindow.setPositionAtTop(true);
		this.form.add(this.courseGradeStatisticsWindow);

		this.bulkEditItemsWindow = new GbModalWindow("bulkEditItemsWindow");
		this.bulkEditItemsWindow.setWidthUnit("%");
		this.bulkEditItemsWindow.setInitialWidth(65);
		this.bulkEditItemsWindow.setPositionAtTop(true);
		this.bulkEditItemsWindow.showUnloadConfirmation(false);
		this.form.add(this.bulkEditItemsWindow);

		add(new CloseOnESCBehavior(bulkEditItemsWindow));

		// first get any settings data from the session
		final GradebookUiSettings settings = getUiSettings();

		SortType sortBy = SortType.SORT_BY_SORTING;
		if (settings.isCategoriesEnabled() && settings.isGroupedByCategory()) {
			// Pre-sort assignments by the categorized sort order
			sortBy = SortType.SORT_BY_CATEGORY;
			this.form.add(new AttributeAppender("class", "gb-grouped-by-category"));
		}

		final List<Assignment> assignments = this.businessService.getGradebookAssignments(sortBy);
		final List<String> students = this.businessService.getGradeableUsers();

		this.hasGradebookItems = !assignments.isEmpty();
		this.hasStudents = !students.isEmpty();
		// categories enabled?
		final boolean categoriesEnabled = this.businessService.categoriesAreEnabled();

		// grading type?
		final GradingType gradingType = GradingType.valueOf(gradebook.getGrade_type());

		this.tableArea = new WebMarkupContainer("gradeTableArea");
		if (!this.hasGradebookItems) {
			this.tableArea.add(AttributeModifier.append("class", "gradeTableArea"));
		}
		this.form.add(this.tableArea);

		final GbAddButton addGradeItem = new GbAddButton("addGradeItem");
		addGradeItem.setDefaultFormProcessing(false);
		addGradeItem.setOutputMarkupId(true);
		this.tableArea.add(addGradeItem);

		final WebMarkupContainer noAssignments = new WebMarkupContainer("noAssignments");
		noAssignments.setVisible(assignments.isEmpty());
		this.tableArea.add(noAssignments);
		final GbAddButton addGradeItem2 = new GbAddButton("addGradeItem2") {
			@Override
			public boolean isVisible() {
				return businessService.isUserAbleToEditAssessments();
			}
		};
		addGradeItem2.setDefaultFormProcessing(false);
		addGradeItem2.setOutputMarkupId(true);
		noAssignments.add(addGradeItem2);

		final WebMarkupContainer noStudents = new WebMarkupContainer("noStudents");
		noStudents.setVisible(students.isEmpty());
		this.tableArea.add(noStudents);

		// Populate the toolbar
		final WebMarkupContainer toolbar = new WebMarkupContainer("toolbar");
		this.tableArea.add(toolbar);

		final WebMarkupContainer toolbarColumnTools = new WebMarkupContainer("gbToolbarColumnTools");
		toolbarColumnTools.setVisible(this.hasGradebookItems);
		toolbar.add(toolbarColumnTools);

		final WebMarkupContainer toggleGradeItemsToolbarItem = new WebMarkupContainer("toggleGradeItemsToolbarItem");
		toolbarColumnTools.add(toggleGradeItemsToolbarItem);

		this.gradeTable = new GbGradeTable("gradeTable",
				new LoadableDetachableModel() {
					@Override
					public GbGradeTableData load() {
						return new GbGradeTableData(GradebookPage.this.businessService, settings);
					}
				});
		this.gradeTable.addEventListener("setScore", new GradeUpdateAction());
		this.gradeTable.addEventListener("gradeRubric", new ViewRubricGradeAction());
		this.gradeTable.addEventListener("viewLog", new ViewGradeLogAction());
		this.gradeTable.addEventListener("editAssignment", new EditAssignmentAction());
		this.gradeTable.addEventListener("viewStatistics", new ViewAssignmentStatisticsAction());
		this.gradeTable.addEventListener("overrideCourseGrade", new OverrideCourseGradeAction());
		this.gradeTable.addEventListener("editComment", new EditCommentAction());
		this.gradeTable.addEventListener("viewGradeSummary", new ViewGradeSummaryAction());
		this.gradeTable.addEventListener("setZeroScore", new SetZeroScoreAction());
		this.gradeTable.addEventListener("viewCourseGradeLog", new ViewCourseGradeLogAction());
		this.gradeTable.addEventListener("deleteAssignment", new DeleteAssignmentAction());
		this.gradeTable.addEventListener("setUngraded", new SetScoreForUngradedAction());
		this.gradeTable.addEventListener("setStudentNameOrder", new SetStudentNameOrderAction());
		this.gradeTable.addEventListener("toggleCourseGradePoints", new ToggleCourseGradePoints());
		this.gradeTable.addEventListener("editSettings", new EditSettingsAction());
		this.gradeTable.addEventListener("moveAssignmentLeft", new MoveAssignmentLeftAction());
		this.gradeTable.addEventListener("moveAssignmentRight", new MoveAssignmentRightAction());
		this.gradeTable.addEventListener("viewCourseGradeStatistics", new ViewCourseGradeStatisticsAction());
		this.gradeTable.addEventListener("excuseGrade", new ExcuseGradeAction());

		this.tableArea.add(this.gradeTable);

		final Button toggleCategoriesToolbarItem = new Button("toggleCategoriesToolbarItem") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (settings.isGroupedByCategory()) {
					add(new AttributeAppender("class", " on"));
				}
				add(new AttributeModifier("aria-pressed", settings.isGroupedByCategory()));
			}

			@Override
			public void onSubmit() {
				settings.setGroupedByCategory(!settings.isGroupedByCategory());
				setUiSettings(settings, true);

				// refresh
				setResponsePage(GradebookPage.class);
			}

			@Override
			public boolean isVisible() {
				return categoriesEnabled;
			}
		};
		toolbarColumnTools.add(toggleCategoriesToolbarItem);

		// sort grade items button
		final GbAjaxLink sortGradeItemsToolbarItem = new GbAjaxLink("sortGradeItemsToolbarItem") {
			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = GradebookPage.this.getSortGradeItemsWindow();

				final Map<String, Object> model = new HashMap<>();
				model.put("categoriesEnabled", categoriesEnabled);
				model.put("settings", settings);

				window.setTitle(getString("sortgradeitems.heading"));
				window.setContent(new SortGradeItemsPanel(window.getContentId(), Model.ofMap(model), window));
				window.setComponentToReturnFocusTo(this);
				window.show(target);
			}

			@Override
			public boolean isVisible() {
				return (GradebookPage.this.businessService.isUserAbleToEditAssessments());
			}
		};
		toolbarColumnTools.add(sortGradeItemsToolbarItem);

		// bulk edit items button
		final GbAjaxLink bulkEditItemsToolbarItem = new GbAjaxLink("bulkEditItemsToolbarItem") {
			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = GradebookPage.this.getBulkEditItemsWindow();

				final String siteId = GradebookPage.this.businessService.getCurrentSiteId();

				window.setTitle(getString("bulkedit.heading"));
				window.setContent(new BulkEditItemsPanel(window.getContentId(), Model.of(siteId), window));
				window.setComponentToReturnFocusTo(this);
				window.show(target);
			}

			@Override
			public boolean isVisible() {
				return (GradebookPage.this.businessService.isUserAbleToEditAssessments());
			}
		};
		toolbarColumnTools.add(bulkEditItemsToolbarItem);

		// section and group dropdown
		final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();

		// if only one group, just show the title
		// otherwise add the 'all groups' option
		// cater for the case where there is only one group visible to TA but they can see everyone.
		if (this.role == GbRole.TA) {

			// if only one group, hide the filter
			if (groups.size() == 1) {
				this.showGroupFilter = false;

				// but need to double check permissions to see if we have any permissions with no group reference
				this.permissions.forEach(p -> {
					if (!StringUtils.equalsIgnoreCase(p.getFunction(), GraderPermission.VIEW_COURSE_GRADE.toString())
							&& StringUtils.isBlank(p.getGroupReference())) {
						this.showGroupFilter = true;
					}
				});
			}
		}

		if (!this.showGroupFilter) {
			toolbar.add(new Label("groupFilterOnlyOne", Model.of(groups.get(0).getTitle())));
		} else {
			toolbar.add(new EmptyPanel("groupFilterOnlyOne").setVisible(false));

			// add the default ALL group to the list
			String allGroupsTitle = getString("groups.all");
			if (this.role == GbRole.TA) {

				// does the TA have any permissions set?
				// we can assume that if they have any then there is probably some sort of group restriction so we can change the label
				if (!this.permissions.isEmpty()) {
					allGroupsTitle = getString("groups.available");
				}
			}
			groups.add(0, new GbGroup(null, allGroupsTitle, null, GbGroup.Type.ALL));
		}

		final DropDownChoice<GbGroup> groupFilter = new DropDownChoice<GbGroup>("groupFilter", new Model<GbGroup>(),
				groups, new ChoiceRenderer<GbGroup>() {
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
				setResponsePage(GradebookPage.class);
			}

		});

		// set selected group, or first item in list
		groupFilter.setModelObject((settings.getGroupFilter() != null) ? settings.getGroupFilter() : groups.get(0));
		groupFilter.setNullValid(false);

		// if only one item, hide the dropdown
		groupFilter.setVisible(groups.size() > 1 && this.hasStudents);

		final WebMarkupContainer studentFilter = new WebMarkupContainer("studentFilter");
		studentFilter.setVisible(this.hasStudents);
		toolbar.add(studentFilter);

		this.tableArea.add(groupFilter);

		final Map<String, Object> togglePanelModel = new HashMap<>();
		togglePanelModel.put("assignments", this.businessService.getGradebookAssignments(sortBy));
		togglePanelModel.put("settings", settings);
		togglePanelModel.put("categoriesEnabled", categoriesEnabled);

		final ToggleGradeItemsToolbarPanel gradeItemsTogglePanel = new ToggleGradeItemsToolbarPanel("gradeItemsTogglePanel",
				Model.ofMap(togglePanelModel));
		add(gradeItemsTogglePanel);

		this.tableArea.add(new WebMarkupContainer("captionToggle").setVisible(this.hasStudents));

		toolbar.setVisible(this.hasStudents || this.hasGradebookItems);

		stopwatch.time("Gradebook page done", stopwatch.getTime());
	}

	/**
	 * Getters for panels to get at modal windows
	 *
	 * @return
	 */
	public GbModalWindow getAddOrEditGradeItemWindow() {
		return this.addOrEditGradeItemWindow;
	}

	public GbModalWindow getStudentGradeSummaryWindow() {
		return this.studentGradeSummaryWindow;
	}

	public GbModalWindow getUpdateUngradedItemsWindow() {
		return this.updateUngradedItemsWindow;
	}

	public GbModalWindow getRubricGradeWindow() {
		return this.rubricGradeWindow;
	}

	public GbModalWindow getGradeLogWindow() {
		return this.gradeLogWindow;
	}

	public GbModalWindow getGradeCommentWindow() {
		return this.gradeCommentWindow;
	}

	public GbModalWindow getDeleteItemWindow() {
		return this.deleteItemWindow;
	}

	public GbModalWindow getAssignmentStatisticsWindow() {
		return this.assignmentStatisticsWindow;
	}

	public GbModalWindow getUpdateCourseGradeDisplayWindow() {
		return this.updateCourseGradeDisplayWindow;
	}

	public GbModalWindow getSortGradeItemsWindow() {
		return this.sortGradeItemsWindow;
	}

	public GbModalWindow getCourseGradeStatisticsWindow() {
		return this.courseGradeStatisticsWindow;
	}

	public GbModalWindow getBulkEditItemsWindow() {
		return this.bulkEditItemsWindow;
	}

	/**
	 * Getter for the GradebookUiSettings. Used to store a few UI related settings in the PreferencesService (serialized to db)
	 *
	 */
	public GradebookUiSettings getUiSettings() {

		GradebookUiSettings settings = (GradebookUiSettings) Session.get().getAttribute("GBNG_UI_SETTINGS");

		if (settings == null) {
			settings = new GradebookUiSettings();
			settings.initializeCategoryColors(this.businessService.getGradebookCategories());
			settings.setCategoryColor(getString(GradebookPage.UNCATEGORISED), GradebookUiSettings.generateRandomRGBColorString(null));
			setUiSettings(settings);
		}

		// See if the user has a database-persisted preference for Group by Category
		String userGbUiCatPref = this.businessService.getUserGbPreference("GROUP_BY_CAT");
		if (StringUtils.isNotBlank(userGbUiCatPref)) {
			settings.setCategoriesEnabled(new Boolean(userGbUiCatPref));
		}
		else {
			settings.setCategoriesEnabled(this.businessService.categoriesAreEnabled());
		}
 
		return settings;
	}

	public void setUiSettings(final GradebookUiSettings settings) {
		setUiSettings(settings, false);
	}

	public void setUiSettings(final GradebookUiSettings settings, final boolean persistToUserPrefs) {
		Session.get().setAttribute("GBNG_UI_SETTINGS", settings);

		// Save the setting to PreferencesService (database)
		if (persistToUserPrefs) {
			this.businessService.setUserGbPreference("GROUP_BY_CAT", settings.isGroupedByCategory() + "");
		}
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = PortalUtils.getCDNQuery();

		// Drag and Drop/Date Picker (requires jQueryUI)
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js%s", version)));

		// Include Sakai Date Picker
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/library/js/lang-datepicker/lang-datepicker.js%s", version)));

		// tablesorted used by student grade summary
		response.render(JavaScriptHeaderItem.forScript("includeWebjarLibrary('jquery.tablesorter')", null));
		response.render(
				JavaScriptHeaderItem.forScript("includeWebjarLibrary('jquery.tablesorter/2.27.7/dist/css/theme.bootstrap.min.css')", null));

		//Feedback reminder for instructors
		response.render(
				JavaScriptHeaderItem.forScript("includeWebjarLibrary('awesomplete')", null));

		// GradebookNG Grade specific styles and behaviour
		response.render(CssHeaderItem
				.forUrl(String.format("/gradebookng-tool/styles/gradebook-grades.css%s", version)));
		response.render(CssHeaderItem
				.forUrl(String.format("/gradebookng-tool/styles/gradebook-gbgrade-table.css%s", version)));
		response.render(CssHeaderItem
				.forUrl(String.format("/gradebookng-tool/styles/gradebook-sorter.css%s", version)));
		response.render(CssHeaderItem
				.forUrl(String.format("/gradebookng-tool/styles/gradebook-print.css%s", version), "print"));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grade-summary.js%s", version)));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-update-ungraded.js%s", version)));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-sorter.js%s", version)));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-connection-poll.js%s", version)));

		final StringValue focusAssignmentId = getPageParameters().get(FOCUS_ASSIGNMENT_ID_PARAM);
		final StringValue showPopupForNewItem = getPageParameters().get(NEW_GBITEM_POPOVER_PARAM);
		if(!showPopupForNewItem.isNull() && !focusAssignmentId.isNull()){
			getPageParameters().remove(FOCUS_ASSIGNMENT_ID_PARAM);
			getPageParameters().remove(NEW_GBITEM_POPOVER_PARAM);
			response.render(JavaScriptHeaderItem
					.forScript(
							String.format("GbGradeTable.focusColumnForAssignmentId(%s,%s)", focusAssignmentId.toString(),showPopupForNewItem),
							null));
		}
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		// add simple feedback nofication to sit above the table
		// which is reset every time the page renders
		this.liveGradingFeedback = new Label("liveGradingFeedback", getString("feedback.saved"));
		this.liveGradingFeedback.setVisible(this.hasGradebookItems && this.hasStudents);
		this.liveGradingFeedback.setOutputMarkupId(true);
		this.liveGradingFeedback.add(DISPLAY_NONE);

		// add the 'saving...' message to the DOM as the JavaScript will
		// need to be the one that displays this message (Wicket will handle
		// the 'saved' and 'error' messages when a grade is changed
		this.liveGradingFeedback.add(new AttributeModifier("data-saving-message", getString("feedback.saving")));
		this.tableArea.addOrReplace(this.liveGradingFeedback);
	}

	public Component updateLiveGradingMessage(final String message) {
		this.liveGradingFeedback.setDefaultModel(Model.of(message));
		if (this.liveGradingFeedback.getBehaviors().contains(DISPLAY_NONE)) {
			this.liveGradingFeedback.remove(DISPLAY_NONE);
		}
		return this.liveGradingFeedback;
	}

	private class GbAddButton extends GbAjaxButton {

		public GbAddButton(final String id) {
			super(id);
		}

		public GbAddButton(final String id, final Form<?> form) {
			super(id, form);
		}

		@Override
		public void onSubmit(final AjaxRequestTarget target, final Form form) {
			final GbModalWindow window = getAddOrEditGradeItemWindow();
			window.setTitle(getString("heading.addgradeitem"));
			window.setComponentToReturnFocusTo(this);
			window.setContent(new AddOrEditGradeItemPanel(window.getContentId(), window, null));
			window.show(target);
		}

		@Override
		public boolean isVisible() {
			return businessService.isUserAbleToEditAssessments() && GradebookPage.this.hasGradebookItems;
		}
	}

	
	private static class CloseOnESCBehavior extends AbstractDefaultAjaxBehavior {
        private final ModalWindow modal;
        public CloseOnESCBehavior(ModalWindow modal) {
            this.modal = modal;
        }    
        @Override
        protected void respond(AjaxRequestTarget target) {
            modal.close(target);
        }    
        @Override
        public void renderHead(Component component, IHeaderResponse response) {
            String aux = "" +
                "$(document).ready(function() {\n" +
                "  $(document).bind('keyup', function(evt) {\n" +
                "    if (evt.keyCode == 27) {\n" +
                getCallbackScript() + "\n" +
                "        evt.preventDefault();\n" +
                "    }\n" +
                "  });\n" +
                "});";
            response.render(JavaScriptHeaderItem.forScript(aux, "closeModal"));
        }
    }
}
