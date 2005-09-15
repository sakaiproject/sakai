/**********************************************************************************
*
* $Id: LoginBean.java 637 2005-07-15 16:35:46Z jholtzman@berkeley.edu $
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

package org.sakaiproject.tool.section.jsf.backingbean.standalone;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.component.section.facade.impl.standalone.AuthnStandaloneImpl;
import org.sakaiproject.component.section.facade.impl.standalone.ContextStandaloneImpl;
import org.sakaiproject.tool.section.jsf.JsfUtil;
import org.sakaiproject.tool.section.jsf.backingbean.CourseDependentBean;

/**
 * @author <a href="jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class LoginBean extends CourseDependentBean {

	private static final Log log = LogFactory.getLog(LoginBean.class);
	
	private static final long serialVersionUID = 1L;

	private String userName;
    private String context;

    public LoginBean() {
    	// Default for testing
		userName = "instructor1";
    }

    public String processSetUserNameAndContext() {
        // We store the username and context as a session attribute in standalone mode
        HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        session.setAttribute(AuthnStandaloneImpl.USER_NAME, userName);
        session.setAttribute(ContextStandaloneImpl.CONTEXT, context);

        Role siteRole = getSiteRole();
        
        if(siteRole.isStudent()) {
        	return "studentView";
        }
        if(siteRole.isInstructor() || siteRole.isTeachingAssistant()) {
        	return "overview";
        }

        if(log.isDebugEnabled()) log.debug(userName + " does not have a role in site " + context);

        JsfUtil.addRedirectSafeInfoMessage("You have no role in this site. " +
        				"Please choose another site or another user, or both.");
        
        // Keep the user on the login page, and exercise the redirect to test messaging
        return "login";
    }
    
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
}


/**************************************************************************************************************************************************************************************************************************************************************
 * $Id: LoginBean.java 637 2005-07-15 16:35:46Z jholtzman@berkeley.edu $
 *************************************************************************************************************************************************************************************************************************************************************/
