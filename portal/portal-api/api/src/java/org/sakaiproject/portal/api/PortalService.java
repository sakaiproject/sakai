package org.sakaiproject.portal.api;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

public interface PortalService
{
    /**
     * A portal request scope attribute that reprenset the placement id of the current request.
     * It should be a string, and should be implimented where the request is portlet dispatched.
     */
    public static final String PLACEMENT_ATTRIBUTE = PortalService.class.getName()+"_placementid";

    /**
     * this is the property in the tool config that defines the portlet context
     * of tool. At the moment we assume that this is in the read-only properties
     * of the tool, but there could be a generic tool placement that enabled any
     * portlet to be mounted
     */
    public static final String TOOL_PORTLET_CONTEXT_PATH = "portlet-context";

    /**
     * this is the property in the tool config that defines the name of the
     * portlet
     */
    public static final String TOOL_PORTLET_NAME = "portlet-name";
    
    
	void  setResetState(String state);
	
	String getResetState();

	StoredState getStoredState();

	boolean isResetRequested(HttpServletRequest req);

	void setStoredState(StoredState storedstate);

	boolean isEnableDirect();

	String getResetStateParam();

	StoredState newStoredState(String string, String string2);

	Iterator<PortletApplicationDescriptor> getRegisteredApplications();

}
