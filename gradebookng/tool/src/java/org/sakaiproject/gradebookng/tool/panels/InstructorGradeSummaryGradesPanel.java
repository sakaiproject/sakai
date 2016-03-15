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
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbCourseGradeLabel;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;

public class InstructorGradeSummaryGradesPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private GbStudentGradeInfo gradeInfo;
	private List<CategoryDefinition> categories;

	GbCategoryType configuredCategoryType;

	public InstructorGradeSummaryGradesPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		final String userId = (String) modelData.get("userId");

		// build the grade matrix for the user
		final List<Assignment> assignments = this.businessService.getGradebookAssignments();

		// TODO catch if this is null, the get(0) will throw an exception
		// TODO also catch the GbException
		this.gradeInfo = this.businessService.buildGradeMatrix(assignments, Collections.singletonList(userId)).get(0);
		this.categories = this.businessService.getGradebookCategories();

		// get configured category type
		// TODO this can be fetched from the Gradebook instead
		this.configuredCategoryType = this.businessService.getGradebookCategoryType();

		// setup
		final GradebookPage gradebookPage = (GradebookPage) getPage();
		final List<String> categoryNames = new ArrayList<String>();
		final Map<String, List<Assignment>> categoryNamesToAssignments = new HashMap<String, List<Assignment>>();

		// iterate over assignments and build map of categoryname to list of assignments
		for (final Assignment assignment : assignments) {

			final String categoryName = getCategoryName(assignment);

			if (!categoryNamesToAssignments.containsKey(categoryName)) {
				categoryNames.add(categoryName);
				categoryNamesToAssignments.put(categoryName, new ArrayList<Assignment>());
			}

			categoryNamesToAssignments.get(categoryName).add(assignment);
		}
		Collections.sort(categoryNames);

		final boolean[] categoryScoreHidden = { false };

		add(new ListView<String>("categoriesList", categoryNames) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<String> categoryItem) {
				final String categoryName = categoryItem.getModelObject();

				final List<Assignment> categoryAssignments = categoryNamesToAssignments.get(categoryName);

				categoryItem.add(new Label("category", categoryName));

				CategoryDefinition categoryDefinition = null;
				for (final CategoryDefinition aCategoryDefinition : InstructorGradeSummaryGradesPanel.this.categories) {
					if (aCategoryDefinition.getName().equals(categoryName)) {
						categoryDefinition = aCategoryDefinition;
						break;
					}
				}

				if (categoryDefinition != null) {
					final Double score = InstructorGradeSummaryGradesPanel.this.gradeInfo.getCategoryAverages()
							.get(categoryDefinition.getId());
					String grade = "";
					if (score != null) {
						grade = FormatHelper.formatDoubleAsPercentage(score);
					}
					categoryItem.add(new Label("categoryGrade", grade));

					String weight = "";
					if (categoryDefinition.getWeight() == null) {
						categoryItem.add(new Label("categoryWeight", ""));
					} else {
						weight = FormatHelper.formatDoubleAsPercentage(categoryDefinition.getWeight() * 100);
						categoryItem.add(new Label("categoryWeight", weight));
					}
				} else {
					categoryItem.add(new Label("categoryGrade", ""));
					categoryItem.add(new Label("categoryWeight", ""));
				}

				categoryItem.add(new ListView<Assignment>("assignmentsForCategory", categoryAssignments) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(final ListItem<Assignment> assignmentItem) {
						final Assignment assignment = assignmentItem.getModelObject();

						final GbGradeInfo gradeInfo = InstructorGradeSummaryGradesPanel.this.gradeInfo.getGrades().get(assignment.getId());

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

						final WebMarkupContainer flags = new WebMarkupContainer("flags");
						flags.add(gradebookPage.buildFlagWithPopover("isExtraCredit", getString("label.gradeitem.extracredit"))
								.setVisible(assignment.getExtraCredit()));
						flags.add(gradebookPage.buildFlagWithPopover("isNotCounted", getString("label.gradeitem.notcounted"))
								.setVisible(!assignment.isCounted()));
						flags.add(gradebookPage.buildFlagWithPopover("isNotReleased", getString("label.gradeitem.notreleased"))
								.setVisible(!assignment.isReleased()));
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
				});
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

		// course grade
		// GbCourseGradeLabel takes care of all permissions, settings and formatting, we just give it the data
		final Gradebook gradebook = this.businessService.getGradebook();
		final CourseGrade courseGrade = this.businessService.getCourseGrade(userId);

		final GradebookUiSettings settings = gradebookPage.getUiSettings();

		final Map<String, Object> courseGradeModelData = new HashMap<>();
		courseGradeModelData.put("currentUserUuid", userId);
		courseGradeModelData.put("currentUserRole", GbRole.INSTRUCTOR);
		courseGradeModelData.put("courseGrade", courseGrade);
		courseGradeModelData.put("gradebook", gradebook);
		courseGradeModelData.put("showPoints", settings.getShowPoints());
		courseGradeModelData.put("showOverride", true);
		add(new GbCourseGradeLabel("courseGrade", Model.ofMap(courseGradeModelData)));

		add(new Label("courseGradeNotReleasedFlag", "*") {
			@Override
			public boolean isVisible() {
				return !gradebook.isCourseGradeDisplayed();
			}
		});

		add(new Label("courseGradeNotReleasedMessage", getString("label.studentsummary.coursegradenotreleasedmessage")) {
			@Override
			public boolean isVisible() {
				return !gradebook.isCourseGradeDisplayed();
			}
		});

		add(new AttributeModifier("data-studentid", userId));

		add(new Label("categoryScoreNotReleased", getString("label.studentsummary.categoryscorenotreleased")) {
			@Override
			public boolean isVisible() {
				return categoryScoreHidden[0];
			}
		});
	}

	/**
	 * Helper to get the category name. Looks at settings as well.
	 *
	 * @param assignment
	 * @return
	 */
	private String getCategoryName(final Assignment assignment) {
		if (this.configuredCategoryType == GbCategoryType.NO_CATEGORY) {
			return getString(GradebookPage.UNCATEGORISED);
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
