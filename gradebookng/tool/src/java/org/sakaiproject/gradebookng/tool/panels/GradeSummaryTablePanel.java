/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.BasePage;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.rubrics.logic.RubricsConstants;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradingType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradeSummaryTablePanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private boolean isGroupedByCategory;
	private boolean assignmentStatsEnabled;

	public GradeSummaryTablePanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);

		// settings for stats display
		final GradebookInformation settings = getSettings();
		this.assignmentStatsEnabled = settings.isAssignmentStatsDisplayed();

	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		final Map<String, Object> data = (Map<String, Object>) getDefaultModelObject();

		final Map<Long, GbGradeInfo> grades = (Map<Long, GbGradeInfo>) data.get("grades");
		final Map<String, List<Assignment>> categoryNamesToAssignments = (Map<String, List<Assignment>>) data
				.get("categoryNamesToAssignments");
		final List<String> categoryNames = (List<String>) data.get("categoryNames");
		final Map<Long, Double> categoryAverages = (Map<Long, Double>) data.get("categoryAverages");
		final boolean categoriesEnabled = (boolean) data.get("categoriesEnabled");
		final boolean isCategoryWeightEnabled = (boolean) data.get("isCategoryWeightEnabled");
		final boolean showingStudentView = (boolean) data.get("showingStudentView");
		final GradingType gradingType = (GradingType) data.get("gradingType");
		final String studentUuid = (String) data.get("studentUuid");
		this.isGroupedByCategory = (boolean) data.get("isGroupedByCategory");
		final Map<String, CategoryDefinition> categoriesMap = (Map<String, CategoryDefinition>) data.get("categoriesMap");
		final ModalWindow assignmentStatsWindow = new ModalWindow("assignmentStatsWindow");
		addOrReplace(assignmentStatsWindow);

		if (getPage() instanceof GradebookPage) {
			final GradebookPage page = (GradebookPage) getPage();
			final GradebookUiSettings settings = page.getUiSettings();
			this.isGroupedByCategory = settings.isGradeSummaryGroupedByCategory();
		}

		final WebMarkupContainer toggleActions = new WebMarkupContainer("toggleActions");
		toggleActions.setVisible(categoriesEnabled);

		final GbAjaxLink toggleCategoriesLink = new GbAjaxLink("toggleCategoriesLink") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (GradeSummaryTablePanel.this.isGroupedByCategory) {
					add(new AttributeAppender("class", " on"));
				}
				add(new AttributeModifier("aria-pressed", GradeSummaryTablePanel.this.isGroupedByCategory));
			}

			@Override
			public void onClick(final AjaxRequestTarget target) {
				if (getPage() instanceof GradebookPage) {
					final GradebookPage page = (GradebookPage) getPage();
					final GradebookUiSettings settings = page.getUiSettings();
					settings.setGradeSummaryGroupedByCategory(!settings.isGradeSummaryGroupedByCategory());
				}

				GradeSummaryTablePanel.this.isGroupedByCategory = !GradeSummaryTablePanel.this.isGroupedByCategory;
				data.put("isGroupedByCategory", GradeSummaryTablePanel.this.isGroupedByCategory);

				target.add(GradeSummaryTablePanel.this);
				target.appendJavaScript(
						String.format("new GradebookGradeSummary($(\"#%s\"), %s);",
								GradeSummaryTablePanel.this.getParent().getMarkupId(),
								showingStudentView));
			}
		};
		toggleActions.add(toggleCategoriesLink);
		toggleActions.addOrReplace(new WebMarkupContainer("expandCategoriesLink").setVisible(this.isGroupedByCategory));
		toggleActions.addOrReplace(new WebMarkupContainer("collapseCategoriesLink").setVisible(this.isGroupedByCategory));
		addOrReplace(toggleActions);

		addOrReplace(new WebMarkupContainer("weightColumnHeader")
				.setVisible(categoriesEnabled && isCategoryWeightEnabled && this.isGroupedByCategory));

		final boolean catColVisible = categoriesEnabled && !this.isGroupedByCategory;
		addOrReplace(new WebMarkupContainer("categoryColumnHeader").setVisible(catColVisible));

		// steal width from date column to give to category column
		addOrReplace(new WebMarkupContainer("dateColumnHeader")
				.add(AttributeModifier.append("class", catColVisible ? "col-md-1" : "col-md-2")));

		// output all of the categories
		// within each we then add the assignments in each category
		// if not grouped by category, render all assignments in one go!
		addOrReplace(new ListView<String>("categoriesList",
				this.isGroupedByCategory ? categoryNames : Arrays.asList(getString(GradebookPage.UNCATEGORISED))) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<String> categoryItem) {
				final String categoryName = categoryItem.getModelObject();

				final List<Assignment> categoryAssignments;
				if (GradeSummaryTablePanel.this.isGroupedByCategory) {
					if (categoryNamesToAssignments.containsKey(categoryName)) {
						categoryAssignments = categoryNamesToAssignments.get(categoryName);
					} else {
						categoryAssignments = new ArrayList<>();
					}
				} else {
					categoryAssignments = new ArrayList<>();
					categoryNamesToAssignments.values().forEach(categoryAssignments::addAll);
				}

				final WebMarkupContainer categoryRow = new WebMarkupContainer("categoryRow");
				categoryRow.setVisible(categoriesEnabled && GradeSummaryTablePanel.this.isGroupedByCategory && !categoryAssignments.isEmpty());
				categoryItem.add(categoryRow);
				categoryRow.add(new Label("category", categoryName));

				// popover flags
				final CategoryFlags cf = getCategoryFlags(categoryName, categoriesMap);
				final WebMarkupContainer flags = new WebMarkupContainer("flags");
				flags.add(newPopoverFlag("isExtraCredit", getString("label.gradeitem.extracreditcategory"), cf.extraCredit));
				flags.add(newPopoverFlag("isEqualWeight", getString("label.gradeitem.equalweightcategory"), cf.equalWeight));
				categoryRow.add(flags.setVisible(cf.hasFlags()));

				final DropInfoPair pair = getDropInfo(categoryName, categoriesMap);
				if (!pair.second.isEmpty()) {
					pair.first += " " + getString("label.category.dropSeparator") + " ";
				}
				final WebMarkupContainer dropInfo = new WebMarkupContainer("categoryDropInfo");
				dropInfo.setVisible(!pair.first.isEmpty());
				dropInfo.add(new Label("categoryDropInfo1", pair.first));
				dropInfo.add(new Label("categoryDropInfo2", pair.second).setVisible(!pair.second.isEmpty()));
				categoryRow.add(dropInfo);

				if (!categoryAssignments.isEmpty()) {
					final Double categoryAverage = categoryAverages.get(categoryAssignments.get(0).getCategoryId());
					if (categoryAverage == null) {
						categoryRow.add(new Label("categoryGrade", getString("label.nocategoryscore")));
					} else {
						categoryRow.add(new Label("categoryGrade", FormatHelper.formatDoubleAsPercentage(categoryAverage)));
					}
				} else {
					categoryRow.add(new Label("categoryGrade", getString("label.nocategoryscore")));
				}

				String categoryWeight = "";
				if (!categoryAssignments.isEmpty()) {
					final Double weight = categoryAssignments.get(0).getWeight();
					if (weight != null) {
						categoryWeight = FormatHelper.formatDoubleAsPercentage(weight * 100);
					}
				}
				categoryRow.add(new Label("categoryWeight", categoryWeight)
						.setVisible(isCategoryWeightEnabled && GradeSummaryTablePanel.this.isGroupedByCategory));

				categoryItem.add(new ListView<Assignment>("assignmentsForCategory", categoryAssignments) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(final ListItem<Assignment> assignmentItem) {
						final Assignment assignment = assignmentItem.getModelObject();

						if (!categoriesEnabled) {
							assignmentItem.add(new AttributeAppender("class", " gb-no-categories"));
						}

						final GbGradeInfo gradeInfo = grades.get(assignment.getId());
						boolean excused;
						if(gradeInfo == null){
							excused = false;
						}else{
							excused = gradeInfo.isExcused();
						}

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

						final GbAjaxLink assignmentStatsLink = new GbAjaxLink(
								"assignmentStatsLink") {

							private static final long serialVersionUID = 1L;

							@Override
							public void onClick(
									final AjaxRequestTarget target) {

								assignmentStatsWindow.setContent(
										new StudentAssignmentStatisticsPanel(
												assignmentStatsWindow
														.getContentId(),
												Model.of(assignment),
												assignmentStatsWindow, rawGrade));
								assignmentStatsWindow.show(target);

							}

							@Override
							public boolean isVisible() {
								return showingStudentView
										&& GradeSummaryTablePanel.this.assignmentStatsEnabled;
							}
						};

						assignmentItem.add(assignmentStatsLink);

						// popover flags
						final WebMarkupContainer flags = new WebMarkupContainer("flags");

						flags.add(newPopoverFlag("isExtraCredit", getString("label.gradeitem.extracredit"), assignment.isExtraCredit()));
						flags.add(newPopoverFlag("isNotCounted", getString("label.gradeitem.notcounted"), !assignment.isCounted()));
						flags.add(newPopoverFlag("isNotReleased", getString("label.gradeitem.notreleased"), !assignment.isReleased()));
						flags.add(newPopoverFlag("isExcused", getString("grade.notifications.excused"), excused));
						String extAppName = new StringResourceModel("label.gradeitem.externalapplabel", null, new Object[] { assignment.getExternalAppName() }).getString();
						flags.add(newPopoverFlag("isExternal", extAppName, assignment.isExternallyMaintained())
								.add(new AttributeModifier("class", "gb-external-app-flag " + GradeSummaryTablePanel.this.businessService.getIconClass(assignment))));
						assignmentItem.add(flags);

						assignmentItem.add(new WebMarkupContainer("weight")
								.setVisible(isCategoryWeightEnabled && GradeSummaryTablePanel.this.isGroupedByCategory));

						final Label dueDate = new Label("dueDate",
								GradeSummaryTablePanel.this.businessService.formatDate(assignment.getDueDate(), getString("label.studentsummary.noduedate")));
						dueDate.add(new AttributeModifier("data-sort-key",
								assignment.getDueDate() == null ? 0 : assignment.getDueDate().getTime()));
						assignmentItem.add(dueDate);

						final WebMarkupContainer gradeScore = new WebMarkupContainer("gradeScore");
						if (GradingType.PERCENTAGE.equals(gradingType)) {
							gradeScore.add(new Label("grade",
									new StringResourceModel("label.percentage.valued", null,
											new Object[] { FormatHelper.formatGrade(rawGrade) })) {
								@Override
								public boolean isVisible() {
									return StringUtils.isNotBlank(rawGrade);
								}
							});

							gradeScore.add(new Label("outOf").setVisible(false));

							final WebMarkupContainer sakaiRubricButton = new WebMarkupContainer("sakai-rubric-student-button");
							sakaiRubricButton.add(AttributeModifier.append("display", "icon"));
							sakaiRubricButton.add(AttributeModifier.append("tool-id", RubricsConstants.RBCS_TOOL_GRADEBOOKNG));
							sakaiRubricButton.add(AttributeModifier.append("evaluated-item-id", assignment.getId() + "." + studentUuid));
							sakaiRubricButton.add(AttributeModifier.append("token", rubricsService.generateJsonWebToken(RubricsConstants.RBCS_TOOL_GRADEBOOKNG)));

							addInstructorAttributeOrHide(sakaiRubricButton, assignment, studentUuid, showingStudentView);

							if (assignment.getId() != null) {
								sakaiRubricButton.add(AttributeModifier.append("entity-id", assignment.getId()));
							}

							gradeScore.add(sakaiRubricButton);
						} else {
							gradeScore.add(
									new Label("grade", FormatHelper.convertEmptyGradeToDash(FormatHelper.formatGradeForDisplay(rawGrade))));
							gradeScore.add(new Label("outOf",
									new StringResourceModel("label.studentsummary.outof", null, assignment.getPoints())));

							final WebMarkupContainer sakaiRubricButton = new WebMarkupContainer("sakai-rubric-student-button");
							sakaiRubricButton.add(AttributeModifier.append("display", "icon"));
							sakaiRubricButton.add(AttributeModifier.append("token", rubricsService.generateJsonWebToken(RubricsConstants.RBCS_TOOL_GRADEBOOKNG)));

							addInstructorAttributeOrHide(sakaiRubricButton, assignment, studentUuid, showingStudentView);

							if (assignment.isExternallyMaintained()) {
								sakaiRubricButton.add(AttributeModifier.append("tool-id", RubricsConstants.RBCS_TOOL_ASSIGNMENT));
								String[] bits = assignment.getExternalId().split("/");
								if (bits != null && bits.length >= 1) {
									String assignmentId = bits[bits.length-1];
									String ownerId = studentUuid;
									try {
										org.sakaiproject.assignment.api.model.Assignment assignmentsAssignment = assignmentService.getAssignment(assignmentId);
										if (assignmentsAssignment.getIsGroup()) {
											Optional<String> groupId = assignmentsAssignment.getGroups().stream().filter(g -> {

												try {
													AuthzGroup group = authzGroupService.getAuthzGroup(g);
													return group.getMember(studentUuid) != null;
												} catch (GroupNotDefinedException gnde) {
													return false;
												}
											}).findAny();

											if (groupId.isPresent()) {
												ownerId = groupId.get();
											} else {
												log.error("Assignment {} is a group assignment, but {} was not in any of the groups", assignmentId, studentUuid);
											}
										}
									} catch (Exception e) {
										log.error("Failed to determine ownerId for submission", e);
									}

									String submissionId = rubricsService.getRubricEvaluationObjectId(assignmentId, ownerId, RubricsConstants.RBCS_TOOL_ASSIGNMENT);
									sakaiRubricButton.add(AttributeModifier.append("entity-id", assignmentId));
									sakaiRubricButton.add(AttributeModifier.append("evaluated-item-id", submissionId));
								} else {
									log.warn(assignment.getExternalId() + " is not a valid assignment reference");
								}
							} else {
								sakaiRubricButton.add(AttributeModifier.append("tool-id", RubricsConstants.RBCS_TOOL_GRADEBOOKNG));
								sakaiRubricButton.add(AttributeModifier.append("evaluated-item-id", assignment.getId() + "." + studentUuid));

								if (assignment.getId() != null) {
									sakaiRubricButton.add(AttributeModifier.append("entity-id", assignment.getId()));
								}
							}

							gradeScore.add(sakaiRubricButton);
						}
						if (gradeInfo != null && gradeInfo.isDroppedFromCategoryScore()) {
							gradeScore.add(AttributeModifier.append("class", "gb-summary-grade-score-dropped"));
						}
						if(gradeInfo != null && excused){
							gradeScore.add(AttributeModifier.append("class", "gb-summary-grade-score-excused"));
						}
						assignmentItem.add(gradeScore);

						assignmentItem.add(new Label("comments", comment));

						final WebMarkupContainer catCon = new WebMarkupContainer("category");
						catCon.setVisible(categoriesEnabled && !GradeSummaryTablePanel.this.isGroupedByCategory);
						catCon.add(new Label("categoryName", assignment.getCategoryName()));

						final CategoryFlags cf = getCategoryFlags(assignment.getCategoryName(), categoriesMap);
						final WebMarkupContainer cflags = new WebMarkupContainer("cflags");
						cflags.add(newPopoverFlag("isExtraCredit", getString("label.gradeitem.extracreditcategory"), cf.extraCredit));
						cflags.add(newPopoverFlag("isEqualWeight", getString("label.gradeitem.equalweightcategory"), cf.equalWeight));
						catCon.add(cflags.setVisible(cf.hasFlags()));

						final DropInfoPair pair = getDropInfo(assignment.getCategoryName(), categoriesMap);
						catCon.add(new Label("categoryDropInfo", pair.first).setVisible(!pair.first.isEmpty()));
						catCon.add(new Label("categoryDropInfo2", pair.second).setVisible(!pair.second.isEmpty()));
						assignmentItem.add(catCon);
					}
				});
			}
		});
	}

	private void addInstructorAttributeOrHide(WebMarkupContainer sakaiRubricButton, Assignment assignment, String studentId, boolean showingStudentView) {

		if (!showingStudentView && (GradeSummaryTablePanel.this.getUserRole() == GbRole.INSTRUCTOR
					|| GradeSummaryTablePanel.this.getUserRole() == GbRole.TA)) {
			sakaiRubricButton.add(AttributeModifier.append("instructor", true));
		} else {
			GradeDefinition gradeDefinition = businessService.getGradeForStudentForItem(studentId, assignment.getId());
			if (assignment.isExternallyMaintained() && gradeDefinition.getGrade() == null) {
				sakaiRubricButton.add(AttributeModifier.replace("force-preview", true));
			}
		}
	}

	private final class DropInfoPair implements Serializable {
		public String first = "";
		public String second = "";
	}

	private DropInfoPair getDropInfo(final String categoryName, final Map<String, CategoryDefinition> categoriesMap) {
		final DropInfoPair pair = new DropInfoPair();
		if (categoryName != null && !categoryName.equals(getString(GradebookPage.UNCATEGORISED))) {
			final List<String> info = FormatHelper.formatCategoryDropInfo(categoriesMap.get(categoryName));
			if (info.size() > 0) {
				pair.first = info.get(0);
			}
			if (info.size() > 1) {
				pair.second = info.get(1);
			}
		}

		return pair;
	}

	public void renderHead(final IHeaderResponse response) {

		final String version = PortalUtils.getCDNQuery();
		response.render(StringHeaderItem.forString(
			"<script src=\"/webcomponents/rubrics/sakai-rubrics-utils.js" + version + "\"></script>"));
		response.render(StringHeaderItem.forString(
			"<script type=\"module\" src=\"/webcomponents/rubrics/rubric-association-requirements.js" + version + "\"></script>"));
	}

	private Component newPopoverFlag(String id, String msg, boolean visible) {
		final BasePage page = (BasePage) getPage();
		return page.buildFlagWithPopover(id, msg, "focus", "#gradeSummaryTable").setVisible(visible);
	}

	private final class CategoryFlags implements Serializable {
		public boolean extraCredit = false;
		public boolean equalWeight = false;

		public boolean hasFlags() {
			return extraCredit || equalWeight;
		}
	}

	private CategoryFlags getCategoryFlags(String catName, final Map<String, CategoryDefinition> categoriesMap) {
		CategoryFlags flags = new CategoryFlags();
		if (catName != null && !catName.equals(getString(GradebookPage.UNCATEGORISED))) {
			CategoryDefinition cat = categoriesMap.get(catName);
			flags.extraCredit = cat != null && Boolean.TRUE.equals(cat.getExtraCredit());
			flags.equalWeight = cat != null && Boolean.TRUE.equals(cat.getEqualWeight());
		}

		return flags;
	}
}
