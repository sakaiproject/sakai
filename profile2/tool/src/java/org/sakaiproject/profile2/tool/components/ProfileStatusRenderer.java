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
import org.sakaiproject.profile2.tool.ProfileApplication;
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
	
	/**
	 * Render a profile image for a user, based on the settings supplied
	 * @param id		- wicket:id
	 * @param userX		- user whose status we are showing, must be a uuid.
	 * @param userY		- user who is looking at the status, used for checking if they are allowed to see it
	 * @param msgClass	- class for the message text
	 * @param dateClass - class for the date text
	 */
	public ProfileStatusRenderer(String id, String userX, String userY, String msgClass, String dateClass) {
		super(id);
		
		setMsgClass(msgClass);
		setDateClass(dateClass);
		
		boolean allowed = false;
		
		log.debug("ProfileStatusRenderer has been added.");
		this.setOutputMarkupPlaceholderTag(true);

		//get API's
        ProfileLogic profileLogic = ProfileApplication.get().getProfileLogic();
		
		//check privacy
		if(StringUtils.equals(userX, userY)) {
			allowed = true;
		} else {
			boolean friend = profileLogic.isUserXFriendOfUserY(userX, userY);
			ProfilePrivacy privacy = profileLogic.getPrivacyRecordForUser(userX);
			allowed = profileLogic.isUserXStatusVisibleByUserY(userX, privacy, userY, friend);
		}
		
		if(!allowed) {
			log.debug("not allowed to view status");
			this.setVisible(false);
			setupBlankFields();
			return;
		}
		
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
			.add(new AttributeModifier("class", true, new Model(this.getMsgClass())))
		);
		this.add(new Label("date", status.getDateFormatted())
			.add(new AttributeModifier("class", true, new Model(this.getDateClass())))
		);
		
	}
	
	
	
	private void setupBlankFields() {
		this.add(new Label("message"));
		this.add(new Label("date"));
	}

	private String getMsgClass() {
		return msgClass;
	}

	private void setMsgClass(String msgClass) {
		this.msgClass = msgClass;
	}

	private String getDateClass() {
		return dateClass;
	}

	private void setDateClass(String dateClass) {
		this.dateClass = dateClass;
	}

	
}
