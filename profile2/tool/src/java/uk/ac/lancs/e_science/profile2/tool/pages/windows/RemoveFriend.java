package uk.ac.lancs.e_science.profile2.tool.pages.windows;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.hbm.Friend;
import uk.ac.lancs.e_science.profile2.tool.pages.MyFriends;

public class RemoveFriend extends Panel {

	
	
	public RemoveFriend(String id, final ModalWindow window, final MyFriends parent, String userId, Friend friend){
        super(id);

        String friendUuid = friend.getUserUuid();
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
				 System.out.println("submit clicked");
				 parent.setFriendRemoved(true); //tell parent to remove friend from display
				 window.close(target);			//close this window
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



