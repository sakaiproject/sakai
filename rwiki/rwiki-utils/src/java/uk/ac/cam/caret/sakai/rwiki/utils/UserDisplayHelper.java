package uk.ac.cam.caret.sakai.rwiki.utils;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;

public class UserDisplayHelper {

	public static String formatDisplayName(String name) {
		//FIXME internationalize
		return formatDisplayName(name, "Unknown");
	}
	
	public static String formatDisplayName(String name, String defaultName) {
		User user;
		try {
			user = UserDirectoryService.getUser(name);
		} catch (IdUnusedException e) {
			return defaultName + " ("+XmlEscaper.xmlEscape(name)+")";
		}
		return XmlEscaper.xmlEscape(user.getDisplayName() + " ("+ name + ")");
	}
}
