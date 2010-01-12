package org.sakaiproject.profile2.tool.pages.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
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

	public GalleryImagePanel(String id, String userUuid, boolean allowed,
			boolean cacheable, GalleryImage image, int galleryPageIndex) {

		super(id);

		log.debug("GalleryImagePanel()");
				
		// add thumbnail image
		add(new GalleryImageRenderer("galleryImageThumbnailRenderer",
				true, image.getThumbnailResource()));

		// create modal window for main image
		final ModalWindow mainImageWindow = new ModalWindow(
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

		// add link for main image
		AjaxLink galleryMainImageLink = new AjaxLink("galleryMainImageLink") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				mainImageWindow.show(target);
			}
		};

		galleryMainImageLink.add(new Label("galleryMainImageLabel",
				new ResourceModel("link.gallery.image.view")));

		add(galleryMainImageLink);
	}

}
