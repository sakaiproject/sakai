package uk.ac.lancs.e_science.profile2.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;


public class MyProfile extends BasePage {

	private transient Logger log = Logger.getLogger(MyProfile.class);

	public MyProfile() {
		
		if(log.isDebugEnabled()) log.debug("MyProfile()");
		
		//heading
		add(new Label("myProfileHeading", new ResourceModel("heading.my")));
		
	}
}
