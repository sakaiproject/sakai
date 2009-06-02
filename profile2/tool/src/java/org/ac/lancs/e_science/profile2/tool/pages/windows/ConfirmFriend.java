package uk.ac.lancs.e_science.profile2.tool.pages.windows;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.util.FormattedText;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileConstants;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.components.FocusOnLoadBehaviour;
import uk.ac.lancs.e_science.profile2.tool.components.ProfileImageRenderer;
import uk.ac.lancs.e_science.profile2.tool.models.FriendAction;
import uk.ac.lancs.e_science.profile2.tool.pages.ViewProfile;

public class ConfirmFriend extends Panel {

	private static final long serialVersionUID = 1L;
	private transient SakaiProxy sakaiProxy;
	private transient Profile profile;
	
	/*
	 * userX is the current user
	 * userY is the user who's friend request we are accepting
	 */
	
	public ConfirmFriend(String id, final ModalWindow window, final FriendAction friendActionModel, final String userX, final String userY){
        super(id);

        //get API's
        sakaiProxy = ProfileApplication.get().getSakaiProxy();
        profile = ProfileApplication.get().getProfile();
        
        //get friendName
        final String friendName = FormattedText.processFormattedText(sakaiProxy.getUserDisplayName(userY), new StringBuffer());
                
        //window setup
		window.setTitle(new StringResourceModel("title.friend.confirm", null, new Object[]{ friendName } )); 
		window.setInitialHeight(150);
		window.setInitialWidth(500);
		window.setResizable(false);
		
		//is this user allowed to view this person's profile image?
		boolean isProfileImageAllowed = profile.isUserXProfileImageVisibleByUserY(userY, userX, false);
		
		//image
		add(new ProfileImageRenderer("image", userY, isProfileImageAllowed, ProfileConstants.PROFILE_IMAGE_THUMBNAIL, true));
		
		
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
				boolean friendRequestFromThisPerson = profile.isFriendRequestPending(userY, userX);
				
				if(!friendRequestFromThisPerson) {
					text.setModel(new StringResourceModel("error.friend.not.pending.confirm", null, new Object[]{ friendName } ));
					this.setEnabled(false);
					this.add(new AttributeModifier("class", true, new Model("disabled")));
					target.addComponent(text);
					target.addComponent(this);
					return;
				}
				
				//if ok, request friend
				if(profile.confirmFriendRequest(userY, userX)) {
					friendActionModel.setConfirmed(true);
					
					//post event
					sakaiProxy.postEvent(ProfileConstants.EVENT_FRIEND_CONFIRM, "/profile/"+userY, true);
					
					//if email is enabled for this message type, send email
					if(profile.isEmailEnabledForThisMessageType(userY, ProfileConstants.EMAIL_NOTIFICATION_CONFIRM)) {
						
										       
						final String currentUserName = sakaiProxy.getUserDisplayName(userX);
				        final String serviceName = sakaiProxy.getServiceName();
				        final String portalUrl = sakaiProxy.getPortalUrl();
				        
						//subject
						final String subject = new StringResourceModel("email.friend.confirm.subject", null, new Object[]{ currentUserName, serviceName } ).getObject().toString();
						
						//email newline
						final String newline = ProfileConstants.EMAIL_NEWLINE;
						
						//message
						StringBuilder message = new StringBuilder();
						message.append(new StringResourceModel("email.friend.confirm.message", null, new Object[]{ currentUserName, serviceName }).getObject().toString());
						
						//url needs to go to userY's (ie other user) myworkspace and then Wicket takes them to their ViewProfile page for userX
				        String url = sakaiProxy.getDirectUrlToUserProfile(userY, urlFor(ViewProfile.class, new PageParameters("id=" + userX)).toString());
				        //tinyurl
				        final String tinyUrl = profile.generateTinyUrl(url);
				        message.append(newline);
						message.append(newline);
				        message.append(new StringResourceModel("email.friend.confirm.link", null, new Object[]{ currentUserName} ).getObject().toString());
				        message.append(newline);
						message.append(new StringResourceModel("email.friend.confirm.link.href", null, new Object[]{ tinyUrl }).getObject().toString());
						
						//standard footer
						message.append(newline);
						message.append(newline);
						message.append(new StringResourceModel("email.footer.1", this, null).getString());
						message.append(newline);
						message.append(new StringResourceModel("email.footer.2", null, new Object[]{ serviceName, portalUrl } ).getObject().toString());
						message.append(newline);
						message.append(new StringResourceModel("email.footer.3", this, null).getString());
						
						//send email
						sakaiProxy.sendEmail(userY, subject, message.toString());
					
					}
						
					
					
					window.close(target);
				} else {
					text.setModel(new StringResourceModel("error.friend.confirm.failed", null, new Object[]{ friendName } ));
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



