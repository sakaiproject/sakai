package org.sakaiproject.dashboard.tool;


import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.dashboard.logic.ExternalLogic;
import org.sakaiproject.dashboard.logic.DashboardLogic;




public class DashboardApplication extends WebApplication {    
    
	protected void init(){
		getResourceSettings().setThrowExceptionOnMissingResource(true);
		getMarkupSettings().setStripWicketTags(true);
	}
	
	public DashboardApplication() {
	}
	
	//set to DEPLOYMENT for production
	public String getConfigurationType() { return Application.DEVELOPMENT; }
	
	//set homepage		
	public Class<Dispatcher> getHomePage() {
		return Dispatcher.class;
	}
	
	//expose Application itself
	public static DashboardApplication get() {
		return (DashboardApplication) Application.get();
	}

	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	public ExternalLogic getExternalLogic() {
		return externalLogic;
	}

	private DashboardLogic dashboardLogic;
	public DashboardLogic getDashboardLogic() {
		return dashboardLogic;
	}
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	
	

}
