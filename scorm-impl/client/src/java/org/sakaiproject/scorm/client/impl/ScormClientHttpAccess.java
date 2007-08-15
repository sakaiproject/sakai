package org.sakaiproject.scorm.client.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;

public class ScormClientHttpAccess implements HttpAccess {
	private static Log log = LogFactory.getLog(ScormClientHttpAccess.class);
			
	public void handleAccess(HttpServletRequest req, HttpServletResponse res, 
			Reference ref, Collection copyrightAcceptedRefs)
		throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException {
		
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = null;
		
		try {
			out = res.getWriter();
			
			out.println("<html><body>Hello, new world.</body></html>");
		} catch (IOException ioe) {
			log.error("Unable to handle access - error getting the response PrintWriter", ioe);
		} finally {
			if (null != out)
				out.close();
		}
	}
	
}
