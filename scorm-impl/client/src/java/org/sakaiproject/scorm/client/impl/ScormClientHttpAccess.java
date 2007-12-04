/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
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
