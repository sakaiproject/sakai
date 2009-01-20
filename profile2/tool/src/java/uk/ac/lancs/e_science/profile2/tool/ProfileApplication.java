package uk.ac.lancs.e_science.profile2.tool;


import org.apache.log4j.Logger;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.pages.errors.InternalErrorPage;
import uk.ac.lancs.e_science.profile2.tool.pages.errors.SessionExpiredPage;




public class ProfileApplication extends WebApplication {    
    
	private transient Logger logger = Logger.getLogger(ProfileApplication.class);
	
	private transient SakaiProxy sakaiProxy;
	private transient Profile profile;

	
	protected void init(){
		
		
		//addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(true);
		//getDebugSettings().setAjaxDebugModeEnabled(log.isDebugEnabled());	
		
		/* if Session expires, show this error instead */
		getApplicationSettings().setPageExpiredErrorPage(SessionExpiredPage.class);
		
		/* if internal error occurr, show this page */
		getApplicationSettings().setInternalErrorPage(InternalErrorPage.class);
		
		//super.init();
		//if(logger.isDebugEnabled()) logger.debug("init()");
		
		/* strip the wicket:id tags from the output HTML */
		getMarkupSettings().setStripWicketTags(true);
		//mountBookmarkablePage("/my", MyProfile.class);
		
		/* a component that is disabled by Wicket will normally have <em> surrounding it. This makes it null */
		getMarkupSettings().setDefaultBeforeDisabledLink(null);
		getMarkupSettings().setDefaultAfterDisabledLink(null);

		
		
		
	}
	
	public ProfileApplication() {
	}
	
	//setup homepage		
	public Class getHomePage() {
		return Dispatcher.class;
	}
	
	//expose ProfileApplication itself
	public static ProfileApplication get() {
		return (ProfileApplication) Application.get();
	}

	//expose SakaiProxy API
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}

	public SakaiProxy getSakaiProxy() {
		return sakaiProxy;
	}
	
	//expose Profile API
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public Profile getProfile() {
		return profile;
	}
		

}
