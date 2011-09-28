/**
 * 
 */
package org.sakaiproject.dash.model;

/**
 * 
 *
 */
public enum ItemType {
	
	CALENDAR_ITEM	(0, CalendarItem.class.getCanonicalName()),
	NEWS_ITEM		(1, NewsItem.class.getCanonicalName());
	
	private int value = 0; 
	private String name = null;
	
	private ItemType(int val, String name) {
		this.value = val;
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public static ItemType fromString(String name) {
		if(name != null && CALENDAR_ITEM.getName().equalsIgnoreCase(name)) {
			return CALENDAR_ITEM;
		} 
		return NEWS_ITEM;
	}

	public static ItemType fromInteger(int val) {
		if(CALENDAR_ITEM.getValue() == val) {
			return CALENDAR_ITEM;
		}
		return NEWS_ITEM;
	}
}
