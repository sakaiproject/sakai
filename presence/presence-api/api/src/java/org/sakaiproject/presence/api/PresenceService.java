/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

// package
package org.sakaiproject.service.legacy.presence;

// imports
import java.util.List;

import org.sakaiproject.service.legacy.entity.Entity;

/**
* <p>A PresenceService keeps track of a session's presence at various locations in the system.</p>
* <p>Location is a combination of site id, (optional) page id and (optional) tool id</p>
* 
* @author University of Michigan, Sakai Software Development Team
* @version $Revision$
*/
public interface PresenceService
{
	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "presence";

	/** Name for the event of establishing presence at a location. */
	public static final String EVENT_PRESENCE = "pres.begin";

	/** Name for the event of ending presence at a location. */
	public static final String EVENT_ABSENCE = "pres.end";

	/**
	 * Form a presence reference from a location id
	 * @param id the location id.
	 * @return A presence reference based on a location id.
	 */
	String presenceReference(String id);

	/**
	 * Construct a location id from site, page, and tool.
	 * @param site The site id.
	 * @param page The page id (optional).
	 * @param tool The tool id (optional).
	 * @return a Location Id.
	 */
	String locationId(String site, String page, String tool);

	/**
	 * Form a description for the location.
	 * @param location The presence location.
	 * @return A description for the location.
	 */
	String getLocationDescription(String location);

	/**
	 * Establish or refresh the presence of the current session in a location.
	 * @param session The session object.
	 * @param locationId A presence location id.
	 */
	void setPresence(String locationId);

	/**
	 * Remove the presence of the current session from a location.
	 * @param session The session object.
	 * @param locationId A presence location id.
	 */
	void removePresence(String locationId);

	/**
	 * Access a List of sessions (UsageSession) now present in a location.
	 * @param locationId A presence location id.
	 * @return The a List of sessions (UsageSession) now present in the location (may be empty).
	 */
	List getPresence(String locationId);

	/**
	 * Access a List of users (User) now present in a location.
	 * @param locationId A presence location id.
	 * @return The a List of users (User) now present in the location (may be empty).
	 */
	List getPresentUsers(String locationId);

	/**
	 * Access a List of all location ids (String).
	 * @return The List of all location ids (Strings) (may be empty).
	 */
	List getLocations();
	
	/**
	 * Access the time (in seconds) after which a presence will timeout.
	 * @return The time (seconds) after which a presence will timeout.
	 */
	int getTimeout();
}



