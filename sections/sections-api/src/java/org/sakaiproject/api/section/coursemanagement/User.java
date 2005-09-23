/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
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

package org.sakaiproject.api.section.coursemanagement;

/**
 * Models a User for use in the Section Awareness API and the Section Manager Tool.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface User {
	/**
	 * @return Returns the userUid, the unique ID returned by the authentication facade.
	 */
	public String getUserUid();

	/**
	 * @return Returns the sortName, displayed when users are listed in order (for example,
	 * "Paine, Thomas" or "Wong Kar-Wai")
	 */
	public String getSortName();

    /**
	 * @return Returns the displayId, AKA "campus ID", a human-meaningful UID for the user (for
	 * example, a student ID number or an institutional email address)
	 */
	public String getDisplayId();

	/**
	 * @return Returns the displayName, displayed when only this user is being referred to
	 * (for example, "Thomas Paine" or "Wong Kar-Wai")
	 */
	public String getDisplayName();

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
