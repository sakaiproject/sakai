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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.api;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Portal Service acts as a focus for all Portal based activities, the service implementation
 * should act as a holder to enable the varous webapps to communicate with one annother.
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
public interface PortalService
{
	/**
	 * A portal request scope attribute that reprenset the placement id of the
	 * current request. It should be a string, and should be implimented where
	 * the request is portlet dispatched.
	 */
	public static final String PLACEMENT_ATTRIBUTE = PortalService.class.getName()
			+ "_placementid";

	/**
	 * this is the property in the tool config that defines the portlet context
	 * of tool. At the moment we assume that this is in the read-only properties
	 * of the tool, but there could be a generic tool placement that enabled any
	 * portlet to be mounted
	 */
	public static final String TOOL_PORTLET_CONTEXT_PATH = "portlet-context";

	/**
	 * this is the property in the tool config that defines the name of the
	 * portlet application
	 */
	public static final String TOOL_PORTLET_APP_NAME = "portlet-app-name";

	/**
	 * this is the property in the tool config that defines the name of the
	 * portlet
	 */
	public static final String TOOL_PORTLET_NAME = "portlet-name";

	/**
	 * ste the state of the portal reset flag.
	 * 
	 * @param state
	 */
	void setResetState(String state);

	/**
	 * Returns a parameter map suitable for appending to a portal URL,
	 * representing that the URL state of a tool being shown with the specified
	 * placementId will be equal to the URLstub. URLstub may contain anchor
	 * state, which the portal implementation may honour if it is capable. The
	 * Map may also include the encoded state of other placements if they are
	 * being shown in the current render state.
	 */
	Map<String, String[]> encodeToolState(String placementId, String URLstub);

	/**
	 * Inverts the operation of encodeToolState, and returns the URL stub which
	 * was supplied for the supplied placementId. Will return <code>null</code>
	 * if there was no special state registered.
	 */
	String decodeToolState(Map<String, String[]> params, String placementId);

	/**
	 * get the state of the state of the portal reset flag
	 * 
	 * @return
	 */
	String getResetState();

	/**
	 * get the StoredState object that is used to hold initial request state on
	 * direct access to a portlet state or on GET or POST that requires other
	 * initial actions.
	 * 
	 * @return
	 */
	StoredState getStoredState();

	/**
	 * Was a reset requested
	 * 
	 * @param req
	 * @return
	 */
	boolean isResetRequested(HttpServletRequest req);

	/**
	 * set the StoredState of the request for later retrieval
	 * 
	 * @param storedstate
	 */
	void setStoredState(StoredState storedstate);

	/**
	 * Is the direct URL mechnism enabled in the configation file.
	 * 
	 * @return
	 */
	boolean isEnableDirect();

	/**
	 * Get the parameter used to communicate reset state operations on the URL
	 * 
	 * @return
	 */
	String getResetStateParam();

	/**
	 * Create a new Stored State
	 * 
	 * @param marker
	 *        the mark within the URL
	 * @param replacement
	 *        and the replacement text on restoration
	 * @return
	 */
	StoredState newStoredState(String marker, String replacement);

	/**
	 * Get an Iterator of Portlet Application Descriptors from the whole of the
	 * application
	 * 
	 * @return
	 */
	Iterator<PortletApplicationDescriptor> getRegisteredApplications();

	/**
	 * get a render engine possibly based on the request
	 * 
	 * @param context -
	 *        the context from whcih to take the render engine.
	 * @param request
	 * @return
	 */
	PortalRenderEngine getRenderEngine(String context, HttpServletRequest request);

	/**
	 * add a render engine to the available render engines.
	 * 
	 * @param context -
	 *        the context to rengister the render engine in, as there may be
	 *        more than one portal in a sakai instance, you need to register the
	 *        render engine against a context. The context should match the
	 *        context used by the portal to retrieve its render engine. This is
	 *        dependant on the Portal implementation details.
	 * @param vengine
	 *        the render engine implementation to register with the portal
	 *        service
	 */
	void addRenderEngine(String context, PortalRenderEngine vengine);

	/**
	 * remove a render engine from the avaialble render engines
	 * 
	 * @param context -
	 *        the context to deregister the render engine from, as there may be
	 *        more than one portal in a sakai instance, you need to deregister
	 *        the render engine from a context. The context should match the
	 *        context used by the portal to retrieve its render engine. This is
	 *        dependant on the Portal implementation details.
	 * @param vengine
	 */
	void removeRenderEngine(String context, PortalRenderEngine vengine);

	/**
	 * Add a PortalHandler to the portal Handler map for the named context.
	 * 
	 * @param portal
	 * @param handler
	 */
	void addHandler(Portal portal, PortalHandler handler);

	/**
	 * Remove the Portal Handler identitied by the URL fragment associated with
	 * the portal Context
	 * 
	 * @param portal
	 * @param urlFragment
	 */
	void removeHandler(Portal portal, String urlFragment);

	/**
	 * Get the PortalHandler map for the portal Context.
	 * 
	 * @param portal
	 * @return
	 */
	Map<String, PortalHandler> getHandlerMap(Portal portal);

	/**
	 * Add a portal to the portal service
	 * 
	 * @param portal
	 */
	void removePortal(Portal portal);

	/**
	 * Remove a portal from the portal service this should perform all the
	 * necessary cleanup
	 * 
	 * @param portal
	 */
	void addPortal(Portal portal);

	/**
	 * Get the implimentation of the StylableService from the portal impl
	 * 
	 * @return
	 */
	StyleAbleProvider getStylableService();

	/**
	 * Add a PortalHandler when you don't have a reference to the portal.
	 * Eg. If the portal handler is in a different servlet context to the portal.
	 * @param portalContext The context of the portal. Eg: charon.
	 * @param handler The portal handler to add.
	 * @see PortalService#removeHandler(String, String)
	 */
	void addHandler(String portalContext, PortalHandler handler);

	/**
	 * Remove a PortalHandler when you don't have a reference to the portal.
	 * @see PortalService#addHandler(String, PortalHandler)
	 */
	void removeHandler(String portalContext, String urlFragment);

	/**
	 * @return
	 */
	SiteNeighbourhoodService getSiteNeighbourhoodService();

}
