/**
 * Copyright (c) 2005-2010 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.qti.exception;

public class RespondusMatchingException extends RuntimeException {
	/**
	 * Creates a new Iso8601FormatException object.
	 *
	 * @param message DOCUMENTATION PENDING
	 */
	public RespondusMatchingException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new Iso8601FormatException object.
	 *
	 * @param message DOCUMENTATION PENDING
	 * @param cause DOCUMENTATION PENDING
	 */
	public RespondusMatchingException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Creates a new Iso8601FormatException object.
	 *
	 * @param cause DOCUMENTATION PENDING
	 */
	public RespondusMatchingException(Throwable cause)
	{
		super(cause);
	}
}
