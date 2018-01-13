/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Sakai Foundation.
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

import java.security.PrivilegedAction;

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;

import com.sun.security.auth.callback.TextCallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/*
 * JaasTestVerify -- attempts to authenticate a user and reports success or an error message
 * Argument: LoginContext [optional, default is "JaasAuthentication"]
 *	(must exist in "login configuration file" specified in ${java.home}/lib/security/java.security)
 *
 * Seth Theriault (slt@columbia.edu)
 * Academic Information Systems, Columbia University
 *  (based on code from various contributors)
 *
 */
@Slf4j
public class JaasTestVerify {

	private static byte[] tokens;
	private GSSContext clientContext;
	private byte[] serviceTickets;
	private GSSContext serverContext;
	
	// Make sure you have an @ in this value, when it get used Java replaces it with a /.
	private String servicePrincipal = "sakai-test@bit.oucs.ox.ac.uk";

	private class UserAction implements PrivilegedAction<Object> {
		public Object run() {
			try {
				tokens = clientContext.initSecContext(serviceTickets, 0, serviceTickets.length);
			} catch (GSSException e) {
				throw new RuntimeException(e);
			}
			return null;
		}
	}
	
	private class ServerAction implements PrivilegedAction<byte[]> {
		public byte[] run() {
			try {
				serviceTickets = serverContext.acceptSecContext(tokens, 0, tokens.length);
			} catch (GSSException e) {
				throw new RuntimeException(e);
			}
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		new JaasTestVerify().run();
	}
	
	public void run() throws Exception {
		LoginContext userContext = null;
		try {

			userContext = new LoginContext("KerberosAuthentication", new TextCallbackHandler());
			userContext.login();

		} catch (LoginException le) {
			log.error(le.getMessage(), le);
		}
		LoginContext serverLoginContext = null;
		try {

			serverLoginContext = new LoginContext("ServiceKerberosAuthentication", new TextCallbackHandler());
			serverLoginContext.login();

		} catch (LoginException le) {
			log.error(le.getMessage(), le);
		}
		
		GSSManager manager = GSSManager.getInstance();
		Oid kerberos = new Oid("1.2.840.113554.1.2.2");

		GSSName serverName = manager.createName(
				servicePrincipal, GSSName.NT_HOSTBASED_SERVICE);
		
		

		clientContext = manager.createContext(
				serverName, kerberos, null,
				GSSContext.DEFAULT_LIFETIME);

		serverContext = manager.createContext((GSSCredential)null);
		serviceTickets = new byte[0];
		tokens = null;
		int exchanges = 0;
		while (!clientContext.isEstablished() && !serverContext.isEstablished() && !(tokens == null && serviceTickets == null)) {
			Subject.doAs(userContext.getSubject(), new UserAction());
			Subject.doAs(serverLoginContext.getSubject(), new ServerAction());
			log.info("Ticket exchanged.");
			if (++exchanges > 50) {
				throw new RuntimeException("Too many tickets exchanged.");
			}
		}
		clientContext.dispose();
		serverContext.dispose();
		
		userContext.logout();
		serverLoginContext.logout();

		log.info("Completed.");

	}
}
