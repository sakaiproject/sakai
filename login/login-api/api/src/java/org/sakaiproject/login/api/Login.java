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
package org.sakaiproject.login.api;

public interface Login {

	/**
	 * The default login name if none is specified.
	 */
	public static final String DEFAULT_LOGIN_CONTEXT = "default";
	
	public static final String EXCEPTION_INVALID_CREDENTIALS = "invalid-credentials";
	
	public static final String EXCEPTION_MISSING_CREDENTIALS = "missing-credentials";
	
	public static final String EXCEPTION_INVALID_WITH_PENALTY = "invalid-credentials-with-penalty";
	
	public static final String EXCEPTION_INVALID = "invalid";
	
	public static final String EXCEPTION_DISABLED = "disabled";
	
}
