/**
 * Copyright (c) 2008-2012 The Sakai Foundation
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
package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Gallery component for rendering a single image.
 */
public class GalleryImageRenderer extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;

	/**
	 * Creates a new instance of <code>GalleryImageRenderer</code>.
	 */
	public GalleryImageRenderer(String id, String imageResourceId) {
		super(id);
		
		if (imageResourceId == null) {
			add(new ContextImage("img",new Model(ProfileConstants.UNAVAILABLE_IMAGE)));
			return;
		}
		else if (sakaiProxy.getResource(imageResourceId) == null) {
			// may have been deleted in CHS
			add(new ContextImage("img",new Model(ProfileConstants.UNAVAILABLE_IMAGE)));
			return;
		}
	
		final byte[] imageBytes = sakaiProxy.getResource(imageResourceId).getBytes();
		
		if (imageBytes != null && imageBytes.length > 0) {

			BufferedDynamicImageResource imageResource = new BufferedDynamicImageResource() {

				private static final long serialVersionUID = 1L;
				@Override
				protected byte[] getImageData(IResource.Attributes ignored) {
					return imageBytes;
				}
			};

			Image myPic = new Image("img", new Model(imageResource));
			myPic.add(new AttributeModifier("alt", new StringResourceModel("profile.gallery.image.alt",this,null).getString()));
			add(myPic);

		} else {
			add(new ContextImage("img",new Model(ProfileConstants.UNAVAILABLE_IMAGE)));
		}
	}

}
