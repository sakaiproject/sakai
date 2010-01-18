/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

package org.sakaiproject.profile2.tool.pages.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.tool.components.GalleryImageRenderer;

/**
 * Gallery component provides a thumbnail view of a gallery image, with a link
 * to the full-size image. If the viewing user is allowed, the full-size image
 * is provided with gallery image modification options.
 */
public class GalleryImagePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(GalleryImagePanel.class);

	private final ModalWindow mainImageWindow;
	
	public GalleryImagePanel(String id, String userUuid, boolean allowed,
			boolean cacheable, GalleryImage image, int galleryPageIndex) {

		super(id);

		log.debug("GalleryImagePanel()");
				
		// add thumbnail image
		add(new GalleryImageRenderer("galleryImageThumbnailRenderer",
				true, image.getThumbnailResource()));

		// create modal window for main image
		mainImageWindow = new ModalWindow(
				"galleryMainImageWindow");

		mainImageWindow.setTitle(image.getDisplayName());
		mainImageWindow.setCookieName("galleryMainImage");
		mainImageWindow.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
		add(mainImageWindow);

		Panel mainImagePanel;
		if (allowed) {
			// create panel with remove / set as profile options
			mainImagePanel = new GalleryImageEdit(mainImageWindow
					.getContentId(), mainImageWindow, userUuid, image,
					galleryPageIndex);
		} else {
			// create view-only panel
			mainImagePanel = new GalleryImageRenderer(mainImageWindow
					.getContentId(), true, image.getMainResource());
		}

		mainImageWindow.setContent(mainImagePanel);
	}
	
	public void displayGalleryImage(AjaxRequestTarget target) {
		mainImageWindow.show(target);
	}

}
