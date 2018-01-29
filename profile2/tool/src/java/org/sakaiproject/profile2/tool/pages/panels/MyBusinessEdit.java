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

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Panel for displaying and editing business profile data.
 */
@Slf4j
public class MyBusinessEdit extends Panel {

	private static final long serialVersionUID = 1L;
	private AjaxTabbedPanel companyProfileTabs;
	private List<CompanyProfile> companyProfilesToAdd = null;
	private List<CompanyProfile> companyProfilesToRemove = null;
	private enum TabDisplay { START, END }
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	public MyBusinessEdit(final String id, final UserProfile userProfile) {
		this(id, userProfile, new ArrayList<CompanyProfile>(),
				new ArrayList<CompanyProfile>(), TabDisplay.START);
	}
		
	public MyBusinessEdit(final String id, final UserProfile userProfile,
			List<CompanyProfile> companyProfilesToAdd,
			List<CompanyProfile> companyProfilesToRemove,
			TabDisplay tabDisplay) {

		super(id);

		log.debug("MyBusinessEdit()");

		this.companyProfilesToAdd = companyProfilesToAdd;
		this.companyProfilesToRemove = companyProfilesToRemove;

		// heading
		add(new Label("heading", new ResourceModel("heading.business.edit")));

		// setup form
		Form form = new Form("form", new Model(userProfile));
		form.setOutputMarkupId(true);

		// form submit feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		form.add(formFeedback);

		// add warning message if superUser and not editing own profile
		Label editWarning = new Label("editWarning");
		editWarning.setVisible(false);
		if (sakaiProxy.isSuperUserAndProxiedToUser(
				userProfile.getUserUuid())) {
			editWarning.setDefaultModel(new StringResourceModel(
					"text.edit.other.warning", null, new Object[] { userProfile
							.getDisplayName() }));
			editWarning.setEscapeModelStrings(false);
			editWarning.setVisible(true);
		}
		form.add(editWarning);

		// business biography
		WebMarkupContainer businessBiographyContainer = new WebMarkupContainer(
				"businessBiographyContainer");
		businessBiographyContainer.add(new Label("businessBiographyLabel",
				new ResourceModel("profile.business.bio")));
		TextArea businessBiography = new TextArea(
				"businessBiography", new PropertyModel<String>(userProfile,
						"businessBiography"));
		businessBiography.setMarkupId("businessbioinput");
		businessBiography.setOutputMarkupId(true);
		//businessBiography.setEditorConfig(CKEditorConfig.createCkConfig());
		businessBiographyContainer.add(businessBiography);
		form.add(businessBiographyContainer);

		// company profiles
		WebMarkupContainer companyProfileEditsContainer = createCompanyProfileEditsContainer(userProfile, tabDisplay);
		form.add(companyProfileEditsContainer);

		AjaxFallbackButton addCompanyProfileButton = createAddCompanyProfileButton(
				id, userProfile, form, formFeedback);
		form.add(addCompanyProfileButton);

		AjaxFallbackButton removeCompanyProfileButton = createRemoveCompanyProfileButton(
				id, userProfile, form);
		form.add(removeCompanyProfileButton);

		AjaxFallbackButton submitButton = createSaveChangesButton(id,
				userProfile, form, formFeedback);
		//submitButton.add(new CKEditorTextArea.CKEditorAjaxSubmitModifier());
		form.add(submitButton);

		AjaxFallbackButton cancelButton = createCancelChangesButton(id,
				userProfile, form);
		form.add(cancelButton);

		add(form);
	}

	private AjaxFallbackButton createCancelChangesButton(final String id,
			final UserProfile userProfile, Form form) {
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel",
				new ResourceModel("button.cancel"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {

				// undo any changes in progress
				for (CompanyProfile profile : companyProfilesToAdd) {
					userProfile.removeCompanyProfile(profile);
				}

				for (CompanyProfile profile : companyProfilesToRemove) {
					userProfile.addCompanyProfile(profile);
				}

				Component newPanel = new MyBusinessDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				MyBusinessEdit.this.replaceWith(newPanel);
				if (target != null) {
					target.add(newPanel);
					target.appendJavaScript("setMainFrameHeight(window.name);");
				}

			}
		};
		cancelButton.setDefaultFormProcessing(false);
		return cancelButton;
	}

	private AjaxFallbackButton createSaveChangesButton(final String id,
			final UserProfile userProfile, Form form, final Label formFeedback) {
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit",
				new ResourceModel("button.save.changes"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {

				if (save(form)) {

					// post update event
					sakaiProxy.postEvent(
							ProfileConstants.EVENT_PROFILE_BUSINESS_UPDATE,
							"/profile/" + userProfile.getUserUuid(), true);

					//post to wall if enabled
					if (true == sakaiProxy.isWallEnabledGlobally() && false == sakaiProxy.isSuperUserAndProxiedToUser(userProfile.getUserUuid())) {
						wallLogic.addNewEventToWall(ProfileConstants.EVENT_PROFILE_BUSINESS_UPDATE, sakaiProxy.getCurrentUserId());
					}
					
					// repaint panel
					Component newPanel = new MyBusinessDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					MyBusinessEdit.this.replaceWith(newPanel);
					if (target != null) {
						target.add(newPanel);
						// resize iframe
						target
								.appendJavaScript("setMainFrameHeight(window.name);");
					}

				} else {
					formFeedback.setDefaultModel(new ResourceModel(
							"error.profile.save.business.failed"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model<String>("save-failed-error")));
					target.add(formFeedback);
				}
			}
		};
		return submitButton;
	}

	private AjaxFallbackButton createRemoveCompanyProfileButton(
			final String id, final UserProfile userProfile, Form form) {
		AjaxFallbackButton removeCompanyProfileButton = new AjaxFallbackButton(
				"removeCompanyProfileButton", new ResourceModel(
						"button.business.remove.profile"), form) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				// if there's nothing to remove
				if (-1 == companyProfileTabs.getSelectedTab()) {
					return;
				}
				
				CompanyProfile companyProfileToRemove = userProfile
						.getCompanyProfiles().get(
								companyProfileTabs.getSelectedTab());

				userProfile.removeCompanyProfile(companyProfileToRemove);

				// this check is in case it's been added but never saved
				if (companyProfilesToAdd.contains(companyProfileToRemove)) {
					companyProfilesToAdd.remove(companyProfileToRemove);
				} else {
					companyProfilesToRemove.add(companyProfileToRemove);
				}

				Component newPanel = new MyBusinessEdit(id, userProfile,
						companyProfilesToAdd, companyProfilesToRemove,
						TabDisplay.START);

				newPanel.setOutputMarkupId(true);
				MyBusinessEdit.this.replaceWith(newPanel);

				if (target != null) {
					target.add(newPanel);
					target.appendJavaScript("setMainFrameHeight(window.name);");
				}
			}
		};
		return removeCompanyProfileButton;
	}

	private AjaxFallbackButton createAddCompanyProfileButton(final String id,
			final UserProfile userProfile, Form form, final Label formFeedback) {
		AjaxFallbackButton addCompanyProfileButton = new AjaxFallbackButton(
				"addCompanyProfileButton", new ResourceModel(
						"button.business.add.profile"), form) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				CompanyProfile companyProfileToAdd = new CompanyProfile(
						userProfile.getUserUuid(), "", "", "");
				companyProfilesToAdd.add(companyProfileToAdd);
				userProfile.addCompanyProfile(companyProfileToAdd);

				Component newPanel = new MyBusinessEdit(id, userProfile,
						companyProfilesToAdd, companyProfilesToRemove,
						TabDisplay.END);
				newPanel.setOutputMarkupId(true);
				MyBusinessEdit.this.replaceWith(newPanel);

				if (target != null) {
					target.add(newPanel);
					// resize iframe
					target
							.prependJavaScript("setMainFrameHeight(window.name);");
				}
			}
		};
		return addCompanyProfileButton;
	}
	
	// creates the company profile edit container
	private WebMarkupContainer createCompanyProfileEditsContainer(
			final UserProfile userProfile, TabDisplay tabDisplay) {

		WebMarkupContainer companyProfilesContainer = new WebMarkupContainer(
				"companyProfilesContainer");

		companyProfilesContainer.add(new Label("companyProfilesLabel",
				new ResourceModel("profile.business.company.profiles")));

		List<ITab> tabs = new ArrayList<ITab>();
		if (null != userProfile.getCompanyProfiles()) {

			for (final CompanyProfile companyProfile : userProfile
					.getCompanyProfiles()) {

				tabs.add(new AbstractTab(new ResourceModel("profile.business.company.profile")) {

					private static final long serialVersionUID = 1L;

					@Override
					public Panel getPanel(String panelId) {

						return new CompanyProfileEdit(panelId, companyProfile);
					}

				});
			}
		}

		companyProfileTabs = new AjaxTabbedPanel("companyProfiles", tabs);
		companyProfilesContainer.add(companyProfileTabs);

		if (tabs.size() > 0) {
			switch (tabDisplay) {
			case START:
				companyProfileTabs.setSelectedTab(0);
				break;
			case END:
				companyProfileTabs.setSelectedTab(tabs.size() - 1);
			}
		} else {
			companyProfilesContainer.setVisible(false);
		}

		return companyProfilesContainer;
	}
	
	// called when the form is to be saved
	private boolean save(Form form) {

		// get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();

		String userId = userProfile.getUserUuid();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);

		sakaiPerson.setBusinessBiography(userProfile.getBusinessBiography());

		// add new company profiles
		for (CompanyProfile companyProfile : companyProfilesToAdd) {
			if (!profileLogic.addNewCompanyProfile(companyProfile)) {
				
				log.info("Couldn't add CompanyProfile for: " + userId);
				return false;
			}
		}
		
		// save company profiles
		for (CompanyProfile companyProfile : userProfile.getCompanyProfiles()) {

			if (!profileLogic.updateCompanyProfile(companyProfile)) {
				
				log.info("Couldn't save CompanyProfile for: " + userId);
				return false;
			}
		}

		// remove any company profile marked for deletion
		for (CompanyProfile companyProfile : companyProfilesToRemove) {
			
			if (!profileLogic.removeCompanyProfile(userId,
					companyProfile.getId())) {
				
				log.info("Couldn't delete CompanyProfile for: " + userId);
				return false;
			}
		}

		// update SakaiPerson
		if (profileLogic.saveUserProfile(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId);
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	}
}
