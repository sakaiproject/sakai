/**
 * $Id$
 * $URL$
 * TestDirectServlet.java - entity-broker - Jan 2, 2009 4:46:11 PM - azeckoski
 **********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.entitybroker.rest.jetty;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.rest.ServiceTestManager;
import org.sakaiproject.entitybroker.util.servlet.DirectServlet;


/**
 * This is a simple version for testing
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class MockDirectServlet extends DirectServlet {

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.util.servlet.DirectServlet#getCurrentLoggedInUserId()
     */
    @Override
    protected String getCurrentLoggedInUserId() {
        return TestData.USER_ID;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.util.servlet.DirectServlet#getEntityRequestHandler()
     */
    @Override
    protected EntityRequestHandler initializeEntityRequestHandler() {
        EntityRequestHandler erh = ServiceTestManager.getInstance().entityRequestHandler;
        return erh;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.util.servlet.DirectServlet#handleUserLogin(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    protected void handleUserLogin(HttpServletRequest req, HttpServletResponse res, String path) {
        RequestDispatcher rd = req.getRequestDispatcher(path);
        try {
            rd.forward(req, res);
        } catch (ServletException e) {
            throw new RuntimeException("Failed to forward request for path: " + path + ", failure: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to forward request for path: " + path + ", failure: " + e.getMessage(), e);
        }
    }

}
