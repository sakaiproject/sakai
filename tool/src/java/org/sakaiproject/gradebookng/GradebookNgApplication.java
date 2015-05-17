package org.sakaiproject.gradebookng;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.gradebookng.tool.pages.NoDataPage;
import org.sakaiproject.gradebookng.tool.pages.PermissionsPage;
import org.sakaiproject.gradebookng.tool.pages.SettingsPage;

/**
 * Main application class
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradebookNgApplication extends WebApplication {    
   
	@Override
	public void init() {
		super.init();
		
		//page mounting for bookmarkable URLs
		//mountPage("/gradebook", GradebookPage.class);
		//mountPage("/settings", SettingsPage.class);
		//mountPage("/importexport", ImportExportPage.class);
		//mountPage("/permissions", PermissionsPage.class);
		
		//remove the version number from the URL so that browser refreshes re-render the page
		//getRequestCycleSettings().setRenderStrategy(IRequestCycleSettings.RenderStrategy.ONE_PASS_RENDER); 
		
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
		//getApplicationSettings().setPageExpiredErrorPage(getHomePage());
		
		//catch the exception page and redirect somewhere
		//getApplicationSettings().setInternalErrorPage(SomePage.class);
		
		// show internal error page rather than default developer page
		//getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
		
		// Intercept the stacktrace so it doesnt fill the page
		getRequestCycleListeners().add(new SakaiRequestCycleListener());

		

   
		//to put this app into deployment mode, see web.xml
	}
	
	/**
	 * Overrides the exception handler so the stacktrace doesnt consume the screen.
	 * @author fivium
	 *
	 */
	public class SakaiRequestCycleListener extends AbstractRequestCycleListener {
		
		@Override
		public IRequestHandler onException(RequestCycle cycle, Exception ex) {
            return null;
        }
	}
	
	
	/**
	 * The main page for our app
	 * 
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<GradebookPage> getHomePage() {
		return GradebookPage.class;
	}
	
	
	/**
     * Constructor
     */
	public GradebookNgApplication() {
	}
	
	

}
