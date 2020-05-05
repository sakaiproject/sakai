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
package org.sakaiproject.gradebookng.tool.stats;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import lombok.Getter;

/**
 * Base class for stats based on a set of grades. Implement this to do the things.
 *
 * Note that this has its own markup for the base set of stats data.
 *
 * Subclasses can optionally provide additional markup in the form of &lt;dt&gt; and &lt;dd&gt; pairs which will be added in to the
 * &gt;dl&lt; list. However this is not mandatory and no markup is required for subclasses if the default set of stats produced are ok.
 *
 * Note: calculateStatistics is called automatically via onInitialise. If you override this, you must set the data for the
 * calculateStatistics via the subclass constructor so that it is available.
 */
public abstract class BaseStatistics extends Panel {

	private static final long serialVersionUID = 1L;

	@Getter
	private DescriptiveStatistics statistics;

	public BaseStatistics(final String id, final IModel<?> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	/**
	 * The meat of the stats. Implement this in your subclass to provide the data. Called automatically.
	 *
	 * @return {@link DescriptiveStatistics} to use for the stats
	 */
	protected abstract DescriptiveStatistics calculateStatistics();

	/**
	 * Calculates the mean for the set
	 *
	 * @return String mean
	 */
	protected final String getMean(final DescriptiveStatistics stats) {
		return stats.getN() > 0 ? String.format("%.2f", stats.getMean()) : "-";
	}

	/**
	 * Calculates the median for the set
	 *
	 * @return String median
	 */
	protected final String getMedian(final DescriptiveStatistics stats) {
		return stats.getN() > 0 ? String.format("%.2f", stats.getPercentile(50)) : "-";
	}

	/**
	 * Calculates the min for the set
	 *
	 * @return String min
	 */
	protected final String getMin(final DescriptiveStatistics stats) {
		return stats.getN() > 0 ? String.format("%.2f", stats.getMin()) : "-";
	}

	/**
	 * Calculates the max for the set
	 *
	 * @return String max
	 */
	protected final String getMax(final DescriptiveStatistics stats) {
		return stats.getN() > 0 ? String.format("%.2f", stats.getMax()) : "-";
	}

	/**
	 * Calculates the standard deviation for the set
	 *
	 * @return String standard deviation
	 */
	protected final String getStandardDeviation(final DescriptiveStatistics stats) {
		return stats.getN() > 0 ? String.format("%.2f", stats.getStandardDeviation()) : "-";
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		this.statistics = calculateStatistics();

		add(new Label("average", getMean(this.statistics)));
		add(new Label("median", getMedian(this.statistics)));
		add(new Label("lowest", getMin(this.statistics)));
		add(new Label("highest", getMax(this.statistics)));
		add(new Label("deviation", getStandardDeviation(this.statistics)));
		add(new Label("graded", String.valueOf(this.statistics.getN())));

	}

}
