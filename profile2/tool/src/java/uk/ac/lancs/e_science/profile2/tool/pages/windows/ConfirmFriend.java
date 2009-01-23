package uk.ac.lancs.e_science.profile2.tool.pages.windows;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.pages.BasePage;

public class ConfirmFriend extends Panel {

	private static final long serialVersionUID = 1L;
	private transient Logger log = Logger.getLogger(ConfirmFriend.class);
	private transient SakaiProxy sakaiProxy;
	private transient Profile profile;

	
	public ConfirmFriend(String id, final ModalWindow window, final BasePage basePage, final String userX, final String userY){
        super(id);

        //get API's
        sakaiProxy = ProfileApplication.get().getSakaiProxy();
        profile = ProfileApplication.get().getProfile();
        
        //get friendName
        String friendName = sakaiProxy.getUserDisplayName(userY);
                
        //window setup
		window.setTitle(new StringResourceModel("title.friend.confirm", null, new Object[]{ friendName } )); 
		window.setInitialHeight(100);
		window.setInitialWidth(400);
				
        //text
        Label text = new Label("text", new StringResourceModel("text.friend.confirm", null, new Object[]{ friendName } ));
        text.setEscapeModelStrings(false);
        add(text);
           
        //setup form		
		Form form = new Form("form");
		form.setOutputMarkupId(true);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.friend.confirm"), form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				//confirm friend (note order must be backwards like this)
				if(profile.confirmFriend(userY, userX)) {
					basePage.setFriendRequestedResult(true);
				} else {
					//it failed, the logs will say why but we need to UI stuff here.
					basePage.setFriendRequestedResult(false);
					target.appendJavascript("alert('Failed to add friend. Check the system logs.');");
				}
				window.close(target);
            }
		};
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            protected void onSubmit(AjaxRequestTarget target, Form form) {
            	basePage.setFriendRequestedResult(false);
            	window.close(target);
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
        
        //add form
        add(form);
        
    }

	
	
}



