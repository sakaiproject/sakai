package uk.ac.lancs.e_science.profile2.tool.pages.panels;


import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.hbm.ProfileStatus;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyStatusPanel extends Panel {
	
	private transient Logger log = Logger.getLogger(MyStatusPanel.class);
	
    private transient SakaiProxy sakaiProxy;
    private transient Profile profile;


	public MyStatusPanel(String id, UserProfile userProfile) {
		super(id);
		
		//create model
		CompoundPropertyModel userProfileModel = new CompoundPropertyModel(userProfile);
		
		//get SakaiProxy API
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile API
		profile = ProfileApplication.get().getProfile();
		
		//a number of factors can disable the status
		boolean displayStatus = true;
		
		//get info
		String displayName = userProfile.getDisplayName();
		ProfileStatus profileStatus = profileStatus = profile.getLatestUserStatus(userProfile.getUserId());
		
		String statusMessage = "test";
		String statusDateStr = "on monday";
		
		if(profileStatus == null) {
			displayStatus = false;
		} else {
			statusMessage = profileStatus.getMessage();
			statusDateStr = profile.convertDateForStatus(profileStatus.getDateAdded());
		}	
			
		//name
		Label profileName = new Label("profileName", displayName);
		add(profileName);
		
		
		//status container
		WebMarkupContainer statusContainer = new WebMarkupContainer("statusContainer");
		statusContainer.setOutputMarkupId(true);
		
		//status
		Label statusMessageLabel = new Label("statusMessage", statusMessage);
		statusContainer.add(statusMessageLabel);
		
		//status last updated
		Label statusDateLabel = new Label("statusDate", statusDateStr);
		statusContainer.add(statusDateLabel);
		
		//status update link
    	Link statusUpdateLink = new Link("statusUpdateLink") {
			public void onClick() {
				//
			}
		};
		statusUpdateLink.add(new Label("statusUpdateLabel",new ResourceModel("link.status.update")));
		statusContainer.add(statusUpdateLink);
		
		
		add(statusContainer);
		
		//if(!displayStatus) {
		//	statusContainer.setVisible(false);
		//}
		
	}
	

	
	
	
	
	
	
}
