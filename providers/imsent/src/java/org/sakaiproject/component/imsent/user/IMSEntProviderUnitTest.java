/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.component.imsent.user;

import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.user.api.UserDirectoryProvider;

public class IMSEntProviderUnitTest
{

	/**********************************************************************************************************************************************************************************************************************************************************
	 * In-House Unit Check
	 *********************************************************************************************************************************************************************************************************************************************************/

	// TODO: Make these real unit tests
	private static boolean m_passUnitTest = true;

	private static void passFail(boolean testVal, String message)
	{
		if (testVal)
		{
			System.out.println("Passed unit Test" + message);
		}
		else
		{
			System.out.println("Failed Unit Test:" + message);
			m_passUnitTest = false;
		}
	}

	public static boolean localUnitTests(UserDirectoryProvider udp, GroupProvider rp)
	{
		m_passUnitTest = true; // Assume we pass
		if (udp != null)
		{
			passFail(!udp.userExists("user1"), "Checking existing user retrieval (user1)");
			passFail(udp.userExists("user7"), "Non existent user test (user7)");
		}
		if (rp != null)
		{
			System.out.println("Testing RP");
		}

		return m_passUnitTest;
	}
}
