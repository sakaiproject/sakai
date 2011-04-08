package org.sakaiproject.dashboard.tool;

import org.apache.wicket.RequestCycle;
import org.sakaiproject.dashboard.logic.ExternalLogic;
import org.sakaiproject.dashboard.logic.DashboardLogic;

public class Locator {
	
	public static ExternalLogic getExternalLogic()
    {
        DashboardApplication app = (DashboardApplication)RequestCycle.get().getApplication();
        return app.getExternalLogic();
    }
	
	public static DashboardLogic getDashboardLogic()
    {
        DashboardApplication app = (DashboardApplication)RequestCycle.get().getApplication();
        return app.getDashboardLogic();
    }
}
