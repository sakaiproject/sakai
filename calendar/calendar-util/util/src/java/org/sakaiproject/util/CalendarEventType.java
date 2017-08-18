package org.sakaiproject.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public enum CalendarEventType {

	ACADEMIC_CALENDAR("Academic Calendar","icon-calendar-academic-calendar","legend.key1", "event.academic.calendar"),
	ACTIVITY("Activity","icon-calendar-activity","legend.key2", "event.activity"),
	CANCELLATION("Cancellation","icon-calendar-cancellation","legend.key3", "event.cancellation"),
	CLASS_SECTION_DISCUSSION("Class section - Discussion","icon-calendar-class-section-discussion","legend.key4", "event.discussion"),
	CLASS_SECTION_LAB("Class section - Lab","icon-calendar-class-section-lab","legend.key5", "event.lab"),
	CLASS_SECTION_LECTURE("Class section - Lecture","icon-calendar-class-section-lecture","legend.key6", "event.lecture"),
	CLASS_SECTION_SMALL_GROUP("Class section - Small Group","icon-calendar-class-section-small-group","legend.key7", "event.smallgroup"),
	CLASS_SESSION("Class session","icon-calendar-class-session","legend.key8", "event.class"),
	COMPUTER_SESSION("Computer Session","icon-calendar-computer-session","legend.key9", "event.computer"),
	DEADLINE("Deadline","icon-calendar-deadline","legend.key10", "event.deadline"),
	EXAM("Exam","icon-calendar-exam","legend.key11", "event.exam"),
	FORMATIVE_ASSESSMENT("Formative Assessment","icon-calendar-formative-assessment","legend.key17", "event.formative"),
	MEETING("Meeting","icon-calendar-meeting","legend.key12", "event.meeting"),
	MULTIDISPLINARY_CONFERENCE("Multidisciplinary Conference","icon-calendar-multidisciplinary-conference","legend.key13", "event.conference"),
	QUIZ("Quiz","icon-calendar-quiz","legend.key14", "event.quiz"),
	SPECIAL_EVENT("Special event","icon-calendar-special-event","legend.key15", "event.special"),
	SUBMISSION_DATE("Submission Date","icon-calendar-submission-date","legend.key18", "event.submission"),
	TUTORIAL("Tutorial","icon-calendar-tutorial","legend.key19", "event.tutorial"),
	WEB_ASSIGNMENT("Web Assignment","icon-calendar-web-assignment","legend.key16", "event.assignment"),
	WORKSHOP("Workshop","icon-calendar-workshop","legend.key20", "event.workshop");

	private String eventType;
	private String icon;
	private String localizedLegend;
	private String importType;
	
	CalendarEventType(String type, String icon, String localizedLegend, String importType) {
		this.eventType = type;
		this.icon = icon;
		this.localizedLegend = localizedLegend;
		this.importType = importType;
	}
	
	public String getType() {
		return this.eventType;
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public String getImportType() { 
		return this.importType;
	}

	public String getLocalizedLegend() {
		return this.localizedLegend;
	}
	
	public static List getTypes() {
		List<String> eventTypes = new ArrayList<>();
		for (CalendarEventType et: CalendarEventType.values()) {
			eventTypes.add(et.getType());
		}
		return eventTypes;
	}
	
	public static Map<String, String> getIcons() {
		Map<String, String> icons = new HashMap<>();
		for (CalendarEventType et: CalendarEventType.values()) {
			icons.put(et.getType(), et.getIcon());
		}
		return icons;
	}
	
	// Users can change their own language preference so actual values are looked up later
	public static Map<String, String> getLocalizedLegends() {
		Map<String, String> localizedLegends = new HashMap<>();
		for (CalendarEventType et: CalendarEventType.values()) {
			localizedLegends.put(et.getType(), et.getLocalizedLegend());
		}
		return localizedLegends;
	}
	
	public static String getLocalizedLegendFromEventType(String eventType) {
		return getLocalizedLegends().get(eventType);
	}

	public static String getEventTypeFromImportType(String importType) {
		for (CalendarEventType et: CalendarEventType.values()) {
			if (et.getImportType().equals(importType)) {
				return et.getType();
			}
		}
		return ACTIVITY.getType();
	}
}