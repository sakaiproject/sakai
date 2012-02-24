/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.authz.api;

/**
 * <p>
 * SecurityAdvisor is a stackable policy process that is given a chance to determine if a security
 * question can be answered,over the logic of the SecurityService component. The advisors at the 
 * top of the stack are consulted first (added last).
 * </p>
 * @see SecurityService#pushAdvisor(SecurityAdvisor)
 * @see SecurityService#popAdvisor(SecurityAdvisor)
 * @see SecurityService#hasAdvisors()
 */
public interface SecurityAdvisor
{
	/**
	 * <p>
	 * SecurityAdvice enumerates different SecurityAdvisor results.
	 * </p>
	 */
	public class SecurityAdvice
	{
		private final String m_id;

		private SecurityAdvice(String id)
		{
			m_id = id;
		}

		public String toString()
		{
			return m_id;
		}

		/** Security result that indicates the end user is allowed the function. */
		public static final SecurityAdvice ALLOWED = new SecurityAdvice("allowed");

		/** Security result that indicates the end user is NOT allowed the function. */
		public static final SecurityAdvice NOT_ALLOWED = new SecurityAdvice("not allowed");

		/** Security result that indicates the SecurityAdvisor cannot answer the question. */
		public static final SecurityAdvice PASS = new SecurityAdvice("pass");
	}

	/**
	 * Can the current session user perform the requested function on the referenced Entity?
	 * 
	 * @param userId
	 *        The user id.
	 * @param function
	 *        The lock id string.
	 * @param reference
	 *        The resource reference string.
	 * @return ALLOWED or NOT_ALLOWED if the advisor can answer that the user can or cannot, or PASS if the advisor cannot answer.
	 */
	SecurityAdvice isAllowed(String userId, String function, String reference);
}