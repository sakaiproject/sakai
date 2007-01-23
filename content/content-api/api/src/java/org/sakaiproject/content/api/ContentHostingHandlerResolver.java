/**
 * 
 */
package org.sakaiproject.content.api;

import org.sakaiproject.entity.api.Edit;

/**
 * @author ieb
 *
 */
public interface ContentHostingHandlerResolver
{

	public static final String CHH_BEAN_NAME =  "sakai:handler-bean-id";

	/**
	 * create a new Collection Edit to allow the CHH implementation to deliver Collections
	 * 
	 * @param id
	 * @return
	 */
	Edit newCollectionEdit(String id);

	/**
	 * create a new Resource Edit to allow the CHH implementation to deliver Resources
	 * 
	 * @param id
	 * @return
	 */
	Edit newResourceEdit(String id);

}
