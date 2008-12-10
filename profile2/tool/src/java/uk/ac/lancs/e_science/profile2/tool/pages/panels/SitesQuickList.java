package uk.ac.lancs.e_science.profile2.tool.pages.panels;



import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

public class SitesQuickList extends Panel {
	
	private transient Logger log = Logger.getLogger(MyInfoDisplay.class);
	

	
	public SitesQuickList(String id, String userId) {
		super(id);
		
		//get list of sites for this user, subset, alphabetically, and according to privacy rules
		
		Label heading = new Label("heading", new ResourceModel("heading.quick.sites"));
		add(heading);
		
		
	}
	
}
