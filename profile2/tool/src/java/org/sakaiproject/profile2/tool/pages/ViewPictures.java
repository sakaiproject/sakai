package org.sakaiproject.profile2.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.dataproviders.GalleryImageDataProvider;
import org.sakaiproject.profile2.tool.pages.panels.GalleryImagePanel;

/**
 * Gallery component for viewing another user's pictures.
 */
public class ViewPictures extends BasePage {

	private static final Logger log = Logger.getLogger(ViewPictures.class);
	
	private GridView gridView;
	
	public ViewPictures(String userUuid) {

		log.debug("ViewPictures()");
			
		configureFeedback();
		createGalleryForm(userUuid);
	}

	private void createGalleryForm(final String userUuid) {
		
		Label galleryHeading = new Label("galleryHeading", new StringResourceModel(
				"heading.pictures.view.pictures", null, new Object[] { Locator
						.getSakaiProxy().getUserDisplayName(userUuid) }));
		
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

		gridView = new GridView("rows", dataProvider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item item) {
				
				GalleryImage image = (GalleryImage) item.getModelObject();
				
				item.add(new GalleryImagePanel("galleryImage", userUuid, false,
						true, image, gridView.getCurrentPage()));
			}

			@Override
			protected void populateEmptyItem(Item item) {
				
				item.add(new Label("galleryImage"));
			}
		};

		gridView.setRows(3);
		gridView.setColumns(4);

		galleryForm.add(gridView);
		galleryForm.add(new PagingNavigator("navigator", gridView));
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
