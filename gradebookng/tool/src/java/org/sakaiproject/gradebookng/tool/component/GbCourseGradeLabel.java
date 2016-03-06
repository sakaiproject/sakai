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
import org.apache.wicket.event.IEvent;
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
import org.sakaiproject.gradebookng.tool.model.ScoreChangedEvent;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Component to render a course grade taking into account the configured settings
 *
 * Is also able to refresh itself via events
 *
 * Ensure you pass in everything that the Model requires.
 */
public class GbCourseGradeLabel extends Label {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<Map<String, Object>> model;

	private transient Gradebook gradebook;
	private boolean showPoints;
	private boolean showOverride;
	private CourseGrade courseGrade;
	private GbRole currentUserRole;
	private String studentUuid;

	public GbCourseGradeLabel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Map<String, Object> modelData = this.model.getObject();

		// required
		this.currentUserRole = (GbRole) modelData.get("currentUserRole");
		this.courseGrade = (CourseGrade) modelData.get("courseGrade");
		this.gradebook = (Gradebook) modelData.get("gradebook");
		this.showPoints = (Boolean) modelData.get("showPoints");
		this.showOverride = (Boolean) modelData.get("showOverride");

		// optional
		this.studentUuid = (String) modelData.get("studentUuid");

		// required for TA and student
		final String currentUserUuid = (String) modelData.get("currentUserUuid");

		// instructor, can view
		if (this.currentUserRole == GbRole.INSTRUCTOR) {
			setDefaultModel(Model.of(buildCourseGrade()));
			// TA, permission check
		} else if (this.currentUserRole == GbRole.TA) {
			// TODO this could be passed in though we have groups to cater for
			if (!this.businessService.isCourseGradeVisible(currentUserUuid)) {
				setDefaultModel(new ResourceModel("label.coursegrade.nopermission"));
			} else {
				setDefaultModel(Model.of(buildCourseGrade()));
			}
			// student, check if course grade released, and permission check
		} else {
			if (this.gradebook.isCourseGradeDisplayed()) {
				if (!this.businessService.isCourseGradeVisible(currentUserUuid)) {
					setDefaultModel(new ResourceModel("label.coursegrade.nopermission"));
				} else {
					setDefaultModel(Model.of(buildCourseGrade()));
				}
			} else {
				setDefaultModel(Model.of(getString("label.coursegrade.studentnotreleased")));
			}
		}

		// always
		setOutputMarkupId(true);
	}

	@Override
	public void onEvent(final IEvent<?> event) {
		super.onEvent(event);
		if (event.getPayload() instanceof ScoreChangedEvent) {
			final ScoreChangedEvent scoreChangedEvent = (ScoreChangedEvent) event.getPayload();

			// if event is for this student (which may not be applicable if we have no student)
			if (StringUtils.equals(this.studentUuid, scoreChangedEvent.getStudentUuid())) {

				// get the new course grade
				final CourseGrade updatedCourseGrade = this.businessService.getCourseGrade(scoreChangedEvent.getStudentUuid());
				this.courseGrade = updatedCourseGrade;

				// update the string and announce
				setDefaultModel(Model.of(buildCourseGrade()));
				scoreChangedEvent.getTarget().add(this);
				scoreChangedEvent.getTarget().appendJavaScript(
						String.format("$('#%s').closest('td').addClass('gb-score-dynamically-updated');", this.getMarkupId()));
			}
		}
	}

	/**
	 * Takes care of checking the values and configured settings to format the course grade into an applicable display format
	 *
	 * Format: Instructor always gets lettergrade + percentage but may also get points depending on setting
	 *
	 * Student gets whatever is configured
	 *
	 * @return formatted string ready for display
	 */
	private String buildCourseGrade() {
		final List<String> parts = new ArrayList<>();

		// letter grade
		String letterGrade = null;
		if (this.showOverride && StringUtils.isNotBlank(this.courseGrade.getEnteredGrade())) {
			letterGrade = this.courseGrade.getEnteredGrade();
		} else {
			letterGrade = this.courseGrade.getMappedGrade();
		}

		if (StringUtils.isNotBlank(letterGrade)
				&& (this.gradebook.isCourseLetterGradeDisplayed() || this.currentUserRole == GbRole.INSTRUCTOR)) {
			parts.add(letterGrade);
		}

		// percentage
		final String calculatedGrade = FormatHelper.formatStringAsPercentage(this.courseGrade.getCalculatedGrade());

		if (StringUtils.isNotBlank(calculatedGrade)
				&& (this.gradebook.isCourseAverageDisplayed() || this.currentUserRole == GbRole.INSTRUCTOR)) {
			if (parts.isEmpty()) {
				parts.add(new StringResourceModel("coursegrade.display.percentage-first", null,
						new Object[] { calculatedGrade }).getString());
			} else {
				parts.add(new StringResourceModel("coursegrade.display.percentage-second", null,
						new Object[] { calculatedGrade }).getString());
			}
		}

		// points
		if (this.showPoints) {

			// don't display points for weighted category type
			final GbCategoryType categoryType = GbCategoryType.valueOf(this.gradebook.getCategory_type());
			if (categoryType != GbCategoryType.WEIGHTED_CATEGORY) {

				final Double pointsEarned = this.courseGrade.getPointsEarned();
				final Double totalPointsPossible = this.courseGrade.getTotalPointsPossible();

				if (this.gradebook.isCoursePointsDisplayed()) {
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

		// if parts is empty, there are no grades, display a -
		if (parts.isEmpty()) {
			parts.add(getString("coursegrade.display.none"));
		}

		return String.join(" ", parts);
	}

}
