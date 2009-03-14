package uk.ac.lancs.e_science.profile2.tool.pages.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.UrlValidator;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.components.CloseButton;
import uk.ac.lancs.e_science.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import uk.ac.lancs.e_science.profile2.tool.components.FeedbackLabel;
import uk.ac.lancs.e_science.profile2.tool.models.Search;
import uk.ac.lancs.e_science.profile2.tool.models.SimpleText;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class ChangeProfilePictureUrl extends Panel{
    
	private static final long serialVersionUID = 1L;
    private transient SakaiProxy sakaiProxy;
    private transient Profile profile;
	private transient Logger log = Logger.getLogger(ChangeProfilePictureUpload.class);

	public ChangeProfilePictureUrl(String id, UserProfile userProfile) {  
        super(id);  
        
		//get SakaiProxy API
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile API
		profile = ProfileApplication.get().getProfile();
		
		//setup SimpleText object 
		SimpleText simpleText = new SimpleText();
		
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
		
		//feedback
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		form.add(feedback);
		
		// filteredErrorLevels will not be shown in the FeedbackPanel
		//this way we can control them. see the onSubmit method for the form
        int[] filteredErrorLevels = new int[]{FeedbackMessage.ERROR};
        feedback.setFilter(new ErrorLevelsFeedbackMessageFilter(filteredErrorLevels));
		
		//upload
		TextField urlField = new TextField("urlField", new PropertyModel(simpleText, "text"));
		urlField.add(new UrlValidator());
		form.add(urlField);
		
		//form feedback will be redirected here
        final FeedbackLabel textFeedback = new FeedbackLabel("textFeedback", form);
        textFeedback.setOutputMarkupId(true);
        form.add(textFeedback);
		
		//submit button
        IndicatingAjaxButton submitButton = new IndicatingAjaxButton("submit", form) {
        	
        	protected void onSubmit(AjaxRequestTarget target, Form form) {

				//get the model
        		SimpleText simpleText = (SimpleText) form.getModelObject();
        		
        		System.out.println("simpleText:" + simpleText);
        		
        	};
    		
        };
        submitButton.setModel(new ResourceModel("button.upload"));
		form.add(submitButton);
		
		
		//add form to page
		add(form);
    }

}


