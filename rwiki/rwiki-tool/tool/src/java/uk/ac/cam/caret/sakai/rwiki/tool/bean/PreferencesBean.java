package uk.ac.cam.caret.sakai.rwiki.tool.bean;

public class PreferencesBean
{

	public static final String NOTIFICATION_PREFERENCE_PARAM = "notificationLevel";

	private String notificationLevel;

	public static final String NO_PREFERENCE = "nopreference";

	public String getNotificationLevel()
	{
		return notificationLevel;
	}

	public void setNotifcationLevel(String notificationLevel)
	{
		this.notificationLevel = notificationLevel;
	}

}
