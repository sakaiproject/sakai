/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

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
