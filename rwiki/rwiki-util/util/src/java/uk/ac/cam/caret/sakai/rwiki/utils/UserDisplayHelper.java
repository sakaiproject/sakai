package uk.ac.cam.caret.sakai.rwiki.utils;

import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

public class UserDisplayHelper
{

	public static String formatDisplayName(String name)
	{
		// FIXME internationalize
		return formatDisplayName(name, "Unknown");
	}

	public static String formatDisplayName(String name, String defaultName)
	{
		User user;
		try
		{
			user = UserDirectoryService.getUser(name);
		}
		catch (UserNotDefinedException e)
		{
			return defaultName + " (" + XmlEscaper.xmlEscape(name) + ")";
		}
		return XmlEscaper.xmlEscape(user.getDisplayName() + " (" + name + ")");
	}
}
