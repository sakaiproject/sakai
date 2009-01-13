/**
 * $Id$
 * $URL$
 * TestDirectServlet.java - entity-broker - Jan 2, 2009 4:46:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.jetty;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.impl.ServiceTestManager;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
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
    protected EntityRequestHandler getEntityRequestHandler() {
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
