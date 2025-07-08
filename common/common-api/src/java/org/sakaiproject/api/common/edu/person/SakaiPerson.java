/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.api.common.edu.person;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @author <a href="mailto:s.swinsburg@lancaster.ac.uk">Steve Swinsburg</a>
 */
import java.util.Date;

public interface SakaiPerson extends EduPerson
{
	
	public static final String PROFILE_SAVE_PERMISSION = "profile.save";
	
	/**
	 * Other information provided
	 * 
	 * @return
	 */
	public String getNotes();

	/**
	 * Other information provided
	 * 
	 * @param notes :
	 *        set other information provided
	 */
	public void setNotes(String notes);

	/**
	 * Set the lock status
	 */
	public void setLocked(Boolean locked);
	
	/**
	 *  Get the locked status
	 * @return
	 */
	public Boolean getLocked();
	
	/**
	 * Set phonetic pronunciation
	 * @param phoneticPronunciation
	 */
	public void setPhoneticPronunciation(String phoneticPronunciation);

	/**
	 * Get phonetic pronunciation
	 * @return
	 */
	public String getPhoneticPronunciation();

	/**
	 * Set the pronouns for this person
	 *
	 * @param The free form string of pronouns
	 */
	public void setPronouns(String pronouns);

	/**
	 * Returns the preferred pronouns for this person
	 *
	 * @return The preferred pronouns in a free form string
	 */
	public String getPronouns();
}
