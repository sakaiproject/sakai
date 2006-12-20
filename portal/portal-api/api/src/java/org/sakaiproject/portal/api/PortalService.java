package org.sakaiproject.portal.api;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

public interface PortalService
{

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
