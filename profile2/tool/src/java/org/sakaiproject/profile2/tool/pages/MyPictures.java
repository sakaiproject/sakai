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

package org.sakaiproject.profile2.tool.pages;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.MultiFileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;
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

	private List<File> addPictureFiles = new ArrayList<File>();
	private FileListView addPictureListView;
	private Folder addPictureUploadFolder;

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

		addPictureUploadFolder = new Folder(System
				.getProperty("java.io.tmpdir"), "addPicturesUploadFolder");
		addPictureUploadFolder.mkdirs();

		Form addPictureForm = new FileUploadForm("addPictureForm", userUuid);

		addPictureForm.setOutputMarkupId(true);
		add(addPictureForm);

		WebMarkupContainer addPictureContainer = new WebMarkupContainer(
				"addPictureContainer");
		addPictureContainer.add(new Label("addPictureLabel", new ResourceModel(
				"pictures.addpicture")));

		addPictureContainer.add(new MultiFileUploadField("choosePicture",
				new PropertyModel<Collection<FileUpload>>(addPictureForm,
						"uploads"), ProfileConstants.MAX_GALLERY_FILE_UPLOADS));

		Button submitButton = new Button("submitPicture", new ResourceModel(
				"button.gallery.upload"));
		addPictureContainer.add(submitButton);

		addPictureContainer.add(new IconWithClueTip("galleryImageUploadToolTip",
				ProfileConstants.INFO_IMAGE, new ResourceModel("text.gallery.upload.tooltip")));
		
		addPictureForm.add(addPictureContainer);

		addPictureFiles.addAll(Arrays
				.asList(addPictureUploadFolder.listFiles()));
		addPictureListView = new FileListView("fileList", addPictureFiles);
		addPictureForm.add(addPictureListView);
	}

	private void createGalleryForm(final String userUuid, int pageToDisplay) {

		Label galleryHeading = new Label("galleryHeading", new ResourceModel(
				"heading.pictures.my.pictures"));
		add(galleryHeading);

		Form galleryForm = new Form("galleryForm");
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

				final GalleryImagePanel imagePanel = new GalleryImagePanel(
						"galleryImage", userUuid, true, true, image, gridView
								.getCurrentPage());

				AjaxLink galleryImageLink = new AjaxLink("galleryItem") {

					public void onClick(AjaxRequestTarget target) {
						imagePanel.displayGalleryImage(target);
					}

				};
				galleryImageLink.add(imagePanel);

				item.add(galleryImageLink);
			}

			@Override
			protected void populateEmptyItem(Item item) {

				Link galleryImageLink = new Link("galleryItem") {
					@Override
					public void onClick() {

					}
				};

				galleryImageLink.add(new Label("galleryImage"));
				item.add(galleryImageLink);
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
		if (pageToDisplay > 0) {
			if (pageToDisplay < gridView.getPageCount()) {
				gridView.setCurrentPage(pageToDisplay);
			}
			else {
				// default to last page for add/remove operations
				gridView.setCurrentPage(gridView.getPageCount() - 1);
			}
		} else {
			gridView.setCurrentPage(0);
		}
	}

	private void configureFeedback() {

		// activate feedback panel
		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		feedbackPanel.setVisible(false);
		
		add(feedbackPanel);

		// don't show filtered feedback errors in feedback panel
		int[] filteredErrorLevels = new int[] { FeedbackMessage.ERROR };
		feedbackPanel.setFilter(new ErrorLevelsFeedbackMessageFilter(
				filteredErrorLevels));
	}

	private class FileListView extends ListView<File> {

		private static final long serialVersionUID = 1L;

		public FileListView(String name, final List<File> files) {
			super(name, files);
		}

		protected void populateItem(ListItem<File> listItem) {
			final File file = (File) listItem.getModelObject();
			listItem.add(new Label("file", file.getName()));
			listItem.add(new Link("delete") {
				public void onClick() {
					Files.remove(file);
				}
			});
		}
	}

	private class FileUploadForm extends Form {

		private static final long serialVersionUID = 1L;

		private final Collection<FileUpload> uploads = new ArrayList<FileUpload>();

		private final String userUuid;

		public FileUploadForm(String id, String userUuid) {
			super(id);

			this.userUuid = userUuid;

			// set form to multipart mode
			setMultiPart(true);

			setMaxSize(Bytes
					.kilobytes(ProfileConstants.MAX_GALLERY_IMAGE_UPLOAD_SIZE));
		}

		public Collection<FileUpload> getUploads() {
			return uploads;
		}

		protected void onSubmit() {
			
			Iterator<FileUpload> filesToUpload = uploads.iterator();
			while (filesToUpload.hasNext()) {
				final FileUpload upload = filesToUpload.next();

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
					log.error("Profile.MyPictures.onSubmit: invalid file type uploaded for gallery");
					error(new StringResourceModel("error.invalid.image.type",
							this, null).getString());
					return;
				}

				byte[] imageBytes = upload.getBytes();

				if (!Locator.getProfileImageService().addProfileGalleryImage(
						userUuid, imageBytes, upload.getContentType(),
						upload.getClientFileName())) {

					error(new StringResourceModel("error.file.save.failed",
							this, null).getString());
					return;
				}

				// post upload event
				getSakaiProxy().postEvent(
						ProfileConstants.EVENT_GALLERY_IMAGE_UPLOAD,
						"/profile/" + sakaiProxy.getCurrentUserId(), true);

				if (sakaiProxy.isSuperUserAndProxiedToUser(userUuid)) {
					setResponsePage(new MyPictures(gridView.getPageCount(),
							userUuid));
				} else {
					setResponsePage(new MyPictures(gridView.getPageCount()));
				}

			}

		}

	}
}
