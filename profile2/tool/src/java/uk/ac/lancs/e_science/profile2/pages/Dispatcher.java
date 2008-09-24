package uk.ac.lancs.e_science.profile2.tool.pages;

public class Dispatcher extends BasePage {
	
	public Dispatcher() {
		super();
		
		/*  example from blog
			if(sakaiProxy.isCurrentUserMaintainer() || securityManager.isCurrentUserTutor())
				setResponsePage(new ViewMembers());
			else
				setResponsePage(new MemberBlog());
		*/
		setResponsePage(new ViewProfile());
		
	}
}
