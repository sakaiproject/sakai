package uk.ac.lancs.e_science.profile2.tool;

import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.Application;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadWebRequest;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.tool.pages.MyProfile;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;



public class ProfileApplication extends WebApplication {    
    
	private transient Logger logger = Logger.getLogger(ProfileApplication.class);
	
	private transient SakaiProxy sakaiProxy;
	private transient Profile profile;

	
	protected void init(){
		
		
		//addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(true);
		//getDebugSettings().setAjaxDebugModeEnabled(log.isDebugEnabled());	
		//getApplicationSettings().setPageExpiredErrorPage(SakaiSessionExpiredPage.class);
		
		
		//super.init();
		//if(logger.isDebugEnabled()) logger.debug("init()");
		getMarkupSettings().setStripWicketTags(true);
		//mountBookmarkablePage("/my", MyProfile.class);
		
		//addComponentInstantiationListener(new SpringComponentInjector(this));
		//getResourceSettings().setThrowExceptionOnMissingResource(true);
		//getDebugSettings().setAjaxDebugModeEnabled(log.isDebugEnabled());	
		//getApplicationSettings().setPageExpiredErrorPage(SakaiSessionExpiredPage.class);
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
