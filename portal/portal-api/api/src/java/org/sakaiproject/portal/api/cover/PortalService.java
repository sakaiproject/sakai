package org.sakaiproject.portal.api.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * A cover to the portal service. This only covers the getting of the implementation
 * and not all the methjods.
 * @author ieb
 *
 */
public class PortalService
{

	/** Possibly cached component instance. */
	private static org.sakaiproject.portal.api.PortalService m_instance = null;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.portal.api.PortalService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.portal.api.PortalService) ComponentManager
						.get(org.sakaiproject.portal.api.PortalService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.portal.api.PortalService) ComponentManager
					.get(org.sakaiproject.portal.api.PortalService.class);
		}
	}

}
