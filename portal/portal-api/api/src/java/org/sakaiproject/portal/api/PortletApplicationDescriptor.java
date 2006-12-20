package org.sakaiproject.portal.api;

import java.util.Iterator;

public interface PortletApplicationDescriptor
{

	String getApplicationName();

	String getApplicationContext();

	String getApplicationId();

	Iterator<PortletDescriptor> getPortlets();

}
