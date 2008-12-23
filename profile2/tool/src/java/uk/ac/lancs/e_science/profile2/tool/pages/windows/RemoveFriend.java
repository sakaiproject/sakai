package uk.ac.lancs.e_science.profile2.tool.pages.windows;

import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.hbm.Friend;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.MyInfoDisplay;

public class RemoveFriend extends Panel {


	public RemoveFriend(String id, String userId, Friend friend){
        super(id);

        String friendUuid = friend.getUserUuid();
        String friendDisplayName = friend.getDisplayName();
                
        //heading
        Label heading = new Label("heading", new ResourceModel("heading.friend.remove"));
        add(heading);
        
        
        //text
        Label text = new Label("text", new StringResourceModel("window.text.friend.remove", null, new Object[]{ friendDisplayName } ));
        text.setEscapeModelStrings( false );
        add(text);
           
        
        //setup form		
		Form form = new Form("form");
		form.setOutputMarkupId(true);
		
		
		
		//submit button
		AjaxButton submitButton = new AjaxButton("submit") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
            }
		};
		form.add(submitButton);
		
        
		//cancel button
		AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", new ResourceModel("button.cancel"), form) {
            protected void onSubmit(AjaxRequestTarget target, Form form) {
            	
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);
        
        //add foprm
        add(form);
        
    }


	
	
	
}
