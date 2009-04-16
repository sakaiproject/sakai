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

package uk.ac.cam.caret.sakai.rwiki.service.message.api.model;

import java.util.Date;

/**
 * @author ieb
 */
public interface Message
{

	/**
	 * @return Returns the id.
	 */
	String getId();

	/**
	 * @param id
	 *        The id to set.
	 */
	void setId(String id);

	/**
	 * @return Returns the lastseen.
	 */
	Date getLastseen();

	/**
	 * @param lastseen
	 *        The lastseen to set.
	 */
	void setLastseen(Date lastseen);

	/**
	 * @return Returns the message.
	 */
	String getMessage();

	/**
	 * @param message
	 *        The message to set.
	 */
	void setMessage(String message);

	/**
	 * @return Returns the pagename.
	 */
	String getPagename();

	/**
	 * @param pagename
	 *        The pagename to set.
	 */
	void setPagename(String pagename);

	/**
	 * @return Returns the pagespace.
	 */
	String getPagespace();

	/**
	 * @param pagespace
	 *        The pagespace to set.
	 */
	void setPagespace(String pagespace);

	/**
	 * @return Returns the sessionid.
	 */
	String getSessionid();

	/**
	 * @param sessionid
	 *        The sessionid to set.
	 */
	void setSessionid(String sessionid);

	/**
	 * @return Returns the user.
	 */
	String getUser();

	/**
	 * @param user
	 *        The user to set.
	 */
	void setUser(String user);

}