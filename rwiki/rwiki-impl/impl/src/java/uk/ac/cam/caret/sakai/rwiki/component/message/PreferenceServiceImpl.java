/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.message;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PreferenceDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Preference;

/**
 * @author ieb
 */
public class PreferenceServiceImpl implements PreferenceService
{

	private static Log log = LogFactory.getLog(MessageServiceImpl.class);

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
