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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.logic.ProfilePreferencesLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.tool.components.GalleryImageRenderer;
import org.sakaiproject.profile2.tool.pages.MyPictures;
import org.sakaiproject.profile2.tool.pages.MyProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Gallery component for viewing a gallery image alongside options, including
 * removing the image and setting the image as the new profile image.
 */
@Slf4j
public class GalleryImageEdit extends Panel {

	private static final long serialVersionUID = 1L;

	private final WebMarkupContainer imageOptionsContainer;
	private final WebMarkupContainer removeConfirmContainer;
	private final WebMarkupContainer setProfileImageConfirmContainer;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileImageLogic")
	private ProfileImageLogic imageLogic;

	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	private ProfilePreferencesLogic preferencesLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	public GalleryImageEdit(String id,
			final String userId, final GalleryImage image,
			final long galleryPageIndex) {

		super(id);

		log.debug("GalleryImageEdit()");

		// feedback label for user alert in event remove/set actions fail
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		add(formFeedback);

		Form imageEditForm = new Form("galleryImageEditForm");

		imageEditForm.setOutputMarkupId(true);
		add(imageEditForm);

		imageOptionsContainer = new WebMarkupContainer("galleryImageOptionsContainer");
		imageOptionsContainer.setOutputMarkupId(true);
		imageOptionsContainer.setOutputMarkupPlaceholderTag(true);

		imageOptionsContainer.add(new Label("removePictureLabel",
				new ResourceModel("pictures.removepicture")));

		AjaxFallbackButton removePictureButton = createRemovePictureButton(imageEditForm);
		imageOptionsContainer.add(removePictureButton);

		Label setProfileImageLabel = new Label("setProfileImageLabel",
				new ResourceModel("pictures.setprofileimage"));
		imageOptionsContainer.add(setProfileImageLabel);

		AjaxFallbackButton setProfileImageButton = createSetProfileImageButton(imageEditForm);

		if ((true == sakaiProxy.isOfficialImageEnabledGlobally() && 
				false == sakaiProxy.isUsingOfficialImageButAlternateSelectionEnabled())
				|| preferencesLogic.getPreferencesRecordForUser(userId).isUseOfficialImage()) {
			
			setProfileImageLabel.setVisible(false);
			setProfileImageButton.setVisible(false);
		}
		
		imageOptionsContainer.add(setProfileImageButton);

		imageEditForm.add(imageOptionsContainer);

		removeConfirmContainer = new WebMarkupContainer("galleryRemoveImageConfirmContainer");
		removeConfirmContainer.setOutputMarkupId(true);
		removeConfirmContainer.setOutputMarkupPlaceholderTag(true);

		Label removeConfirmLabel = new Label("removePictureConfirmLabel",
				new ResourceModel("pictures.removepicture.confirm"));
		removeConfirmContainer.add(removeConfirmLabel);

		AjaxFallbackButton removeConfirmButton = createRemoveConfirmButton(
				userId, image, (int) galleryPageIndex, formFeedback,
				imageEditForm);
		//removeConfirmButton.add(new FocusOnLoadBehaviour());
		removeConfirmContainer.add(removeConfirmButton);

		AjaxFallbackButton removeCancelButton = createRemoveCancelButton(imageEditForm);
		removeConfirmContainer.add(removeCancelButton);

		removeConfirmContainer.setVisible(false);
		imageEditForm.add(removeConfirmContainer);
		
		setProfileImageConfirmContainer = new WebMarkupContainer("gallerySetProfileImageConfirmContainer");
		setProfileImageConfirmContainer.setOutputMarkupId(true);
		setProfileImageConfirmContainer.setOutputMarkupPlaceholderTag(true);
		
		Label setProfileImageConfirmLabel = new Label("setProfileImageConfirmLabel",
				new ResourceModel("pictures.setprofileimage.confirm"));
		setProfileImageConfirmContainer.add(setProfileImageConfirmLabel);

		
		AjaxFallbackButton setProfileImageConfirmButton = createSetProfileImageConfirmButton(
				userId, image, (int) galleryPageIndex, formFeedback,
				imageEditForm);
		
		//setProfileImageConfirmButton.add(new FocusOnLoadBehaviour());
		setProfileImageConfirmContainer.add(setProfileImageConfirmButton);
		
		AjaxFallbackButton setProfileImageCancelButton = createSetProfileImageCancelButton(imageEditForm);
		setProfileImageConfirmContainer.add(setProfileImageCancelButton);

		setProfileImageConfirmContainer.setVisible(false);
		imageEditForm.add(setProfileImageConfirmContainer);
		
		add(new GalleryImageRenderer("galleryImageMainRenderer", image
				.getMainResource()));
	}

	private AjaxFallbackButton createRemoveCancelButton(Form imageEditForm) {
		AjaxFallbackButton removeCancelButton = new AjaxFallbackButton(
				"galleryRemoveImageCancelButton", new ResourceModel(
						"button.cancel"), imageEditForm) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				target.appendJavaScript("$('#"
						+ removeConfirmContainer.getMarkupId() + "').hide();");

				imageOptionsContainer.setVisible(true);
				target.add(imageOptionsContainer);

				target.appendJavaScript("setMainFrameHeight(window.name);");
			}

		};
		return removeCancelButton;
	}

	private AjaxFallbackButton createRemoveConfirmButton(
			final String userId,
			final GalleryImage image, final int galleryPageIndex,
			final Label formFeedback, Form imageEditForm) {
		AjaxFallbackButton removeConfirmButton = new AjaxFallbackButton(
				"galleryRemoveImageConfirmButton", new ResourceModel(
						"button.gallery.remove.confirm"), imageEditForm) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				if (imageLogic.removeGalleryImage(
						userId, image.getId())) {

					setResponsePage(new MyPictures(galleryPageIndex));
					
				} else {
					// user alert
					formFeedback.setDefaultModel(new ResourceModel(
							"error.gallery.remove.failed"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model("alertMessage")));

					target.add(formFeedback);
				}
			}
		};
		return removeConfirmButton;
	}

	private AjaxFallbackButton createRemovePictureButton(Form imageEditForm) {
		AjaxFallbackButton removePictureButton = new AjaxFallbackButton(
				"galleryImageRemoveButton", new ResourceModel(
						"button.gallery.remove"), imageEditForm) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {

				imageOptionsContainer.setVisible(false);

				target.appendJavaScript("$('#"
						+ imageOptionsContainer.getMarkupId() + "').hide();");

				removeConfirmContainer.setVisible(true);
				target.add(removeConfirmContainer);
				target.appendJavaScript("setMainFrameHeight(window.name);");
			}

		};
		return removePictureButton;
	}

	private AjaxFallbackButton createSetProfileImageCancelButton(Form imageEditForm) {
		AjaxFallbackButton removeCancelButton = new AjaxFallbackButton(
				"gallerySetProfileImageCancelButton", new ResourceModel(
						"button.cancel"), imageEditForm) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				target.appendJavaScript("$('#"
						+ setProfileImageConfirmContainer.getMarkupId() + "').hide();");

				imageOptionsContainer.setVisible(true);
				target.add(imageOptionsContainer);

				target.appendJavaScript("setMainFrameHeight(window.name);");
			}

		};
		return removeCancelButton;
	}
	
	private AjaxFallbackButton createSetProfileImageButton(Form imageEditForm) {
		AjaxFallbackButton setProfileImageButton = new AjaxFallbackButton(
				"gallerySetProfileImageButton", new ResourceModel(
						"button.gallery.setprofile"), imageEditForm) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {

				imageOptionsContainer.setVisible(false);

				target.appendJavaScript("$('#"
						+ imageOptionsContainer.getMarkupId() + "').hide();");

				setProfileImageConfirmContainer.setVisible(true);
				target.add(setProfileImageConfirmContainer);
				target.appendJavaScript("setMainFrameHeight(window.name);");
			}

		};
		return setProfileImageButton;
	}
	
	private AjaxFallbackButton createSetProfileImageConfirmButton(
			final String userId,
			final GalleryImage image, final int galleryPageIndex,
			final Label formFeedback, Form imageEditForm) {

		AjaxFallbackButton setProfileImageButton = new AjaxFallbackButton(
				"gallerySetProfileImageConfirmButton", new ResourceModel(
						"button.gallery.setprofile.confirm"), imageEditForm) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {

				if (imageLogic.setUploadedProfileImage(
						userId,
						sakaiProxy.getResource(
								image.getMainResource()).getBytes(), "", "")) {

					sakaiProxy.postEvent(
							ProfileConstants.EVENT_PROFILE_IMAGE_CHANGE_UPLOAD,
							"/profile/" + userId, true);

					if (true == sakaiProxy.isWallEnabledGlobally()) {
						wallLogic
								.addNewEventToWall(
										ProfileConstants.EVENT_PROFILE_IMAGE_CHANGE_UPLOAD,
										sakaiProxy.getCurrentUserId());
					}
					
					if (sakaiProxy.isSuperUserAndProxiedToUser(
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

					target.add(formFeedback);
				}
			}
		};
		return setProfileImageButton;
	}

}
