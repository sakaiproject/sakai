package uk.ac.lancs.e_science.profile2.tool.pages.panels;


import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyStatusPanel extends Panel {
	
	private transient Logger log = Logger.getLogger(MyStatusPanel.class);
	
	//needs to be redone with forms and panels


	//panel constructor
	public MyStatusPanel(String id, UserProfile userProfile) {
		super(id);
		
		
		//get info for the status/heading block
		String displayName = userProfile.getDisplayName();
		String status = userProfile.getStatus();
		Date statusLastUpdated = userProfile.getStatusLastUpdated();
		
		String statusLastUpdatedStr = "on Monday";
		
		/*convert the date out into a better format:
		 * if today = 'today'
		 * if yesterday = 'yesterday'
		 * if this week = 'on Weekday'
		 * if last week = 'last week'
		 * past a week ago we don't display the status last updated
		*/
		
		//name
		add(new Label("profileHeadingName", displayName));
		//status
		add(new Label("profileHeadingStatus", status));
		//status last updated
		add(new Label("profileHeadingStatusLastUpdated", statusLastUpdatedStr));
		
		//status update link
    	Link statusUpdateLink = new Link("statusUpdateLink") {
			public void onClick() {
				//
			}
		};
		statusUpdateLink.add(new Label("statusUpdateLabel",new ResourceModel("link.status.update")));
		add(statusUpdateLink);
		
		
	}
	

	
	
	
	
	
	
}
