package org.sakaiproject.profile2.tool.pages;


import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.profile2.tool.pages.panels.ConfirmedFriends;
import org.sakaiproject.profile2.tool.pages.panels.RequestedFriends;
import org.sakaiproject.profile2.util.ProfileConstants;

public class MyFriends extends BasePage {

	private static final Logger log = Logger.getLogger(MyFriends.class);
	private Panel confirmedFriends;
	private Panel requestedFriends;
	
	public MyFriends() {
		
		log.debug("MyFriends()");
				
		//get current user
		final String userId = sakaiProxy.getCurrentUserId();

		//friend requests panel
		requestedFriends = new RequestedFriends("requestedFriends", this, userId);
		requestedFriends.setOutputMarkupId(true);
		add(requestedFriends);
		
		
		//confirmed friends panel
		confirmedFriends = new ConfirmedFriends("confirmedFriends", userId);
		confirmedFriends.setOutputMarkupId(true);
		add(confirmedFriends);
		
		//post view event
		sakaiProxy.postEvent(ProfileConstants.EVENT_FRIENDS_VIEW_OWN, "/profile/"+userId, false);
		
	}
	
	//method to allow us to update the confirmedFriends panel
	public void updateConfirmedFriends(AjaxRequestTarget target, String userId) {
		
		ConfirmedFriends newPanel = new ConfirmedFriends("confirmedFriends", userId);
		newPanel.setOutputMarkupId(true);
		confirmedFriends.replaceWith(newPanel);
		confirmedFriends=newPanel; //keep reference up to date!
		if(target != null) {
			target.addComponent(newPanel);
			//resize iframe
			target.appendJavascript("setMainFrameHeight(window.name);");
		}
		
	}
	
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MyFriends has been deserialized.");
		//re-init our transient objects
		//profile = getProfile();
		//sakaiProxy = getSakaiProxy();
	}
	
}



