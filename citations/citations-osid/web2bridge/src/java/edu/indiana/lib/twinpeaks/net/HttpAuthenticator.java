/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.net;

import edu.indiana.lib.twinpeaks.util.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.util.*;


/**
 * Handle network authentication.
 *<p>
 * When we try to access a protected site,
 * <code>getPasswordAuthentication()</code> is called by the network
 * connection code (<code>HttpURLConnection</code> in our case) to obtain
 * any credentials (username and password) required to access the site.
 *<p>
 * Note:
 * This only occurs until we successfully "log in" - after that, we are
 * not invoked again for the site in question.
 */
@Slf4j
public class HttpAuthenticator extends java.net.Authenticator {

	private HashMap	credentialMap = new HashMap();

  /**
   * Establish realm login credentials
   * @param realm Realm for authentication (<code>ADS</code>)
   * @param username User for login
   * @param password Password
   */
  public synchronized void setCredentials(String realm, String username,
  																											String password) {
    credentialMap.put(realm, new Credentials(username, password));
  }

  /**
   * Override the default implementation to provide the credentials required
   * for our network authentication
   */
  protected synchronized PasswordAuthentication getPasswordAuthentication() {
		Credentials 	credential;
		int						attempts;

    log.debug("Authorization requested for \""
            					+ getRequestingPrompt()
            					+ "\", scheme: \""
            					+ getRequestingScheme()
            					+ "\", site: \""
            					+ getRequestingSite()
            					+ "\"");

		credential = (Credentials) credentialMap.get(getRequestingPrompt());
		if (credential == null) {
			log.warn("No credentials configured");
			return null;
		}

    /*
     * If the login has been rejected once, quit now.  This avoids a
     * "redirect loop" with the server.  Reset the counter every few
     * attempts to allow an an occasional authorization "retry".
     */
    attempts = credential.getAuthorizationAttempts() + 1;
    credential.setAuthorizationAttempts(attempts);

    if (attempts > 1) {
      if ((attempts % 3) == 0) {
        credential.setAuthorizationAttempts(0);
      }
      log.warn("Authorization refused");
       return null;
    }
    log.warn("Returning credentials for authorization");
    return new PasswordAuthentication(credential.getUsername(),
    																	credential.getPassword());
  }

	/**
	 * Store authentication information
	 */
  private static class Credentials {
    private String  username;
	  private char[]  password;
	  private int			attempts;

		private Credentials() {
		}

		/**
		 * Constructor
		 * @param username This user
		 * @param password And password
		 */
		public Credentials(String username, String password) {
      this.username = username;
	    this.password = password.toCharArray();
	    this.attempts	= 0;
	  }

		/**
		 * Get this username
		 * @return The username
		 */
	  public String getUsername() {
	  	return username;
	  }

		/**
		 * Get the password for this user
		 * @return The password
		 */
	  public char[] getPassword() {
	  	return password;
	  }

		/**
		 * Set the number of attempted logins for this user
		 * @param value The number of attempted logins
		 */
	  public synchronized void setAuthorizationAttempts(int value) {
	  	attempts = value;
	  }

		/**
		 * Get the number of attempted logins for this user
		 * @return The number of attempted logins
		 */
	  public synchronized int getAuthorizationAttempts() {
	  	return attempts;
	  }
	}
}