package org.sakaiproject.tool.section;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.sakaiproject.api.section.facade.manager.Authz;
import org.sakaiproject.api.section.facade.manager.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class EntryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(EntryServlet.class);

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException {
		doGet(req, resp);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if(log.isDebugEnabled()) log.debug("Entering sections tool... determining role appropriate view");

        ApplicationContext ac = (ApplicationContext)getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        Authn authnService = (Authn)ac.getBean("org.sakaiproject.api.section.facade.manager.Authn");
        Authz authzService = (Authz)ac.getBean("org.sakaiproject.api.section.facade.manager.Authz");
        Context contextService = (Context)ac.getBean("org.sakaiproject.api.section.facade.manager.Context");

        String userUid = authnService.getUserUid(null);
        String siteContext = contextService.getContext(null);
        
        boolean viewAllSections = authzService.isViewAllSectionsAllowed(userUid, siteContext);
        boolean viewOwnSections = authzService.isViewOwnSectionsAllowed(userUid, siteContext);

        StringBuffer path = new StringBuffer(request.getContextPath());
        if(viewAllSections) {
            if(log.isDebugEnabled()) log.debug("Sending user to the overview page");
            path.append("/overview.jsf");
        } else if (viewOwnSections) {
            if(log.isDebugEnabled()) log.debug("Sending user to the student view page");
            path.append("/studentView.jsf");
        } else {
            // The role filter has not been invoked yet, so this could happen here
            path.append("/noRole.jsp");
        }
        String queryString = request.getQueryString();
        if (queryString != null) {
			path.append("?").append(queryString);
		}
        try {
			response.sendRedirect(path.toString());
		} catch (IOException e) {
			log.error("Could not redirect user: " + e);
		}
		
	}

}
