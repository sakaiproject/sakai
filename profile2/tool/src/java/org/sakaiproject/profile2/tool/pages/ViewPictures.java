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
package org.sakaiproject.profile2.tool.pages;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.components.GalleryImageRenderer;
import org.sakaiproject.profile2.tool.dataproviders.GalleryImageDataProvider;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Gallery component for viewing another user's pictures.
 */
@Slf4j
public class ViewPictures extends BasePage {

	private GridView gridView;
	
	public ViewPictures(String userUuid) {

		log.debug("ViewPictures()");
			
		configureFeedback();
		createGalleryForm(userUuid);
	}

	private void createGalleryForm(final String userUuid) {
		
		Label galleryHeading = new Label("galleryHeading", new StringResourceModel(
				"heading.pictures.view.pictures", null, new Object[] { sakaiProxy.getUserDisplayName(userUuid) }));
		
		add(galleryHeading);

		Form galleryForm = new Form("galleryForm") {

			private static final long serialVersionUID = 1L;
		};
		galleryForm.setOutputMarkupId(true);

		populateGallery(galleryForm, userUuid);

		add(galleryForm);
	}
	
	private void populateGallery(Form galleryForm, final String userUuid) {

		IDataProvider dataProvider = new GalleryImageDataProvider(userUuid);

		long numImages = dataProvider.size();
		
		gridView = new GridView("rows", dataProvider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item item) {

				final GalleryImage image = (GalleryImage) item.getModelObject();

				final GalleryImageRenderer galleryImageThumbnailRenderer = new GalleryImageRenderer(
						"galleryImageThumbnailRenderer", image
								.getThumbnailResource());

				AjaxLink galleryImageLink = new AjaxLink("galleryItem") {

					public void onClick(AjaxRequestTarget target) {
						setResponsePage(new ViewPicture(image));
					}

				};
				galleryImageLink.add(galleryImageThumbnailRenderer);

				item.add(galleryImageLink);
			}

			@Override
			protected void populateEmptyItem(Item item) {

				Link galleryImageLink = new Link("galleryItem") {
					@Override
					public void onClick() {

					}
				};

				galleryImageLink.add(new Label("galleryImageThumbnailRenderer"));
				item.add(galleryImageLink);
			}
		};

		gridView.setRows(3);
		gridView.setColumns(4);
		
		galleryForm.add(gridView);
		
		//pager
		if (numImages == 0) {
			galleryForm.add(new PagingNavigator("navigator", gridView).setVisible(false));
		} else if (numImages <= ProfileConstants.MAX_GALLERY_IMAGES_PER_PAGE) {
			galleryForm.add(new PagingNavigator("navigator", gridView).setVisible(false));
		} else {
			galleryForm.add(new PagingNavigator("navigator", gridView));
		}
	}
	
	private void configureFeedback() {

		// activate feedback panel
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);

		// don't show filtered feedback errors in feedback panel
		int[] filteredErrorLevels = new int[] { FeedbackMessage.ERROR };
		feedback.setFilter(new ErrorLevelsFeedbackMessageFilter(
				filteredErrorLevels));
	}
}
