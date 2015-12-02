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
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.Category;

import java.util.*;

public class InstructorGradeSummaryGradesPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private GbStudentGradeInfo gradeInfo;
	private List<CategoryDefinition> categories;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public InstructorGradeSummaryGradesPanel(String id, IModel<Map<String, Object>> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,Object> modelData = (Map<String,Object>) this.getDefaultModelObject();
		String userId = (String) modelData.get("userId");
		String displayName = (String) modelData.get("displayName");
		
		//build the grade matrix for the user
		final List<Assignment> assignments = this.businessService.getGradebookAssignments();
        
		//TODO catch if this is null, the get(0) will throw an exception
		//TODO also catch the GbException
		this.gradeInfo = this.businessService.buildGradeMatrix(assignments, Collections.singletonList(userId)).get(0);
		this.categories = this.businessService.getGradebookCategories();

		final List<String> categoryNames = new ArrayList<String>();
		final Map<String, List<Assignment>> categoriesToAssignments = new HashMap<String, List<Assignment>>();

		Iterator<Assignment> assignmentIterator = assignments.iterator();
		while (assignmentIterator.hasNext()) {
			Assignment assignment = assignmentIterator.next();

			String categoryName = assignment.getCategoryName() == null ? GradebookPage.UNCATEGORIZED : assignment.getCategoryName();

			if (!categoriesToAssignments.containsKey(categoryName)) {
				categoryNames.add(categoryName);
				categoriesToAssignments.put(categoryName, new ArrayList<Assignment>());
			}

			categoriesToAssignments.get(categoryName).add(assignment);
		}

		Collections.sort(categoryNames);

		final boolean[] categoryScoreHidden = { false };

		add(new ListView<String>("categoriesList", categoryNames) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> categoryItem) {
				final String categoryName = categoryItem.getModelObject();

				List<Assignment> categoryAssignments = categoriesToAssignments.get(categoryName);

				categoryItem.add(new Label("category", categoryName));

				CategoryDefinition categoryDefinition = null;
				for (CategoryDefinition aCategoryDefinition : categories) {
					if (aCategoryDefinition.getName().equals(categoryName)) {
						categoryDefinition = aCategoryDefinition;
						break;
					}
				}

				if (categoryDefinition != null) {
					Double score = gradeInfo.getCategoryAverages().get(categoryDefinition.getId());
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
					protected void populateItem(ListItem<Assignment> assignmentItem) {
						final Assignment assignment = assignmentItem.getModelObject();

						GbGradeInfo gradeInfo = InstructorGradeSummaryGradesPanel.this.gradeInfo.getGrades().get(assignment.getId());

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

						GradebookPage gradebookPage = (GradebookPage) getPage();
						WebMarkupContainer flags = new WebMarkupContainer("flags");
						flags.add(gradebookPage.buildFlagWithPopover("isExtraCredit", getString("label.gradeitem.extracredit")).setVisible(assignment.getExtraCredit()));
						flags.add(gradebookPage.buildFlagWithPopover("isNotCounted", getString("label.gradeitem.notcounted")).setVisible(!assignment.isCounted()));
						flags.add(gradebookPage.buildFlagWithPopover("isNotReleased", getString("label.gradeitem.notreleased")).setVisible(!assignment.isReleased()));
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

		//course grade
		final Gradebook gradebook = businessService.getGradebook();
		
		String currentUserUuid = this.businessService.getCurrentUser().getId();
		if (!this.businessService.isCourseGradeVisible(currentUserUuid)) {
			add(new Label("courseGrade", new ResourceModel("label.coursegrade.nopermission")));
		} else {
			add(new Label("courseGrade", this.gradeInfo.getCourseGrade()));
		}
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
}
