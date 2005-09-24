/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades;

/**
 * Facade to abstract external authentication services.
 * Since this is an application-wide singleton pointing to an otherwise opaque service,
 * we do not assume that the authenticator has access to (for example) an up-to-date
 * fully constructed FacesContext.
 */
public interface Authn {
	/**
	 * @param whatToAuthn the javax.servlet.http.HttpServletRequest or
	 *     javax.portlet.PortletRequest for which authentication should be checked. Since
	 *     they don't share an interface, a generic object is passed.
	 * @return an ID uniquely identifying the currently authenticated user in a
	 *     site, or null if the user has not been authenticated.
	 */
	public String getUserUid(Object whatToAuthn);
}


