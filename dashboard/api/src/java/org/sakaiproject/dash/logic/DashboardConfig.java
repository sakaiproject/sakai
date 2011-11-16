/**
 * 
 */
package org.sakaiproject.dash.logic;

/**
 * 
 *
 */
public interface DashboardConfig {
	
	public static final String PROP_DEFAULT_ITEMS_IN_PANEL = "PROP_DEFAULT_ITEMS_IN_PANEL";
	public static final String PROP_DEFAULT_ITEMS_IN_DISCLOSURE = "PROP_DEFAULT_ITEMS_IN_DISCLOSURE";
	public static final String PROP_DEFAULT_ITEMS_IN_GROUP = "PROP_DEFAULT_ITEMS_IN_GROUP";
	public static final String PROP_REMOVE_ITEMS_AFTER_WEEKS = "PROP_REMOVE_ITEMS_AFTER_WEEKS";
	public static final String PROP_REMOVE_STARRED_ITEMS_AFTER_WEEKS = "PROP_REMOVE_STARRED_ITEMS_AFTER_WEEKS";
	
	// daysBetweenHorizonUpdates
	public static final String PROP_DAYS_BETWEEN_HORIZ0N_UPDATES = "PROP_DAYS_BETWEEN_HORIZ0N_UPDATES";
	public static final String PROP_WEEKS_TO_HORIZON = "PROP_WEEKS_TO_HORIZON";
	
	/** Modes are TEXT (1), LIST (2) or HIDDEN (0). Default is TEXT.  */
	public static final String PROP_MOTD_MODE = "PROP_MOTD_MODE";
	
	// horizon settings
	
	
	
	public Integer getConfigValue(String propertyName, Integer propertyValue);
	
	public void setConfigValue(String propertyName, Integer propertyValue);
	
}
