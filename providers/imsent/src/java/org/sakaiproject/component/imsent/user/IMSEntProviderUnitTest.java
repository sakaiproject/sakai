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

/*
 * Created on May 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.sakaiproject.component.imsent.user;

import org.sakaiproject.service.legacy.coursemanagement.CourseManagementProvider;
import org.sakaiproject.service.legacy.realm.RealmProvider;
import org.sakaiproject.service.legacy.user.UserDirectoryProvider;

public class IMSEntProviderUnitTest {

	/**********************************************************************************************************************************************************************************************************************************************************
	 * In-House Unit Check
	 *********************************************************************************************************************************************************************************************************************************************************/

	// TODO: Make these real unit tests
	
	private static boolean m_passUnitTest = true;
	
	private static void passFail(boolean testVal, String message)
	{
		if (testVal)
		{
			System.out.println("Passed unit Test"+message);
		}
		else
		{
			System.out.println("Failed Unit Test:"+message);
			m_passUnitTest = false;			
		}
	}

	public static boolean localUnitTests(UserDirectoryProvider udp, RealmProvider rp, 
			CourseManagementProvider cp)
	{
		m_passUnitTest = true;  // Assume we pass
		if ( udp != null ) 
		{
			passFail( ! udp.userExists("user1"), "Checking existing user retrieval (user1)");
			passFail ( udp.userExists("user7"), "Non existent user test (user7)");
		}
		if ( rp != null )
		{
			System.out.println("Testing RP");
		}
		if ( cp != null )
		{
			System.out.println("Testing CP");
		}

		return m_passUnitTest;
		
	}
}
