package org.sakaiproject.profile2.tool.pages.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.components.GalleryImageRenderer;
import org.sakaiproject.profile2.tool.pages.MyPictures;
import org.sakaiproject.profile2.tool.pages.MyProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Gallery component for viewing a gallery image alongside options, including
 * removing the image and setting the image as the new profile image.
 */
public class GalleryImageEdit extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(GalleryImageEdit.class);

	public GalleryImageEdit(String id, final ModalWindow mainImageWindow,
			final String userId, final GalleryImage image,
			final int galleryPageIndex) {

		super(id);

		log.debug("GalleryImageEdit()");

		// feedback label for user alert in event remove/set actions fail
		Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		add(formFeedback);

		Form imageOptionsForm = new Form("galleryImageOptionsForm");

		imageOptionsForm.setOutputMarkupId(true);
		add(imageOptionsForm);

		WebMarkupContainer imageOptionsContainer = new WebMarkupContainer(
				"galleryImageOptionsContainer");

		imageOptionsContainer.add(new Label("removePictureLabel",
				new ResourceModel("pictures.removepicture")));

		AjaxFallbackButton removePictureButton = createRemovePictureButton(
				mainImageWindow, userId, image, galleryPageIndex,
				imageOptionsForm, formFeedback);

		imageOptionsContainer.add(removePictureButton);

		imageOptionsContainer.add(new Label("setProfileImageLabel",
				new ResourceModel("pictures.setprofileimage")));

		AjaxFallbackButton setProfileImageButton = createSetProfileImageButton(
				mainImageWindow, userId, image, galleryPageIndex,
				imageOptionsForm, formFeedback);

		imageOptionsContainer.add(setProfileImageButton);

		imageOptionsForm.add(imageOptionsContainer);

		add(new GalleryImageRenderer("galleryImageMainRenderer", true,
				image.getMainResource()));
	}

	private AjaxFallbackButton createSetProfileImageButton(
			final ModalWindow mainImageWindow, final String userId,
			final GalleryImage image, final int galleryPageIndex,
			Form imageOptionsForm, final Label formFeedback) {

		AjaxFallbackButton setProfileImageButton = new AjaxFallbackButton(
				"galleryImageSetProfileButton", new ResourceModel(
						"button.gallery.setprofile"), imageOptionsForm) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {

				if (Locator.getProfileImageService().setProfileImage(
						userId,
						Locator.getSakaiProxy().getResource(
								image.getMainResource()), "", "")) {

					Locator.getSakaiProxy().postEvent(
							ProfileConstants.EVENT_PROFILE_IMAGE_CHANGE_UPLOAD,
							"/profile/" + userId, true);

					// close modal window
					mainImageWindow.close(target);

					if (Locator.getSakaiProxy().isSuperUserAndProxiedToUser(
							userId)) {
						setResponsePage(new MyProfile(userId));
					} else {
						setResponsePage(new MyProfile());
					}

				} else {
					// user alert
					formFeedback.setDefaultModel(new ResourceModel(
							"error.gallery.setprofile.failed"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model("alertMessage")));

					target.addComponent(formFeedback);
				}
			}
		};
		return setProfileImageButton;
	}

	private AjaxFallbackButton createRemovePictureButton(
			final ModalWindow mainImageWindow, final String userId,
			final GalleryImage image, final int galleryPageIndex,
			Form imageOptionsForm, final Label formFeedback) {

		AjaxFallbackButton removePictureButton = new AjaxFallbackButton(
				"galleryImageRemoveButton", new ResourceModel(
						"button.gallery.remove"), imageOptionsForm) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {

				if (Locator.getProfileImageService().removeProfileGalleryImage(
						userId, image)) {

					// close modal window
					mainImageWindow.close(target);

					if (Locator.getSakaiProxy().isSuperUserAndProxiedToUser(
							userId)) {
						setResponsePage(new MyPictures(galleryPageIndex, userId));
					} else {
						setResponsePage(new MyPictures(galleryPageIndex));
					}
				} else {
					// user alert
					formFeedback.setDefaultModel(new ResourceModel(
							"error.gallery.remove.failed"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model("alertMessage")));

					target.addComponent(formFeedback);
				}
			}

		};
		return removePictureButton;
	}

}
