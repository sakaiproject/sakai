package uk.ac.lancs.e_science.profile2.tool.pages;


import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

import uk.ac.lancs.e_science.profile2.api.ProfileUtilityManager;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.ConfirmedFriends;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.MyContactDisplay;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.RequestedFriends;

public class MyFriends extends BasePage {

	private transient Logger log = Logger.getLogger(MyFriends.class);
	
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
		sakaiProxy.postEvent(ProfileUtilityManager.EVENT_FRIENDS_VIEW_OWN, userId, false);
		
		
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
	
	
	
}



