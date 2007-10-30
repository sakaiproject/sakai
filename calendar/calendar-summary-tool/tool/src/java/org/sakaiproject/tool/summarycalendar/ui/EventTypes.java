package org.sakaiproject.tool.summarycalendar.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.util.ResourceLoader;


public class EventTypes {

	private static ResourceLoader		msgs					= new ResourceLoader("org.sakaiproject.tool.summarycalendar.bundle.Messages");	

	private static List<String>			eventTypes				= null;
	static{
		eventTypes = new ArrayList<String>();
		eventTypes.add("Academic Calendar");
		eventTypes.add("Activity");
		eventTypes.add("Cancellation");
		eventTypes.add("Class section - Discussion");
		eventTypes.add("Class section - Lab");
		eventTypes.add("Class section - Lecture");
		eventTypes.add("Class section - Small Group");
		eventTypes.add("Class session");
		eventTypes.add("Computer Session");
		eventTypes.add("Deadline");
		eventTypes.add("Exam");
		eventTypes.add("Meeting");
		eventTypes.add("Multidisciplinary Conference");
		eventTypes.add("Quiz");
		eventTypes.add("Special event");
		eventTypes.add("Web Assignment");
	}

	private static Map<String, String>	eventTypesToLocalized	= new HashMap<String, String>();

	
	public EventTypes() {
		EventTypes.reloadLocalization();
	}

	/**
	 * Get localized calendar event type
	 */
	public static String getLocalizedEventType(String eventType) {
		return eventTypesToLocalized.get(eventType);
	}
	
	/**
	 * Get list of calendar event types (not localized)
	 */
	public static List<String> getEventTypes() {
		return eventTypes;
	}

	/**
	 * Get map of calendar event type -> localized calendar event type
	 */
	public static Map<String,String> getEventTypesToLocalizedMap() {
		return eventTypesToLocalized;
	}

	/**
	 * Reload localization for calendar event types
	 */
	public static void reloadLocalization() {
		eventTypesToLocalized = new HashMap<String, String>();
		eventTypesToLocalized.put("Academic Calendar", msgs.getString("del.acad"));
		eventTypesToLocalized.put("Activity", msgs.getString("del.activ"));
		eventTypesToLocalized.put("Cancellation", msgs.getString("del.cancell"));
		eventTypesToLocalized.put("Class section - Discussion", msgs.getString("del.class.disc"));
		eventTypesToLocalized.put("Class section - Lab", msgs.getString("del.class.lab"));
		eventTypesToLocalized.put("Class section - Lecture", msgs.getString("del.class.lect"));
		eventTypesToLocalized.put("Class section - Small Group", msgs.getString("del.class.small"));
		eventTypesToLocalized.put("Class session", msgs.getString("del.classsession"));
		eventTypesToLocalized.put("Computer Session", msgs.getString("del.computer"));
		eventTypesToLocalized.put("Deadline", msgs.getString("del.dead"));
		eventTypesToLocalized.put("Exam", msgs.getString("del.exam"));
		eventTypesToLocalized.put("Meeting", msgs.getString("del.meet"));
		eventTypesToLocalized.put("Multidisciplinary Conference", msgs.getString("del.multi"));
		eventTypesToLocalized.put("Quiz", msgs.getString("del.quiz"));
		eventTypesToLocalized.put("Special event", msgs.getString("del.special"));
		eventTypesToLocalized.put("Web Assignment", msgs.getString("del.web"));
	}

}
