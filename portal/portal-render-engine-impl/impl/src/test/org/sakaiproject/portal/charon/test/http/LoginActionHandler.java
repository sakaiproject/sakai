/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.test.http;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ieb
 */
@Slf4j
public class LoginActionHandler implements ActionHandler
{
	private String userName;

	private String password;

	/**
	 * @param admin_password
	 * @param admin_user
	 */
	public LoginActionHandler(String user, String password)
	{
		this.userName = user;
		this.password = password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.charon.test.http.ActionHandler#post(com.meterware.httpunit.WebConversation,
	 *      com.meterware.httpunit.WebForm)
	 */
	public WebResponse post(WebConversation wc, WebForm f, String action)
			throws IOException, SAXException
	{
		log.info("Performing Form On " + action);
		if ("/portal/relogin".equals(action))
		{
			log.info("Performing Login " + action);
			f.setParameter("eid", userName);
			f.setParameter("pw", password);
			SubmitButton submit = f.getSubmitButton("submit");
			submit.click();
			return wc.getCurrentPage();
		}
		return null;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

}
