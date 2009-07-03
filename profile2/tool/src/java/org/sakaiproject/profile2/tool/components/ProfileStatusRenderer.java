package org.sakaiproject.profile2.tool.components;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.util.ProfileUtils;

/** 
 * This is a helper panel for displaying a user's status.
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileStatusRenderer extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ProfileStatusRenderer.class);
	private String msgClass;
	private String dateClass;
	private boolean allowed;
	private boolean friend;
	
	/**
	 * Render the status panel for the user. Privacy checks according to the requesting user.
	 * @param id		- wicket:id
	 * @param userX		- user whose status we are showing, must be a uuid.
	 * @param userY		- user who is looking at the status, used for checking if they are allowed to see it
	 * @param msgClass	- class for the message text
	 * @param dateClass - class for the date text
	 */
	public ProfileStatusRenderer(String id, String userX, String userY, String msgClass, String dateClass) {
		super(id);
		
		//set classes
		this.msgClass = msgClass;
		this.dateClass = dateClass;
		
		//check allowed
		this.allowed = checkAllowed(userX, userY);
		
		//do it
		renderStatus(id, userX);
	}
	
	/**
	 * Render the status panel for the user. Privacy checks according to the requesting user and taken from the supplied object
	 * @param id		- wicket:id
	 * @param userX		- user whose status we are showing, must be a uuid.
	 * @param privacy	- privacy record for userX
	 * @param userY		- user who is looking at the status, used for checking if they are allowed to see it
	 * @param msgClass	- class for the message text
	 * @param dateClass - class for the date text
	 */
	public ProfileStatusRenderer(String id, String userX, ProfilePrivacy privacy, String userY, String msgClass, String dateClass) {
		super(id);
		
		//set classes
		this.msgClass = msgClass;
		this.dateClass = dateClass;
		
		//check allowed
		this.allowed = checkAllowed(userX, userY, privacy);
		
		//do it
		renderStatus(id, userX);
	}
	
	/**
	 * Render the status panel for the user. Privacy checks according to the requesting user, ProfilePrivacy and friend status supplied
	 * @param id		- wicket:id
	 * @param userX		- user whose status we are showing, must be a uuid.
	 * @param privacy	- privacy record for userX
	 * @param userY		- user who is looking at the status, used for checking if they are allowed to see it
	 * @param friend	- if userX and userY are friends
	 * @param msgClass	- class for the message text
	 * @param dateClass - class for the date text
	 */
	public ProfileStatusRenderer(String id, String userX, ProfilePrivacy privacy, String userY, boolean friend, String msgClass, String dateClass) {
		super(id);
		
		//set classes
		this.msgClass = msgClass;
		this.dateClass = dateClass;
		this.friend = friend;
		
		//check allowed
		this.allowed = checkAllowed(userX, userY, privacy, friend);
		
		//do it
		renderStatus(id, userX);
	}
	
	/**
	 * Render the status panel for the user. No privacy checks, already know if it is allowed.
	 * @param id		- wicket:id
	 * @param userX		- user whose status we are showing, must be a uuid.
	 * @param userY		- user who is looking at the status, used for checking if they are allowed to see it
	 * @param allowed	- if you already know if they are allowed
	 * @param msgClass	- class for the message text
	 * @param dateClass - class for the date text
	 */
	public ProfileStatusRenderer(String id, String userX, String userY, boolean allowed, String msgClass, String dateClass) {
		super(id);
		
		//set classes
		this.msgClass = msgClass;
		this.dateClass = dateClass;
		
		//already know if allowed so just set
		this.allowed = allowed;
		
		//do it
		renderStatus(id, userX);
	}
	
	/**
	 * Actual rendering method
	 * @param id		wicket:id
	 * @param userX		userUuid 
	 */
	private void renderStatus(String id, String userX) {
	
		log.debug("ProfileStatusRenderer has been added.");
		this.setOutputMarkupPlaceholderTag(true);
		
		if(!allowed) {
			log.debug("not allowed to view status");
			this.setVisible(false);
			setupBlankFields();
			return;
		}

		//get API's
        ProfileLogic profileLogic = getProfileLogic();
		
		//get status
		ProfileStatus status = profileLogic.getUserStatus(userX);
		if(status == null) {
			log.debug("status null");
			this.setVisible(false);
			setupBlankFields();
			return;
		}
		
		//check message
		if(StringUtils.isBlank(status.getMessage())) {
			log.debug("status message blank");
			this.setVisible(false);
			setupBlankFields();
			return;
		}
		
		//format date
		status.setDateFormatted(ProfileUtils.convertDateForStatus(status.getDateAdded()));
		
		//output
		this.add(new Label("message", status.getMessage())
			.add(new AttributeModifier("class", true, new Model(this.msgClass)))
		);
		this.add(new Label("date", status.getDateFormatted())
			.add(new AttributeModifier("class", true, new Model(this.dateClass)))
		);
		
	}	
		
		
	
	//helper to render blank fields
	private void setupBlankFields() {
		this.add(new Label("message"));
		this.add(new Label("date"));
	}
	
	//helper to check if allowed
	private boolean checkAllowed(String userX, String userY) {
		
		//get API's
        ProfileLogic profileLogic = getProfileLogic();
		
        //if bad userUuids
        if(blankUuids(userX, userY)) {
        	return false;
        }
        
        //if same
        if(equalUuids(userX, userY)) {
			return true;
        }
        
        //do privacy check
        return checkAllowed(userX, userY, profileLogic.getPrivacyRecordForUser(userX));
	}
	
	//helper to check if allowed, given ProfilePrivacy as well
	private boolean checkAllowed(String userX, String userY, ProfilePrivacy privacy) {
		
		//get API's
        ProfileLogic profileLogic = getProfileLogic();
		
        //if bad userUuids
        if(blankUuids(userX, userY)) {
        	return false;
        }
        
        //if same
        if(equalUuids(userX, userY)) {
			return true;
        }
        
        //do privacy check
    	return checkAllowed(userX, userY, profileLogic.getPrivacyRecordForUser(userX), profileLogic.isUserXFriendOfUserY(userX, userY));
	}
	
	/**
	 * Actual privacy check method
	 * @param userX
	 * @param userY
	 * @param privacy
	 * @param friend
	 * @return
	 */
	private boolean checkAllowed(String userX, String userY, ProfilePrivacy privacy, boolean friend) {
		
		//get API's
        ProfileLogic profileLogic = getProfileLogic();
		
        //if bad userUuids
        if(blankUuids(userX, userY)) {
        	return false;
        }
        
        //if same
        if(equalUuids(userX, userY)) {
			return true;
        }
        
        //do privacy check
		return profileLogic.isUserXStatusVisibleByUserY(userX, privacy, userY, friend);
	}
	
	//helper
	private boolean blankUuids(String userX, String userY) {
		return (StringUtils.isBlank(userX) || StringUtils.isBlank(userY));
	}
	
	//helper
	private boolean equalUuids(String userX, String userY) {
		return (StringUtils.equals(userX, userY));
	}

	//helper
	private ProfileLogic getProfileLogic() {
		return Locator.getProfileLogic();
	}
	
}
