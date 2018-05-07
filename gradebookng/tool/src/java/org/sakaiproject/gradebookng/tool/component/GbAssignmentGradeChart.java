package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

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
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		
		// render immediately
		response.render(OnLoadHeaderItem.forScript("renderChart('" + this.assignmentId + "');"));
	}

}

