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
	private BufferedImage bufferedImage = null;
	private Image chartImage = null;

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
