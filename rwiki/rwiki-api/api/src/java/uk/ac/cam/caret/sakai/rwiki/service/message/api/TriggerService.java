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

package uk.ac.cam.caret.sakai.rwiki.service.message.api;

import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Trigger;

/**
 * @author ieb
 */
public interface TriggerService
{
	/**
	 * Fire triggers based on space
	 * 
	 * @param space
	 */
	void fireSpaceTriggers(String space);

	/**
	 * Fire Triggers on page
	 * 
	 * @param space
	 * @param page
	 */
	void firePageTriggers(String space, String page);

	/**
	 * add a new Trigger
	 * 
	 * @param user
	 * @param space
	 * @param page
	 * @param spec
	 */
	void addTrigger(String user, String space, String page, String spec);

	/**
	 * remove a Trigger
	 * 
	 * @param trigger
	 */
	void removeTrigger(Trigger trigger);

	/**
	 * Update a Trigger
	 * 
	 * @param trigger
	 */
	void updateTrigger(Trigger trigger);

	/**
	 * Get a list of triggers for this user on this page
	 * 
	 * @param user
	 * @param space
	 * @param page
	 * @return
	 */
	List getUserTriggers(String user, String space, String page);

	/**
	 * Get a list of triggers for this user in the space
	 * 
	 * @param user
	 * @param space
	 * @return
	 */
	List getUserTriggers(String user, String space);

	/**
	 * get all triggers for this user
	 * 
	 * @param user
	 * @return
	 */
	List getUserTriggers(String user);

	/**
	 * get all triggers for the page
	 * 
	 * @param space
	 * @param page
	 * @return
	 */
	List getPageTriggers(String space, String page);

	/**
	 * Get a list of all triggers for the space
	 * 
	 * @param space
	 * @return
	 */
	List getSpaceTriggers(String space);

}
