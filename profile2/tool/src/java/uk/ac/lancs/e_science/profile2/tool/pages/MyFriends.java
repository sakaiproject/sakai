package uk.ac.lancs.e_science.profile2.tool.pages;


import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.Panel;

import uk.ac.lancs.e_science.profile2.tool.pages.panels.ConfirmedFriends;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.RequestedFriends;

public class MyFriends extends BasePage {

	private transient Logger log = Logger.getLogger(MyFriends.class);
	
	public MyFriends() {
		
		if(log.isDebugEnabled()) log.debug("MyFriends()");
		
		//get current user
		final String userId = sakaiProxy.getCurrentUserId();

		//friend requests panel
		Panel requestedFriends = new RequestedFriends("requestedFriends", userId);
		requestedFriends.setOutputMarkupId(true);
		add(requestedFriends);
		
		
		//confirmed friends panel
		Panel confirmedFriends = new ConfirmedFriends("confirmedFriends", userId);
		confirmedFriends.setOutputMarkupId(true);
		add(confirmedFriends);
	}
	
}



