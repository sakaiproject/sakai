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
package org.sakaiproject.component.section.facade.impl.standalone;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.sakaiproject.section.api.facade.manager.Authn;

/**
 * Standalone implementation of Authn, using the HttpSession to store current
 * authentication credentials.
 * 
 * @author <a href="jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class AuthnStandaloneImpl implements Authn {
    public static final String USER_NAME = "username";

    /**
     * @see org.sakaiproject.section.api.facade.managers.Authn#getUserUid()
     */
    public String getUserUid(Object request) {
    	HttpSession session = null;
    	if(request == null) {
            session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
    	} else {
    		session = ((HttpServletRequest)request).getSession();
    	}
        return (String)session.getAttribute(USER_NAME);
    }

}
