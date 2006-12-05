package org.sakaiproject.portal.service;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

public class PortalServiceImpl implements PortalService
{
    /**
     * Parameter to force state reset
     */
    public static final String PARM_STATE_RESET = "sakai.state.reset";
	
    public StoredState getStoredState() {
        Session s = SessionManager.getCurrentSession();
        StoredState ss = (StoredState) s.getAttribute("direct-stored-state");
        return ss;
    }

    public void setStoredState(StoredState ss) {
        Session s = SessionManager.getCurrentSession();
        if (s.getAttribute("direct-stored-state") == null || ss == null) {
            s.setAttribute("direct-stored-state", ss);
        }
    }
    // To allow us to retain reset state across redirects
    public String getResetState() {
        Session s = SessionManager.getCurrentSession();
        String ss = (String) s.getAttribute("reset-stored-state");
        return ss;
    }

    public void setResetState(String ss) {
        Session s = SessionManager.getCurrentSession();
        if (s.getAttribute("reset-stored-state") == null || ss == null) {
            s.setAttribute("reset-stored-state", ss);
        }
    }

	public boolean isEnableDirect()
	{
		return "true".equals(ServerConfigurationService.getString(
	            "charon.directurl", "true"));
	}

	public boolean isResetRequested(HttpServletRequest req)
	{
		return "true".equals(req
				.getParameter(PARM_STATE_RESET))
				|| "true".equals(getResetState());
	}

	public String getResetStateParam()
	{
		// TODO Auto-generated method stub
		return PARM_STATE_RESET;
	}

	public StoredState newStoredState(String marker, String replacement)
	{
		return new StoredStateImpl(marker,replacement);
	}


}
