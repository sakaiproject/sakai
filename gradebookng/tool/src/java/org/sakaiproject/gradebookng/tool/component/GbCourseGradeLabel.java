/*
 * Copyright (c) Orchestral Developments Ltd and the Orion Health group of companies (2001 - 2016).
 *
 * This document is copyright. Except for the purpose of fair reviewing, no part
 * of this publication may be reproduced or transmitted in any form or by any
 * means, electronic or mechanical, including photocopying, recording, or any
 * information storage and retrieval system, without permission in writing from
 * the publisher. Infringers of copyright render themselves liable for
 * prosecution.
 */
package org.sakaiproject.gradebookng.tool.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Component to render a course grade taking into account the configured setings
 */
public class GbCourseGradeLabel extends Label {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<Map<String, Object>> model;

	public GbCourseGradeLabel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Map<String, Object> modelData = this.model.getObject();
		final String studentUuid = (String) modelData.get("studentUuid");
		final GbRole role = (GbRole) modelData.get("role");
		final CourseGrade courseGrade = (CourseGrade) modelData.get("courseGrade");
		final GradebookInformation settings = (GradebookInformation) modelData.get("settings");
		final Boolean showPoints = (Boolean) modelData.get("showPoints");

		final Gradebook gradebook = this.businessService.getGradebook();
		if (gradebook.isCourseGradeDisplayed()) {

			// **********
			// THIS NEEDS A BIT OF CLEANUP, pass in this info via a diff constructor. in the model though!
			// **********

			// check permission for current user to view course grade
			// otherwise fetch and render it
			final String currentUserUuid = this.businessService.getCurrentUser().getId();
			if (!this.businessService.isCourseGradeVisible(currentUserUuid)) {
				setDefaultModel(new ResourceModel("label.coursegrade.nopermission"));
			} else {
				// final CourseGrade courseGrade = this.businessService.getCourseGrade(this.userId);
				// final GradebookInformation settings = this.businessService.getGradebookSettings();
				setDefaultModel(Model.of(buildCourseGrade(settings, courseGrade)));
			}
		} else {
			setDefaultModel(Model.of(getString("label.studentsummary.coursegradenotreleased")));
		}
	}

	/**
	 * Takes care of checking the values and configured settings to format the course grade into an applicable display format
	 *
	 * @param settings {@link GradebookInformation} object holding the settings
	 * @param courseGrade the {@link CourseGrade} object holding the values
	 * @return formatted string ready for display
	 */
	public String buildCourseGrade(final GradebookInformation settings, final CourseGrade courseGrade) {
		final List<String> parts = new ArrayList<>();

		// from schema
		final String mappedGrade = courseGrade.getMappedGrade();

		// override
		final String enteredGrade = courseGrade.getEnteredGrade();

		// percentage
		final String calculatedGrade = FormatHelper.formatStringAsPercentage(courseGrade.getCalculatedGrade());

		// points
		final Double pointsEarned = courseGrade.getPointsEarned();
		final Double totalPointsPossible = courseGrade.getTotalPointsPossible();

		if (settings.isCourseLetterGradeDisplayed()) {
			if (StringUtils.isNotBlank(mappedGrade)) {
				parts.add(mappedGrade);
			} else if (StringUtils.isNotBlank(enteredGrade)) {
				parts.add(enteredGrade);
			}
		}

		if (settings.isCourseAverageDisplayed()) {
			if (parts.isEmpty()) {
				parts.add(new StringResourceModel("coursegrade.display.percentage-first", null,
						new Object[] { calculatedGrade }).getString());
			} else {
				parts.add(new StringResourceModel("coursegrade.display.percentage-second", null,
						new Object[] { calculatedGrade }).getString());
			}
		}

		// don't display points for weighted category type
		final GbCategoryType categoryType = GbCategoryType.valueOf(settings.getCategoryType());
		if (categoryType != GbCategoryType.WEIGHTED_CATEGORY) {

			if (settings.isCoursePointsDisplayed()) {
				if (parts.isEmpty()) {
					parts.add(new StringResourceModel("coursegrade.display.points-first", null,
							new Object[] { pointsEarned, totalPointsPossible }).getString());
				} else {
					parts.add(new StringResourceModel("coursegrade.display.points-second", null,
							new Object[] { pointsEarned, totalPointsPossible }).getString());
				}
			}
		}

		return StringUtils.join(parts, " ");
	}
}
