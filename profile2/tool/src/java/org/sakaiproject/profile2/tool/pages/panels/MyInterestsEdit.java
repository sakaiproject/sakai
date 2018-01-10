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
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
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
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class MyInterestsEdit extends Panel {
	
	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	private ProfileLogic profileLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	public MyInterestsEdit(final String id, final UserProfile userProfile) {
		super(id);
		
		log.debug("MyInterestsEdit()");
		
		//this panel
		final Component thisPanel = this;
			
		//get userId
		final String userId = userProfile.getUserUuid();
		
		//heading
		add(new Label("heading", new ResourceModel("heading.interests.edit")));
		
		//setup form		
		Form form = new Form("form", new Model(userProfile));
		form.setOutputMarkupId(true);
		
		//form submit feedback
		final Label formFeedback = new Label("formFeedback");
		formFeedback.setOutputMarkupPlaceholderTag(true);
		form.add(formFeedback);
		
		//add warning message if superUser and not editing own profile
		Label editWarning = new Label("editWarning");
		editWarning.setVisible(false);
		if(sakaiProxy.isSuperUserAndProxiedToUser(userId)) {
			editWarning.setDefaultModel(new StringResourceModel("text.edit.other.warning", null, new Object[]{ userProfile.getDisplayName() } ));
			editWarning.setEscapeModelStrings(false);
			editWarning.setVisible(true);
		}
		form.add(editWarning);
		
		//We don't need to get the info from userProfile, we load it into the form with a property model
	    //just make sure that the form element id's match those in the model
		
		//favourite books
		WebMarkupContainer booksContainer = new WebMarkupContainer("booksContainer");
		booksContainer.add(new Label("booksLabel", new ResourceModel("profile.favourite.books")));
		TextArea favouriteBooks = new TextArea("favouriteBooks", new PropertyModel(userProfile, "favouriteBooks"));
		favouriteBooks.setMarkupId("favouritebooksinput");
		favouriteBooks.setOutputMarkupId(true);
		booksContainer.add(favouriteBooks);
		form.add(booksContainer);
		
		//favourite tv shows
		WebMarkupContainer tvContainer = new WebMarkupContainer("tvContainer");
		tvContainer.add(new Label("tvLabel", new ResourceModel("profile.favourite.tv")));
		TextArea favouriteTvShows = new TextArea("favouriteTvShows", new PropertyModel(userProfile, "favouriteTvShows"));
		favouriteTvShows.setMarkupId("favouritetvinput");
		favouriteTvShows.setOutputMarkupId(true);
		tvContainer.add(favouriteTvShows);
		form.add(tvContainer);
		
		//favourite movies
		WebMarkupContainer moviesContainer = new WebMarkupContainer("moviesContainer");
		moviesContainer.add(new Label("moviesLabel", new ResourceModel("profile.favourite.movies")));
		TextArea favouriteMovies = new TextArea("favouriteMovies", new PropertyModel(userProfile, "favouriteMovies"));
		favouriteMovies.setMarkupId("favouritemoviesinput");
		favouriteMovies.setOutputMarkupId(true);
		moviesContainer.add(favouriteMovies);
		form.add(moviesContainer);
		
		//favourite quotes
		WebMarkupContainer quotesContainer = new WebMarkupContainer("quotesContainer");
		quotesContainer.add(new Label("quotesLabel", new ResourceModel("profile.favourite.quotes")));
		TextArea favouriteQuotes = new TextArea("favouriteQuotes", new PropertyModel(userProfile, "favouriteQuotes"));
		favouriteQuotes.setMarkupId("favouritequotesinput");
		favouriteQuotes.setOutputMarkupId(true);
		quotesContainer.add(favouriteQuotes);
		form.add(quotesContainer);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.save.changes"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel
				if(save(form)) {

					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_INTERESTS_UPDATE, "/profile/"+userId, true);
					
					//post to wall if enabled
					if (true == sakaiProxy.isWallEnabledGlobally() && false == sakaiProxy.isSuperUserAndProxiedToUser(userId)) {
						wallLogic.addNewEventToWall(ProfileConstants.EVENT_PROFILE_INTERESTS_UPDATE, sakaiProxy.getCurrentUserId());
					}
					
					//repaint panel
					Component newPanel = new MyInterestsDisplay(id, userProfile);
					newPanel.setOutputMarkupId(true);
					thisPanel.replaceWith(newPanel);
					if(target != null) {
						target.add(newPanel);
						//resize iframe
						target.appendJavaScript("setMainFrameHeight(window.name);");
					}
				
				} else {
					//String js = "alert('Failed to save information. Contact your system administrator.');";
					//target.prependJavascript(js);
					
					formFeedback.setDefaultModel(new ResourceModel("error.profile.save.interests.failed"));
					formFeedback.add(new AttributeModifier("class", true, new Model<String>("save-failed-error")));	
					target.add(formFeedback);
				}
            }
			
			//@Override
			//protected IAjaxCallDecorator getAjaxCallDecorator() {
			//	return CKEditorTextArea.getAjaxCallDecoratedToUpdateElementForAllEditorsOnPage();
			//}
		};
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
            	Component newPanel = new MyInterestsDisplay(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.add(newPanel);
					//resize iframe
					target.appendJavaScript("setMainFrameHeight(window.name);");
					//need a scrollTo action here, to scroll down the page to the section
				}
            	
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
		
        //feedback stuff - make this a class and insance it with diff params
        //WebMarkupContainer formFeedback = new WebMarkupContainer("formFeedback");
		//formFeedback.add(new Label("feedbackMsg", "some message"));
		//formFeedback.add(new AjaxIndicator("feedbackImg"));
		//form.add(formFeedback);
        
        
		
		//add form to page
		add(form);
		
	}
	
	//called when the form is to be saved
	private boolean save(Form form) {
		
		//get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();
		
		//get userId from the UserProfile (because admin could be editing), then get existing SakaiPerson for that userId
		String userId = userProfile.getUserUuid();
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
		
		//get values and set into SakaiPerson
		sakaiPerson.setFavouriteBooks(userProfile.getFavouriteBooks());
		sakaiPerson.setFavouriteTvShows(userProfile.getFavouriteTvShows());
		sakaiPerson.setFavouriteMovies(userProfile.getFavouriteMovies());
		sakaiPerson.setFavouriteQuotes(userProfile.getFavouriteQuotes());

		//update SakaiPerson
		if(profileLogic.saveUserProfile(sakaiPerson)) {
			log.info("Saved SakaiPerson for: " + userId );
			return true;
		} else {
			log.info("Couldn't save SakaiPerson for: " + userId);
			return false;
		}
	}

}
