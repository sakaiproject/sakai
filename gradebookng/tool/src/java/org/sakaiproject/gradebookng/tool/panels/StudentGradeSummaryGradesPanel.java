package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.pages.BasePage;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class StudentGradeSummaryGradesPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public StudentGradeSummaryGradesPanel(String id, IModel<Map<String, Object>> model) {
		super(id, model);
	}

		@Override
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,Object> modelData = (Map<String,Object>) this.getDefaultModelObject();
		String userId = (String) modelData.get("userId");
		String displayName = (String) modelData.get("displayName");

		//get grades
		final Map<Assignment, GbGradeInfo> grades = this.businessService.getGradesForStudent(userId);
		final List<Assignment> assignments = new ArrayList(grades.keySet());
		
		final List<String> categoryNames = new ArrayList<String>();
		final Map<String, List<Assignment>> categoriesToAssignments = new HashMap<String, List<Assignment>>();
		final Map<String, String> categoryAverages = new HashMap<>();

		Iterator<Assignment> assignmentIterator = assignments.iterator();
		while (assignmentIterator.hasNext()) {
			Assignment assignment = assignmentIterator.next();
			String category = assignment.getCategoryName() == null ? GradebookPage.UNCATEGORIZED : assignment.getCategoryName();

			if (!categoriesToAssignments.containsKey(category)) {
				categoryNames.add(category);
				categoriesToAssignments.put(category, new ArrayList<Assignment>());

				Double categoryAverage = businessService.getCategoryScoreForStudent(assignment.getCategoryId(), userId, grades);
				if (categoryAverage == null || category.equals(GradebookPage.UNCATEGORIZED)) {
					categoryAverages.put(category, getString("label.nocategoryscore"));
				} else {
					categoryAverages.put(category, FormatHelper.formatDoubleAsPercentage(categoryAverage));
				}
			}

			categoriesToAssignments.get(category).add(assignment);
		}

		Collections.sort(categoryNames);

		add(new ListView<String>("categoriesList", categoryNames) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> categoryItem) {
				final String category = categoryItem.getModelObject();

				List<Assignment> categoryAssignments = categoriesToAssignments.get(category);

				categoryItem.add(new Label("category", category));
				categoryItem.add(new Label("categoryGrade", categoryAverages.get(category)));

				String categoryWeight = "";
				if (!categoryAssignments.isEmpty()) {
					Double weight = categoryAssignments.get(0).getWeight();
					if (weight != null) {
						categoryWeight = FormatHelper.formatDoubleAsPercentage(weight * 100);
					}
				}
				categoryItem.add(new Label("categoryWeight", categoryWeight));

				categoryItem.add(new ListView<Assignment>("assignmentsForCategory", categoryAssignments) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(ListItem<Assignment> assignmentItem) {
						final Assignment assignment = assignmentItem.getModelObject();

						if (!assignment.isReleased()) {
							assignmentItem.setVisible(false);
						}

						GbGradeInfo gradeInfo = grades.get(assignment);

						final String rawGrade;
						String comment;
						if(gradeInfo != null) {
							rawGrade = gradeInfo.getGrade();
							comment = gradeInfo.getGradeComment();
						} else {
							rawGrade = "";
							comment = "";
						}

						Label title = new Label("title", assignment.getName());
						assignmentItem.add(title);

						BasePage page = (BasePage) getPage();
						WebMarkupContainer flags = new WebMarkupContainer("flags");
						flags.add(page.buildFlagWithPopover("isExtraCredit", getString("label.gradeitem.extracredit")).setVisible(assignment.getExtraCredit()));
						flags.add(page.buildFlagWithPopover("isNotCounted", getString("label.gradeitem.notcounted")).setVisible(!assignment.isCounted()));
						assignmentItem.add(flags);

						assignmentItem.add(new Label("dueDate", FormatHelper.formatDate(assignment.getDueDate(), getString("label.studentsummary.noduedate"))));
						assignmentItem.add(new Label("grade", FormatHelper.formatGrade(rawGrade)));
						assignmentItem.add(new Label("outOf",  new StringResourceModel("label.studentsummary.outof", null, new Object[] { assignment.getPoints() })) {
							@Override
							public boolean isVisible() {
								return StringUtils.isNotBlank(rawGrade);
							}
						});
						assignmentItem.add(new Label("comments", comment));
					}
				});
			}
		});

		final Gradebook gradebook = businessService.getGradebook();
		if (gradebook.isCourseGradeDisplayed()) {
			
			//check permission for current user to view course grade
			//otherwise fetch and render it
			String currentUserUuid = this.businessService.getCurrentUser().getId();
			if (!this.businessService.isCourseGradeVisible(currentUserUuid)) {
				add(new Label("courseGrade", new ResourceModel("label.coursegrade.nopermission")));
			} else {
				CourseGrade courseGrade = this.businessService.getCourseGrade(userId);
				if(StringUtils.isBlank(courseGrade.getEnteredGrade()) && StringUtils.isBlank(courseGrade.getMappedGrade())) {
					add(new Label("courseGrade", new ResourceModel("label.studentsummary.coursegrade.none")));
				} else if(StringUtils.isNotBlank(courseGrade.getEnteredGrade())){
					add(new Label("courseGrade", courseGrade.getEnteredGrade()));
				} else {
					add(new Label("courseGrade", new StringResourceModel("label.studentsummary.coursegrade.display", null, new Object[] { courseGrade.getMappedGrade(), FormatHelper.formatStringAsPercentage(courseGrade.getCalculatedGrade()) } )));
				}
			}
		} else {
			add(new Label("courseGrade", getString("label.studentsummary.coursegradenotreleased")));
		}

		add(new AttributeModifier("data-studentid", userId));
	}
}
