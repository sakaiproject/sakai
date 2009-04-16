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

package uk.ac.cam.caret.sakai.rwiki.service.message.api.dao;

import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Preference;

/**
 * @author ieb
 */
public interface PreferenceDao
{
	Preference createPreference(String user, String context, String type,
			String preference);

	List findByUser(String user);

	List findExactByUser(String user, String context);

	Preference findExactByUser(String user, String context, String type);

	List findByUser(String user, String context);

	List findByUser(String user, String context, String type);

	void update(Object o);

	int delete(String user, String context, String type);

	int deleteExact(String user, String context, String type);
}
