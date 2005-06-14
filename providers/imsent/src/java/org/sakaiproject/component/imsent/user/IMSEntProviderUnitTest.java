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
