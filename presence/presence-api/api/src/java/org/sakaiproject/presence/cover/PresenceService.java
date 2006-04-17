/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.service.legacy.presence.cover;

import org.sakaiproject.service.framework.component.cover.ComponentManager;

/**
* <p>PresenceService is a static Cover for the {@link org.sakaiproject.service.legacy.presence.PresenceService PresenceService};
* see that interface for usage details.</p>
* 
* @author University of Michigan, Sakai Software Development Team
* @version $Revision$
*/
public class PresenceService
{
	/**
	 * Access the component instance: special cover only method.
	 * @return the component instance.
	 */
	public static org.sakaiproject.service.legacy.presence.PresenceService getInstance()
	{
		if (ComponentManager.CACHE_SINGLETONS)
		{
			if (m_instance == null) m_instance = (org.sakaiproject.service.legacy.presence.PresenceService) ComponentManager.get(org.sakaiproject.service.legacy.presence.PresenceService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.service.legacy.presence.PresenceService) ComponentManager.get(org.sakaiproject.service.legacy.presence.PresenceService.class);
		}
	}
	private static org.sakaiproject.service.legacy.presence.PresenceService m_instance = null;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.service.legacy.presence.PresenceService.REFERENCE_ROOT;
	public static java.lang.String EVENT_PRESENCE = org.sakaiproject.service.legacy.presence.PresenceService.EVENT_PRESENCE;
	public static java.lang.String EVENT_ABSENCE = org.sakaiproject.service.legacy.presence.PresenceService.EVENT_ABSENCE;

	public static java.lang.String presenceReference(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.presence.PresenceService service = getInstance();
		if (service == null)
			return null;

		return service.presenceReference(param0);
	}

	public static java.lang.String locationId(java.lang.String param0, java.lang.String param1, java.lang.String param2)
	{
		org.sakaiproject.service.legacy.presence.PresenceService service = getInstance();
		if (service == null)
			return null;

		return service.locationId(param0, param1, param2);
	}

	public static java.lang.String getLocationDescription(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.presence.PresenceService service = getInstance();
		if (service == null)
			return null;

		return service.getLocationDescription(param0);
	}

	public static void setPresence(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.presence.PresenceService service = getInstance();
		if (service == null)
			return;

		service.setPresence(param0);
	}

	public static void removePresence(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.presence.PresenceService service = getInstance();
		if (service == null)
			return;

		service.removePresence(param0);
	}

	public static java.util.List getPresence(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.presence.PresenceService service = getInstance();
		if (service == null)
			return null;

		return service.getPresence(param0);
	}

	public static java.util.List getPresentUsers(java.lang.String param0)
	{
		org.sakaiproject.service.legacy.presence.PresenceService service = getInstance();
		if (service == null)
			return null;

		return service.getPresentUsers(param0);
	}

	public static java.util.List getLocations()
	{
		org.sakaiproject.service.legacy.presence.PresenceService service = getInstance();
		if (service == null)
			return null;

		return service.getLocations();
	}

	public static int getTimeout()
	{
		org.sakaiproject.service.legacy.presence.PresenceService service = getInstance();
		if (service == null)
			return 0;

		return service.getTimeout();
	}
}



