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

import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import lombok.extern.slf4j.Slf4j;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/**
 * Attempts to authenticate a user and reports success or an error message
 *
 * @author Matthew Buckett
 *
 */
@Slf4j
public class JassAuthenticate {

	private GSSContext clientContext;
	private GSSContext serverContext;
	
	private byte[] acceptTokens = new byte[0];
	private byte[] initTokens = new byte[0];
	
	private String serverGSS;
	private int exchangeLimit = 50;

	private String servicePrincipal;
	private String userPrincipal;
	
	private boolean verifyServiceTicket = false;
	
	/**
	 * Get ready for JAAS authentication, but don't verify a service ticket.
	 */
	public JassAuthenticate(String userPrincipal) {
		this.userPrincipal = userPrincipal;
		verifyServiceTicket = false;
	}

	/**
	 * Get ready for JAAS authentication but attempt todo service ticket verification.
	 */
	public JassAuthenticate(String serverGSS, String servicePrincipal, String userPrincipal) {
		this.serverGSS = serverGSS;
		this.servicePrincipal = servicePrincipal;
		this.userPrincipal = userPrincipal;
		verifyServiceTicket = true;
	}
	
	private class InitiatorAction implements PrivilegedAction<Void> {
		public Void run() {
			try {
				initTokens = clientContext.initSecContext(acceptTokens, 0, acceptTokens.length);
			} catch (GSSException e) {
				throw new RuntimeException("Failed to initiate.", e);
			}
			return null;
		}
	}
	
	private class AcceptorAction implements PrivilegedAction<Void> {
		public Void run() {
			try {
				acceptTokens = serverContext.acceptSecContext(initTokens, 0, initTokens.length);
			} catch (GSSException e) {
				throw new RuntimeException("Failed to accept.", e);
			}
			return null;
		}
	}

	public boolean attemptAuthentication(String username, String password) {
		LoginContext userLoginContext = null;
		LoginContext serverLoginContext = null;

		try {
			// This may well fail so run catch exceptions here.
			try {
				userLoginContext = new LoginContext(userPrincipal, new UsernamePasswordCallback(username, password));
				userLoginContext.login();
			} catch (LoginException le) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to authenticate "+ username, le);
				}
				return false;
			}
			if(!verifyServiceTicket) {
				log.debug("Authenticated ok and not attempting service ticket verification");
				return true;
			}
			// Shouldn't ever fail
			serverLoginContext = new LoginContext(servicePrincipal, new NullCallbackHandler());
			serverLoginContext.login();

			GSSManager manager = GSSManager.getInstance();
			Oid kerberos = new Oid("1.2.840.113554.1.2.2");

			GSSName serverName = manager.createName(
					serverGSS, GSSName.NT_HOSTBASED_SERVICE);

			clientContext = manager.createContext(
					serverName, kerberos, null,
					GSSContext.DEFAULT_LIFETIME);

			serverContext = manager.createContext((GSSCredential)null);

			int exchanges = 0;
			while (!clientContext.isEstablished() && !serverContext.isEstablished() && !(initTokens == null && acceptTokens == null)) {
				Subject.doAs(userLoginContext.getSubject(), new InitiatorAction());
				Subject.doAs(serverLoginContext.getSubject(), new AcceptorAction());
				if (++exchanges > exchangeLimit) {
					throw new RuntimeException("Too many tickets exchanged ("+ exchangeLimit+ ").");
				}
			}
			log.debug("Authenticated ok and verified service ticket");
			return true;
		} catch (GSSException gsse) {
			log.warn("Failed to verify ticket.", gsse);
		} catch (LoginException le) {
			log.warn("Failed to login with keytab.", le);
		} finally {
			try {
			if (clientContext != null) 
				clientContext.dispose();
			if (serverContext != null)
				serverContext.dispose();

			if (userLoginContext != null)
				userLoginContext.logout();
			if (serverLoginContext!= null)
				serverLoginContext.logout();
			} catch (Exception e) {
				log.error("Failed to tidy up after attempting authentication.", e);
			}
		}
		return false;
	}
	
	public boolean isVerifyServiceTicket() {
		return verifyServiceTicket;
	}
}
