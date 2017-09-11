/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

package org.sakaiproject.tool.gradebook.facades;

/**
 * Facade to abstract external authentication services.
 * Since this is an application-wide singleton pointing to an otherwise opaque service,
 * we do not assume that the authenticator has access to (for example) an up-to-date
 * fully constructed FacesContext.
 */
public interface Authn {
	/**
	 * @return an ID uniquely identifying the currently authenticated user in a
	 *     site, or null if the user has not been authenticated.
	 */
	public String getUserUid();

	/**
	 * @param whatToAuthn the javax.servlet.http.HttpServletRequest or
	 *     javax.portlet.PortletRequest for which authentication should be checked. Since
	 *     they don't share an interface, a generic object is passed.
	 */
	public void setAuthnContext(Object whatToAuthn);
}


