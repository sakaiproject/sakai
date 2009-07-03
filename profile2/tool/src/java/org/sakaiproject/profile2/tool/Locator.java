package org.sakaiproject.profile2.tool;

import org.apache.wicket.RequestCycle;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;

public class Locator {

	public static SakaiProxy getSakaiProxy()
    {
        ProfileApplication app = (ProfileApplication)RequestCycle.get().getApplication();
        return app.getSakaiProxy();
    }
	
	public static ProfileLogic getProfileLogic()
    {
		ProfileApplication app = (ProfileApplication)RequestCycle.get().getApplication();
        return app.getProfileLogic();
    }
	
}
