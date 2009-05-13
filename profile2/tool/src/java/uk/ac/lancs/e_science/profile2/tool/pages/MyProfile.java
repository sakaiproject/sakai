package uk.ac.lancs.e_science.profile2.tool.pages;


import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.ProfileUtilityManager;
import uk.ac.lancs.e_science.profile2.api.exception.ProfileNotDefinedException;
import uk.ac.lancs.e_science.profile2.tool.components.ProfileImageRenderer;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.ChangeProfilePictureUpload;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.ChangeProfilePictureUrl;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.FriendsFeed;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.MyContactDisplay;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.MyInfoDisplay;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.MyInterestsDisplay;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.MyStatusPanel;


public class MyProfile extends BasePage {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MyProfile.class);

	public MyProfile()   {
		
		log.debug("MyProfile()");
		
		//add the feedback panel for any error messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		add(feedbackPanel);
		feedbackPanel.setVisible(false); //hide by default

		//get current user
		String userId = sakaiProxy.getCurrentUserId();
		
		//get SakaiPerson for this user
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
		//if null, create one (and a privacy record)
		if(sakaiPerson == null) {
			log.warn("No SakaiPerson for " + userId + ". Creating one.");
			sakaiPerson = sakaiProxy.createSakaiPerson(userId);
			//if its still null, throw exception
			if(sakaiPerson == null) {
				throw new ProfileNotDefinedException("Couldn't create a SakaiPerson for " + userId);
			}
			//post create event
			sakaiProxy.postEvent(ProfileUtilityManager.EVENT_PROFILE_NEW, userId, true);
		} 
		
		//post view event
		sakaiProxy.postEvent(ProfileUtilityManager.EVENT_PROFILE_VIEW_OWN, "/profile/"+userId, false);

		//get some values from SakaiPerson or SakaiProxy if empty
		//SakaiPerson returns NULL strings if value is not set, not blank ones
	
		//these must come from Account to keep it all in sync
		//we *could* get a User object here and get the values.
		String userDisplayName = sakaiProxy.getUserDisplayName(userId);
		/*
		String userFirstName = sakaiProxy.getUserFirstName(userId);
		String userLastName = sakaiProxy.getUserLastName(userId);
		*/

		String userEmail = sakaiProxy.getUserEmail(userId);
		
		//create instance of the UserProfile class
		//we then pass the userProfile in the constructor to the child panels
		UserProfile userProfile = new UserProfile();
				
		//get rest of values from SakaiPerson and setup UserProfile
		userProfile.setUserId(userId);
		
		userProfile.setNickname(sakaiPerson.getNickname());
		userProfile.setDateOfBirth(sakaiPerson.getDateOfBirth());
		userProfile.setDisplayName(userDisplayName);
		//userProfile.setFirstName(userFirstName);
		//userProfile.setLastName(userLastName);
		//userProfile.setMiddleName(sakaiPerson.getInitials());
		
		userProfile.setEmail(userEmail);
		userProfile.setHomepage(sakaiPerson.getLabeledURI());
		userProfile.setHomephone(sakaiPerson.getHomePhone());
		userProfile.setWorkphone(sakaiPerson.getTelephoneNumber());
		userProfile.setMobilephone(sakaiPerson.getMobile());
		userProfile.setFacsimile(sakaiPerson.getFacsimileTelephoneNumber());
		
		userProfile.setFavouriteBooks(sakaiPerson.getFavouriteBooks());
		userProfile.setFavouriteTvShows(sakaiPerson.getFavouriteTvShows());
		userProfile.setFavouriteMovies(sakaiPerson.getFavouriteMovies());
		userProfile.setFavouriteQuotes(sakaiPerson.getFavouriteQuotes());
		userProfile.setOtherInformation(sakaiPerson.getNotes());
	
		//what type of picture changing method do we use?
		int profilePictureType = sakaiProxy.getProfilePictureType();
		
		//change picture panel (upload or url depending on property)
		final Panel changePicture;
		
		//if upload
		if(profilePictureType == ProfileImageManager.PICTURE_SETTING_UPLOAD) {
			changePicture = new ChangeProfilePictureUpload("changePicture");
		} else if (profilePictureType == ProfileImageManager.PICTURE_SETTING_URL) {
			changePicture = new ChangeProfilePictureUrl("changePicture");
		} else {
			//no valid option for changing picture was returned from the Profile2 API.
			log.error("Invalid picture type returned: " + profilePictureType);
			changePicture = new EmptyPanel("changePicture");
		}
		changePicture.setOutputMarkupPlaceholderTag(true);
		changePicture.setVisible(false);
		add(changePicture);
		
		//add the current picture
		add(new ProfileImageRenderer("photo", userId, true, ProfileImageManager.PROFILE_IMAGE_MAIN, true));
		
		//change profile image button
		AjaxFallbackLink changePictureLink = new AjaxFallbackLink("changePictureLink") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				
				target.addComponent(changePicture);
				changePicture.setVisible(true);
				
				//resize iframe
				target.prependJavascript("setMainFrameHeight(window.name);");
				//when the editImageButton is clicked, show the panel
				//its possible this will push the content lower than the iframe, so make sure the iframe size is good.
				//String js = "$('#" + changePicture.getMarkupId() + "').slideToggle()";
				//target.appendJavascript(js);
				//target.appendJavascript("alert('" + changePicture.getMarkupId() + "')");
				
			}
						
		};
		changePictureLink.add(new Label("changePictureLabel", new ResourceModel("link.change.profile.picture")));
		
		//is picture changing disabled?
		if(!sakaiProxy.isProfilePictureChangeEnabled()) {
			changePictureLink.setEnabled(false);
			changePictureLink.setVisible(false);
		}
		add(changePictureLink);
		
		//END OF TODO FROM ABOVE
		
		//status panel
		Panel myStatusPanel = new MyStatusPanel("myStatusPanel", userProfile);
		add(myStatusPanel);
		
		//info panel - load the display version by default
		Panel myInfoDisplay = new MyInfoDisplay("myInfo", userProfile);
		myInfoDisplay.setOutputMarkupId(true);
		add(myInfoDisplay);
		
		//contact panel - load the display version by default
		Panel myContactDisplay = new MyContactDisplay("myContact", userProfile);
		myContactDisplay.setOutputMarkupId(true);
		add(myContactDisplay);
		
		//interests panel - load the display version by default
		Panel myInterestsDisplay = new MyInterestsDisplay("myInterests", userProfile);
		myInterestsDisplay.setOutputMarkupId(true);
		add(myInterestsDisplay);
		
		//my quick links panel
		
		
		//friends feed panel for self
		Panel friendsFeed = new FriendsFeed("friendsFeed", userId, userId);
		friendsFeed.setOutputMarkupId(true);
		add(friendsFeed);

	}
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyProfile has been deserialized.");
		//re-init our transient objects
		profile = getProfile();
		sakaiProxy = getSakaiProxy();
	}	
	
	
	
}
