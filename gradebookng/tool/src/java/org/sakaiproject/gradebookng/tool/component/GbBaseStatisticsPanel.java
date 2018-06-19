package org.sakaiproject.gradebookng.tool.component;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Base class for stats based on a set of grades. Implement this to do the things.
 */
public abstract class GbBaseStatisticsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	protected int totalGrades;

	public GbBaseStatisticsPanel(final String id, final IModel<?> model) {
		super(id, model);
	}

	/**
	 * The meat of the stats
	 *
	 * @return
	 */
	protected abstract DescriptiveStatistics calculateStatistics();

	/**
	 * Calculates the mean for the set
	 *
	 * @return String mean
	 */
	protected final String getMean(final DescriptiveStatistics stats) {
		return this.totalGrades > 0 ? String.format("%.2f", stats.getMean()) : "-";
	}

	/**
	 * Calculates the median for the set
	 *
	 * @return String median
	 */
	protected final String getMedian(final DescriptiveStatistics stats) {
		return this.totalGrades > 0 ? String.format("%.2f", stats.getPercentile(50)) : "-";
	}

	/**
	 * Calculates the min for the set
	 *
	 * @return String min
	 */
	protected final String getMin(final DescriptiveStatistics stats) {
		return this.totalGrades > 0 ? String.format("%.2f", stats.getMin()) : "-";
	}

	/**
	 * Calculates the max for the set
	 *
	 * @return String max
	 */
	protected final String getMax(final DescriptiveStatistics stats) {
		return this.totalGrades > 0 ? String.format("%.2f", stats.getMax()) : "-";
	}

	/**
	 * Calculates the standard deviation for the set
	 *
	 * @return String standard deviation
	 */
	protected final String getStandardDeviation(final DescriptiveStatistics stats) {
		return this.totalGrades > 0 ? String.format("%.2f", stats.getStandardDeviation()) : "-";
	}

}

