package uk.ac.lancs.e_science.profile2.tool;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.Application;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.pages.Dispatcher;



public class ProfileApplication extends WebApplication {    
    
	private transient Logger logger = Logger.getLogger(ProfileApplication.class);
	
	private transient SakaiProxy sakaiProxy;
	
	protected void init(){
		
		//super.init();
		//if(logger.isDebugEnabled()) logger.debug("init()");
		//getMarkupSettings().setStripWicketTags(true);
		//mountBookmarkablePage("/home", Dispatcher.class);
		
		//addComponentInstantiationListener(new SpringComponentInjector(this));
		//getResourceSettings().setThrowExceptionOnMissingResource(true);
		//getDebugSettings().setAjaxDebugModeEnabled(log.isDebugEnabled());	
		//getApplicationSettings().setPageExpiredErrorPage(SakaiSessionExpiredPage.class);
	}
	
	public ProfileApplication() {
	}
	
	public static ProfileApplication get()
	{
		return (ProfileApplication) Application.get();
	}
		
	
	public Class getHomePage() {
		return Dispatcher.class;
	}

	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}

	public SakaiProxy getSakaiProxy() {
		return sakaiProxy;
	}
	
	
	
	

}
