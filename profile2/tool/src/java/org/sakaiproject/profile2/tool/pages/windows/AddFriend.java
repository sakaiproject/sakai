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
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.tool.ProfileApplication;
import org.sakaiproject.profile2.tool.components.FocusOnLoadBehaviour;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.models.FriendAction;
import org.sakaiproject.profile2.tool.pages.MyFriends;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.util.FormattedText;

public class AddFriend extends Panel {

	private static final long serialVersionUID = 1L;
	private transient SakaiProxy sakaiProxy;
	private transient ProfileLogic profileLogic;
	
	/*
	 * userX is the current user
	 * userY is the user to add
	 */
	
	public AddFriend(String id, final ModalWindow window, final FriendAction friendActionModel, final String userX, final String userY){
        super(id);

        //get API's
        sakaiProxy = ProfileApplication.get().getSakaiProxy();
        profileLogic = ProfileApplication.get().getProfileLogic();
        
        //get friendName
        final String friendName = FormattedText.processFormattedText(sakaiProxy.getUserDisplayName(userY), new StringBuffer());
        
        //window setup
		window.setTitle(new StringResourceModel("title.friend.add", null, new Object[]{ friendName } )); 
		window.setInitialHeight(150);
		window.setInitialWidth(500);
		window.setResizable(false);
		
		//privacy
		ProfilePrivacy privacy = profileLogic.getPrivacyRecordForUser(userY);
		boolean isProfileImageAllowed = profileLogic.isUserXProfileImageVisibleByUserY(userY, privacy, userX, false);
		
		//image
		add(new ProfileImageRenderer("image", userY, isProfileImageAllowed, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, true));
		
        //text
		final Label text = new Label("text", new StringResourceModel("text.friend.add", null, new Object[]{ friendName } ));
        text.setEscapeModelStrings(false);
        text.setOutputMarkupId(true);
        add(text);
                   
        //setup form		
		Form form = new Form("form");
		form.setOutputMarkupId(true);
				
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.friend.add"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				/* double checking */
				
				//friend?
				if(profileLogic.isUserXFriendOfUserY(userX, userY)) {
					text.setModel(new StringResourceModel("error.friend.already.confirmed", null, new Object[]{ friendName } ));
					this.setEnabled(false);
					this.add(new AttributeModifier("class", true, new Model("disabled")));
					target.addComponent(text);
					target.addComponent(this);
					return;
				}
				
				//has a friend request already been made to this person?
				if(profileLogic.isFriendRequestPending(userX, userY)) {
					text.setModel(new StringResourceModel("error.friend.already.pending", null, new Object[]{ friendName } ));
					this.setEnabled(false);
					this.add(new AttributeModifier("class", true, new Model("disabled")));
					target.addComponent(text);
					target.addComponent(this);
					return;
				}
				
				//has a friend request been made from this person to the current user?
				if(profileLogic.isFriendRequestPending(userY, userX)) {
					text.setModel(new StringResourceModel("error.friend.already.pending", null, new Object[]{ friendName } ));
					this.setEnabled(false);
					this.add(new AttributeModifier("class", true, new Model("disabled")));
					target.addComponent(text);
					target.addComponent(this);
					return;
				}
				
				//if ok, request friend
				if(profileLogic.requestFriend(userX, userY)) {
					friendActionModel.setRequested(true);
					
					//post event
					sakaiProxy.postEvent(ProfileConstants.EVENT_FRIEND_REQUEST, "/profile/"+userY, true);
					
					//if email is enabled for this message type, send email
					if(profileLogic.isEmailEnabledForThisMessageType(userY, ProfileConstants.EMAIL_NOTIFICATION_REQUEST)) {
						
						//get some info
				        final String currentUserName = sakaiProxy.getUserDisplayName(userX);
				        final String serviceName = sakaiProxy.getServiceName();
				        final String portalUrl = sakaiProxy.getPortalUrl();
	
						//url needs to go to userY's (ie other user) myworkspace and wicket takes them to their MyFriends page
				        final String url = sakaiProxy.getDirectUrlToUserProfile(userY, urlFor(MyFriends.class, null).toString());
	
				        //tinyUrl
				        final String tinyUrl = profileLogic.generateTinyUrl(url);
				        
						//subject
						final String subject = new StringResourceModel("email.friend.request.subject", null, new Object[]{ currentUserName, serviceName } ).getObject().toString();
						
						//email newline
						final String newline = ProfileConstants.EMAIL_NEWLINE;
						
						//message
						StringBuilder message = new StringBuilder();
						message.append(new StringResourceModel("email.friend.request.message", null, new Object[]{ currentUserName, serviceName }).getObject().toString());
						message.append(newline);
						message.append(newline);
						message.append(new StringResourceModel("email.friend.request.link", null, new Object[]{ currentUserName }).getObject().toString());
						message.append(newline);
						message.append(new StringResourceModel("email.friend.request.link.href", null, new Object[]{ tinyUrl }).getObject().toString());
						message.append(newline);
						message.append(newline);
						message.append(new StringResourceModel("email.footer.1", this, null).getString());
						message.append(newline);
						message.append(new StringResourceModel("email.footer.2", null, new Object[]{ serviceName, portalUrl } ).getObject().toString());
						message.append(newline);
						message.append(new StringResourceModel("email.footer.3", this, null).getString());
	
						//send email (this method will format it properly, then send it)
						sakaiProxy.sendEmail(userY, subject, message.toString());
					}
					
					window.close(target);
				} else {
					text.setModel(new StringResourceModel("error.friend.add.failed", null, new Object[]{ friendName } ));
					this.setEnabled(false);
					this.add(new AttributeModifier("class", true, new Model("disabled")));
					target.addComponent(text);
					target.addComponent(this);
					return;
				}
				
            }
		};
		submitButton.add(new FocusOnLoadBehaviour());
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				friendActionModel.setRequested(false);
            	window.close(target);
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
        
        //add form
        add(form);
    }

		
}



