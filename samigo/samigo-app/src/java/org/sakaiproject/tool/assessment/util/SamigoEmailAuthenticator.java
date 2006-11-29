/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/ItemService.java $
 * $Id: ItemService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
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

package org.sakaiproject.tool.assessment.util;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.sakaiproject.component.cover.ServerConfigurationService;
/**
 * The ItemService calls persistent service locator to reach the
 * manager on the back end.
 */
public class SamigoEmailAuthenticator extends Authenticator{

	/**
	 * Creates a new SamigoEmailService object.
	 */
	public SamigoEmailAuthenticator() {
		super();
	}
	
	protected PasswordAuthentication getPasswordAuthentication() {
		String username = ServerConfigurationService.getString("samigo.email.username");
		String password = ServerConfigurationService.getString("samigo.email.password");
		PasswordAuthentication passwordAuthentication = new PasswordAuthentication(username, password);
		return passwordAuthentication;
	}
}
