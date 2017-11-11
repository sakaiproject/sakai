/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business.exception;

/**
 * An exception that methods can throw to indicate something went wrong. The message will give more detail. TODO clean this up, make it
 * checked
 */
public class GbException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GbException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GbException(final Throwable cause) {
		super(cause);
	}

	public GbException(final String message) {
		super(message);
	}

	public GbException() {
		super();
	}

}
