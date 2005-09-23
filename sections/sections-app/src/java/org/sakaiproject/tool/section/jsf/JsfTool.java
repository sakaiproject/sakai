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

package org.sakaiproject.tool.section.jsf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.api.section.facade.manager.Authz;
import org.sakaiproject.api.section.facade.manager.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * Extension to the Sakai JsfTool servlet, computing the default page view
 * depending on the user's role (permissions).
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class JsfTool extends org.sakaiproject.jsf.util.JsfTool {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(JsfTool.class);
	
	/**
	 * @inheritDoc
	 */
	protected String computeDefaultTarget() {
        if(log.isDebugEnabled()) log.debug("Entering sections tool... determining role appropriate view");

        ApplicationContext ac = (ApplicationContext)getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        Authn authnService = (Authn)ac.getBean("org.sakaiproject.api.section.facade.manager.Authn");
        Authz authzService = (Authz)ac.getBean("org.sakaiproject.api.section.facade.manager.Authz");
        Context contextService = (Context)ac.getBean("org.sakaiproject.api.section.facade.manager.Context");

        String userUid = authnService.getUserUid(null);
        String siteContext = contextService.getContext(null);
        Role siteRole = authzService.getSiteRole(userUid, siteContext);

        String target;
        if(siteRole.isInstructor() || siteRole.isTeachingAssistant()) {
            if(log.isDebugEnabled()) log.debug("Sending user to the overview page");
            target = "/overview";
        } else if (siteRole.isStudent()) {
            if(log.isDebugEnabled()) log.debug("Sending user to the student view page");
            target = "/studentView";
        } else {
            // The role filter has not been invoked yet, so this could happen here
            throw new RuntimeException("User " + userUid + " attempted to access sections in site " +
            		siteContext + " without any role");
        }
        return target;
    }
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
