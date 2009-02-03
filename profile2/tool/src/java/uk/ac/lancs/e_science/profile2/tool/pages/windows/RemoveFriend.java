package uk.ac.lancs.e_science.profile2.tool.pages.windows;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileImageManager;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.components.FocusOnLoadBehaviour;
import uk.ac.lancs.e_science.profile2.tool.models.FriendAction;

public class RemoveFriend extends Panel {

	private static final long serialVersionUID = 1L;
	private transient Logger log = Logger.getLogger(AddFriend.class);
	private transient SakaiProxy sakaiProxy;
	private transient Profile profile;

	/*
	 * userX is the current user
	 * userY is the user to remove
	 */
	
	public RemoveFriend(String id, final ModalWindow window, final FriendAction friendActionModel, final String userX, final String userY, final byte[] image){
        super(id);

        //get API's
        sakaiProxy = ProfileApplication.get().getSakaiProxy();
        profile = ProfileApplication.get().getProfile();
        
        //get friendName
        final String friendName = sakaiProxy.getUserDisplayName(userY);
                
        //window setup
		window.setTitle(new ResourceModel("title.friend.remove")); 
		window.setInitialHeight(150);
		window.setInitialWidth(500);
		window.setResizable(false);
		
		//image (already set just need to use it/use default)
    	if(image != null && image.length > 0){
			BufferedDynamicImageResource photoResource = new BufferedDynamicImageResource(){
				private static final long serialVersionUID = 1L;
				protected byte[] getImageData() {
					return image;
				}
			};
			add(new Image("image",photoResource));
		} else {
			add(new ContextImage("image",new Model(ProfileImageManager.UNAVAILABLE_IMAGE)));
		}
		
        //text
		final Label text = new Label("text", new StringResourceModel("text.friend.remove", null, new Object[]{ friendName } ));
        text.setEscapeModelStrings(false);
        text.setOutputMarkupId(true);
        add(text);
                   
        //setup form		
		Form form = new Form("form");
		form.setOutputMarkupId(true);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.friend.remove"), form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				/* double checking */
				
				//must be friend in order to remove them
				boolean friend = profile.isUserXFriendOfUserY(userX, userY);
				
				if(!friend) {
					text.setModel(new StringResourceModel("error.friend.not.friend", null, new Object[]{ friendName } ));
					this.setEnabled(false);
					this.add(new AttributeModifier("class", true, new Model("disabled")));
					target.addComponent(text);
					target.addComponent(this);
					return;
				}
				
				
				//if ok, remove friend
				if(profile.removeFriend(userX, userY)) {
					friendActionModel.setRemoved(true);
					window.close(target);
				} else {
					text.setModel(new StringResourceModel("error.friend.remove.failed", null, new Object[]{ friendName } ));
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
				friendActionModel.setRemoved(false);
            	window.close(target);
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
        
        //add form
        add(form);
        
        
       
        

    }

	
	
	
	
}



