package org.sakaiproject.gradebookng.tool.component;

import org.sakaiproject.gradebookng.tool.model.GbChartData;

/**
 * Panel that renders the individual assignment grade charts
 */
public class GbAssignmentGradeChart extends GbBaseChart {

	private static final long serialVersionUID = 1L;

	private final long assignmentId;

	public GbAssignmentGradeChart(final String id, final long assignmentId) {
		super(id);
		this.assignmentId = assignmentId;
	}

	@Override
	protected GbChartData getData() {
		return null;

	}

}

