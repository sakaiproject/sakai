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
package org.sakaiproject.profile2.tool.pages.panels;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.tool.components.GalleryImageRenderer;
import org.sakaiproject.profile2.tool.dataproviders.GalleryImageRandomizedDataProvider;
import org.sakaiproject.profile2.tool.pages.MyPictures;
import org.sakaiproject.profile2.tool.pages.ViewPicture;
import org.sakaiproject.profile2.tool.pages.ViewPictures;

/**
 * Gallery image feed component which sits on MyProfile/ViewProfile.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@Slf4j
public class GalleryFeed extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;	

	@SuppressWarnings("unchecked")
	public GalleryFeed(String id, final String ownerUserId,
			final String viewingUserId) {

		super(id);

		log.debug("GalleryFeed()");

		Label heading;
		if (viewingUserId.equals(ownerUserId)) {
			heading = new Label("heading", new ResourceModel(
					"heading.widget.my.pictures"));
		} else {
			heading = new Label("heading", new StringResourceModel(
					"heading.widget.view.pictures", null, new Object[] { sakaiProxy.getUserDisplayName(ownerUserId) }));
		}
		add(heading);
		
		IDataProvider dataProvider = new GalleryImageRandomizedDataProvider(ownerUserId);

		GridView dataView = new GridView("rows", dataProvider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateEmptyItem(Item item) {
				// TODO make "fake" clickable
				Link emptyImageLink = new Link("galleryFeedItem") {
					public void onClick() {
					}

				};

				Label galleryFeedPicture = new Label("galleryImageThumbnailRenderer", "");
				emptyImageLink.add(galleryFeedPicture);

				item.add(emptyImageLink);
			}

			@Override
			protected void populateItem(Item item) {

				final GalleryImage image = (GalleryImage) item.getModelObject();

				GalleryImageRenderer galleryImageThumbnailRenderer = new GalleryImageRenderer(
						"galleryImageThumbnailRenderer", image
								.getThumbnailResource());

				AjaxLink galleryFeedItem = new AjaxLink("galleryFeedItem") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						// view-only (i.e. no edit functionality)
						setResponsePage(new ViewPicture(image));
					}
				};
				galleryFeedItem.add(galleryImageThumbnailRenderer);

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

				if (sakaiProxy.isSuperUserAndProxiedToUser(ownerUserId)) {
					setResponsePage(new ViewPictures(ownerUserId));
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
			numPicturesLabel.setDefaultModel(new ResourceModel(
					"text.gallery.feed.num.none"));
			viewPicturesLabel = new Label("viewPicturesLabel",
					new ResourceModel("link.gallery.feed.addnew"));

			if (!viewingUserId.equals(ownerUserId) || 
					sakaiProxy.isSuperUserAndProxiedToUser(ownerUserId)) {
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

}
