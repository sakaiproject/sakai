/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.section.jsf.backingbean.standalone;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.section.facade.impl.standalone.AuthnStandaloneImpl;
import org.sakaiproject.component.section.facade.impl.standalone.ContextStandaloneImpl;
import org.sakaiproject.tool.section.jsf.JsfUtil;
import org.sakaiproject.tool.section.jsf.backingbean.CourseDependentBean;

/**
 * Controls the login page for the standalone webapp, which allows a person to
 * choose the current user and site context.
 * 
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

        if(isSectionEnrollmentMangementEnabled() || isSectionManagementEnabled() ||
        		isSectionOptionsManagementEnabled() || isSectionTaManagementEnabled()) {
        	return "overview";
        }
        
        if(isViewOwnSectionsEnabled()) {
        	return "studentView";
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
