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

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.util.ProfileUtils;

public class MyInterestsDisplay extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MyInfoDisplay.class);
	private int visibleFieldCount = 0;
	private transient SakaiProxy sakaiProxy;
	
	public MyInterestsDisplay(final String id, final UserProfile userProfile) {
		super(id);
		
		//this panel stuff
		final Component thisPanel = this;
		
		//get API's
		sakaiProxy = getSakaiProxy();
		
		//get userProfile from userProfileModel
		//UserProfile userProfile = (UserProfile) this.getModelObject();
		
		//get info from userProfile since we need to validate it and turn things off if not set.
		//otherwise we could just use a propertymodel

		// social networking
		String facebookUsername = userProfile.getFacebookUsername();
		String linkedinUsername = userProfile.getLinkedinUsername();
		String myspaceUsername = userProfile.getMyspaceUsername();
		String skypeUsername = userProfile.getSkypeUsername();
		String twitterUsername = userProfile.getTwitterUsername();
		
		// favourites and other
		String favouriteBooks = userProfile.getFavouriteBooks();
		String favouriteTvShows = userProfile.getFavouriteTvShows();
		String favouriteMovies = userProfile.getFavouriteMovies();
		String favouriteQuotes = userProfile.getFavouriteQuotes();
		String otherInformation = ProfileUtils.unescapeHtml(userProfile.getOtherInformation());
		
		//heading
		add(new Label("heading", new ResourceModel("heading.interests")));
		
		//social networking
		
		//facebook
		WebMarkupContainer facebookContainer = new WebMarkupContainer("facebookContainer");
		facebookContainer.add(new Label("facebookLabel", new ResourceModel("profile.socialnetworking.facebook")));
		facebookContainer.add(new ExternalLink("facebookLink", ProfileUtils.getFacebookURL(facebookUsername), ProfileUtils.getFacebookURL(facebookUsername)));
		add(facebookContainer);
		if(StringUtils.isBlank(facebookUsername)) {
			facebookContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//linkedin
		WebMarkupContainer linkedinContainer = new WebMarkupContainer("linkedinContainer");
		linkedinContainer.add(new Label("linkedinLabel", new ResourceModel("profile.socialnetworking.linkedin")));
		linkedinContainer.add(new ExternalLink("linkedinLink", ProfileUtils.getLinkedinURL(linkedinUsername), ProfileUtils.getLinkedinURL(linkedinUsername)));
		add(linkedinContainer);
		if(StringUtils.isBlank(linkedinUsername)) {
			linkedinContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//myspace
		WebMarkupContainer myspaceContainer = new WebMarkupContainer("myspaceContainer");
		myspaceContainer.add(new Label("myspaceLabel", new ResourceModel("profile.socialnetworking.myspace")));
		myspaceContainer.add(new ExternalLink("myspaceLink", ProfileUtils.getMyspaceURL(myspaceUsername), ProfileUtils.getMyspaceURL(myspaceUsername)));
		add(myspaceContainer);
		if(StringUtils.isBlank(myspaceUsername)) {
			myspaceContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//twitter
		WebMarkupContainer twitterContainer = new WebMarkupContainer("twitterContainer");
		twitterContainer.add(new Label("twitterLabel", new ResourceModel("profile.socialnetworking.twitter")));
		twitterContainer.add(new ExternalLink("twitterLink", ProfileUtils.getTwitterURL(twitterUsername), ProfileUtils.getTwitterURL(twitterUsername)));
		add(twitterContainer);
		if(StringUtils.isBlank(twitterUsername)) {
			twitterContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//skypeme (no URL, as we don't want user skyping themselves)
		WebMarkupContainer skypeContainer = new WebMarkupContainer("skypeContainer");
		skypeContainer.add(new Label("skypeLabel", new ResourceModel("profile.socialnetworking.skype")));
		skypeContainer.add(new Label("skypeLink", skypeUsername));
		add(skypeContainer);
		if (StringUtils.isBlank(skypeUsername)) {
			skypeContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//favourite books
		WebMarkupContainer booksContainer = new WebMarkupContainer("booksContainer");
		booksContainer.add(new Label("booksLabel", new ResourceModel("profile.favourite.books")));
		booksContainer.add(new Label("favouriteBooks", favouriteBooks));
		add(booksContainer);
		if(StringUtils.isBlank(favouriteBooks)) {
			booksContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//favourite tv shows
		WebMarkupContainer tvContainer = new WebMarkupContainer("tvContainer");
		tvContainer.add(new Label("tvLabel", new ResourceModel("profile.favourite.tv")));
		tvContainer.add(new Label("favouriteTvShows", favouriteTvShows));
		add(tvContainer);
		if(StringUtils.isBlank(favouriteTvShows)) {
			tvContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//favourite movies
		WebMarkupContainer moviesContainer = new WebMarkupContainer("moviesContainer");
		moviesContainer.add(new Label("moviesLabel", new ResourceModel("profile.favourite.movies")));
		moviesContainer.add(new Label("favouriteMovies", favouriteMovies));
		add(moviesContainer);
		if(StringUtils.isBlank(favouriteMovies)) {
			moviesContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//favourite quotes
		WebMarkupContainer quotesContainer = new WebMarkupContainer("quotesContainer");
		quotesContainer.add(new Label("quotesLabel", new ResourceModel("profile.favourite.quotes")));
		quotesContainer.add(new Label("favouriteQuotes", favouriteQuotes));
		add(quotesContainer);
		if(StringUtils.isBlank(favouriteQuotes)) {
			quotesContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		//other info
		WebMarkupContainer otherContainer = new WebMarkupContainer("otherContainer");
		otherContainer.add(new Label("otherLabel", new ResourceModel("profile.other")));
		otherContainer.add(new Label("otherInformation", ProfileUtils.escapeHtmlForDisplay(otherInformation)).setEscapeModelStrings(false));
		add(otherContainer);
		if(StringUtils.isBlank(otherInformation)) {
			otherContainer.setVisible(false);
		} else {
			visibleFieldCount++;
		}
		
		
				
		//edit button
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new ResourceModel("button.edit")) {
			
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyInterestsEdit(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
					//resize iframe
					target.appendJavascript("setMainFrameHeight(window.name);");
				}
				
			}
						
		};
		editButton.add(new Label("editButtonLabel", new ResourceModel("button.edit")));
		editButton.setOutputMarkupId(true);
		
		if(userProfile.isLocked() && !sakaiProxy.isSuperUser()) {
			editButton.setVisible(false);
		}
		
		add(editButton);
		
		//no fields message
		Label noFieldsMessage = new Label("noFieldsMessage", new ResourceModel("text.no.fields"));
		add(noFieldsMessage);
		if(visibleFieldCount > 0) {
			noFieldsMessage.setVisible(false);
		}
		
	}
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyInterestsDisplay has been deserialized.");
		//re-init our transient objects
		sakaiProxy = getSakaiProxy();
	}

	
	private SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}
	
}
