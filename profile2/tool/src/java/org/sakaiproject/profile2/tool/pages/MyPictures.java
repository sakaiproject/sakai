package org.sakaiproject.profile2.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.tool.dataproviders.GalleryImageDataProvider;
import org.sakaiproject.profile2.tool.pages.panels.GalleryImagePanel;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Main gallery component for viewing the current user's picture gallery.
 */
public class MyPictures extends BasePage {

	private static final Logger log = Logger.getLogger(MyPictures.class);

	private FileUploadField addPictureField;
	private GridView gridView;

	/**
	 * Constructor for current user.
	 */
	public MyPictures() {
		this(0, Locator.getSakaiProxy().getCurrentUserId());
	}

	/**
	 * Constructor for current user.
	 * 
	 * @param pageToDisplay
	 *            gallery page index used to set the page the user is returned
	 *            to after making a change to the gallery.
	 */
	public MyPictures(int pageToDisplay) {
		this(pageToDisplay, Locator.getSakaiProxy().getCurrentUserId());
	}

	/**
	 * This constructor enables an admin user to edit another user's gallery.
	 */
	public MyPictures(String userUuid) {
		this(0, userUuid);
	}

	/**
	 * This constructor enables an admin user to edit another user's gallery.
	 */
	public MyPictures(int pageToDisplay, String userUuid) {

		log.debug("MyPictures()");

		configureFeedback();
		createGalleryForm(userUuid, pageToDisplay);
		createAddPictureForm(userUuid);
	}

	private void createAddPictureForm(final String userUuid) {

		Form addPictureForm = new Form("addPictureForm") {

			private static final long serialVersionUID = 1L;

			// handle the file upload
			public void onSubmit() {

				FileUpload upload = addPictureField.getFileUpload();

				if (upload == null) {
					log.error("Profile.MyPictures: upload was null.");
					error(new StringResourceModel("error.no.file.uploaded",
							this, null).getString());
					return;
				} else if (upload.getSize() == 0) {
					log.error("Profile.MyPictures.onSubmit: upload was empty.");
					error(new StringResourceModel("error.empty.file.uploaded",
							this, null).getString());
					return;
				} else if (!ProfileUtils.checkContentTypeForProfileImage(upload
						.getContentType())) {
					log
							.error("Profile.MyPictures.onSubmit: invalid file type uploaded for gallery");
					error(new StringResourceModel("error.invalid.image.type",
							this, null).getString());
					return;
				} else {

					byte[] imageBytes = upload.getBytes();

					if (Locator.getProfileImageService()
							.addProfileGalleryImage(userUuid, imageBytes,
									upload.getContentType(),
									upload.getClientFileName())) {

						// post upload event
						getSakaiProxy().postEvent(
								ProfileConstants.EVENT_GALLERY_IMAGE_UPLOAD,
								"/profile/" + sakaiProxy.getCurrentUserId(),
								true);

						if (sakaiProxy.isSuperUserAndProxiedToUser(userUuid)) {
							setResponsePage(new MyPictures(gridView
									.getPageCount() - 1, userUuid));
						} else {
							setResponsePage(new MyPictures(gridView
									.getPageCount() - 1));
						}

					} else {
						error(new StringResourceModel("error.file.save.failed",
								this, null).getString());
						return;
					}
				}
			}
		};

		addPictureForm.setOutputMarkupId(true);
		add(addPictureForm);

		WebMarkupContainer addPictureContainer = new WebMarkupContainer(
				"addPictureContainer");
		addPictureContainer.add(new Label("addPictureLabel", new ResourceModel(
				"pictures.addpicture")));
		addPictureField = new FileUploadField("choosePicture");
		addPictureContainer.add(addPictureField);
		Button submitButton = new Button("submitPicture", new ResourceModel(
				"button.gallery.upload"));
		addPictureContainer.add(submitButton);
		addPictureForm.add(addPictureContainer);
	}

	private void createGalleryForm(final String userUuid, int pageToDisplay) {

		Label galleryHeading = new Label("galleryHeading", new ResourceModel(
				"heading.pictures.my.pictures"));
		add(galleryHeading);

		Form galleryForm = new Form("galleryForm") {

			private static final long serialVersionUID = 1L;
		};
		galleryForm.setOutputMarkupId(true);

		populateGallery(galleryForm, userUuid, pageToDisplay);

		add(galleryForm);

		Label addPictureHeading = new Label("addPictureHeading",
				new ResourceModel("heading.pictures.addpicture"));
		add(addPictureHeading);
	}

	/**
	 * Populates gallery using GalleryImageDataProvider for given user. The
	 * pageToDisplay allows us to return the user to the gallery page they were
	 * previously viewing after removing an image from the gallery.
	 */
	private void populateGallery(Form galleryForm, final String userUuid,
			int pageToDisplay) {

		IDataProvider dataProvider = new GalleryImageDataProvider(userUuid);

		gridView = new GridView("rows", dataProvider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(Item item) {

				GalleryImage image = (GalleryImage) item.getModelObject();

				item.add(new GalleryImagePanel("galleryImage", userUuid, true,
						true, image, gridView.getCurrentPage()));
			}

			@Override
			protected void populateEmptyItem(Item item) {

				item.add(new Label("galleryImage", ""));
			}
		};
		gridView.setRows(3);
		gridView.setColumns(4);

		galleryForm.add(gridView);
		if (gridView.getItemCount() == 0) {
			galleryForm.add(new PagingNavigator("navigator", gridView)
					.setVisible(false));
		} else {
			galleryForm.add(new PagingNavigator("navigator", gridView));
		}

		// set page to display
		if (pageToDisplay > -1 && pageToDisplay < gridView.getPageCount()) {
			gridView.setCurrentPage(pageToDisplay);
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
