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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.util.GbStopWatch;
import org.sakaiproject.gradebookng.tool.actions.DeleteAssignmentAction;
import org.sakaiproject.gradebookng.tool.actions.EditAssignmentAction;
import org.sakaiproject.gradebookng.tool.actions.EditCommentAction;
import org.sakaiproject.gradebookng.tool.actions.EditSettingsAction;
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
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.component.GbGradeTable;
import org.sakaiproject.gradebookng.tool.model.GbGradeTableData;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.panels.AddOrEditGradeItemPanel;
import org.sakaiproject.gradebookng.tool.panels.SortGradeItemsPanel;
import org.sakaiproject.gradebookng.tool.panels.ToggleGradeItemsToolbarPanel;
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

	// flag to indicate a category is uncategorised
	// doubles as a translation key
	public static final String UNCATEGORISED = "gradebookpage.uncategorised";

	GbModalWindow addOrEditGradeItemWindow;
	GbModalWindow studentGradeSummaryWindow;
	GbModalWindow updateUngradedItemsWindow;
	GbModalWindow gradeLogWindow;
	GbModalWindow gradeCommentWindow;
	GbModalWindow deleteItemWindow;
	GbModalWindow assignmentStatisticsWindow;
	GbModalWindow updateCourseGradeDisplayWindow;
	GbModalWindow sortGradeItemsWindow;
	GbModalWindow courseGradeStatisticsWindow;

	Label liveGradingFeedback;
	boolean hasAssignmentsAndGrades;
	private static final AttributeModifier DISPLAY_NONE = new AttributeModifier("style", "display: none");

	Form<Void> form;

	List<PermissionDefinition> permissions = new ArrayList<>();
	boolean showGroupFilter = true;
	private GbGradeTable gradeTable;

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
			if (this.permissions.isEmpty()) {
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

		final GbAjaxButton addGradeItem = new GbAjaxButton("addGradeItem") {
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
				return GradebookPage.this.role == GbRole.INSTRUCTOR;
			}
		};
		addGradeItem.setDefaultFormProcessing(false);
		addGradeItem.setOutputMarkupId(true);
		this.form.add(addGradeItem);

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

		this.hasAssignmentsAndGrades = !assignments.isEmpty() && !students.isEmpty();

		// categories enabled?
		final boolean categoriesEnabled = this.businessService.categoriesAreEnabled();

		// grading type?
		final GradingType gradingType = GradingType.valueOf(gradebook.getGrade_type());

		final WebMarkupContainer noAssignments = new WebMarkupContainer("noAssignments");
		noAssignments.setVisible(assignments.isEmpty());
		this.form.add(noAssignments);

		final WebMarkupContainer noStudents = new WebMarkupContainer("noStudents");
		noStudents.setVisible(students.isEmpty());
		this.form.add(noStudents);

		// Populate the toolbar
		final WebMarkupContainer toolbar = new WebMarkupContainer("toolbar");
		this.form.add(toolbar);

		final WebMarkupContainer toggleGradeItemsToolbarItem = new WebMarkupContainer("toggleGradeItemsToolbarItem");
		toolbar.add(toggleGradeItemsToolbarItem);

		this.gradeTable = new GbGradeTable("gradeTable",
				new LoadableDetachableModel() {
					@Override
					public GbGradeTableData load() {
						return new GbGradeTableData(GradebookPage.this.businessService, settings);
					}
				});
		this.gradeTable.addEventListener("setScore", new GradeUpdateAction());
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


		this.form.add(this.gradeTable);

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
				setUiSettings(settings);

				// refresh
				setResponsePage(GradebookPage.class);
			}

			@Override
			public boolean isVisible() {
				return categoriesEnabled;
			}
		};
		toolbar.add(toggleCategoriesToolbarItem);

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
				return GradebookPage.this.role == GbRole.INSTRUCTOR;
			}
		};
		toolbar.add(sortGradeItemsToolbarItem);

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
		if (groups.size() == 1 || !this.hasAssignmentsAndGrades) {
			groupFilter.setVisible(false);
		}

		final WebMarkupContainer studentFilter = new WebMarkupContainer("studentFilter");
		studentFilter.setVisible(this.hasAssignmentsAndGrades);
		toolbar.add(studentFilter);

		this.form.add(groupFilter);

		final Map<String, Object> togglePanelModel = new HashMap<>();
		togglePanelModel.put("assignments", this.businessService.getGradebookAssignments(sortBy));
		togglePanelModel.put("settings", settings);
		togglePanelModel.put("categoriesEnabled", categoriesEnabled);

		final ToggleGradeItemsToolbarPanel gradeItemsTogglePanel =
			new ToggleGradeItemsToolbarPanel("gradeItemsTogglePanel", Model.ofMap(togglePanelModel));
		add(gradeItemsTogglePanel);

		this.form.add(new WebMarkupContainer("captionToggle").setVisible(this.hasAssignmentsAndGrades));

		//
		// hide/show components
		//

		// Only show the toolbar if there are students and grade items
		toolbar.setVisible(!assignments.isEmpty());

		// Show the table if there are grade items
		this.gradeTable.setVisible(!assignments.isEmpty());

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

	/**
	 * Getter for the GradebookUiSettings. Used to store a few UI related settings for the current session only.
	 *
	 */
	public GradebookUiSettings getUiSettings() {

		GradebookUiSettings settings = (GradebookUiSettings) Session.get().getAttribute("GBNG_UI_SETTINGS");

		if (settings == null) {
			settings = new GradebookUiSettings();
			settings.setCategoriesEnabled(this.businessService.categoriesAreEnabled());
			settings.initializeCategoryColors(this.businessService.getGradebookCategories());
			settings.setCategoryColor(getString(GradebookPage.UNCATEGORISED), GradebookUiSettings.generateRandomRGBColorString(null));
			setUiSettings(settings);
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
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js?version=%s", version)));

		// Include Sakai Date Picker
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/library/js/lang-datepicker/lang-datepicker.js?version=%s", version)));

		// tablesorted used by student grade summary
		response.render(CssHeaderItem
				.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/css/theme.bootstrap.min.css?version=%s", version)));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/js/jquery.tablesorter.min.js?version=%s", version)));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/js/jquery.tablesorter.widgets.min.js?version=%s", version)));

		// GradebookNG Grade specific styles and behaviour
		response.render(CssHeaderItem
				.forUrl(String.format("/gradebookng-tool/styles/gradebook-grades.css?version=%s", version)));
		response.render(CssHeaderItem
				.forUrl(String.format("/gradebookng-tool/styles/gradebook-gbgrade-table.css?version=%s", version)));
		response.render(CssHeaderItem
				.forUrl(String.format("/gradebookng-tool/styles/gradebook-sorter.css?version=%s", version)));
		response.render(CssHeaderItem
				.forUrl(String.format("/gradebookng-tool/styles/gradebook-print.css?version=%s", version), "print"));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grade-summary.js?version=%s", version)));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-update-ungraded.js?version=%s", version)));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-sorter.js?version=%s", version)));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-connection-poll.js?version=%s", version)));

		final StringValue focusAssignmentId = getPageParameters().get(FOCUS_ASSIGNMENT_ID_PARAM);
		if (!focusAssignmentId.isNull()) {
			getPageParameters().remove(FOCUS_ASSIGNMENT_ID_PARAM);
			response.render(JavaScriptHeaderItem
					.forScript(
							String.format("GbGradeTable.focusColumnForAssignmentId(%s)", focusAssignmentId.toString()),
							null));
		}
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		// add simple feedback nofication to sit above the table
		// which is reset every time the page renders
		this.liveGradingFeedback = new Label("liveGradingFeedback", getString("feedback.saved"));
		this.liveGradingFeedback.setVisible(this.hasAssignmentsAndGrades);
		this.liveGradingFeedback.setOutputMarkupId(true);
		this.liveGradingFeedback.add(DISPLAY_NONE);

		// add the 'saving...' message to the DOM as the JavaScript will
		// need to be the one that displays this message (Wicket will handle
		// the 'saved' and 'error' messages when a grade is changed
		this.liveGradingFeedback.add(new AttributeModifier("data-saving-message", getString("feedback.saving")));
		this.form.addOrReplace(this.liveGradingFeedback);
	}

	public Component updateLiveGradingMessage(final String message) {
		this.liveGradingFeedback.setDefaultModel(Model.of(message));
		if (this.liveGradingFeedback.getBehaviors().contains(DISPLAY_NONE)) {
			this.liveGradingFeedback.remove(DISPLAY_NONE);
		}
		return this.liveGradingFeedback;
	}
}
