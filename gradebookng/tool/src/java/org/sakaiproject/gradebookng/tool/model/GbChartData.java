/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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

	/**
	 * The label (letter grade or percentage range) that the student's grade sits within. 
	 * Providing this value is optional and sets that bar in the graph to a different 
	 * colour so a student can see where their mark fits in comparison to others.
	 */
	@Getter
	@Setter
	private String studentGradeRange;

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
