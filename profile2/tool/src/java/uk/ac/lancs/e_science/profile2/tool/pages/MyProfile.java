package uk.ac.lancs.e_science.profile2.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;


public class MyProfile extends BasePage {

	private transient Logger log = Logger.getLogger(MyProfile.class);

	public MyProfile() {
		
		if(log.isDebugEnabled()) log.debug("MyProfile()");
		
		//add the feedback panel for any error messages, go here.
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		add(feedbackPanel);
		feedbackPanel.setVisible(false); //hide by default

		
		//get some user info
		String userId = sakaiProxy.getCurrentUserId();
		String userDisplayName = sakaiProxy.getUserDisplayName(userId);
		String userStatus = profile.getUserStatus(userId);
		String userStatusLastUpdated = profile.getUserStatusLastUpdated(userId);

		//get SakaiPerson
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
		//if the person does not have a sakaiPerson entry, we need to create them one
		if(sakaiPerson == null) {
			error("You don't have a sakaiPerson record, crap.");
			feedbackPanel.setVisible(true); //explicitly show it

		} 
		
		//heading
		add(new Label("profileHeadingName", userDisplayName));
		//status
		add(new Label("profileHeadingStatus", userStatus));
		//status last updated
		add(new Label("profileHeadingStatusLastUpdated", userStatusLastUpdated));
	    
		
		
		
		

		
		//panels
		Panel myInfoPanel = new MyInfoPanel("myInfoPanel", sakaiProxy, profile, sakaiPerson);
		add(myInfoPanel);
		//add(new MyInterestsPanel("myInterestsPanel"));
		
		
		
	}
	
	
	
}
