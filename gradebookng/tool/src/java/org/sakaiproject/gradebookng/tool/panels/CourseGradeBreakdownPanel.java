/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.model.GbBreakdownItem;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.SortType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by David P. Bauer [dbauer1@udayton.edu] on 8/1/17.
 */
public class CourseGradeBreakdownPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    @SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    protected GradebookNgBusinessService businessService;

    @SpringBean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    protected ServerConfigurationService serverConfigurationService;

    private Double overAllPoints = 0D;
    private final ModalWindow window;
    private boolean weightedCategories;

    public CourseGradeBreakdownPanel(final String id, final ModalWindow window) {
        super(id);
        this.window = window;
        add(new GbFeedbackPanel("items-feedback"));
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        this.weightedCategories = Objects.equals(this.businessService.getGradebookSettings(currentGradebookUid, currentSiteId).getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY);
        final IModel<List<GbBreakdownItem>> loadableItemList = new LoadableDetachableModel<List<GbBreakdownItem>>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected List<GbBreakdownItem> load() {
                return getItemsList();
            }
        };
        final WebMarkupContainer itemListContainer = new WebMarkupContainer("item-list-container");
        itemListContainer.setOutputMarkupId(true);
        add(itemListContainer);
        final ListView<GbBreakdownItem> itemListView = new ListView<>("items", loadableItemList) {
            private static final long serialVersionUID = 1L;

            int studentCount = businessService.getGradeableUsers(currentGradebookUid, currentSiteId, null).size();

            @Override
            protected void populateItem(ListItem<GbBreakdownItem> item) {
                final GbBreakdownItem gbItem = item.getModelObject();
                final Assignment assignment = gbItem.getGradebookItem();
                final CategoryDefinition categoryDefinition = gbItem.getCategoryDefinition();
                final GradebookPage gradebookPage = (GradebookPage) getPage();
                final WebMarkupContainer externalAppFlag = gradebookPage.buildFlagWithPopover("externalAppFlag", "");
                if (assignment == null || StringUtils.isBlank(assignment.getExternalAppName())) {
                    externalAppFlag.setVisible(false);
                } else {
                    externalAppFlag.add(new AttributeModifier("data-content", gradebookPage.generatePopoverContent(new StringResourceModel("label.gradeitem.externalapplabel").setParameters(assignment.getExternalAppName()).getString())));
                    String iconClass = businessService.getIconClass(assignment);
                    externalAppFlag.add(new AttributeModifier("class", "gb-external-app-flag " + iconClass));
                }
                item.add(externalAppFlag);
                item.add(new Label("name-label", assignment == null ? categoryDefinition.getName() : assignment.getName()));
                item.add(gradebookPage.buildFlagWithPopover("extraCreditFlag", getString("label.gradeitem.extracredit"))
                        .setVisible(assignment != null && assignment.getExtraCredit()));
                item.add(gradebookPage.buildFlagWithPopover("notCountedFlag", getString("label.gradeitem.notcounted"))
                        .setVisible(assignment != null && !assignment.getCounted()));
                item.add(gradebookPage.buildFlagWithPopover("notReleasedFlag", getString("label.gradeitem.notreleased"))
                        .setVisible(assignment != null && !assignment.getReleased()));
                if (categoryDefinition != null) {
                    item.add(new AttributeModifier("data-category-id", categoryDefinition.getId()));
                }
                item.add(gradebookPage.buildFlagWithPopover("extraCreditCategoryFlag", getString("label.gradeitem.extracreditcategory"))
                        .setVisible(categoryDefinition != null && Boolean.TRUE.equals(categoryDefinition.getExtraCredit())));
                item.add(gradebookPage.buildFlagWithPopover("dropLowestCategoryFlag", getString("label.category.droplowest").replace("{0}",
                        String.valueOf(categoryDefinition != null ? categoryDefinition.getDropLowest() : "")))
                        .setVisible(categoryDefinition != null && categoryDefinition.getDropLowest() != null && categoryDefinition.getDropLowest() > 0));
                item.add(gradebookPage.buildFlagWithPopover("dropHighestCategoryFlag", getString("label.category.drophighest").replace("{0}",
                        String.valueOf(categoryDefinition != null ? categoryDefinition.getDropHighest() : "")))
                        .setVisible(categoryDefinition != null && categoryDefinition.getDropHighest() != null && categoryDefinition.getDropHighest() > 0));
                item.add(gradebookPage.buildFlagWithPopover("keepHighestCategoryFlag", getString("label.category.keephighest").replace("{0}",
                        String.valueOf(categoryDefinition != null ? categoryDefinition.getKeepHighest() : "")))
                        .setVisible(categoryDefinition != null && categoryDefinition.getKeepHighest() != null && categoryDefinition.getKeepHighest() > 0));
                WebMarkupContainer numberGradedCol = new WebMarkupContainer("number-graded");
                Label numberGradedLabel = new Label("number-graded-label", (gbItem.getNumGraded() + " / " + studentCount)) {
                    @Override
                    public boolean isVisible() {
                        return assignment != null;
                    }
                };
                if (assignment != null && gbItem.getNumGraded() < studentCount) {
                    numberGradedCol.add(new AttributeAppender("class", " ungradedStudents"));
                }
                numberGradedCol.add(numberGradedLabel);
                item.add(numberGradedCol);
                if (assignment != null) {
                    // We know this is an assignment
                    item.add(new Label("out-of-label", assignment.getPoints()));
                } else {
                    // This item is a category
                    String categoryPointsOrWeight;
                    if (categoryDefinition.getId() == -1) {
                        categoryPointsOrWeight = "-";
                        item.add(new AttributeAppender("class", " table-light"));
                    } else {
                        categoryPointsOrWeight = CourseGradeBreakdownPanel.this.weightedCategories ? FormatHelper.formatDoubleAsPercentage(gbItem.getCategoryPointsOrWeight() * 100) : FormatHelper.formatDoubleToDecimal(gbItem.getCategoryPointsOrWeight());
                        item.add(new AttributeAppender("class", " table-secondary"));
                        if (categoryDefinition.getExtraCredit()) {
                            categoryPointsOrWeight = "+" + categoryPointsOrWeight;
                        }
                    }
                    item.add(new Label("out-of-label", categoryPointsOrWeight));
                    item.add(new AttributeAppender("class", " categoryRow"));
                }
            }
        };
        itemListContainer.add(itemListView);
        WebMarkupContainer totalPtsContainer = new WebMarkupContainer("total-points-container") {
            @Override
            public boolean isVisible() {
                return !CourseGradeBreakdownPanel.this.weightedCategories;
            }
        };
        itemListContainer.add(totalPtsContainer);
        totalPtsContainer.add(new Label("total-points", CourseGradeBreakdownPanel.this.overAllPoints));
        add(new GbAjaxLink<>("done") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                CourseGradeBreakdownPanel.this.window.close(target);
            }
        });
    }

    private int countNumberofGradedAssignments(List<GbStudentGradeInfo> grades, long assignmentId){
        final AtomicInteger gradedCounter = new AtomicInteger(0);
        grades.forEach(g -> {
            final GbGradeInfo gradeInfo = g.getGrades().get(assignmentId);
            if (gradeInfo != null) {
                if (gradeInfo.getGrade() != null) {
                    gradedCounter.incrementAndGet();
                }
            }
        });
        return gradedCounter.get();
    }

    private List<GbBreakdownItem> getItemsList() {
        SortType sortBy = SortType.SORT_BY_SORTING;
        final List<GbBreakdownItem> itemList = new ArrayList<>();
        if (this.businessService.categoriesAreEnabled(currentGradebookUid, currentSiteId)) {
            sortBy = SortType.SORT_BY_CATEGORY;
            // Returns a sorted list of all categories in the Gradebook
            final List<CategoryDefinition> categories = this.businessService.getGradebookCategories(currentGradebookUid, currentSiteId);
            for (CategoryDefinition categoryDefinition : categories) {
                Double totalCategoryPoints = 0D;
                // Get sorted list of assignments for the category.
                final List<Assignment> assignmentList = this.businessService.getGradebookAssignmentsForCategory(currentGradebookUid, currentSiteId, categoryDefinition.getId(), sortBy);
                final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(currentGradebookUid, currentSiteId,
                        assignmentList,
                        businessService.getGradeableUsers(currentGradebookUid, currentSiteId, null),
                        null);
                final List<GbBreakdownItem> tempList = new ArrayList<>();
                int numberToDrop = 0;
                int numberToKeep = assignmentList.size();
                if ((categoryDefinition.getDropHighest() != null && categoryDefinition.getDropHighest() > 0)
                        || categoryDefinition.getDropLowest() != null && categoryDefinition.getDropLowest() > 0) {
                    numberToDrop = categoryDefinition.getDropLowest() + categoryDefinition.getDropHighest();
                } else if (categoryDefinition.getKeepHighest() != null && categoryDefinition.getKeepHighest() > 0) {
                    numberToKeep = categoryDefinition.getKeepHighest();
                }
                for (Assignment assignment : assignmentList) {
                    tempList.add(new GbBreakdownItem(assignment.getId(), null, assignment, null, countNumberofGradedAssignments(grades, assignment.getId())));
                    if (assignment.getCounted() && !assignment.getExtraCredit()) {
                        if (numberToDrop == 0 && numberToKeep > 0) {
                            totalCategoryPoints += assignment.getPoints();
                            if (!categoryDefinition.getExtraCredit()) {
                                CourseGradeBreakdownPanel.this.overAllPoints = CourseGradeBreakdownPanel.this.overAllPoints + assignment.getPoints();
                            }
                            numberToKeep--;
                        } else if (numberToDrop > 0) {
                            numberToDrop--;
                        }
                    }
                }
                // Add Category itself to list
                itemList.add(new GbBreakdownItem(categoryDefinition.getId(), categoryDefinition, null, weightedCategories ? categoryDefinition.getWeight() : totalCategoryPoints, null));
                // Add all assignments in the category
                itemList.addAll(tempList);
            }
            final List<Assignment> uncategorizedAssignments = this.businessService.getGradebookAssignmentsForCategory(currentGradebookUid, currentSiteId, null, sortBy);
            if (uncategorizedAssignments != null && !uncategorizedAssignments.isEmpty()) {
                final List<GbBreakdownItem> uncategorizedGbItems = new ArrayList<>();
                final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(currentGradebookUid, currentSiteId,
                        uncategorizedAssignments,
                        businessService.getGradeableUsers(currentGradebookUid, currentSiteId, null),
                        null);
                for (Assignment assignment : uncategorizedAssignments) {
                    uncategorizedGbItems.add(new GbBreakdownItem(assignment.getId(), null, assignment, null, countNumberofGradedAssignments(grades, assignment.getId())));
                }
                final CategoryDefinition uncategorized = new CategoryDefinition();
                uncategorized.setId(-1L);
                uncategorized.setName("Uncategorized");
                // Add the "Uncategorized" category
                itemList.add(new GbBreakdownItem(uncategorized.getId(), uncategorized, null, 0D, null));
                // Add all uncategorized items
                itemList.addAll(uncategorizedGbItems);
            }
        } else {
            // Categories are not enabled so just add all assignments to the list.
            final List<Assignment> allAssignments = this.businessService.getGradebookAssignments(currentGradebookUid, currentSiteId, sortBy);
            final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(currentGradebookUid, currentSiteId,
                        allAssignments,
                        businessService.getGradeableUsers(currentGradebookUid, currentSiteId, null),
                        null);
            for (final Assignment assignment : allAssignments) {
                itemList.add(new GbBreakdownItem(assignment.getId(), null, assignment, null, countNumberofGradedAssignments(grades, assignment.getId())));
                if (assignment.getCounted() && !assignment.getExtraCredit()) {
                    CourseGradeBreakdownPanel.this.overAllPoints += assignment.getPoints();
                }
            }
        }
        return itemList;
    }
}
