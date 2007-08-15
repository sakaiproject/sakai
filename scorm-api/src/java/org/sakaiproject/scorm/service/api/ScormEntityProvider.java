package org.sakaiproject.scorm.service.api;

public interface ScormEntityProvider {
	public static String ENTITY_PREFIX = "scorm";
	
	public static String LAUNCH_ID = "launch";
	public static String NAVIGATE_ID = "navigate";
	public static String CONTENT_ID = "content";
	public static String RESOURCE_ID = "resource";
	
	public static String[] VALID_IDS = { LAUNCH_ID, NAVIGATE_ID, CONTENT_ID, RESOURCE_ID };
}
