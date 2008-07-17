package org.sakaiproject.site.util;

import java.util.Comparator;

import org.sakaiproject.tool.api.Tool;

public class ToolComparator implements Comparator {
	/**
	 * implementing the Comparator compare function
	 * 
	 * @param o1
	 *            The first object
	 * @param o2
	 *            The second object
	 * @return The compare result. 1 is o1 < o2; 0 is o1.equals(o2); -1
	 *         otherwise
	 */
	public int compare(Object o1, Object o2) {
		try {
			return ((Tool) o1).getTitle().compareToIgnoreCase(((Tool) o2).getTitle());
		} catch (Exception e) {
		}
		return -1;

	} // compare

} // ToolComparator
