/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import com.sun.security.auth.callback.TextCallbackHandler;

/*
 * JaasTest -- attempts to authenticate a user and reports success or an error message
 * Argument: LoginContext [optional, default is "JaasAuthentication"]
 *	(must exist in "login configuration file" specified in ${java.home}/lib/security/java.security)
 *
 * Seth Theriault (slt@columbia.edu)
 * Academic Information Systems, Columbia University
 *  (based on code from various contributors)
 *
 */
public class JaasTest {

    public static void main(String[] args) {

	String testcontext = "";

	if (null == args || args.length != 1) {

		testcontext = "JaasAuthentication";
   
	} else testcontext = args[0];

	System.out.println("\nLoginContext for testing: " + testcontext);
	System.out.println("Enter a username and password to test this LoginContext.\n");

	LoginContext lc = null;
	try {

		lc = new LoginContext(testcontext, new TextCallbackHandler());

	} catch (LoginException le) {
            
		System.err.println("Cannot create LoginContext. " + le.getMessage());
		System.exit(-1);

	} catch (SecurityException se) {
		System.err.println("Cannot create LoginContext. " + se.getMessage());
		System.exit(-1);
	} 

        try {

            // attempt authentication
            lc.login();
            lc.logout();

        } catch (LoginException le) {

		System.err.println("\nAuthentication FAILED.");
		System.err.println("Error message:\n --> " + le.getMessage());
		System.exit(-1);
        }
		System.out.println("Authentication SUCCEEDED.");
    }
}


