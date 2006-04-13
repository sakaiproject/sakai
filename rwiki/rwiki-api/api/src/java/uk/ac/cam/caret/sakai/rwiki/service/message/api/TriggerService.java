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
