package org.sakaiproject.gradebookng.tool.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that wraps up the summary of grades for an assignment or course grade. Provides the dataset, config and labels to be used by the
 * chart
 */
public class GbChartData {

	/**
	 * Key is the label, ie letter grade, value is the count of students that have that grade
	 */
	@Getter
	@Setter
	private Map<String, Integer> dataset;

	@Getter
	@Setter
	private String chartTitle;

	@Getter
	@Setter
	private String xAxisLabel;

	@Getter
	@Setter
	private String yAxisLabel;

	/**
	 * 'horizontalBar' or 'bar' are currently supported options
	 */
	@Getter
	@Setter
	private String chartType;

	/**
	 * HTML ID of the chart
	 */
	@Getter
	@Setter
	private String chartId;

	public GbChartData() {
		this.dataset = new LinkedHashMap<>();
	}

	/**
	 * If label present then increment its count, otherwise add it
	 *
	 * @param label add to this axis increment
	 */
	public void add(final String label) {
		this.dataset.computeIfPresent(label, (k, v) -> v + 1);
		this.dataset.computeIfAbsent(label, value -> 1);
	}

	/**
	 * Add the label but keep the count as zero. Useful for adding increments to axes without affecting the stats.
	 *
	 * @param label label to add as an axis increment
	 */
	public void addZeroed(final String label) {
		this.dataset.put(label, 0);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
