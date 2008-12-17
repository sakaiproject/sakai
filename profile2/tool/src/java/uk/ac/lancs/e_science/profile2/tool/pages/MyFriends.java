package uk.ac.lancs.e_science.profile2.tool.pages;


import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.ResourceModel;


public class MyFriends extends BasePage {

	private transient Logger log = Logger.getLogger(MyFriends.class);
		
	public MyFriends() {
		
		if(log.isDebugEnabled()) log.debug("MyFriends()");
		
		//add the feedback panel for any error messages
		FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		add(feedbackPanel);
		feedbackPanel.setVisible(false); //hide by default

		//get current user
		String userId = sakaiProxy.getCurrentUserId();

		Label heading = new Label("heading", new ResourceModel("heading.friends"));
		add(heading);
	}
}



