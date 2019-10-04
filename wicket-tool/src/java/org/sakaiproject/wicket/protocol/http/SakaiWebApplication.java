/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.wicket.protocol.http;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.settings.ExceptionSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import org.sakaiproject.wicket.markup.html.ErrorPage;
import org.sakaiproject.wicket.markup.html.SakaiSessionExpiredPage;

@Slf4j
public abstract class SakaiWebApplication extends WebApplication
{
	@Override
	protected void init()
	{
		// Configure for Spring injection
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));

		// Throw an exception if we are missing a property
		getResourceSettings().setThrowExceptionOnMissingResource(true);

		// Remove the wicket specific tags from the generated markup
		getMarkupSettings().setStripWicketTags(true);

		// On Wicket session timeout, redirect to the SakaiSessionExpiredPage
		getApplicationSettings().setPageExpiredErrorPage(SakaiSessionExpiredPage.class);

		// Cleanup the HTML
		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);

		getExceptionSettings().setUnexpectedExceptionDisplay(ExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);

		// Intercept any unexpected error stacktrace and take to our ErrorPage
		getRequestCycleListeners().add(new AbstractRequestCycleListener()
		{
			@Override
			public IRequestHandler onException(final RequestCycle cycle, final Exception e)
			{
				return new RenderPageRequestHandler(new PageProvider(new ErrorPage(e)));
			}
		});

		// Disable Wicket's loading of jQuery - we load Sakai's preferred version in SakaiPortletWebPage.java
		getJavaScriptLibrarySettings().setJQueryReference(new PackageResourceReference(SakaiWebApplication.class, "empty.js"));
	}
}
