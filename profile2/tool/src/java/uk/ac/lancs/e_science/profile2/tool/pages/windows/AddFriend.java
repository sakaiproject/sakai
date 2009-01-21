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
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.pages.BasePage;

public class AddFriend extends Panel {

	private static final long serialVersionUID = 1L;
	private transient Logger log = Logger.getLogger(AddFriend.class);
	
	public AddFriend(String id, final ModalWindow window, final BasePage basePage, final String currentUserId, final String friendUserId, String friendName){
        super(id);

        //window setup
		window.setTitle(new StringResourceModel("title.friend.add", null, new Object[]{ friendName } )); 
		window.setInitialHeight(100);
		window.setInitialWidth(400);
				
        //text
        Label text = new Label("text", new StringResourceModel("text.friend.add", null, new Object[]{ friendName } ));
        text.setEscapeModelStrings(false);
        add(text);
           
        //setup form		
		Form form = new Form("form");
		form.setOutputMarkupId(true);
		
		//submit button
		AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", new ResourceModel("button.friend.add"), form) {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				//get Profile API
				Profile profile = ProfileApplication.get().getProfile();
				 
				//request friend
				if(profile.requestFriend(currentUserId, friendUserId)) {
					basePage.setConfirmResult(true);
					log.info("User: " + currentUserId + " added friend: " + friendUserId);
				} else {
					//it failed, the logs will say why but we need to UI stuff here.
					basePage.setConfirmResult(false);
					target.appendJavascript("alert('Failed to add friend. Check the system logs.');");
				}
				window.close(target);
            }
		};
		//submitButton.setLabel(new ResourceModel("button.friend.add"));
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            protected void onSubmit(AjaxRequestTarget target, Form form) {
            	basePage.setConfirmResult(false);
            	window.close(target);
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
        
        //add form
        add(form);
        
    }

	
	
}



