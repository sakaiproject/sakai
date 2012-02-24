/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.courier.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
* <p>CourierService is a static Cover for the {@link org.sakaiproject.courier.api.CourierService CourierService};
* see that interface for usage details.</p>
*/
public class CourierService
{
	/**
	 * Access the component instance: special cover only method.
	 * @return the component instance.
	 */
	public static org.sakaiproject.courier.api.CourierService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null) m_instance = (org.sakaiproject.courier.api.CourierService) ComponentManager.get(org.sakaiproject.courier.api.CourierService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.courier.api.CourierService) ComponentManager.get(org.sakaiproject.courier.api.CourierService.class);
		}
	}
	private static org.sakaiproject.courier.api.CourierService m_instance = null;



	public static java.lang.String SERVICE_NAME = org.sakaiproject.courier.api.CourierService.SERVICE_NAME;

	public static void deliver(org.sakaiproject.courier.api.Delivery param0)
	{
		org.sakaiproject.courier.api.CourierService service = getInstance();
		if (service == null)
			return;

		service.deliver(param0);
	}

	public static java.util.List getDeliveries(java.lang.String param0)
	{
		org.sakaiproject.courier.api.CourierService service = getInstance();
		if (service == null)
			return null;

		return service.getDeliveries(param0);
	}

	public static boolean hasDeliveries(java.lang.String param0)
	{
		org.sakaiproject.courier.api.CourierService service = getInstance();
		if (service == null)
			return false;

		return service.hasDeliveries(param0);
	}

	public static void clear(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.courier.api.CourierService service = getInstance();
		if (service == null)
			return;

		service.clear(param0, param1);
	}

	public static void clear(java.lang.String param0)
	{
		org.sakaiproject.courier.api.CourierService service = getInstance();
		if (service == null)
			return;

		service.clear(param0);
	}

	public static java.util.List<org.sakaiproject.courier.api.DeliveryProvider> getDeliveryProviders() {
		org.sakaiproject.courier.api.CourierService service = getInstance();
		if (service == null)
			return null;

		return service.getDeliveryProviders();
	}
	
	void registerDeliveryProvider(org.sakaiproject.courier.api.DeliveryProvider provider) {
		org.sakaiproject.courier.api.CourierService service = getInstance();
		if (service == null)
			return;

		service.registerDeliveryProvider(provider);
	}
}



