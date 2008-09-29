package uk.ac.lancs.e_science.profile2.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.api.common.edu.person.SakaiPerson;


public class MyProfile extends BasePage {

	private transient Logger log = Logger.getLogger(MyProfile.class);

	public MyProfile() {
		
		if(log.isDebugEnabled()) log.debug("MyProfile()");
		
		//get some user info
		String userId = sakaiProxy.getCurrentUserId();
		String userDisplayName = sakaiProxy.getUserDisplayName(userId);
		//String userStatus = profile.getUserStatus(userId);
		//String userStatusLastUpdated = profile.getUserStatusLastUpdated(userId);
		String userStatus = "is at the computer";
		String userStatusLastUpdated = "on Monday";

		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);
		
		//if the person does not have a sakaiPseron entry, we need to create them one
		
		
		
		
		//heading
		add(new Label("profileHeadingName", userDisplayName));
		//status
		add(new Label("profileHeadingStatus", userStatus));
		//status last updated
		add(new Label("profileHeadingStatusLastUpdated", "hello"));
	    

		//panels
		add(new MyInfoPanel("myInfoPanel", sakaiProxy, profile, sakaiPerson));
		add(new MyInterestsPanel("myInterestsPanel"));
		
		
	}
	
	
	
}
