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
package org.sakaiproject.profile2.tool.pages.windows;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfilePreferencesLogic;
import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.components.ProfileImage;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.util.FormattedText;

public class ConfirmFriend extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	private SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePreferencesLogic")
	private ProfilePreferencesLogic preferencesLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	private ProfilePrivacyLogic privacyLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileConnectionsLogic")
	private ProfileConnectionsLogic connectionsLogic;
	
	/*
	 * userX is the current user
	 * userY is the user who's friend request we are accepting
	 */
	
	public ConfirmFriend(String id, final ModalWindow window, final FriendAction friendActionModel, final String userX, final String userY){
        super(id);

        //get friendName
        final String friendName = FormattedText.processFormattedText(sakaiProxy.getUserDisplayName(userY), new StringBuffer());
                
        //window setup
		window.setTitle(new StringResourceModel("title.friend.confirm", null, new Object[]{ friendName } )); 
		window.setInitialHeight(150);
		window.setInitialWidth(500);
		window.setResizable(false);
		
		//prefs and privacy
		ProfilePreferences prefs = preferencesLogic.getPreferencesRecordForUser(userY);
		ProfilePrivacy privacy = privacyLogic.getPrivacyRecordForUser(userY);
		
		//image
		ProfileImage image = new ProfileImage("image", new Model<String>(userY));
		image.setSize(ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
		add(image);
		
        //text
		final Label text = new Label("text", new StringResourceModel("text.friend.confirm", null, new Object[]{ friendName } ));
        text.setEscapeModelStrings(false);
        text.setOutputMarkupId(true);
        add(text);
                   
        //setup form		
		Form form = new Form("form");
		form.setOutputMarkupId(true);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.friend.confirm"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				/* double checking */
				
				//must exist a pending friend request FROM userY to userX in order to confirm it
				boolean friendRequestFromThisPerson = connectionsLogic.isFriendRequestPending(userY, userX);
				
				if(!friendRequestFromThisPerson) {
					text.setDefaultModel(new StringResourceModel("error.friend.not.pending.confirm", null, new Object[]{ friendName } ));
					this.setEnabled(false);
					this.add(new AttributeModifier("class", true, new Model("disabled")));
					target.add(text);
					target.add(this);
					return;
				}
				
				//if ok, request friend
				if(connectionsLogic.confirmFriendRequest(userY, userX)) {
					friendActionModel.setConfirmed(true);
					window.close(target);
				} else {
					text.setDefaultModel(new StringResourceModel("error.friend.confirm.failed", null, new Object[]{ friendName } ));
					this.setEnabled(false);
					this.add(new AttributeModifier("class", true, new Model("disabled")));
					target.add(text);
					target.add(this);
					return;
				}
				
            }
		};
		//submitButton.add(new FocusOnLoadBehaviour());
		submitButton.add(new AttributeModifier("title", true, new StringResourceModel("accessibility.connection.confirm", null, new Object[]{ friendName } )));
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				friendActionModel.setConfirmed(false);
            	window.close(target);
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
        
        //add form
        add(form);
        
    }

	
}



