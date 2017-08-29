/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.IResource;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;

/**
 * Renders a {@link NonCachingImage} via JFreeChart using the {@link JFreeChart} data supplied as the model. Attach this to an img tag
 */
public class JFreeChartImageWithToolTip extends NonCachingImage {

	private static final long serialVersionUID = 1L;
	private final String imageMapId;
	private final int width;
	private final int height;
	private final ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo(new StandardEntityCollection());

	/**
	 * Build the chart
	 * 
	 * @param id wicket id
	 * @param model data as {@link IModel} of type {@link JFreeChart}
	 * @param imageMapId HTML id of the tooltip connected via the usemap attribute in the HTML tag
	 * @param width width of chart
	 * @param height height of chart
	 */
	public JFreeChartImageWithToolTip(final String id, final IModel<JFreeChart> model,
			final String imageMapId, final int width, final int height) {
		super(id, model);
		this.imageMapId = imageMapId;
		this.width = width;
		this.height = height;

		setOutputMarkupId(true);
	}

	@Override
	protected IResource getImageResource() {
		IResource imageResource = null;
		final JFreeChart chart = (JFreeChart) getDefaultModelObject();
		imageResource = new DynamicImageResource() {
			private static final long serialVersionUID = 1L;

			@Override
			protected byte[] getImageData(final Attributes attributes) {
				final ByteArrayOutputStream stream = new ByteArrayOutputStream();
				try {
					if (chart != null) {
						JFreeChartImageWithToolTip.this.chartRenderingInfo.clear();
						ChartUtilities.writeChartAsPNG(stream, chart, JFreeChartImageWithToolTip.this.width,
								JFreeChartImageWithToolTip.this.height, JFreeChartImageWithToolTip.this.chartRenderingInfo);
					}
				} catch (final IOException ex) {
					// TODO logging for rendering chart error
				}
				return stream.toByteArray();
			}
		};
		return imageResource;
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		final JFreeChart chart = (JFreeChart) getDefaultModelObject();
		if (chart == null) {
			return;
		}
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			this.chartRenderingInfo.clear();
			ChartUtilities.writeChartAsPNG(stream, chart, this.width, this.height, this.chartRenderingInfo);
		} catch (final IOException ex) {
			// do something
		}
		replaceComponentTagBody(markupStream, openTag, ChartUtilities.getImageMap(this.imageMapId, this.chartRenderingInfo));
	}
}
