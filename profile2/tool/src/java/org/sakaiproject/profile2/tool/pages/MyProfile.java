package org.sakaiproject.profile2.tool.pages;


import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.exception.ProfileNotDefinedException;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.models.UserProfile;
import org.sakaiproject.profile2.tool.pages.panels.ChangeProfilePictureUpload;
import org.sakaiproject.profile2.tool.pages.panels.ChangeProfilePictureUrl;
import org.sakaiproject.profile2.tool.pages.panels.FriendsFeed;
import org.sakaiproject.profile2.tool.pages.panels.MyAcademicDisplay;
import org.sakaiproject.profile2.tool.pages.panels.MyContactDisplay;
import org.sakaiproject.profile2.tool.pages.panels.MyInfoDisplay;
import org.sakaiproject.profile2.tool.pages.panels.MyInterestsDisplay;
import org.sakaiproject.profile2.tool.pages.panels.MyStatusPanel;
import org.sakaiproject.profile2.util.ProfileConstants;


public class MyProfile extends BasePage {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MyProfile.class);
	

	/**
	 * Default constructor if viewing own
	 */
	public MyProfile()   {
		log.debug("MyProfile()");

		//get user for this profile and render it
		String userUuid = sakaiProxy.getCurrentUserId();
		renderMyProfile(userUuid);
	}
	
	/**
	 * This constructor is called if we are viewing someone elses but in edit mode.
	 * This will only be called if we were a superuser editing someone else's profile.
	 * An additional catch is also in place.
	 * @param parameters 
	 */
	public MyProfile(final String userUuid)   {
		log.debug("MyProfile(" + userUuid +")");
		
		//double check only super users
		if(!sakaiProxy.isSuperUser()) {
			log.error("MyProfile: user " + sakaiProxy.getCurrentUserId() + " attempted to access MyProfile for " + userUuid + ". Redirecting...");
			throw new RestartResponseException(new MyProfile());
		}
		//render for given user
		renderMyProfile(userUuid);
	}
	
	/**
	 * Does the actual rendering of the page
	 * @param userUuid
	 */
	private void renderMyProfile(final String userUuid) {
				
		//add the feedback panel for any error messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		add(feedbackPanel);
		feedbackPanel.setVisible(false); //hide by default
		
		//get SakaiPerson for this user
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		//if null, create one 
		if(sakaiPerson == null) {
			log.warn("No SakaiPerson for " + userUuid + ". Creating one.");
			sakaiPerson = sakaiProxy.createSakaiPerson(userUuid);
			//if its still null, throw exception
			if(sakaiPerson == null) {
				throw new ProfileNotDefinedException("Couldn't create a SakaiPerson for " + userUuid);
			}
			//post create event
			sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_NEW, userUuid, true);
		} 
		
		//post view event
		sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_VIEW_OWN, "/profile/"+userUuid, false);

		//get some values from SakaiPerson or SakaiProxy if empty
		//SakaiPerson returns NULL strings if value is not set, not blank ones
	
		//these must come from Account to keep it all in sync
		//we *could* get a User object here and get the values.
		String userDisplayName = sakaiProxy.getUserDisplayName(userUuid);
		/*
		String userFirstName = sakaiProxy.getUserFirstName(userId);
		String userLastName = sakaiProxy.getUserLastName(userId);
		*/

		String userEmail = sakaiProxy.getUserEmail(userUuid);
		
		//create instance of the UserProfile class
		//we then pass the userProfile in the constructor to the child panels
		UserProfile userProfile = new UserProfile();
				
		//get rest of values from SakaiPerson and setup UserProfile
		userProfile.setUserId(userUuid);
		
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
		
		userProfile.setDepartment(sakaiPerson.getOrganizationalUnit());
		userProfile.setPosition(sakaiPerson.getTitle());
		userProfile.setSchool(sakaiPerson.getCampus());
		userProfile.setRoom(sakaiPerson.getRoomNumber());
		
		userProfile.setCourse(sakaiPerson.getEducationCourse());
		userProfile.setSubjects(sakaiPerson.getEducationSubjects());
		
		userProfile.setFavouriteBooks(sakaiPerson.getFavouriteBooks());
		userProfile.setFavouriteTvShows(sakaiPerson.getFavouriteTvShows());
		userProfile.setFavouriteMovies(sakaiPerson.getFavouriteMovies());
		userProfile.setFavouriteQuotes(sakaiPerson.getFavouriteQuotes());
		userProfile.setOtherInformation(sakaiPerson.getNotes());
		
		//PRFL-97 workaround. SakaiPerson table needs to be upgraded so locked is not null, but this handles it if not upgraded.
		if(sakaiPerson.getLocked() == null) {
			userProfile.setLocked(false);
			this.setLocked(false);
		} else {
			this.setLocked(sakaiPerson.getLocked());
			userProfile.setLocked(this.isLocked());
		}
		
	
		//what type of picture changing method do we use?
		int profilePictureType = sakaiProxy.getProfilePictureType();
		
		//change picture panel (upload or url depending on property)
		final Panel changePicture;
		
		//render appropriate panel with appropriate constructor ie if superUser etc
		if(profilePictureType == ProfileConstants.PICTURE_SETTING_UPLOAD) {
			
			if(sakaiProxy.isSuperUserAndProxiedToUser(userUuid)){
				changePicture = new ChangeProfilePictureUpload("changePicture", userUuid);
			} else {
				changePicture = new ChangeProfilePictureUpload("changePicture");
			}
		} else if (profilePictureType == ProfileConstants.PICTURE_SETTING_URL) {
			if(sakaiProxy.isSuperUserAndProxiedToUser(userUuid)){
				changePicture = new ChangeProfilePictureUrl("changePicture", userUuid);
			} else {
				changePicture = new ChangeProfilePictureUrl("changePicture");
			}
		} else {
			//no valid option for changing picture was returned from the Profile2 API.
			log.error("Invalid picture type returned: " + profilePictureType);
			changePicture = new EmptyPanel("changePicture");
		}
		changePicture.setOutputMarkupPlaceholderTag(true);
		changePicture.setVisible(false);
		add(changePicture);
		
		//add the current picture
		add(new ProfileImageRenderer("photo", userUuid, true, ProfileConstants.PROFILE_IMAGE_MAIN, true));
		
		//change profile image button
		AjaxLink changePictureLink = new AjaxLink("changePictureLink") {
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
		if((!sakaiProxy.isProfilePictureChangeEnabled() || userProfile.isLocked()) && !sakaiProxy.isSuperUser()) {
			changePictureLink.setEnabled(false);
			changePictureLink.setVisible(false);
		}
		add(changePictureLink);
		
		
		/* SIDELINKS */
		WebMarkupContainer sideLinks = new WebMarkupContainer("sideLinks");
		int visibleSideLinksCount = 0;
		
		WebMarkupContainer lockProfileContainer = new WebMarkupContainer("lockProfileContainer");
		
		//ADMIN: LOCK/UNLOCK A PROFILE
		final Label lockProfileLabel = new Label("lockProfileLabel");
		
		final AjaxLink lockProfileLink = new AjaxLink("lockProfileLink") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				//toggle it to be opposite of what it currently is, update labels
				boolean locked = isLocked();
    			if(sakaiProxy.toggleProfileLocked(userUuid, !locked)) {
    				setLocked(!locked);
    				log.info("MyProfile(): SuperUser toggled lock status of profile for " + userUuid + " to " + !locked);
    				lockProfileLabel.setModel(new ResourceModel("link.profile.locked." + isLocked()));
    				this.add(new AttributeModifier("title", true, new ResourceModel("text.profile.locked." + isLocked())));
    				target.addComponent(this);
    			}
			}
		};
		
		lockProfileLink.add(lockProfileLabel);
				
		//setup link/label and windows with special property based on locked status
		lockProfileLabel.setModel(new ResourceModel("link.profile.locked." + isLocked()));
		lockProfileLink.add(new AttributeModifier("title", true, new ResourceModel("text.profile.locked." + isLocked())));
		
		lockProfileContainer.add(lockProfileLink);
		
		//show lock/unlock link to superUser and only if not own profile
		if(!sakaiProxy.isSuperUserAndProxiedToUser(userUuid)) {
			lockProfileContainer.setVisible(false);
		} else {
			visibleSideLinksCount++;
		}
		
		//hide entire list if no links to show
		if(visibleSideLinksCount == 0) {
			sideLinks.setVisible(false);
		}
		
		sideLinks.add(lockProfileContainer);
		add(sideLinks);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
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
		
		//academic panel - load the display version by default
		Panel myAcademicDisplay = new MyAcademicDisplay("myAcademic", userProfile);
		myAcademicDisplay.setOutputMarkupId(true);
		add(myAcademicDisplay);
		
		//interests panel - load the display version by default
		Panel myInterestsDisplay = new MyInterestsDisplay("myInterests", userProfile);
		myInterestsDisplay.setOutputMarkupId(true);
		add(myInterestsDisplay);
		
		//my quick links panel
		
		
		//friends feed panel for self
		Panel friendsFeed = new FriendsFeed("friendsFeed", userUuid, userUuid);
		friendsFeed.setOutputMarkupId(true);
		add(friendsFeed);

	}
	
	
		
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyProfile has been deserialized.");
		//re-init our transient objects
		profileLogic = getProfileLogic();
		sakaiProxy = getSakaiProxy();
	}	
	
	
	private boolean locked;
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
}
