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
package org.sakaiproject.portal.charon.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.authz.cover.SecurityService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateBugReportHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "generatebugreport";

	public GenerateBugReportHandler()
	{
		setUrlFragment(GenerateBugReportHandler.URL_FRAGMENT);
	}

	@Override
	public int doPost(String[] parts, HttpServletRequest req, HttpServletResponse res, Session session)
		throws PortalHandlerException
	{
		return NEXT;
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res, Session session)
		throws PortalHandlerException
	{
		if ((parts.length == 2) && (parts[1].equals(GenerateBugReportHandler.URL_FRAGMENT))) {
			if (!SecurityService.isSuperUser()) {
				log.debug("No bug report generated because user isn't a superuser");
			} else {
				throw new RuntimeException(URL_FRAGMENT);
			}
		}

		return NEXT;
	}
}
