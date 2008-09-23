package uk.ac.lancs.e_science.profile2;

import org.apache.wicket.protocol.http.WebApplication;
import uk.ac.lancs.e_science.profile2.pages.Dispatcher;
import org.apache.log4j.Logger;


public class ProfileApplication extends WebApplication {    
    
	private transient Logger logger = Logger.getLogger(ProfileApplication.class);

	
	public ProfileApplication() {
	}
	
	protected void init(){
		//addComponentInstantiationListener(new SpringComponentInjector(this));
		//getResourceSettings().setThrowExceptionOnMissingResource(true);
		//getDebugSettings().setAjaxDebugModeEnabled(log.isDebugEnabled());	
		//getApplicationSettings().setPageExpiredErrorPage(SakaiSessionExpiredPage.class);
	}
	
	
	public Class getHomePage() {
		return Dispatcher.class;
	}

}
