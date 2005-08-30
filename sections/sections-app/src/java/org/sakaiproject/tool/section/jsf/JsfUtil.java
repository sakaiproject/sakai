/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.section.jsf;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

public class JsfUtil {
	private static final String SESSION_ATTR = "org.sakaiproject.tool.section.jsf.JsfUtil";
	
	public static Locale getLocale() {
		return FacesContext.getCurrentInstance().getViewRoot().getLocale();
	}
	
	public static String getLocalizedMessage(String key) {
        String bundleName = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
        ResourceBundle rb = ResourceBundle.getBundle(bundleName, getLocale());
        return rb.getString(key);
	}

	public static String getStringFromParam(String string) {
		return (String)FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap().get(string);
	}
	
	/**
	 * Keeps a string value in the session so it can be retrieved across redirects.
	 * 
	 * @param value The string to maintain across http redirects
	 * @param facesContext The current FacesContext instance
	 */
//	public static void storeRedirectSafeAttribute(String value, FacesContext facesContext) {
//		((HttpSession)facesContext.getExternalContext().getSession(true)).setAttribute(SESSION_ATTR, value);
//	}

	/**
	 * Retrieves the string value stored across a redirect, then clears the value from the session.
	 * 
	 * @param facesContext The current FacesContext instance
	 * @return The string value, or null if there is nothing stored in the session
	 */
//	public static String getRedirectSafeAttribute(FacesContext facesContext) {
//		HttpSession session = (HttpSession)facesContext.getExternalContext().getSession(true);
//		String value = (String)session.getAttribute(SESSION_ATTR);
//		session.removeAttribute(SESSION_ATTR);
//		return value;
//	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
