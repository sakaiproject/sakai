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
package org.sakaiproject.gradebookng;

import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import org.sakaiproject.gradebookng.framework.GradebookNgStringResourceLoader;
import org.sakaiproject.gradebookng.tool.pages.AccessDeniedPage;
import org.sakaiproject.gradebookng.tool.pages.ErrorPage;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.gradebookng.tool.pages.PermissionsPage;
import org.sakaiproject.gradebookng.tool.pages.SettingsPage;
import org.sakaiproject.gradebookng.tool.pages.StudentPage;

/**
 * Main application class
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradebookNgApplication extends WebApplication {

	@Override
	public void init() {
		super.init();

		// page mounting for bookmarkable URLs
		mountPage("/grades", GradebookPage.class);
		mountPage("/settings", SettingsPage.class);
		mountPage("/importexport", ImportExportPage.class);
		mountPage("/permissions", PermissionsPage.class);
		mountPage("/gradebook", StudentPage.class);
		mountPage("/accessdenied", AccessDeniedPage.class);
		mountPage("/error", ErrorPage.class);

		// remove the version number from the URL so that browser refreshes re-render the page
		getRequestCycleSettings().setRenderStrategy(RenderStrategy.ONE_PASS_RENDER);

		// Configure for Spring injection
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));

		// Add ResourceLoader that integrates with Sakai's Resource Loader
		getResourceSettings().getStringResourceLoaders().add(0, new GradebookNgStringResourceLoader());

		// Don't throw an exception if we are missing a property, just fallback
		getResourceSettings().setThrowExceptionOnMissingResource(false);

		// Remove the wicket specific tags from the generated markup
		getMarkupSettings().setStripWicketTags(true);

		// Don't add any extra tags around a disabled link (default is <em></em>)
		getMarkupSettings().setDefaultBeforeDisabledLink(null);
		getMarkupSettings().setDefaultAfterDisabledLink(null);

		// On Wicket session timeout, redirect to main page
		// getApplicationSettings().setPageExpiredErrorPage(getHomePage());

		// Intercept any unexpected error stacktrace and take to our page
		getRequestCycleListeners().add(new AbstractRequestCycleListener() {
			@Override
			public IRequestHandler onException(final RequestCycle cycle, final Exception e) {
				return new RenderPageRequestHandler(new PageProvider(new ErrorPage(e)));
			}
		});

		// Disable Wicket's loading of jQuery - we load Sakai's preferred version in BasePage.java
		getJavaScriptLibrarySettings().setJQueryReference(new PackageResourceReference(GradebookNgApplication.class, "empty.js"));

		// cleanup the HTML
		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
	}

	/**
	 * The main page for our app
	 *
	 * @return
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<GradebookPage> getHomePage() {
		return GradebookPage.class;
	}

	/**
	 * Constructor
	 */
	public GradebookNgApplication() {}
}
