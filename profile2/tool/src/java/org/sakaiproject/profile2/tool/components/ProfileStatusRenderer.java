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
package org.sakaiproject.profile2.tool.components;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.ProfileStatusLogic;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ProfileStatus;

/** 
 * This is a helper panel for displaying a user's status.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ProfileStatusRenderer extends Panel {

	private static final long serialVersionUID = 1L;

	private String userUuid;
	private ProfilePrivacy privacy;
	private String msgClass;
	private String dateClass;
	
	private boolean hasStatusSet = false;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	private ProfilePrivacyLogic privacyLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileStatusLogic")
	private ProfileStatusLogic statusLogic;
	
	/**
	 * Render the status panel for the user.
	 * <p>Where possible, use a fuller constructor.</p>
	 * @param id		- wicket:id
	 * @param userUuid	- uuid for user whose status we are showing
	 * @param msgClass	- class for the message text
	 * @param dateClass - class for the date text
	 */
	public ProfileStatusRenderer(final String id, final String userUuid, final String msgClass, final String dateClass) {
		super(id);
		
		//set incoming
		this.userUuid = userUuid;
		this.msgClass = msgClass;
		this.dateClass = dateClass;
		
		//get data
		this.privacy = privacyLogic.getPrivacyRecordForUser(userUuid);
		
		//render
		renderStatus();
	}
	
	/**
	 * Render the status panel for the user.
	 * @param id		- wicket:id
	 * @param userUuid	- uuid for user whose status we are showing
	 * @param privacy	- ProfilePrivacy object for user
	 * @param msgClass	- class for the message text
	 * @param dateClass - class for the date text
	 */
	public ProfileStatusRenderer(final String id, final String userUuid, final ProfilePrivacy privacy, final String msgClass, final String dateClass) {
		super(id);
		
		//set incoming
		this.userUuid = userUuid;
		this.privacy = privacy;
		this.msgClass = msgClass;
		this.dateClass = dateClass;
		
		//render
		renderStatus();
	}
	
	
	/**
	 * Render the status panel for the user. 
	 * @param id		- wicket:id
	 * @param person	- Person object for user whose status we are showing
	 * @param msgClass	- class for the message text
	 * @param dateClass - class for the date text
	 */
	public ProfileStatusRenderer(final String id, final Person person, final String msgClass, final String dateClass) {
		super(id);
		
		//set incoming
		this.msgClass = msgClass;
		this.dateClass = dateClass;
		
		//extract data
		this.userUuid = person.getUuid();
		this.privacy = person.getPrivacy();
		
		//render
		renderStatus();
	}
	
	
	
	
	
	/**
	 * Render method
	 */
	private void renderStatus() {
	
		log.debug("ProfileStatusRenderer has been added.");
		setOutputMarkupPlaceholderTag(true);
		
		//get status
		ProfileStatus status = statusLogic.getUserStatus(userUuid, privacy);

		//get status
		if(status == null) {
			log.debug("ProfileStatus null");
			setVisible(false);
			setupBlankFields();
			return;
		}
		
		//check message
		if(StringUtils.isBlank(status.getMessage())) {
			log.debug("ProfileStatus message blank");
			setVisible(false);
			setupBlankFields();
			return;
		}
		
		this.hasStatusSet = true;
		
		//output
		add(new Label("message", status.getMessage())
			.add(new AttributeModifier("class", true, new Model<String>(msgClass)))
		);
		add(new Label("date", status.getDateFormatted())
			.add(new AttributeModifier("class", true, new Model<String>(dateClass)))
		);
	
	}	
		
	//helper to render blank fields
	private void setupBlankFields() {
		add(new Label("message"));
		add(new Label("date"));
	}
	
	// if there is text
	public boolean hasStatusSet() {
		return this.hasStatusSet;
	}
	
}
