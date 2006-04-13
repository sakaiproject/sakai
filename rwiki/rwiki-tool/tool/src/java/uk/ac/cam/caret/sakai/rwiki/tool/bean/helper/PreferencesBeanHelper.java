/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PreferencesBean;

/**
 * @author andrew
 */
public class PreferencesBeanHelper
{

	public static PreferencesBean createPreferencesBean(String currentUser,
			String pageSpace, PreferenceService preferenceService)
	{

		String preferences = preferenceService.findPreferenceAt(currentUser,
				pageSpace, PreferenceService.MAIL_NOTIFCIATION);

		PreferencesBean pb = new PreferencesBean();

		if (preferences == null || "".equals(preferences))
		{
			preferences = PreferencesBean.NO_PREFERENCE;
		}

		pb.setNotifcationLevel(preferences);

		return pb;
	}

}
