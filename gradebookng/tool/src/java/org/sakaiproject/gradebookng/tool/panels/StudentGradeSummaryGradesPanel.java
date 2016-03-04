package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.pages.BasePage;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;

public class StudentGradeSummaryGradesPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	GbCategoryType configuredCategoryType;

	// used as a visibility flag. if any are released, show the table
	boolean someAssignmentsReleased = false;

	public StudentGradeSummaryGradesPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		final String userId = (String) modelData.get("userId");

		// get grades
		final Map<Assignment, GbGradeInfo> grades = this.businessService.getGradesForStudent(userId);
		final List<Assignment> assignments = new ArrayList(grades.keySet());

		// get gradebook
		final Gradebook gradebook = this.businessService.getGradebook();

		// get configured category type
		// TODO this can come from the Gradebook object above rather than a separate lookup
		this.configuredCategoryType = this.businessService.getGradebookCategoryType();

		// setup
		final List<String> categoryNames = new ArrayList<String>();
		final Map<String, List<Assignment>> categoryNamesToAssignments = new HashMap<String, List<Assignment>>();
		final Map<String, String> categoryAverages = new HashMap<>();

		// if gradebook release setting disabled, no work to do
		if (!gradebook.isAssignmentsDisplayed()) {

			// iterate over assignments and build map of categoryname to list of assignments as well as category averages
			for (final Assignment assignment : assignments) {

				// if an assignment is released, update the flag (but don't set it false again)
				// then build the category map. we don't do any of this for unreleased gradebook items
				if (assignment.isReleased()) {
					this.someAssignmentsReleased = true;

					final String categoryName = getCategoryName(assignment);

					if (!categoryNamesToAssignments.containsKey(categoryName)) {
						categoryNames.add(categoryName);
						categoryNamesToAssignments.put(categoryName, new ArrayList<Assignment>());

						final Double categoryAverage = this.businessService.getCategoryScoreForStudent(assignment.getCategoryId(), userId);
						if (categoryAverage == null || categoryName.equals(GradebookPage.UNCATEGORISED)) {
							categoryAverages.put(categoryName, getString("label.nocategoryscore"));
						} else {
							categoryAverages.put(categoryName, FormatHelper.formatDoubleAsPercentage(categoryAverage));
						}
					}

					categoryNamesToAssignments.get(categoryName).add(assignment);
				}
			}
			Collections.sort(categoryNames);
		}

		// output all of the categories
		// within each we then add the assignments in each category
		add(new ListView<String>("categoriesList", categoryNames) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<String> categoryItem) {
				final String categoryName = categoryItem.getModelObject();

				final List<Assignment> categoryAssignments = categoryNamesToAssignments.get(categoryName);

				categoryItem.add(new Label("category", categoryName));
				categoryItem.add(new Label("categoryGrade", categoryAverages.get(categoryName)));

				String categoryWeight = "";
				if (!categoryAssignments.isEmpty()) {
					final Double weight = categoryAssignments.get(0).getWeight();
					if (weight != null) {
						categoryWeight = FormatHelper.formatDoubleAsPercentage(weight * 100);
					}
				}
				categoryItem.add(new Label("categoryWeight", categoryWeight));

				categoryItem.add(new ListView<Assignment>("assignmentsForCategory", categoryAssignments) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(final ListItem<Assignment> assignmentItem) {
						final Assignment assignment = assignmentItem.getModelObject();

						if (!assignment.isReleased()) {
							assignmentItem.setVisible(false);
						}

						final GbGradeInfo gradeInfo = grades.get(assignment);

						final String rawGrade;
						String comment;
						if (gradeInfo != null) {
							rawGrade = gradeInfo.getGrade();
							comment = gradeInfo.getGradeComment();
						} else {
							rawGrade = "";
							comment = "";
						}

						final Label title = new Label("title", assignment.getName());
						assignmentItem.add(title);

						final BasePage page = (BasePage) getPage();
						final WebMarkupContainer flags = new WebMarkupContainer("flags");
						flags.add(page.buildFlagWithPopover("isExtraCredit", getString("label.gradeitem.extracredit"))
								.setVisible(assignment.getExtraCredit()));
						flags.add(page.buildFlagWithPopover("isNotCounted", getString("label.gradeitem.notcounted"))
								.setVisible(!assignment.isCounted()));
						assignmentItem.add(flags);

						assignmentItem.add(new Label("dueDate",
								FormatHelper.formatDate(assignment.getDueDate(), getString("label.studentsummary.noduedate"))));
						assignmentItem.add(new Label("grade", FormatHelper.formatGrade(rawGrade)));
						assignmentItem.add(new Label("outOf",
								new StringResourceModel("label.studentsummary.outof", null, new Object[] { assignment.getPoints() })) {
							@Override
							public boolean isVisible() {
								return StringUtils.isNotBlank(rawGrade);
							}
						});
						assignmentItem.add(new Label("comments", comment));
					}

					@Override
					public void renderHead(final IHeaderResponse response) {
						super.renderHead(response);

						// hide the weight column if weightings are not enabled
						if (!isCategoryWeightEnabled()) {
							response.render(OnDomReadyHeaderItem.forScript("$('.weight-col').hide();"));
						}

					}
				});
			}

			// used as a general visiblity for the entire table. If no assignments, either there are none or none are released.
			@Override
			public boolean isVisible() {
				return StudentGradeSummaryGradesPanel.this.someAssignmentsReleased;
			}
		});

		// no assignmnts message
		final WebMarkupContainer noAssignments = new WebMarkupContainer("noAssignments") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !StudentGradeSummaryGradesPanel.this.someAssignmentsReleased;
			}
		};
		add(noAssignments);

		if (gradebook.isCourseGradeDisplayed()) {

			// check permission for current user to view course grade
			// otherwise fetch and render it
			final String currentUserUuid = this.businessService.getCurrentUser().getId();
			if (!this.businessService.isCourseGradeVisible(currentUserUuid)) {
				add(new Label("courseGrade", new ResourceModel("label.coursegrade.nopermission")));
			} else {
				final CourseGrade courseGrade = this.businessService.getCourseGrade(userId);
				if (StringUtils.isBlank(courseGrade.getEnteredGrade()) && StringUtils.isBlank(courseGrade.getMappedGrade())) {
					add(new Label("courseGrade", new ResourceModel("label.studentsummary.coursegrade.none")));
				} else if (StringUtils.isNotBlank(courseGrade.getEnteredGrade())) {
					add(new Label("courseGrade", courseGrade.getEnteredGrade()));
				} else {
					add(new Label("courseGrade", new StringResourceModel("label.studentsummary.coursegrade.display", null, new Object[] {
							courseGrade.getMappedGrade(), FormatHelper.formatStringAsPercentage(courseGrade.getCalculatedGrade()) })));
				}
			}
		} else {
			add(new Label("courseGrade", getString("label.studentsummary.coursegradenotreleased")));
		}

		add(new AttributeModifier("data-studentid", userId));
	}

	/**
	 * Helper to get the category name. Looks at settings as well.
	 *
	 * @param assignment
	 * @return
	 */
	private String getCategoryName(final Assignment assignment) {
		if (this.configuredCategoryType == GbCategoryType.NO_CATEGORY) {
			return getString("gradebookpage.uncategorised");
		}
		return StringUtils.isBlank(assignment.getCategoryName()) ? getString(GradebookPage.UNCATEGORISED) : assignment.getCategoryName();
	}

	/**
	 * Helper to determine if weightings are enabled
	 *
	 * @return
	 */
	private boolean isCategoryWeightEnabled() {
		return (this.configuredCategoryType == GbCategoryType.WEIGHTED_CATEGORY) ? true : false;
	}
}
