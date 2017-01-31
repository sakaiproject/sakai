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

import org.apache.wicket.Component;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.core.util.resource.locator.ResourceStreamLocator;
import org.apache.wicket.core.util.file.WebApplicationPath;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.devutils.debugbar.InspectorDebugPanel;
import org.apache.wicket.devutils.debugbar.PageSizeDebugPanel;
import org.apache.wicket.devutils.debugbar.SessionSizeDebugPanel;
import org.apache.wicket.devutils.debugbar.VersionDebugContributor;
import org.apache.wicket.request.IRequestMapper;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.pages.OverviewPage;
import org.sakaiproject.util.ResourceLoader;


public class SiteStatsApplication extends WebApplication {
	private boolean					debug	= false;
	
	private transient SakaiFacade	facade;

	@Override
	protected void init() {
		super.init();

		// Configure general wicket application settings
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getMarkupSettings().setStripWicketTags(true);
		getResourceSettings().getStringResourceLoaders().add(new SiteStatsStringResourceLoader());
		getResourceSettings().getResourceFinders().add(new WebApplicationPath(getServletContext(), "html"));
		getResourceSettings().setResourceStreamLocator(new SiteStatsResourceStreamLocator());
		getDebugSettings().setAjaxDebugModeEnabled(debug);

		// Home page
		mountPage("/home", OverviewPage.class);
		
		// On wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(OverviewPage.class);
		getApplicationSettings().setAccessDeniedPage(OverviewPage.class);

		// Debugging
		debug = ServerConfigurationService.getBoolean("sitestats.debug", false);
		if(debug) {
			getDebugSettings().setComponentUseCheck(true);
			getDebugSettings().setAjaxDebugModeEnabled(true);
		    getDebugSettings().setLinePreciseReportingOnAddComponentEnabled(true);
		    getDebugSettings().setLinePreciseReportingOnNewComponentEnabled(true);
		    getDebugSettings().setOutputComponentPath(true);
		    getDebugSettings().setOutputMarkupContainerClassName(true);
			getDebugSettings().setDevelopmentUtilitiesEnabled(true);
		    getMarkupSettings().setStripWicketTags(false);
			getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_EXCEPTION_PAGE);
			// register standard debug contributors so that just setting the sitestats.debug property is enough to turn these on
			// otherwise, you have to turn wicket development mode on to get this populated due to the order methods are called
			DebugBar.registerContributor(VersionDebugContributor.DEBUG_BAR_CONTRIB, this);
			DebugBar.registerContributor(InspectorDebugPanel.DEBUG_BAR_CONTRIB, this);
			DebugBar.registerContributor(SessionSizeDebugPanel.DEBUG_BAR_CONTRIB, this);
			DebugBar.registerContributor(PageSizeDebugPanel.DEBUG_BAR_CONTRIB, this);
		}
		else
		{
			// Throw RuntimeDeceptions so they are caught by the Sakai ErrorReportHandler
			getRequestCycleListeners().add(new AbstractRequestCycleListener()
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
		getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory()); // Different key per user
		final IRequestMapper cryptoMapper = new CryptoMapper(getRootRequestMapper(), this); 
		setRootRequestMapper(cryptoMapper);
	}
	
	@SuppressWarnings("unchecked")
	public Class getHomePage() {
		return OverviewPage.class;
	}

	public SakaiFacade getFacade() {
		return facade;
	}

	public void setFacade(final SakaiFacade facade) {
		this.facade = facade;
	}

	/**
	 * Custom bundle loader to pickup bundles from sitestats-bundles/
	 * @author Nuno Fernandes
	 */
	private static class SiteStatsStringResourceLoader implements IStringResourceLoader {
		private ResourceLoader	messages	= new ResourceLoader("Messages");
		private ResourceLoader	events		= new ResourceLoader("Events");
		private ResourceLoader	nav			= new ResourceLoader("Navigator");

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
			ResourceLoader msgs = new ResourceLoader("Messages");
			msgs.setContextLocale(locale);
			String value = msgs.getString(key, null);
			if(value == null){
				ResourceLoader evnts = new ResourceLoader("Events");
				evnts.setContextLocale(locale);
				value = evnts.getString(key, null);
			}
			if(value == null){
				ResourceLoader nav = new ResourceLoader("Navigator");
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
