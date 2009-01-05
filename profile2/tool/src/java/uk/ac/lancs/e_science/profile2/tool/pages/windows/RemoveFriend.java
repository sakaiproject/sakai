package uk.ac.lancs.e_science.profile2.tool.pages.windows;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.hbm.Friend;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.pages.MyFriends;

public class RemoveFriend extends Panel {

	private transient Logger log = Logger.getLogger(RemoveFriend.class);
	
	public RemoveFriend(String id, final ModalWindow window, final MyFriends parent, final String userId, Friend friend){
        super(id);

        if(log.isDebugEnabled()) log.debug("RemoveFriend()");
        
        final String friendUuid = friend.getUserUuid();
        String friendDisplayName = friend.getDisplayName();
      
        //text
        Label text = new Label("text", new StringResourceModel("text.friend.remove", null, new Object[]{ friendDisplayName } ));
        text.setEscapeModelStrings(false);
        add(text);
           
        //setup form		
		Form form = new Form("form");
		form.setOutputMarkupId(true);

		//submit button
		AjaxButton submitButton = new AjaxButton("submit") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				log.info("User: " + userId + " attempted to remove friend: " + friendUuid);
				
				//get Profile API
				Profile profile = ProfileApplication.get().getProfile();
				 
				//try to remove friend 
				if(profile.removeFriend(userId, friendUuid)) {
					log.info("User: " + userId + " removed friend: " + friendUuid);
					parent.setFriendRemoved(true); 	//tell parent to remove friend from display
				} else {
					//it failed, the logs will say why but we need to UI stuff here.
					target.appendJavascript("alert('Failed to remove friend. Check the system logs.');");
				}
				 
				window.close(target);				//close this window
            }
		};
		submitButton.setLabel(new ResourceModel("button.friend.remove"));
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            protected void onSubmit(AjaxRequestTarget target, Form form) {
            	parent.setFriendRemoved(false);	//tell parent not to remove friend from display
            	window.close(target); 			//close this window
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
        
        //add form
        add(form);
        
    }

}



