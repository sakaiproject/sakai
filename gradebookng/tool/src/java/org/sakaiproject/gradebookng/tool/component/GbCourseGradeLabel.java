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
		final String currentUserUuid = (String) modelData.get("currentUserUuid");
		final GbRole currentUserRole = (GbRole) modelData.get("currentUserRole");
		final CourseGrade courseGrade = (CourseGrade) modelData.get("courseGrade");
		final GradebookInformation settings = (GradebookInformation) modelData.get("settings");
		final Boolean showPoints = (Boolean) modelData.get("showPoints");

		// instructor, can view
		if (currentUserRole == GbRole.INSTRUCTOR) {
			setDefaultModel(Model.of(buildCourseGrade(settings, courseGrade, showPoints)));
			// TA, permission check
		} else if (currentUserRole == GbRole.TA) {
			if (!this.businessService.isCourseGradeVisible(currentUserUuid)) {
				setDefaultModel(new ResourceModel("label.coursegrade.nopermission"));
			} else {
				setDefaultModel(Model.of(buildCourseGrade(settings, courseGrade, showPoints)));
			}
			// student, check if course grade released, and permission check
		} else {
			final Gradebook gradebook = this.businessService.getGradebook();
			if (gradebook.isCourseGradeDisplayed()) {
				if (!this.businessService.isCourseGradeVisible(currentUserUuid)) {
					setDefaultModel(new ResourceModel("label.coursegrade.nopermission"));
				} else {
					setDefaultModel(Model.of(buildCourseGrade(settings, courseGrade, showPoints)));
				}
			} else {
				setDefaultModel(Model.of(getString("label.studentsummary.coursegradenotreleased")));
			}
		}

	}

	/**
	 * Takes care of checking the values and configured settings to format the course grade into an applicable display format
	 *
	 * @param settings {@link GradebookInformation} object holding the settings
	 * @param courseGrade the {@link CourseGrade} object holding the values
	 * @param showPoints whether or not to include points. May not be visible due to settings though.
	 * @return formatted string ready for display
	 */
	public String buildCourseGrade(final GradebookInformation settings, final CourseGrade courseGrade, final boolean showPoints) {
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

		if (showPoints) {
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
		}

		return StringUtils.join(parts, " ");
	}
}
