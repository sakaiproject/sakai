package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.UrlValidator;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileConstants;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.components.CloseButton;
import uk.ac.lancs.e_science.profile2.tool.models.SimpleText;
import uk.ac.lancs.e_science.profile2.tool.pages.MyProfile;

public class ChangeProfilePictureUrl extends Panel{
    
	private static final long serialVersionUID = 1L;
    private transient SakaiProxy sakaiProxy;
    private transient Profile profile;
	private static final Logger log = Logger.getLogger(ChangeProfilePictureUrl.class);

	public ChangeProfilePictureUrl(String id) {  
        super(id);  
        
        log.debug("ChangeProfilePictureUrl()");
        
		//get SakaiProxy API
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile API
		profile = ProfileApplication.get().getProfile();
			
		//get userId
		final String userId = sakaiProxy.getCurrentUserId();
		
		//setup SimpleText object 
		SimpleText simpleText = new SimpleText();
		
		//do they already have a URL that should be loaded in here?
		String externalUrl = profile.getExternalImageUrl(userId, ProfileConstants.PROFILE_IMAGE_MAIN);
		
		if(externalUrl != null) {
			simpleText.setText(externalUrl);
		}
		
		//setup form model using the SimpleText object
		CompoundPropertyModel formModel = new CompoundPropertyModel(simpleText);
		
        //setup form	
		Form form = new Form("form", formModel);
		form.setOutputMarkupId(true);
        
        //close button component
        CloseButton closeButton = new CloseButton("closeButton", this);
        closeButton.setOutputMarkupId(true);
		form.add(closeButton);
      
        //text
		Label textEnterUrl = new Label("textEnterUrl", new ResourceModel("text.image.url"));
		form.add(textEnterUrl);
		
		//upload
		TextField urlField = new TextField("urlField", new PropertyModel(simpleText, "text"));
		urlField.setRequired(true);
		urlField.add(new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_2_SLASHES));
		form.add(urlField);
		
		//feedback (styled to remove the list)
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);
		
		//submit button
        IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", form) {
        	
        	protected void onSubmit(AjaxRequestTarget target, Form form) {

				//get the model (already validated)
        		SimpleText simpleText = (SimpleText) form.getModelObject();
        		
        		//get the url
        		String url = simpleText.getText();
        		
        		//save it
        		if(profile.saveExternalImage(userId, url, null)) {
       		
	        		//log it
					log.info("User " + userId + " successfully changed profile picture by url.");
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_IMAGE_CHANGE_URL, "/profile/"+userId, true);
					
					//refresh image data
					setResponsePage(new MyProfile());
        		} else {
        			error(new StringResourceModel("error.url.save.failed", this, null).getString());
        			return;
        		}
        		
        		
        	};
        	
        	// update feedback panel if validation failed
        	protected void onError(AjaxRequestTarget target, Form form) { 
        		log.debug("ChangeProfilePictureUrl.onSubmit validation failed.");
        	    target.addComponent(feedback); 
        	} 
    		
        };
        submitButton.setModel(new ResourceModel("button.upload"));
		form.add(submitButton);
		
		
		//add form to page
		add(form);
    }

	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("ChangeProfilePictureUrl has been deserialized.");
		//re-init our transient objects
		profile = ProfileApplication.get().getProfile();
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
	}
	
	
	
}


