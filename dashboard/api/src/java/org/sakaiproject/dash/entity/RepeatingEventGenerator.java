/**
 * 
 */
package org.sakaiproject.dash.entity;

import java.util.Date;
import java.util.Map;

/**
 * A RepeatingEventGenerator is an EntityType that can add repeating calendar items 
 * to the calendar.  It provides a method to identify dates on which those repeating
 * calendar items will occur. 
 *
 */
public interface RepeatingEventGenerator extends EntityType {
	
	/**
	 * Returns a list of times at which the repeating event occurs 
	 * between the beginDate and the endDate.  The list is filtered
	 * to eliminate any previously excluded events. 
	 * @param entityReference
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public Map<Integer,Date> generateRepeatingEventDates(String entityReference, Date beginDate, Date endDate);

}
