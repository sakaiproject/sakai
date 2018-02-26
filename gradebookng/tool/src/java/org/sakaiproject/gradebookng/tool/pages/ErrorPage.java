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
package org.sakaiproject.gradebookng.tool.pages;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * Page displayed when an internal error occurred.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ErrorPage extends BasePage {

	private static final String SAK_PROP_PORTAL_SHOW_ERROR = "portal.error.showdetail";
	private static final boolean SAK_PROP_PORTAL_SHOW_ERROR_DEFAULT = true;

	private static final long serialVersionUID = 1L;

	public ErrorPage(final Exception e) {

		final String stacktrace = ExceptionUtils.getStackTrace(e);

		// log the stacktrace
		log.error(stacktrace);

		// generate an error code so we can log the exception with it without giving the user the stacktrace
		// note that wicket will already have logged the stacktrace so we aren't going to bother logging it again
		final String code = RandomStringUtils.randomAlphanumeric(10);
		log.error("User supplied error code for the above stacktrace: {}", code);

		final Label error = new Label("error", new StringResourceModel("errorpage.text", null, new Object[] { code }));
		error.setEscapeModelStrings(false);
		add(error);

		// Display the stack trace only if the application is configured to do so
		boolean showStackTraces = ServerConfigurationService.getBoolean(SAK_PROP_PORTAL_SHOW_ERROR, SAK_PROP_PORTAL_SHOW_ERROR_DEFAULT);
		Label trace = new Label("stacktrace", stacktrace);
		if (!showStackTraces && !businessService.isSuperUser()) {
			trace.setVisible(false);
		}
		add(trace);
	}
}
