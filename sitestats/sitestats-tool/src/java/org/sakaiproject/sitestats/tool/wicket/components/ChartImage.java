/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.components;

import java.awt.image.BufferedImage;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.time.Duration;


/**
 * @author Nuno Fernandes
 */
@SuppressWarnings("serial")
@Slf4j
public class ChartImage extends Panel {
	private static final long 		serialVersionUID = 1L;
	private transient BufferedImage	bufferedImage = null;
	private Image 					chartImage = null;

	public ChartImage(String id, final BufferedImage image) {
		super(id);
		this.bufferedImage = image;

		createImage();
		add(chartImage);
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
		createImage();
	}

	public BufferedImage getBufferedImage() {
		log.debug("getBufferedImage()");
		return bufferedImage;
	}

	private void createImage() {
		chartImage = new Image("chartImage") {
			@Override
			protected IResource getImageResource() {
				return new DynamicImageResource() {

					@Override
					protected byte[] getImageData(IResource.Attributes attributes) {
						return toImageData(getBufferedImage());
					}

					// adapted from https://cwiki.apache.org/confluence/display/WICKET/JFreeChart+and+wicket+example
					@Override
					protected void configureResponse(AbstractResource.ResourceResponse response, IResource.Attributes attributes)
					{
						super.configureResponse(response, attributes);
						
						response.setCacheDuration(Duration.NONE);
						response.setCacheScope(WebResponse.CacheScope.PRIVATE);
					}
				};
			}
		};
		chartImage.setOutputMarkupId(true);
	}	
	
}
