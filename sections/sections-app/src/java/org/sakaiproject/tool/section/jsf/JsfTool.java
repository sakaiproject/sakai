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
import org.sakaiproject.component.section.cover.CourseManager;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class JsfTool extends org.sakaiproject.jsf.util.JsfTool {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(JsfTool.class);
	
	protected String computeDefaultTarget() {
        if(log.isDebugEnabled()) log.debug("Entering sections tool... determining role appropriate view");

        ApplicationContext ac = (ApplicationContext)getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        Authn authnService = (Authn)ac.getBean("org.sakaiproject.api.section.facade.manager.Authn");
        Authz authzService = (Authz)ac.getBean("org.sakaiproject.api.section.facade.manager.Authz");
        Context contextService = (Context)ac.getBean("org.sakaiproject.api.section.facade.manager.Context");

        String userUuid = authnService.getUserUuid(null);
        String siteContext = contextService.getContext(null);
        Role siteRole = authzService.getSiteRole(userUuid, siteContext);

        String target;
        if(siteRole.isInstructor() || siteRole.isTeachingAssistant()) {
        	// Create the course if it doesn't exist
        	if(!CourseManager.courseExists(siteContext)) {
        		String title = siteContext; // Can't be null in the db, so use this if need be
        		try {
            		title = contextService.getContextTitle(null);
        		} catch (Exception e) {
        			log.error("Unable to find site title in context " + siteContext);
        		}
        		if(log.isInfoEnabled()) log.info("Creating a new Course in site " + siteContext);
        		CourseManager.createCourse(siteContext, title, false, false, false);
        	}
            if(log.isInfoEnabled()) log.info("Sending user to the overview page");
            target = "/overview";
        } else if (siteRole.isStudent()) {
            if(log.isInfoEnabled()) log.info("Sending user to the student view page");
            target = "/studentView";
        } else {
            // The role filter has not been invoked yet, so this could happen here
            throw new RuntimeException("User " + userUuid + " attempted to access sections in site " +
            		siteContext + " without any role");
        }
        if(log.isDebugEnabled()) log.info("target = " + target);
        return target;
    }
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
