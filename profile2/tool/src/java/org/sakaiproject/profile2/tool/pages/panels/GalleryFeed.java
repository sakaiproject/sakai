package org.sakaiproject.profile2.tool.pages.panels;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.components.GalleryImageRenderer;
import org.sakaiproject.profile2.tool.dataproviders.GalleryImageDataProvider;
import org.sakaiproject.profile2.tool.pages.MyPictures;
import org.sakaiproject.profile2.tool.pages.ViewPictures;

/**
 * Gallery image feed component which sits on MyProfile/ViewProfile.
 */
public class GalleryFeed extends Panel {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger
			.getLogger(ChangeProfilePictureUrl.class);

	@SuppressWarnings("unchecked")
	public GalleryFeed(String id, final String ownerUserId,
			final String viewingUserId) {
		
		super(id);

		log.debug("GalleryFeed()");

		Label heading;
		if (viewingUserId.equals(ownerUserId)) {
			heading = new Label("heading", new ResourceModel(
					"heading.feed.my.pictures"));
		} else {
			heading = new Label("heading", new StringResourceModel(
					"heading.feed.view.pictures", null, new Object[] { Locator
							.getSakaiProxy().getUserDisplayName(ownerUserId) }));
		}

		add(heading);

		IDataProvider dataProvider = new GalleryImageDataProvider(ownerUserId);

		GridView dataView = new GridView("rows", dataProvider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateEmptyItem(Item item) {

				WebMarkupContainer galleryFeedItem = new WebMarkupContainer("galleryFeedItem");
				Label galleryFeedPicture = new Label("galleryFeedPicture", "");
				galleryFeedItem.add(galleryFeedPicture);
				
				item.add(galleryFeedItem);
			}

			@Override
			protected void populateItem(Item item) {

				WebMarkupContainer galleryFeedItem = new WebMarkupContainer(
						"galleryFeedItem");

				GalleryImage image = (GalleryImage) item.getModelObject();

				galleryFeedItem.add(new GalleryImageRenderer(
						"galleryFeedPicture", true, image
								.getThumbnailResource()));
				
				item.add(galleryFeedItem);

			}

		};
		// limit gallery to 3x2 thumbnails
		dataView.setColumns(3);
		dataView.setRows(2);

		add(dataView);
	
		AjaxLink viewPicturesLink = new AjaxLink("viewPicturesLink") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				if (Locator.getSakaiProxy().isSuperUserAndProxiedToUser(
						ownerUserId)) {
					setResponsePage(new MyPictures(ownerUserId));
				} else if (viewingUserId.equals(ownerUserId)) {					
					setResponsePage(new MyPictures());
				} else {
					setResponsePage(new ViewPictures(ownerUserId));
				}
			}

		};

		Label numPicturesLabel = new Label("numPicturesLabel");
		add(numPicturesLabel);
		
		Label viewPicturesLabel;
		
		if (dataView.getItemCount() == 0) {
			numPicturesLabel.setDefaultModel(new ResourceModel("text.gallery.feed.num.none"));
			viewPicturesLabel = new Label("viewPicturesLabel",
					new ResourceModel("link.gallery.feed.addnew"));
			
			if (!viewingUserId.equals(ownerUserId)) {
				viewPicturesLink.setVisible(false);
			}
			
		} else {
			numPicturesLabel.setVisible(false);
			viewPicturesLabel = new Label("viewPicturesLabel",
					new ResourceModel("link.gallery.feed.view"));
		}
		
		viewPicturesLink.add(viewPicturesLabel);

		add(viewPicturesLink);
	}

	// deserialisation for back button
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		log.debug("GalleryFeed has been deserialized.");
	}

}
