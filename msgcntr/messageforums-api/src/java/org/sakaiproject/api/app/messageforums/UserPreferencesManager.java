/**
 * 
 */
package org.sakaiproject.api.app.messageforums;

import java.util.TimeZone;

/**
 * @author branden
 *
 */
public interface UserPreferencesManager {

	/**
	 * @return
	 * 		The timezone the user has configured.
	 */
	public TimeZone getTimeZone();
	
}