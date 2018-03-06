/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

package org.sakaiproject.service.gradebook.shared;

/**
 * Wrapper for the common security exception that can be thrown from the gradebook when a user doesn't have permission to perform an action
 */
public class GradebookSecurityException extends SecurityException {

	private static final long serialVersionUID = 1L;
	private static final String MSG = "You do not have permission to perform this operation";

	/**
	 * Throw with a default message
	 */
	public GradebookSecurityException() {
		super(MSG);
    }

	/**
	 * Throw with the supplied message
	 * 
	 * @param message
	 */
	public GradebookSecurityException(final String message) {
		super(message);
	}
}



