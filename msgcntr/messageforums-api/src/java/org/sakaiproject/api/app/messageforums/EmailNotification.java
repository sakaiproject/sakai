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

package org.sakaiproject.api.app.messageforums;

public interface EmailNotification {
	public final static String EMAIL_REPLY_TO_ANY_MESSAGE = "2";
	public final static String EMAIL_REPLY_TO_MY_MESSAGE = "1";
	public final static String EMAIL_NONE = "0";
	

	public Long getId();

	public void setId(Long id);

	public Integer getVersion();

	public void setVersion(Integer version);

	public String getUserId();

	public void setUserId(String userId);

	public String getContextId();

	public void setContextId(String contextId);

	public String getNotificationLevel();

	public void setNotificationLevel(String currlevel);
}
