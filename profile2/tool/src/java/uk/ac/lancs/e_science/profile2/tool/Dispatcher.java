package uk.ac.lancs.e_science.profile2.tool;

import uk.ac.lancs.e_science.profile2.tool.pages.BasePage;
import uk.ac.lancs.e_science.profile2.tool.pages.MyProfile;

public class Dispatcher extends BasePage {
	
	public Dispatcher() {
		super();
		
		/*  example from blog
			if(sakaiProxy.isCurrentUserMaintainer() || securityManager.isCurrentUserTutor())
				setResponsePage(new ViewMembers());
			else
				setResponsePage(new MemberBlog());
		*/
		setResponsePage(new MyProfile());
		
	}
}
