package org.sakaiproject.scorm.api;

public interface ScormConstants {

	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = "/scorm";
	
	public static String IS_CONTENT_PACKAGE_PROPERTY = "scorm:is_content_package";
	public static String CONTENT_PACKAGE_TITLE_PROPERTY = "CONTENT_PACKAGE_TITLE";
	
	public static final int CONTENT_PACKAGE_STATUS_UNKNOWN = 0;
	public static final int CONTENT_PACKAGE_STATUS_OPEN = 1;
	public static final int CONTENT_PACKAGE_STATUS_OVERDUE = 2;
	public static final int CONTENT_PACKAGE_STATUS_CLOSED = 3;
	public static final int CONTENT_PACKAGE_STATUS_NOTYETOPEN = 4;
	
	public static final int NOT_ACCESSED = 0;
	public static final int INCOMPLETE = 1;
	public static final int COMPLETED = 2;
	public static final int GRADED = 3;
	
	public final static String CMI_OBJECTIVES_ROOT = "cmi.objectives.";
	public final static String CMI_COMPLETION_STATUS = "cmi.completion_status";
	public final static String CMI_SUCCESS_STATUS = "cmi.success_status";
	public final static String CMI_ENTRY = "cmi.entry";
	public final static String CMI_SCORE_SCALED = "cmi.score.scaled";
	public final static String CMI_SESSION_TIME = "cmi.session_time";
	
	public static final String DEFAULT_USER_AUDIO_LEVEL = "1";
	public static final String DEFAULT_USER_AUDIO_CAPTIONING = "0";
	public static final String DEFAULT_USER_DELIVERY_SPEED = "1";
	public static final String DEFAULT_USER_LANGUAGE = "";
	
	public static final String PREF_USER_AUDIO_LEVEL = "1";
	public static final String PREF_USER_AUDIO_CAPTIONING = "0";
	public static final String PREF_USER_DELIVERY_SPEED = "1";
	public static final String PREF_USER_LANGUAGE = "English";
	
	public static final int VALIDATION_SUCCESS = 0;
	public static final int VALIDATION_NOFILE = 1;
	public static final int VALIDATION_NOMANIFEST = 2;
	public static final int VALIDATION_NOTWELLFORMED = 3;
	public static final int VALIDATION_NOTVALIDROOT = 4;
	public static final int VALIDATION_NOTVALIDSCHEMA = 5;
	public static final int VALIDATION_NOTVALIDPROFILE = 6;
	public static final int VALIDATION_MISSINGREQUIREDFILES = 7;
	public static final int VALIDATION_CONVERTFAILED = -1;
	public static final int VALIDATION_WRONGMIMETYPE = 8;
	
	
}
