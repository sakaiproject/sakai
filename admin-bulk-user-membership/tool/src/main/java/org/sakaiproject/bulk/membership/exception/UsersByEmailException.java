/**
 * Copyright (c) 2003-2023 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.bulk.membership.exception;

/**
 * <p>
 * UsersByEmailException is thrown when getting a user by email gets more than one user.
 * </p>
 */
public class UsersByEmailException extends Exception
{
	private String m_email = null;

	public UsersByEmailException(String email)
	{
		m_email = email;
	}

	public String toString()
	{
		return super.toString() + " email=" + m_email;
	}

	public String getMessage()
	{
		return m_email;
	}
}
