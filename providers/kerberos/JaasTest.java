/***********************************************************************************
 *
 * $Header: $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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


