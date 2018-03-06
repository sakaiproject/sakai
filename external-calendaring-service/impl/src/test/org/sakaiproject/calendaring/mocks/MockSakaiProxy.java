/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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

package org.sakaiproject.calendaring.mocks;

import org.sakaiproject.calendaring.logic.SakaiProxy;

/**
 * Mock of SakaiProxy so we can call the main service API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class MockSakaiProxy implements SakaiProxy {

	public static final String NO_EMAIL_ID = "noEmailPlease";

	@Override
	public String getCurrentUserId() {
		return "abc123";
	}

	@Override
	public boolean isSuperUser() {
		return false;
	}

	@Override
	public String getServerName() {
		return "server_xyz";
	}

	@Override
	public String getCalendarFilePath() {
		return System.getProperty("java.io.tmpdir");
	}

	@Override
	public String getUserEmail(String uuid) {
		return uuid.equals(NO_EMAIL_ID) ? "" : uuid + "@email.com";
	}

	@Override
	public String getUserDisplayName(String uuid) {
		return "User " + uuid;
	}

	@Override
	public boolean isIcsEnabled() {
		return true;
	}

	@Override
	public boolean isCleanupEnabled() {
		return true;
	}

}
