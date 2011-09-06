/**
 * 
 */
package org.sakaiproject.dash.entity;

/**
 * 
 *
 */
public enum EntityLinkStrategy {
	
	/** 
	 * Open a disclosure in the dashboard with a little info (description, date, etc) 
	 * and possibly a navigation link.
	 * Requires an implementation of org.sakaiproject.dash.entity.EntityType be registered
	 * with org.sakaiproject.dash.logic.DashboardLogic and that org.sakaiproject.dash.entity.EntityType.getProperties() 
	 * returns a mapping of specific key-value pairs.
	 */
	SHOW_PROPERTIES,
	
	/** 
	 * Open a dialog in the dashboard with an HTML fragment provided by some other code. 
	 * Requires that the entity URL provide an HTML fragment that will can be retrieved 
	 * with an AJAX request and  
	 */
	SHOW_DIALOG,
	
	/**  
	 * Open the access-url, which may navigate away from the dashboard. 
	 *  
	 */
	ACCESS_URL
	
}
