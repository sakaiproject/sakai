/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.calendaring.logic;

/**
 * An interface to abstract all Sakai related API calls. This does not form part of the public API for the ExternalCalendaringService.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface SakaiProxy {

	/**
	 * Get current user id
	 * @return
	 */
	public String getCurrentUserId();
	
	/**
	 * Is the current user a superUser? (anyone in admin realm)
	 * @return
	 */
	public boolean isSuperUser();
	
	/**
	 * Get the name of the server
	 * @return
	 */
	public String getServerName();
	
	/**
	 * Get the configured calendar file path on the server. This is where the ICS files are created. 
	 * Defaults to java.io.tmpdir if not provided
	 * @return
	 */
	public String getCalendarFilePath();
	
	/**
	 * Get the email address for this user
	 * @param uuid
	 * @return
	 */
	public String getUserEmail(String uuid);
	
	/**
	 * Get the display name for this user
	 * @param uuid
	 * @return
	 */
	public String getUserDisplayName(String uuid);
	
	/**
	 * Is the ICS service enabled? If not, all operations will be no-ops. Defaults to true.
	 * @return
	 */
	public boolean isIcsEnabled();
	
	/**
	 * Is cleanup enabled? If so, generated files will be cleaned up (deleted via File.deleteOnExit()) when the JVM exits. 
	 * Once a file has been generated and used it is no longer needed nor used again, so this defaults to true.
	 * @return
	 */
	public boolean isCleanupEnabled();
}
