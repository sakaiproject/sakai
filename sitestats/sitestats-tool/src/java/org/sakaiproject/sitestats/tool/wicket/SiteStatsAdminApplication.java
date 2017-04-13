/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket;

import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.settings.IExceptionSettings;
import org.sakaiproject.sitestats.tool.wicket.pages.AdminPage;


public class SiteStatsAdminApplication extends SiteStatsApplication {
	
	protected void init() {
		super.init();

		// Home page
		mountPage("/admin", AdminPage.class);

		// On wicket session timeout or wicket exception, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(AdminPage.class);
		getApplicationSettings().setAccessDeniedPage(AdminPage.class);
		getApplicationSettings().setInternalErrorPage(AdminPage.class);

		// show internal error page rather than default developer page
		getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);

		// Encrypt URLs. This immediately sets up a session (note that things like CSS now becomes bound to the session)
		getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory()); // Different key per user
		final IRequestMapper cryptoMapper = new CryptoMapper(getRootRequestMapper(), this); 
		setRootRequestMapper(cryptoMapper);
	}

	@SuppressWarnings("unchecked")
	public Class getHomePage() {
		return AdminPage.class;
	}
}
