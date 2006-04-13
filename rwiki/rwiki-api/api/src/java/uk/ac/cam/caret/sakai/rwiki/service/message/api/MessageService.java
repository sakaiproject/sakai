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

/**
 * @author ieb
 */
public interface MessageService
{

	/**
	 * Updates the session presence record in the wiki
	 * 
	 * @param session
	 * @param user
	 * @param page
	 * @param space
	 */
	void updatePresence(String session, String user, String page, String space);

	void addMessage(String session, String user, String page, String space,
			String message);

	/**
	 * returns a List of the Messages associated with the session
	 * 
	 * @param session
	 * @return
	 */
	List getSessionMessages(String session);

	/**
	 * Returns List of the Messages in the space
	 * 
	 * @param space
	 * @return
	 */
	List getMessagesInSpace(String space);

	/**
	 * Returns List of Messages in the page
	 * 
	 * @param space
	 * @param page
	 * @return
	 */
	List getMessagesInPage(String space, String page);

	/**
	 * Returns List representation of the users in the space
	 * 
	 * @param space
	 * @return
	 */
	List getUsersInSpace(String space);

	/**
	 * Returns List representation of the users on the page
	 * 
	 * @param space
	 * @param page
	 * @return
	 */
	List getUsersOnPage(String space, String page);

	/**
	 * @param pageSpace
	 * @param pageName
	 * @return
	 */
	List getUsersInSpaceOnly(String pageSpace, String pageName);

}
