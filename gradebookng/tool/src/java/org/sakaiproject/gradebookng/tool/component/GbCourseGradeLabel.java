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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
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

	private final String userId;

	public GbCourseGradeLabel(final String id, final String userId) {
		super(id);
		this.userId = userId;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

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
				final CourseGrade courseGrade = this.businessService.getCourseGrade(this.userId);
				final GradebookInformation settings = this.businessService.getGradebookSettings();
				setDefaultModel(Model.of(buildCourseGrade(settings, courseGrade)));
			}
		} else {
			setDefaultModel(Model.of(getString("label.studentsummary.coursegradenotreleased")));
		}
	}

	private String buildCourseGrade(final GradebookInformation settings, final CourseGrade courseGrade) {
		final List<String> parts = new ArrayList<>();

		if (settings.isCourseLetterGradeDisplayed()) {
			if (StringUtils.isNotBlank(courseGrade.getMappedGrade())) {
				parts.add(courseGrade.getMappedGrade());
			} else if (StringUtils.isNotBlank(courseGrade.getEnteredGrade())) {
				parts.add(courseGrade.getEnteredGrade());
			}
		}

		if (settings.isCourseAverageDisplayed()) {
			if (parts.isEmpty()) {
				parts.add(new StringResourceModel("coursegrade.display.percentage-first", null,
						new Object[] { courseGrade.getCalculatedGrade() }).getString());
			} else {
				parts.add(new StringResourceModel("coursegrade.display.percentage-second", null,
						new Object[] { courseGrade.getCalculatedGrade() }).getString());
			}
		}

		if (settings.isCoursePointsDisplayed()) {
			if (parts.isEmpty()) {
				parts.add(new StringResourceModel("coursegrade.display.points-first", null,
						new Object[] { courseGrade.getPointsEarned(), courseGrade.getTotalPointsPossible() }).getString());
			} else {
				parts.add(new StringResourceModel("coursegrade.display.points-second", null,
						new Object[] { courseGrade.getPointsEarned(), courseGrade.getTotalPointsPossible() }).getString());
			}
		}

		return StringUtils.join(parts, " ");
	}
}
