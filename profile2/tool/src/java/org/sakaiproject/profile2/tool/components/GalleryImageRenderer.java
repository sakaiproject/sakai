package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Gallery component for rendering a single image.
 */
public class GalleryImageRenderer extends Panel {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of <code>GalleryImageRenderer</code>.
	 */
	public GalleryImageRenderer(String id, boolean cacheable,
			String imageResourceId) {
		super(id);
		
		if (imageResourceId == null) {
			add(new ContextImage("img",new Model(ProfileConstants.UNAVAILABLE_IMAGE)));
			return;
		}
		
		SakaiProxy sakaiProxy = Locator.getSakaiProxy();

		final byte[] imageBytes = sakaiProxy.getResource(imageResourceId);
		
		if (imageBytes != null && imageBytes.length > 0) {

			BufferedDynamicImageResource imageResource = new BufferedDynamicImageResource() {

				private static final long serialVersionUID = 1L;

				protected byte[] getImageData() {
					return imageBytes;
				}
			};

			add(new Image("img", new Model(imageResource)));
		} else {
			add(new ContextImage("img",new Model(ProfileConstants.UNAVAILABLE_IMAGE)));
		}
	}

}
