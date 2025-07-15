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
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
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
import org.sakaiproject.gradebookng.tool.actions.EditCourseGradeCommentAction;
import org.sakaiproject.gradebookng.tool.actions.EditSettingsAction;
import org.sakaiproject.gradebookng.tool.actions.ExcuseGradeAction;
import org.sakaiproject.gradebookng.tool.actions.GradeUpdateAction;
import org.sakaiproject.gradebookng.tool.actions.MoveAssignmentAction;
import org.sakaiproject.gradebookng.tool.actions.OverrideCourseGradeAction;
import org.sakaiproject.gradebookng.tool.actions.QuickEntryAction;
import org.sakaiproject.gradebookng.tool.actions.SetScoreForUngradedAction;
import org.sakaiproject.gradebookng.tool.actions.SetStudentNameOrderAction;
import org.sakaiproject.gradebookng.tool.actions.SetZeroScoreAction;
import org.sakaiproject.gradebookng.tool.actions.ToggleCourseGradePoints;
import org.sakaiproject.gradebookng.tool.actions.ViewAssignmentStatisticsAction;
import org.sakaiproject.gradebookng.tool.actions.ViewCourseGradeLogAction;
import org.sakaiproject.gradebookng.tool.actions.ViewCourseGradeStatisticsAction;
import org.sakaiproject.gradebookng.tool.actions.CourseGradeBreakdownAction;
import org.sakaiproject.gradebookng.tool.actions.ViewGradeLogAction;
import org.sakaiproject.gradebookng.tool.actions.ViewGradeSummaryAction;
import org.sakaiproject.gradebookng.tool.actions.ViewRubricGradeAction;
import org.sakaiproject.gradebookng.tool.actions.ViewRubricPreviewAction;
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
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GraderPermission;
import org.sakaiproject.grading.api.PermissionDefinition;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.wicket.component.SakaiAjaxButton;

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
	GbModalWindow rubricPreviewWindow;
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

	private String gradebookUid;
	private String siteId;

	private final WebMarkupContainer tableArea;

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public GradebookPage() {
		disableLink(this.gradebookPageLink);

		if (this.role == GbRole.NONE) {
			sendToAccessDeniedPage(getString("error.role"));
		}

		gradebookUid = getCurrentGradebookUid();
		siteId = getCurrentSiteId();

		// get Gradebook to save additional calls later
		final Gradebook gradebook = this.businessService.getGradebook(gradebookUid, siteId);

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
			this.permissions = this.businessService.getPermissionsForUser(this.currentUserUuid, gradebookUid, siteId);
			if (this.permissions.isEmpty()
					|| (this.permissions.size() == 1 && StringUtils.equals(this.permissions.get(0).getFunctionName(), GraderPermission.NONE.toString()))) {
				sendToAccessDeniedPage(getString("ta.nopermission"));
			}
		}
		// This is not a Student or TA, so it is either custom role or an Instructor.
		else if (!this.businessService.isUserAbleToEditAssessments(siteId)) {
			sendToAccessDeniedPage(getString("ta.nopermission"));
		}

		final GbStopWatch stopwatch = new GbStopWatch();
		stopwatch.start();
		stopwatch.time("GradebookPage init", stopwatch.getTime());

		this.form = new Form<>("form");
		add(this.form);

		this.form.add(new AttributeModifier("data-site-id", siteId));
		this.form.add(new AttributeModifier("data-guid", gradebook != null ? gradebookUid : siteId));
		this.form.add(new AttributeModifier("data-gradestimestamp", new Date().getTime()));

		/**
		 * Note that SEMI_TRANSPARENT has a 100% black background and TRANSPARENT is overridden to 10% opacity
		 */
		this.addOrEditGradeItemWindow = new GbModalWindow("addOrEditGradeItemWindow");
		this.addOrEditGradeItemWindow.showUnloadConfirmation(false);
		this.addOrEditGradeItemWindow.setCssClassName("w_blue modal-add-or-edit-gbitem");
		this.form.add(this.addOrEditGradeItemWindow);

		this.studentGradeSummaryWindow = new GbModalWindow("studentGradeSummaryWindow");
		this.studentGradeSummaryWindow.setWidthUnit("%");
		this.studentGradeSummaryWindow.setInitialWidth(70);
		this.form.add(this.studentGradeSummaryWindow);

		this.updateUngradedItemsWindow = new GbModalWindow("updateUngradedItemsWindow");
		this.form.add(this.updateUngradedItemsWindow);

		this.rubricGradeWindow = new GbModalWindow("rubricGradeWindow");
		this.form.add(this.rubricGradeWindow);

		this.rubricPreviewWindow = new GbModalWindow("rubricPreviewWindow");
		this.form.add(this.rubricPreviewWindow);

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
		this.form.add(this.assignmentStatisticsWindow);

		this.updateCourseGradeDisplayWindow = new GbModalWindow("updateCourseGradeDisplayWindow");
		this.form.add(this.updateCourseGradeDisplayWindow);

		this.courseGradeStatisticsWindow = new GbModalWindow("courseGradeStatisticsWindow");
		this.form.add(this.courseGradeStatisticsWindow);

		this.bulkEditItemsWindow = new GbModalWindow("bulkEditItemsWindow");
		this.bulkEditItemsWindow.setWidthUnit("%");
		this.bulkEditItemsWindow.setInitialWidth(65);
		this.bulkEditItemsWindow.showUnloadConfirmation(false);
		this.form.add(this.bulkEditItemsWindow);

		add(new CloseOnESCBehavior(bulkEditItemsWindow));

		// first get any settings data from the session
		final GradebookUiSettings settings = getUiSettings();

		SortType sortBy = SortType.SORT_BY_SORTING;
		if (settings.isCategoriesEnabled() && settings.isGroupedByCategory()) {
			// Pre-sort assignments by the categorized sort order
			sortBy = SortType.SORT_BY_CATEGORY;
			this.form.add(new AttributeAppender("class", "gb-grouped-by-category", " "));
		}
		// section and group dropdown
		final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups(gradebookUid, siteId);

		final DropDownChoice<GbGroup> groupFilter = new DropDownChoice<>("groupFilter", new Model<>(),
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
 
		if (!gradebookUid.equals(siteId)) {
			for (GbGroup g : groups) {
				if (g.getId() != null && g.getId().equals(gradebookUid)) {
					settings.setGroupFilter(g);
					setUiSettings(settings);
					groupFilter.setVisible(false);
					break;
				}
			}
		}

		final List<Assignment> assignments = this.businessService.getGradebookAssignments(gradebookUid, siteId, sortBy);
		String selectedGroup = (settings.getGroupFilter() != null && !GbGroup.Type.ALL.equals(settings.getGroupFilter().getType())) ? settings.getGroupFilter().getId() : null;
		final List<String> students = this.businessService.getGradeableUsers(gradebookUid, siteId, selectedGroup);

		this.hasGradebookItems = !assignments.isEmpty();
		this.hasStudents = !students.isEmpty();
		// categories enabled?
		final boolean categoriesEnabled = this.businessService.categoriesAreEnabled(gradebookUid, siteId);

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
				return businessService.isUserAbleToEditAssessments(siteId);
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
						return new GbGradeTableData(gradebookUid, siteId, businessService, settings, toolManager, rubricsService);
					}
				});
		GradeUpdateAction setScore = new GradeUpdateAction();
		setScore.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("setScore", setScore);
		ViewRubricGradeAction gradeRubric = new ViewRubricGradeAction();
		gradeRubric.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("gradeRubric", gradeRubric);
		this.gradeTable.addEventListener("viewLog", new ViewGradeLogAction());
		EditAssignmentAction editAssignment = new EditAssignmentAction();
		editAssignment.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("editAssignment", editAssignment);
		ViewAssignmentStatisticsAction viewStatistics = new ViewAssignmentStatisticsAction();
		viewStatistics.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("viewStatistics", viewStatistics);
		OverrideCourseGradeAction overrideCourseGrade = new OverrideCourseGradeAction();
		overrideCourseGrade.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("overrideCourseGrade", overrideCourseGrade);
		EditCommentAction editComment = new EditCommentAction();
		editComment.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("editComment", editComment);
		ViewGradeSummaryAction viewGradeSummary = new ViewGradeSummaryAction();
		viewGradeSummary.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("viewGradeSummary", viewGradeSummary);
		SetZeroScoreAction setZeroScore = new SetZeroScoreAction();
		setZeroScore.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("setZeroScore", setZeroScore);
		ViewCourseGradeLogAction viewCourseGradeLog = new ViewCourseGradeLogAction();
		viewCourseGradeLog.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("viewCourseGradeLog", viewCourseGradeLog);
		DeleteAssignmentAction deleteAssignment = new DeleteAssignmentAction();
		deleteAssignment.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("deleteAssignment", deleteAssignment);
		SetScoreForUngradedAction setUngraded = new SetScoreForUngradedAction();
		setUngraded.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("setUngraded", setUngraded);
		this.gradeTable.addEventListener("setStudentNameOrder", new SetStudentNameOrderAction());
		this.gradeTable.addEventListener("toggleCourseGradePoints", new ToggleCourseGradePoints());
		this.gradeTable.addEventListener("editSettings", new EditSettingsAction());
		MoveAssignmentAction moveAssignmentLeft = new MoveAssignmentAction(-1);
		moveAssignmentLeft.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("moveAssignmentLeft", moveAssignmentLeft);
		MoveAssignmentAction moveAssignmentRight = new MoveAssignmentAction(1);
		moveAssignmentRight.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("moveAssignmentRight", moveAssignmentRight);
		ViewCourseGradeStatisticsAction viewCourseGradeStatistics = new ViewCourseGradeStatisticsAction();
		viewCourseGradeStatistics.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("viewCourseGradeStatistics", viewCourseGradeStatistics);
		ExcuseGradeAction excuseGrade = new ExcuseGradeAction();
		excuseGrade.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("excuseGrade", excuseGrade);
		EditCourseGradeCommentAction editCourseGradeComment = new EditCourseGradeCommentAction();
		editCourseGradeComment.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("editCourseGradeComment", editCourseGradeComment);
		ViewRubricPreviewAction previewRubric = new ViewRubricPreviewAction();
		previewRubric.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("previewRubric", previewRubric);
		QuickEntryAction quickEntry = new QuickEntryAction();
		quickEntry.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("quickEntry", quickEntry);
		CourseGradeBreakdownAction viewCourseGradeBreakdown = new CourseGradeBreakdownAction();
		viewCourseGradeBreakdown.setCurrentGradebookAndSite(gradebookUid, siteId);
		this.gradeTable.addEventListener("viewCourseGradeBreakdown", viewCourseGradeBreakdown);

		this.tableArea.add(this.gradeTable);

		final SakaiAjaxButton toggleCategoriesToolbarItem = new SakaiAjaxButton("toggleCategoriesToolbarItem") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				String iconCssClass = settings.isGroupedByCategory() ? " si-check-square" : " si-empty-square";
				add(new AttributeAppender("class", iconCssClass));
				add(new AttributeModifier("aria-pressed", settings.isGroupedByCategory()));
				setWillRenderOnClick(true);
			}

			@Override
			public void onSubmit(AjaxRequestTarget target) {
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
		final GbAjaxLink<Void> sortGradeItemsToolbarItem = new GbAjaxLink<>("sortGradeItemsToolbarItem") {
			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = GradebookPage.this.getSortGradeItemsWindow();

				final Map<String, Object> model = new HashMap<>();
				model.put("categoriesEnabled", categoriesEnabled);
				model.put("settings", settings);

				window.setTitle(getString("sortgradeitems.heading"));
				SortGradeItemsPanel sgip = new SortGradeItemsPanel(window.getContentId(), Model.ofMap(model), window);
				sgip.setCurrentGradebookAndSite(gradebookUid, siteId);
				window.setContent(sgip);
				window.setComponentToReturnFocusTo(this);
				window.show(target);
			}

			@Override
			public boolean isVisible() {
				return (GradebookPage.this.businessService.isUserAbleToEditAssessments(siteId));
			}
		};
		toolbarColumnTools.add(sortGradeItemsToolbarItem);

		// bulk edit items button
		final GbAjaxLink<Void> bulkEditItemsToolbarItem = new GbAjaxLink<>("bulkEditItemsToolbarItem") {
			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GbModalWindow window = GradebookPage.this.getBulkEditItemsWindow();

				window.setTitle(getString("bulkedit.heading"));
				BulkEditItemsPanel panel = new BulkEditItemsPanel(window.getContentId(), window);
				panel.setCurrentGradebookAndSite(gradebookUid, siteId);
				window.setContent(panel.setOutputMarkupId(true));
				window.setComponentToReturnFocusTo(this);
				window.show(target);
				target.appendJavaScript("GBBE.init('" + panel.getMarkupId() + "');");
			}

			@Override
			public boolean isVisible() {
				return (GradebookPage.this.businessService.isUserAbleToEditAssessments(siteId));
			}
		};
		toolbarColumnTools.add(bulkEditItemsToolbarItem);

		// if only one group, just show the title
		// otherwise add the 'all groups' option
		// cater for the case where there is only one group visible to TA but they can see everyone.
		if (this.role == GbRole.TA) {

			// if only one group, hide the filter
			if (groups.size() == 1) {
				this.showGroupFilter = false;

				// but need to double check permissions to see if we have any permissions with no group reference
				this.permissions.forEach(p -> {
					if (!StringUtils.equalsIgnoreCase(p.getFunctionName(), GraderPermission.VIEW_COURSE_GRADE.toString())
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
			groups.add(0, new GbGroup("allGroups", allGroupsTitle, null, GbGroup.Type.ALL));
		}

		groupFilter.add(new AjaxFormComponentUpdatingBehavior("change") {
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
		groupFilter.setVisible(groups.size() > 1 && gradebookUid.equals(siteId));

		final WebMarkupContainer studentFilter = new WebMarkupContainer("studentFilter");
		studentFilter.setVisible(this.hasStudents);
		toolbar.add(studentFilter);

		this.tableArea.add(groupFilter);

		final Map<String, Object> togglePanelModel = new HashMap<>();
		togglePanelModel.put("assignments", this.businessService.getGradebookAssignments(gradebookUid, siteId, sortBy));
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

	public GbModalWindow getRubricPreviewWindow() {
		return this.rubricPreviewWindow;
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
			settings.setCategoriesEnabled(this.businessService.categoriesAreEnabled(gradebookUid, siteId));
			settings.initializeCategoryColors(this.businessService.getGradebookCategories(gradebookUid, siteId));
			settings.setCategoryColor(getString(GradebookPage.UNCATEGORISED), GradebookUiSettings.generateRandomRGBColorString(null));
			setUiSettings(settings);
		}

		// See if the user has a database-persisted preference for Group by Category
		String userGbUiCatPref = this.getUserGbPreference("GROUP_BY_CAT");
		if (StringUtils.isNotBlank(userGbUiCatPref)) {
			settings.setGroupedByCategory(Boolean.parseBoolean(userGbUiCatPref));
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
			this.setUserGbPreference("GROUP_BY_CAT", settings.isGroupedByCategory() + "");
		}
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = PortalUtils.getCDNQuery();

		// tablesorted used by student grade summary
		response.render(JavaScriptHeaderItem.forScript("includeWebjarLibrary('jquery.tablesorter')", null));
		response.render(
				JavaScriptHeaderItem.forScript("includeWebjarLibrary('jquery.tablesorter/2.27.7/dist/css/theme.bootstrap.min.css')", null));

		//Feedback reminder for instructors
		response.render(
				JavaScriptHeaderItem.forScript("includeWebjarLibrary('awesomplete')", null));

		// GradebookNG Grade specific behaviour
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
		if(!showPopupForNewItem.isNull() && !focusAssignmentId.isNull() && this.hasStudents){
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

		@Override
		public void onSubmit(final AjaxRequestTarget target) {
			final GbModalWindow window = getAddOrEditGradeItemWindow();
			window.setTitle(getString("heading.addgradeitem"));
			window.setComponentToReturnFocusTo(this);
			AddOrEditGradeItemPanel aegip = new AddOrEditGradeItemPanel(window.getContentId(), window, null);
			aegip.setCurrentGradebookAndSite(gradebookUid, siteId);
			window.setContent(aegip);
			window.show(target);
		}

		@Override
		public boolean isVisible() {
			return businessService.isUserAbleToEditAssessments(siteId) && GradebookPage.this.hasGradebookItems;
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
