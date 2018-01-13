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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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
import org.sakaiproject.profile2.tool.components.GalleryImageRenderer;
import org.sakaiproject.profile2.tool.components.IconWithClueTip;
import org.sakaiproject.profile2.tool.dataproviders.GalleryImageDataProvider;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Main gallery component for viewing the current user's picture gallery.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@Slf4j
public class MyPictures extends BasePage {

	private List<File> addPictureFiles = new ArrayList<File>();
	private FileListView addPictureListView;
	private Folder addPictureUploadFolder;
	private GridView gridView;
	
	/**
	 * Constructor for current user.
	 */
	public MyPictures() {
		renderMyPictures(0, sakaiProxy.getCurrentUserId());
	}

	/**
	 * Constructor for current user.
	 * 
	 * @param pageToDisplay
	 *            gallery page index used to set the page the user is returned
	 *            to after making a change to the gallery.
	 */
	public MyPictures(long pageToDisplay) {
		renderMyPictures(pageToDisplay, sakaiProxy.getCurrentUserId());
	}

	/**
	 * Does the actual rendering of the page
	 */
	private void renderMyPictures(long pageToDisplay, String userUuid) {

		log.debug("MyPictures()");
		
		disableLink(myPicturesLink);

		createGalleryForm(userUuid, pageToDisplay);
		createAddPictureForm(userUuid);
	}

	private void createAddPictureForm(final String userUuid) {

		addPictureUploadFolder = new Folder(System
				.getProperty("java.io.tmpdir"), "addPicturesUploadFolder");
		addPictureUploadFolder.mkdirs();
		
		//file feedback will be redirected here
        final FeedbackPanel fileFeedback = new FeedbackPanel("fileFeedback");
        fileFeedback.setOutputMarkupId(true);
        
		Form addPictureForm = new FileUploadForm("form", userUuid, fileFeedback);
        addPictureForm.add(fileFeedback);
		addPictureForm.setOutputMarkupId(true);
		add(addPictureForm);
		
		Label invalidFileTypeMessageLabel = new Label("invalidFileTypeMessage",new ResourceModel("pictures.filetypewarning"));
		invalidFileTypeMessageLabel.setMarkupId("invalidFileTypeMessage");
		invalidFileTypeMessageLabel.setOutputMarkupId(true);
		addPictureForm.add(invalidFileTypeMessageLabel);
		
		WebMarkupContainer addPictureContainer = new WebMarkupContainer(
				"addPictureContainer");
		addPictureContainer.add(new Label("addPictureLabel", new ResourceModel(
				"pictures.addpicture")));

		addPictureContainer.add(new MultiFileUploadField("choosePicture",
				new PropertyModel<Collection<FileUpload>>(addPictureForm,
						"uploads"), ProfileConstants.MAX_GALLERY_FILE_UPLOADS));

		IndicatingAjaxButton submitButton = new IndicatingAjaxButton(
				"submitPicture", new ResourceModel("button.gallery.upload")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				target.add(fileFeedback);
			}
			
        	protected void onError(AjaxRequestTarget target, Form form) { 
        		log.debug("MyPictures.onSubmit validation failed.");
        	    target.add(fileFeedback); 
        	} 

		};
		addPictureContainer.add(submitButton);

		addPictureContainer
				.add(new IconWithClueTip("galleryImageUploadToolTip",
						ProfileConstants.INFO_IMAGE, new StringResourceModel(
								"text.gallery.upload.tooltip", null,
								new Object[] { sakaiProxy.getMaxProfilePictureSize()
										* ProfileConstants.MAX_GALLERY_FILE_UPLOADS })));
		
		addPictureForm.add(addPictureContainer);
		
		addPictureFiles.addAll(Arrays
				.asList(addPictureUploadFolder.listFiles()));
		addPictureListView = new FileListView("fileList", addPictureFiles);
		addPictureForm.add(addPictureListView);
	}

	private void createGalleryForm(final String userUuid, long pageToDisplay) {

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
			long pageToDisplay) {

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
						setResponsePage(new MyPicture(userUuid, image, getCurrentPage()));
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

		Label noPicturesLabel;

		//pager
		if (numImages == 0) {
			galleryForm.add(new PagingNavigator("navigator", gridView).setVisible(false));
			noPicturesLabel = new Label("noPicturesLabel", new ResourceModel("text.gallery.pictures.num.none"));
		} else if (numImages <= ProfileConstants.MAX_GALLERY_IMAGES_PER_PAGE) {
			galleryForm.add(new PagingNavigator("navigator", gridView).setVisible(false));
			noPicturesLabel = new Label("noPicturesLabel");
			noPicturesLabel.setVisible(false);
		} else {
			galleryForm.add(new PagingNavigator("navigator", gridView));
			noPicturesLabel = new Label("noPicturesLabel");
			noPicturesLabel.setVisible(false);
		}
		
		
		
		

		galleryForm.add(noPicturesLabel);
		
		// set page to display
		if (pageToDisplay > 0) {
			if (pageToDisplay < gridView.getPageCount()) {
				gridView.setCurrentPage(pageToDisplay);
			} else {
				// default to last page for add/remove operations
				gridView.setCurrentPage(gridView.getPageCount() - 1);
			}
		} else {
			gridView.setCurrentPage(0);
		}
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

		private FeedbackPanel fileFeedback;
		
		public FileUploadForm(String id, String userUuid, FeedbackPanel fileFeedback) {
			super(id);

			this.userUuid = userUuid;
			this.fileFeedback = fileFeedback;

			// set form to multipart mode
			setMultiPart(true);

			setMaxSize(Bytes.megabytes(sakaiProxy.getMaxProfilePictureSize()
					* ProfileConstants.MAX_GALLERY_FILE_UPLOADS));
		}

		public Collection<FileUpload> getUploads() {
			return uploads;
		}

		protected void onSubmit() {
			
			if (uploads.size() == 0) {
				error(new StringResourceModel("error.gallery.upload.warning", this, null).getString());
				return;
			}
			
			Iterator<FileUpload> filesToUpload = uploads.iterator();
			
			while (filesToUpload.hasNext()) {
				final FileUpload upload = filesToUpload.next();

				if (upload == null) {
					log.error("picture upload was null.");
					error(new StringResourceModel("error.no.file.uploaded", this, null).getString());
					return;
				} else if (upload.getSize() == 0) {
					log.error("picture upload was empty.");
					error(new StringResourceModel("error.empty.file.uploaded", this, null).getString());
					return;
				} else if (!ProfileUtils.checkContentTypeForProfileImage(upload
						.getContentType())) {
					log.error("attempted to upload invalid file type to gallery");
					error(new StringResourceModel("error.invalid.image.type", this, null).getString());
					return;
				}

				byte[] imageBytes = upload.getBytes();

				if (!imageLogic.addGalleryImage(
						userUuid, imageBytes, upload.getContentType(),
						FilenameUtils.getName(upload.getClientFileName()))) {

					log.error("unable to save gallery image");
					error(new StringResourceModel("error.file.save.failed", this, null).getString());
					return;
				}

				// post upload event
				sakaiProxy.postEvent(
						ProfileConstants.EVENT_GALLERY_IMAGE_UPLOAD,
						"/profile/" + sakaiProxy.getCurrentUserId(), true);
			}
			
			// post to walls if wall enabled
			if (true == sakaiProxy.isWallEnabledGlobally()) {
				wallLogic.addNewEventToWall(ProfileConstants.EVENT_GALLERY_IMAGE_UPLOAD, sakaiProxy.getCurrentUserId());
			}
			
			setResponsePage(new MyPictures(gridView.getPageCount()));
		}
		
	}
	
}
