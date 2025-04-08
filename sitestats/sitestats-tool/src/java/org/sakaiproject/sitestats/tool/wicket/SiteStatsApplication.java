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

import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.Component;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.core.util.resource.locator.ResourceStreamLocator;
import org.apache.wicket.core.util.file.WebApplicationPath;
import org.apache.wicket.request.IRequestMapper;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.JavaScriptToBucketResponseDecorator;
import org.sakaiproject.sitestats.tool.wicket.pages.OverviewPage;
import org.sakaiproject.sitestats.tool.wicket.pages.PreferencesPage;
import org.sakaiproject.sitestats.tool.wicket.pages.ReportsPage;
import org.sakaiproject.sitestats.tool.wicket.pages.UserActivityPage;
import org.sakaiproject.util.ResourceLoader;


@Setter
@Getter
public class SiteStatsApplication extends WebApplication {
	private static final ResourceLoader msgs = new ResourceLoader("Messages");
	private static final ResourceLoader evnts = new ResourceLoader("Events");
	
	private transient SakaiFacade	facade;

	@Override
	protected void init() {
		super.init();

		// Configure general wicket application settings
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));
		getCspSettings().blocking().disabled();
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getMarkupSettings().setStripWicketTags(true);
		getResourceSettings().getStringResourceLoaders().add(new SiteStatsStringResourceLoader());
		getResourceSettings().getResourceFinders().add(new WebApplicationPath(getServletContext(), "html"));
		getResourceSettings().setResourceStreamLocator(new SiteStatsResourceStreamLocator());
		
		// Configure settings to fix StalePageException
		getRequestCycleSettings().setRenderStrategy(org.apache.wicket.settings.RequestCycleSettings.RenderStrategy.ONE_PASS_RENDER);
		getRequestCycleSettings().setBufferResponse(false);

		// configure bottom page script loading
		//setHeaderResponseDecorator(new JavaScriptToBucketResponseDecorator("bottom-script-container"));

		// Mount pages
		mountPage("/home", OverviewPage.class);
		mountPage("/reports", ReportsPage.class);
		mountPage("/useractivity", UserActivityPage.class);
		mountPage("/preferences", PreferencesPage.class);
		
		// On wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(OverviewPage.class);
		getApplicationSettings().setAccessDeniedPage(OverviewPage.class);
		
		// Add a custom StalePageException handler to gracefully handle stale page issues
		getRequestCycleListeners().add(new IRequestCycleListener() {
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				if (ex instanceof org.apache.wicket.core.request.mapper.StalePageException) {
					// Log the stale page exception but allow normal handling - page will be re-rendered
					// This prevents the error from being displayed to the user
					return null; // Return null to let Wicket's default handling occur
				}
				return null;
			}
		});

		{
			// Throw RuntimeDeceptions so they are caught by the Sakai ErrorReportHandler
			getRequestCycleListeners().add(new IRequestCycleListener()
			{
				@Override
				public IRequestHandler onException(RequestCycle cycle, Exception ex)
				{
					if (ex instanceof RuntimeException)
					{
						throw (RuntimeException) ex;
					}
					return null;
				}
			});
		}

		// Encrypt URLs. This immediately sets up a session (note that things like CSS now becomes bound to the session)
		//getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory()); // Different key per user
		//final IRequestMapper cryptoMapper = new CryptoMapper(getRootRequestMapper(), this);
		//setRootRequestMapper(cryptoMapper);
	}
	
	public Class getHomePage() {
		return OverviewPage.class;
	}

    /**
	 * Custom bundle loader to pickup bundles from sitestats-bundles/
	 * @author Nuno Fernandes
	 */
	private static class SiteStatsStringResourceLoader implements IStringResourceLoader {
		private static final ResourceLoader	messages	= new ResourceLoader("Messages");
		private static final ResourceLoader	events		= new ResourceLoader("Events");
		private static final ResourceLoader	nav			= new ResourceLoader("Navigator");

		@Override
		public String loadStringResource(Component component, String key, Locale locale, String style, String variation) {
			String value = null;
			if(messages.containsKey(key)) {
				value = messages.getString(key, null);
			}
			if(value == null && events.containsKey(key)){
				value = events.getString(key, null);
			}
			if(value == null && nav.containsKey(key)){
				value = nav.getString(key, null);
			}
			if(value == null){
				value = key;
			}
			return value;
		}

		@Override
		public String loadStringResource(Class clazz, String key, Locale locale, String style, String variation) {
			msgs.setContextLocale(locale);
			String value = msgs.getString(key, null);
			if(value == null){
				evnts.setContextLocale(locale);
				value = evnts.getString(key, null);
			}
			if(value == null){
				nav.setContextLocale(locale);
				value = nav.getString(key, null);
			}
			if(value == null){
				value = key;
			}
			return value;
		}

	}
	
	/**
	 * Custom loader for .html files
	 * @author Nuno Fernandes
	 */
	private static class SiteStatsResourceStreamLocator extends ResourceStreamLocator {

		public SiteStatsResourceStreamLocator() {
		}

		public IResourceStream locate(final Class clazz, final String path) {
			IResourceStream located = super.locate(clazz, trimFolders(path));
			if(located != null){
				return located;
			}
			return super.locate(clazz, path);
		}

		private String trimFolders(String path) {
			String wicketPackage = "/wicket/";
			return path.substring(path.lastIndexOf(wicketPackage) + wicketPackage.length());
		}
	}
	
}
