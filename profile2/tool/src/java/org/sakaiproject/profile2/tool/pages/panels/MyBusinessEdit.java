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

package org.sakaiproject.profile2.tool.pages.panels;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.CompanyProfile;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.util.ProfileConstants;

public class MyBusinessEdit extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MyInterestsEdit.class);
	
	private TabbedPanel companyProfileTabs;

	private List<CompanyProfile> profilesToSave = null;
	
	public MyBusinessEdit(final String id, final UserProfile userProfile) {
		this(id, userProfile, null);
	}
	
	public MyBusinessEdit(final String id, final UserProfile userProfile,
			final List<CompanyProfile> profilesToSaveExternal) {

		super(id);

		log.debug("MyBusinessEdit()");

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
		if (Locator.getSakaiProxy().isSuperUserAndProxiedToUser(
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
		TextArea businessBiography = new TextArea("businessBiography",
				new PropertyModel(userProfile, "businessBiography"));
		businessBiographyContainer.add(businessBiography);
		form.add(businessBiographyContainer);

		WebMarkupContainer companyProfileEditsContainer = createCompanyProfileEditsContainer(
				userProfile, profilesToSaveExternal);
		form.add(companyProfileEditsContainer);

		AjaxFallbackButton addCompanyProfileButton = new AjaxFallbackButton(
				"addCompanyProfileButton", new ResourceModel(
						"button.business.add.profile"), form) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				// TODO Auto-generated method stub

			}

		};
		form.add(addCompanyProfileButton);

		AjaxFallbackButton removeCompanyProfileButton = new AjaxFallbackButton(
				"removeCompanyProfileButton", new ResourceModel(
						"button.business.remove.profile"), form) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				if (profilesToSave.size() > 0) {
					profilesToSave.remove(companyProfileTabs.getSelectedTab());
					
					companyProfileTabs.getTabs().remove(
							companyProfileTabs.getSelectedTab());
					companyProfileTabs.setSelectedTab(0);
				}

				// repaint panel
				Component newPanel = new MyBusinessEdit(id, userProfile,
						profilesToSave);
				newPanel.setOutputMarkupId(true);
				MyBusinessEdit.this.replaceWith(newPanel);
				if (target != null) {
					target.addComponent(newPanel);
					// resize iframe
					target.appendJavascript("setMainFrameHeight(window.name);");
				}

			}

		};
		form.add(removeCompanyProfileButton);

		// submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit",
				new ResourceModel("button.save.changes"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				// save() form, show message, then load display panel
				if (save(form)) {

					// post update event
					Locator.getSakaiProxy().postEvent(
							ProfileConstants.EVENT_PROFILE_BUSINESS_UPDATE,
							"/profile/" + userProfile.getUserUuid(), true);

					// repaint panel
					Component newPanel = new MyBusinessDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					MyBusinessEdit.this.replaceWith(newPanel);
					if (target != null) {
						target.addComponent(newPanel);
						// resize iframe
						target
								.appendJavascript("setMainFrameHeight(window.name);");
					}

				} else {
					// String js =
					// "alert('Failed to save information. Contact your system administrator.');";
					// target.prependJavascript(js);

					formFeedback.setDefaultModel(new ResourceModel(
							"error.profile.save.business.failed"));
					formFeedback.add(new AttributeModifier("class", true,
							new Model<String>("save-failed-error")));
					target.addComponent(formFeedback);
				}
			}
		};
		form.add(submitButton);

		// cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel",
				new ResourceModel("button.cancel"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				Component newPanel = new MyBusinessDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				MyBusinessEdit.this.replaceWith(newPanel);
				if (target != null) {
					target.addComponent(newPanel);
					// resize iframe
					target.appendJavascript("setMainFrameHeight(window.name);");
					// need a scrollTo action here, to scroll down the page to
					// the section
				}

			}
		};
		cancelButton.setDefaultFormProcessing(false);
		form.add(cancelButton);

		// add form to page
		add(form);
	}
	
	private WebMarkupContainer createCompanyProfileEditsContainer(
			final UserProfile userProfile, final List<CompanyProfile> profilesToSaveExternal) {

		if (null == profilesToSaveExternal) {
			profilesToSave = new ArrayList<CompanyProfile>();
			
			if (null != userProfile.getCompanyProfiles()) {
				for (CompanyProfile companyProfile : userProfile.getCompanyProfiles()) {
					profilesToSave.add(companyProfile);
				}
			}
		} else {
			profilesToSave = profilesToSaveExternal;
		}
		
		WebMarkupContainer companyProfilesContainer = new WebMarkupContainer(
				"companyProfilesContainer");

		companyProfilesContainer.add(new Label("companyProfilesLabel",
				new ResourceModel("profile.business.company.profiles")));

		List<ITab> tabs = new ArrayList<ITab>();
		if (null != userProfile.getCompanyProfiles()) {

			int companyProfileNum = 1;
			for (final CompanyProfile companyProfile : userProfile
					.getCompanyProfiles()) {

				// ignore those marked for removal
				
				log.info("there are " + profilesToSave.size() + " marked to save");
				
				for (CompanyProfile profile : profilesToSave) {
					log.info("profile to save: " + profile.getCompanyName());
				}
				
				if (!profilesToSave.contains(companyProfile)) {
					continue;
				}
				
				log.info("adding tab for company " + companyProfileNum);
				
				tabs.add(new AbstractTab(new Model<String>("Company "
						+ companyProfileNum++)) {

					private static final long serialVersionUID = 1L;

					@Override
					public Panel getPanel(String panelId) {

						return new CompanyProfileEdit(panelId, companyProfile);
					}

				});
			}
		}

		companyProfileTabs = new TabbedPanel("companyProfiles", tabs);
		companyProfilesContainer.add(companyProfileTabs);
		
		return companyProfilesContainer;

	}
	
	//called when the form is to be saved
	private boolean save(Form form) {
		
		//get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();
		
		//get SakaiProxy, get userId from the UserProfile (because admin could be editing), then get existing SakaiPerson for that userId
		SakaiProxy sakaiProxy = Locator.getSakaiProxy();
		
		String userId = userProfile.getUserUuid();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
		
		sakaiPerson.setBusinessBiography(userProfile.getBusinessBiography());
		
		// TODO save company profiles
		//Locator.getProfileLogic().r

		//update SakaiPerson
		if(sakaiProxy.updateSakaiPerson(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId );
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	}
}
