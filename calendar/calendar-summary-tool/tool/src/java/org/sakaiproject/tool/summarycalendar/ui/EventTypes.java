/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

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

	private static ResourceLoader		msgs					= new ResourceLoader("calendar");	

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
		eventTypesToLocalized.put("Academic Calendar", msgs.getString("legend.key1"));
		eventTypesToLocalized.put("Activity", msgs.getString("legend.key2"));
		eventTypesToLocalized.put("Cancellation", msgs.getString("legend.key3"));
		eventTypesToLocalized.put("Class section - Discussion", msgs.getString("legend.key4"));
		eventTypesToLocalized.put("Class section - Lab", msgs.getString("legend.key5"));
		eventTypesToLocalized.put("Class section - Lecture", msgs.getString("legend.key6"));
		eventTypesToLocalized.put("Class section - Small Group", msgs.getString("legend.key7"));
		eventTypesToLocalized.put("Class session", msgs.getString("legend.key8"));
		eventTypesToLocalized.put("Computer Session", msgs.getString("legend.key9"));
		eventTypesToLocalized.put("Deadline", msgs.getString("legend.key10"));
		eventTypesToLocalized.put("Exam", msgs.getString("legend.key11"));
		eventTypesToLocalized.put("Meeting", msgs.getString("legend.key12"));
		eventTypesToLocalized.put("Multidisciplinary Conference", msgs.getString("legend.key13"));
		eventTypesToLocalized.put("Quiz", msgs.getString("legend.key14"));
		eventTypesToLocalized.put("Special event", msgs.getString("legend.key15"));
		eventTypesToLocalized.put("Web Assignment", msgs.getString("legend.key16"));
	}

}
