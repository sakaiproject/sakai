/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.render.portlet.services;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.portlet.PortletRequest;

import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.internal.InternalPortletPreference;
import org.apache.pluto.internal.impl.PortletPreferenceImpl;
import org.apache.pluto.spi.optional.P3PAttributes;
import org.apache.pluto.spi.optional.PortalAdministrationService;
import org.apache.pluto.spi.optional.PortletEnvironmentService;
import org.apache.pluto.spi.optional.PortletInfoService;
import org.apache.pluto.spi.optional.PortletInvocationEvent;
import org.apache.pluto.spi.optional.PortletInvocationListener;
import org.apache.pluto.spi.optional.PortletInvokerService;
import org.apache.pluto.spi.optional.PortletPreferencesService;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.apache.pluto.spi.optional.UserInfoService;
// This new service is added in Pluto 1.1.6 and later
// https://issues.apache.org/jira/browse/PLUTO-489
// http://jira.sakaiproject.org/browse/SAK-19011
import org.apache.pluto.spi.optional.RequestAttributeService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author csev
 * @since Sakai 2.4
 * @version $Rev$
 */
@Slf4j
public class SakaiOptionalPortletContainerServices implements OptionalContainerServices
{

	protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

	// OptionalContainerServices Impl ------------------------------------------

	private UserInfoService userInfoService = new SakaiUserInfoService();

	private PortalAdministrationService portalAdministrationService = new SakaiPortalAdministrationService();

	private PortletPreferencesService prefService = new SakaiPortletPreferencesService();

	private boolean prefLog = true;

	public PortletPreferencesService getPortletPreferencesService()
	{
		if (prefLog)
			log.info("Sakai Optional Portal Services returning " + prefService);
		prefLog = false; // Only log once
		return prefService;
	}

	public PortletRegistryService getPortletRegistryService()
	{
		return null;
	}

	public PortletEnvironmentService getPortletEnvironmentService()
	{
		return null;
	}

	public PortletInvokerService getPortletInvokerService()
	{
		return null;
	}

	public PortletInfoService getPortletInfoService()
	{
		return null;
	}

	// This new service is added in Pluto 1.1.6 and later
	// https://issues.apache.org/jira/browse/PLUTO-489
        // http://jira.sakaiproject.org/browse/SAK-19011
	public RequestAttributeService getRequestAttributeService() {
		return null;
	}

	public PortalAdministrationService getPortalAdministrationService()
	{
		return portalAdministrationService;
	}

	public UserInfoService getUserInfoService()
	{
		return userInfoService;
	}

	// Our implementations of these local services
	// At some level this could return a clever proxy which did lazy loading
	public class SakaiUserInfoService implements UserInfoService
	{

		public Map getUserInfo(PortletRequest request, PortletWindow window)
				throws PortletContainerException
		{
			return getUserInfo(request);
		}

		public Map getUserInfo(PortletRequest request) throws PortletContainerException
		{

			Map retval = null;

			setupThread(request, true);

			User user = UserDirectoryService.getCurrentUser();
			if (user != null)
			{
				log.debug("Found Current User={}", user.getEid());
				retval = new HashMap<String, String>();
				retval.put(P3PAttributes.USER_HOME_INFO_ONLINE_EMAIL, user.getEmail());
				retval
						.put(P3PAttributes.USER_BUSINESS_INFO_ONLINE_EMAIL, user
								.getEmail());
				retval.put(P3PAttributes.USER_NAME_GIVEN, user.getFirstName());
				retval.put(P3PAttributes.USER_NAME_FAMILY, user.getLastName());
				retval.put(P3PAttributes.USER_NAME_NICKNAME, user.getDisplayName());

				// Add some GridSphere compatibility
				retval.put("user.name", user.getEid());
				retval.put("user.id", user.getEid());
				retval.put("user.login.id", user.getEid());
				retval.put("user.name.full", user.getDisplayName());
				retval.put("user.name.first", user.getFirstName());
				retval.put("user.name.last", user.getLastName());
				retval.put("user.email", user.getEmail());

				// GridSphere not supported yet
				// user.organization, user.lastlogintime, user.timezone,
				// user.locale
			}

			if (retval == null) retval = new HashMap();
			log.debug("Returning={}", retval);
			return retval;
		}
	}

	public class SakaiPortalAdministrationService implements PortalAdministrationService
	{
		private List administrativeRequestListeners = null;

		private List portletInvocationListeners = null;

		public List getAdministrativeRequestListeners()
		{
			if (administrativeRequestListeners == null)
			{
				administrativeRequestListeners = new ArrayList();
			}
			return administrativeRequestListeners;
		}

		public List getPortletInvocationListeners()
		{
			if (portletInvocationListeners == null)
			{
				portletInvocationListeners = new ArrayList(1);
				portletInvocationListeners.add(new SakaiPortletServletListener());
			}
			return portletInvocationListeners;
		}

		public class SakaiPortletServletListener implements PortletInvocationListener
		{

			public void onBegin(PortletInvocationEvent event)
			{
				log.debug("======== onBegin!");
				setupThread(event.getPortletRequest(), true);
			}

			public void onEnd(PortletInvocationEvent event)
			{
				log.debug("======== onEnd!");
			}

			public void onError(PortletInvocationEvent event, Throwable error)
			{
				log.debug("======== onError!");
			}

		}
	}

	private void setupThread(PortletRequest request, boolean doLog)
	{

		String placementId = (String) request
				.getAttribute("org.sakaiproject.portal.api.PortalService_placementid");
		log.debug("place from getAttribute = {}", placementId);
		if (placementId == null)
		{
			if (doLog) log.info("No Placement found");
			return; // We have nothing to work with
		}

		Session session = SessionManager.getCurrentSession();
		log.debug("Session = {}", session);
		if (session == null)
		{
			if (doLog) log.info("No Session found placementId=" + placementId);
			return; // We have nothing to work with
		}

		log.debug("UserId={} UserEID={}", session.getUserId(), session.getUserEid());

		// Check to see if there is already a placement in place
		Placement ppp = (Placement) ThreadLocalManager.get(CURRENT_PLACEMENT);
		log.debug("ThreadLocal CURRENT_PLACEMENT={}", ppp);
		if (ppp != null)
		{
			log.debug("ThreadLocal CURRENT_PLACEMENT ID={}", ppp.getId());
			if (placementId.equals(ppp.getId()))
			{
				log.debug("Thread already setup");
				return; // Placement in place
			}
		}

		// find the tool from some site (ToolConfiguration extends Placement)
		ToolConfiguration siteTool = SiteService.findTool(placementId);
		log.debug("siteTool={}", siteTool);
		if (siteTool == null)
		{
			if (doLog)
				log.info("No ToolConfiguration found, placementId=" + placementId
						+ " session=" + session);
			return;
		}

		// Actually store the placement in Thread Local
		ThreadLocalManager.set(CURRENT_PLACEMENT, siteTool);

		if (log.isDebugEnabled()) {
			// *** Testing Printout to see how well we have the APIs Configured ****
			ToolSession ts = SessionManager.getCurrentToolSession();
			log.debug("*** TEST *** \nTool Session = {}", ts);
			if ( ts != null ) {
				log.debug("ToolSession.getId={}", ts.getId());
			}

			Placement placement = ToolManager.getCurrentPlacement();
			log.debug("Placement = {}", placement);

			if ( placement != null ) {
				log.debug("Context = {}", placement.getContext());
			}
		}
	}

	public class SakaiPortletPreferencesService implements PortletPreferencesService
	{

		public SakaiPortletPreferencesService()
		{
			// Do nothing.
		}

		/**
		 * Returns the stored portlet preferences array. The preferences managed
		 * by this service should be protected from being directly accessed, so
		 * this method returns a cloned copy of the stored preferences.
		 * 
		 * @param portletWindow
		 *        the portlet window.
		 * @param request
		 *        the portlet request from which the remote user is retrieved.
		 * @return a copy of the stored portlet preferences array.
		 * @throws PortletContainerException
		 */
		public InternalPortletPreference[] getStoredPreferences(
				PortletWindow portletWindow, PortletRequest request)
				throws PortletContainerException
		{

			boolean readOnly = true;

			// Set up the thread if we have not already done so
			setupThread(request, true);

			// Get the Placement Id
			String key = portletWindow.getId().getStringId();

			// find the tool from some site
			ToolConfiguration siteTool = SiteService.findTool(key);
			log.debug("siteTool={}", siteTool);

			ArrayList<InternalPortletPreference> prefArray = new ArrayList<InternalPortletPreference>();
			if (siteTool != null)
			{
				String siteId = siteTool.getSiteId();
				log.debug("siteId={}", siteId);

				String siteReference = SiteService.siteReference(siteId);
				log.debug("Reference={}", siteReference);

				// If you don't have site.upd - Mark all references as read only
				readOnly = !SecurityService.unlock(SiteService.SECURE_UPDATE_SITE,
						siteReference);

				Properties props = siteTool.getPlacementConfig();
				log.debug("props = {}", props);
				for (Enumeration e = props.propertyNames(); e.hasMoreElements();)
				{
					String propertyName = (String) e.nextElement();
					log.debug("Property name = {}", propertyName);

					if (propertyName != null && propertyName.startsWith("javax.portlet") 
							&& propertyName.length() > 14)
					{
						String propertyValue = props.getProperty(propertyName);
						String[] propertyList = deSerializeStringArray(propertyValue);
						String internalName = propertyName.substring(14);
						log.debug("internalName={} propertyList={}", internalName, propertyList);
						InternalPortletPreference newPref = new PortletPreferenceImpl(
								internalName, propertyList, readOnly);
						log.debug("newPref = {}", newPref);
						prefArray.add(newPref);
					}
					else if ( propertyName != null ) 
					{
						String propertyValue = props.getProperty(propertyName);
						String internalName = "sakai:" + propertyName;
						String[] propertyList = new String[1];
						propertyList[0] = propertyValue;
						log.debug("internalName={} propertyList={}", internalName, propertyList);
						InternalPortletPreference newPref = new PortletPreferenceImpl(
								internalName, propertyList, readOnly);
						log.debug("newPref = {}", newPref);
						prefArray.add(newPref);
					}
				}
			}

			InternalPortletPreference[] preferences = new InternalPortletPreference[prefArray
					.size()];

			preferences = (InternalPortletPreference[]) prefArray.toArray(preferences);
			log.debug("Got {} stored preferences.", preferences.length);
			return preferences;
		}

		/**
		 * Stores the portlet preferences to the in-memory storage. This method
		 * should be invoked after the portlet preferences are validated by the
		 * preference validator (if defined).
		 * <p>
		 * The preferences managed by this service should be protected from
		 * being directly accessed, so this method clones the passed-in
		 * preferences array and saves it.
		 * </p>
		 * 
		 * @see javax.portlet.PortletPreferences#store()
		 * @param portletWindow
		 *        the portlet window
		 * @param request
		 *        the portlet request from which the remote user is retrieved.
		 * @param preferences
		 *        the portlet preferences to store.
		 * @throws PortletContainerException
		 */
		public void store(PortletWindow portletWindow, PortletRequest request,
				InternalPortletPreference[] preferences) throws PortletContainerException
		{

			// Set up the thread if we have not already done so
			setupThread(request, true);

			String key = portletWindow.getId().getStringId();

			// find the tool from some site
			ToolConfiguration siteTool = SiteService.findTool(key);
			log.debug("siteTool={}", siteTool);
			if (siteTool == null) return;

			Properties props = siteTool.getPlacementConfig();
			if (props == null) return;

			String siteId = siteTool.getSiteId();
			log.debug("siteId={}", siteId);

			String siteReference = SiteService.siteReference(siteId);
			log.debug("Reference={}", siteReference);

			// If you don't have site.upd - silently return not storing
			// In an ideal world perhaps we should throw java.io.IOException
			// As per PortletPreferences API on the store() method
			if (!SecurityService.unlock(SiteService.SECURE_UPDATE_SITE, siteReference))
			{
				log.debug("You do not have site.upd - silently returning and not storing");
				return;
			}

			log.debug("props before cleanup= {}", props);

			boolean changed = false;

			// Remove properties from the placement which did not come back to
			// be stored
			if (props != null)
			{
				for (Enumeration e = props.propertyNames(); e.hasMoreElements();)
				{
					String propertyName = (String) e.nextElement();
					log.debug("Checking Sakai property name = {}", propertyName);
					if (propertyName != null && (propertyName.startsWith("javax.portlet"))
							&& propertyName.length() > 14)
					{
						String internalName = propertyName.substring(14);
						log.debug("making sure we still have a prop named internalName={}", internalName);
						boolean found = false;
						for (int i = 0; i < preferences.length; i++)
						{
							if (preferences[i] != null)
							{
								String propName = preferences[i].getName();
								log.debug("Store[{}]={}", i, propName);
								if (internalName.equals(propName))
								{
									found = true;
									break;
								}
							}
						}
						if (!found)
						{
							log.debug("Removing {}", propertyName);
							props.remove(propertyName);
							changed = true;
						}
					// A sakai direct property
					} else {
						boolean found = false;
						for (int i = 0; i < preferences.length; i++)
						{
							if (preferences[i] != null)
							{
								String propName = "sakai:"+preferences[i].getName();
								if (propertyName.equals(propName))
								{
									found = true;
									break;
								}
							}
						}
						if (!found)
						{
							log.debug("Removing {}", propertyName);
							props.remove(propertyName);
							changed = true;
						}
                                        }
				}
			}

			log.debug("props after cleanup= {}", props);

			// Add / up date which are still there 
			for (int i = 0; i < preferences.length; i++)
			{
				log.debug("Store[{}] = {}", i, preferences[i]);
				if (preferences[i] != null && props != null)
				{
					String propName = preferences[i].getName();
					if ( propName == null || propName.length() < 1 ) continue;
					log.debug("Property Name={}", propName);

					//New property prefix SAK-30354 
					String propKey = "javax.portlet-" + propName;
					String storeString = serializeStringArray(preferences[i].getValues());

					// Write directly to the Sakai properties
					if ( propName.startsWith("sakai:") && propName.length() > 6 )
					{
						propKey = propName.substring(6);
						storeString = preferences[i].getValues()[0];
					}

					// Grab the property to see if it changed
					String oldString = props.getProperty(propKey);

					log.debug("propKey = {}", propKey);
					log.debug("storeString = {}", storeString);
					log.debug("oldString = {}", oldString);
					if (storeString!=null && !storeString.equals(oldString))
					{
						log.debug("Setting {} value={}", propKey, storeString);
						props.setProperty(propKey, storeString);
						changed = true;
					}
				}
			}

			log.debug("props after update= {}", props);
			log.debug("changed={}", changed);

			if (changed && siteTool != null)
			{
				siteTool.save();
				log.debug("Saved");
			}

			log.debug("Portlet preferences stored for: {}", key);
		}

		private String serializeStringArray(String[] input)
		{
			if (input == null || input.length < 1) return null;

			StringBuffer retval = new StringBuffer();
			for (int i = 0; i < input.length; i++)
			{
				if (i > 0) retval.append("!");
				if ( input[i] != null ) retval.append(URLEncoder.encode(input[i]));
			}
			return retval.toString();
		}

		private String[] deSerializeStringArray(String input)
		{
			log.debug("Input={}", input);
			String[] retval = input.split("!");
			log.debug("Found {} items.", retval.length);
			for (int i = 0; i < retval.length; i++)
			{
				log.debug("retval[{}]={}", i, retval[i]);
				if ( retval[i] == null ) continue;
				retval[i] = URLDecoder.decode(retval[i]);
			}
			return retval;
		}

	} // End of SakaiPortletPreferencesService

}
