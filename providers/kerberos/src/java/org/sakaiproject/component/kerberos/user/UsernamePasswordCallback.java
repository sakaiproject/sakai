/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
 ***************************************************************/
package org.sakaiproject.component.kerberos.user;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Simple callback handler that supplies a username and password.
 * 
 * @author Matthew Buckett
 */
public class UsernamePasswordCallback implements CallbackHandler {

	private final String username;
	private final String password;

	public UsernamePasswordCallback(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (Callback callback : callbacks) {
			if (callback instanceof NameCallback) {
				NameCallback nameCallback = (NameCallback) callback;
				nameCallback.setName(username);
			} else if (callback instanceof PasswordCallback) {
				PasswordCallback passwordCallback = (PasswordCallback) callback;
				passwordCallback.setPassword(password.toCharArray());
			} else {
				throw new UnsupportedCallbackException(callback,
						"Only username and password supported.");
			}
		}

	}

}
