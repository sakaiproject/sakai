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

package uk.ac.cam.caret.sakai.rwiki.component.message;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PreferenceDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Preference;

/**
 * @author ieb
 */
@Slf4j
public class PreferenceServiceImpl implements PreferenceService
{
	private PreferenceDao preferenceDao = null;

	/**
	 * {@inheritDoc}
	 */
	public void updatePreference(String user, String context, String type,
			String preference)
	{
		Preference pref = preferenceDao.findExactByUser(user, context, type);
		if (pref != null)
		{
			pref.setPreference(preference);
			pref.setLastseen(new Date());
			preferenceDao.update(pref);
		}
		else
		{
			pref = preferenceDao.createPreference(user, context, type,
					preference);
			preferenceDao.update(pref);
		}
	}

	public String findPreferenceAt(String user, String context, String type)
	{
		String baseSearch = "/";
		List l = preferenceDao.findByUser(user, baseSearch, type);
		Preference selected = null;
		for (Iterator i = l.iterator(); i.hasNext();)
		{
			Preference p = (Preference) i.next();

			if (context.startsWith(p.getPrefcontext()))
			{

				if (selected == null
						|| p.getPrefcontext().length() > selected
								.getPrefcontext().length())
				{
					selected = p;
				}
			}
		}
		if (selected == null) return null;
		return selected.getPreference();
	}

	/**
	 * @return Returns the preferenceDao.
	 */
	public PreferenceDao getPreferenceDao()
	{
		return preferenceDao;
	}

	/**
	 * @param preferenceDao
	 *        The preferenceDao to set.
	 */
	public void setPreferenceDao(PreferenceDao preferenceDao)
	{
		this.preferenceDao = preferenceDao;
	}

	public void deleteAllPreferences(String user, String context, String type)
	{
		preferenceDao.delete(user, context, type);
	}

	public void deletePreference(String user, String context, String type)
	{
		preferenceDao.deleteExact(user, context, type);
	}

}
