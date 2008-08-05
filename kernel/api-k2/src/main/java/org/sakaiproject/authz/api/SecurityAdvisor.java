/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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

package org.sakaiproject.authz.api;

import org.sakaiproject.entity.api.Entity;

/**
 * <p>
 * SecurityAdvisor is a stackable policy process that is given a chance to
 * determine if a security question can be answered, over the logic of the
 * SecurityService component.
 * </p>
 */
public interface SecurityAdvisor {
	/**
	 * <p>
	 * SecurityAdvice enumerates different SecurityAdvisor results.
	 * </p>
	 */
	public static enum SecurityAdvice {
		/** Security result that indicates the end user is allowed the function. */
		ALLOWED("allowed"),

		/**
		 * Security result that indicates the end user is NOT allowed the
		 * function.
		 */
		NOT_ALLOWED("not allowed"),

		/**
		 * Security result that indicates the SecurityAdvisor cannot answer the
		 * question.
		 */
		PASS("pass");

		private final String advice;

		private SecurityAdvice(String advice) {
			this.advice = advice;
		}

		public String toString() {
			return advice;
		}

	}

	/**
	 * Can the current session user perform the requested function on the
	 * referenced Entity?
	 * 
	 * @param userId
	 *            The user id.
	 * @param function
	 *            The lock id string.
	 * @param reference
	 *            The resource reference string.
	 * @return ALLOWED or NOT_ALLOWED if the advisor can answer that the user
	 *         can or cannot, or PASS if the advisor cannot answer.
	 */
	SecurityAdvice isAllowed(String userId, Entity.Permission function, String reference);
}