/*
 ***************************************************************************
 * Copyright 2003-2005 Luca Passani, passani at eunet.no                   *
 * Distributed under the Mozilla Public License                            *
 *   http://www.mozilla.org/NPL/MPL-1.1.txt                                *
 ***************************************************************************
 *   $Author$
 *   $Header: /cvsroot/wurfl/tools/java/wurflapi-xom/antbuild/src/net/sourceforge/wurfl/wurflapi/WurflServletInit.java,v 1.1 2005/02/13 15:11:39 passani Exp $
 */

package net.sourceforge.wurfl.wurflapi;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class WurflServletInit extends HttpServlet {


    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	//Initialize WURFL with '/WEB-INF/wurfl.xml'
       	System.out.println("WurflServletInit.class servlet: About to initialize web-app and load wurfl");
	if ( !ObjectsManager.isWurflInitialized() )  {
	    ObjectsManager.initFromWebApplication(config.getServletContext());
	} else {
	    System.out.println("WurflServletInit.class: Wurfl was already initialized");
	}
	
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println("init servlet (Loaded on startup for WURFL initialization)");


    }

    /**
     * We are going to perform the same operations for POST requests
     * as for GET methods, so this method just sends the request to
     * the doGet method.
     */

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        doGet(request, response);
    }


}
