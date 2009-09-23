/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.components;

import java.awt.image.BufferedImage;

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebResponse;


/**
 * @author Nuno Fernandes
 */
@SuppressWarnings("serial")
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
		System.out.println("getBufferedImage()");
		return bufferedImage;
	}

	private void createImage() {
		chartImage = new Image("chartImage") {
			@Override
			protected Resource getImageResource() {
				return new DynamicImageResource() {

					@Override
					protected byte[] getImageData() {
						return toImageData(getBufferedImage());
					}

					@Override
					protected void setHeaders(WebResponse response) {
//						if(isCacheable()){
//							super.setHeaders(response);
//						}else{
							response.setHeader("Pragma", "no-cache");
							response.setHeader("Cache-Control", "no-cache");
							response.setDateHeader("Expires", 0);
//						}
					}
				};
			}
		};
		chartImage.setOutputMarkupId(true);
	}	
	
}
