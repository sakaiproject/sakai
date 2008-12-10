package uk.ac.lancs.e_science.profile2.tool.pages.panels;



import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;


public class FriendsQuickList extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoDisplay.class);
	

	
	public FriendsQuickList(String id, String userId) {
		super(id);
		
		//get randomised set of friends for this user, in future, list may be according to some rules		
		Label heading = new Label("heading", new ResourceModel("heading.quick.friends"));
		add(heading);
		
		
	}
	
	
	
}
