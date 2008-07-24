package org.sakaiproject.springframework.orm.hibernate.cover;

import org.sakaiproject.component.cover.ComponentManager;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jul 25, 2007
 * Time: 10:57:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class VendorHbmTransformer 
{
	/**
	 * Access the component instance: special cover only method.
	 *
	 * @return the component instance.
	 */
	public static org.sakaiproject.springframework.orm.hibernate.VendorHbmTransformer getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.springframework.orm.hibernate.VendorHbmTransformer)
                        ComponentManager.get(org.sakaiproject.springframework.orm.hibernate.VendorHbmTransformer.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.springframework.orm.hibernate.VendorHbmTransformer)
                    ComponentManager.get(org.sakaiproject.db.api.SqlService.class);
		}
	}

	private static org.sakaiproject.springframework.orm.hibernate.VendorHbmTransformer m_instance = null;

    public static InputStream getTransformedMapping(InputStream mappingDoc)
	{
		org.sakaiproject.springframework.orm.hibernate.VendorHbmTransformer service = getInstance();
		if (service == null) return null;

		return service.getTransformedMapping(mappingDoc);
	}

}

