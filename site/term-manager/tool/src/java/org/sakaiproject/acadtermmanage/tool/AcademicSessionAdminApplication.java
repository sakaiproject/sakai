package org.sakaiproject.acadtermmanage.tool;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.acadtermmanage.tool.pages.SemesterPage;

/**
 * 
 * 
 * Based on the Wicket 1-4 Archetype by Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class AcademicSessionAdminApplication extends WebApplication {    
   
	@Override
	protected void init() {
		
		//Configure for Spring injection
	    getComponentInstantiationListeners().add(new SpringComponentInjector(this));
		
		
		//Don't throw an exception if we are missing a property, just fallback
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		
		//Remove the wicket specific tags from the generated markup
		getMarkupSettings().setStripWicketTags(true);
		
		//Don't add any extra tags around a disabled link (default is <em></em>)
		getMarkupSettings().setDefaultBeforeDisabledLink(null);
		getMarkupSettings().setDefaultAfterDisabledLink(null);
				
		// On Wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(getHomePage());
		getApplicationSettings().setAccessDeniedPage(getHomePage());
		

		
		getRequestCycleListeners().add(new AbstractRequestCycleListener() {
			@Override
			 public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				 if (ex instanceof RuntimeException) {
					 RuntimeException re = (RuntimeException)ex;
	                throw re;
				 }
				 return super.onException(cycle, ex);
            }
		});
		
	}
	
	
	/**
	 * The main page for our app
	 * 
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<? extends Page> getHomePage() {
		return SemesterPage.class;
	}
	
	
	
	

}
