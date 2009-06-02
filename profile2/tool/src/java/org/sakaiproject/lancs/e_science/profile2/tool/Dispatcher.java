package uk.ac.lancs.e_science.profile2.tool;

import uk.ac.lancs.e_science.profile2.tool.pages.BasePage;
import uk.ac.lancs.e_science.profile2.tool.pages.MyProfile;

public class Dispatcher extends BasePage {
	
	public Dispatcher() {
		super();
		
		setResponsePage(new MyProfile());
		
	}
}
