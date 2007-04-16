package org.sakaiproject.scorm.client;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res)
    	throws ServletException, IOException {
	
		PrintWriter writer = res.getWriter();
		
		writer.println("<html><body>Hello new world</body></html>");
		
		writer.close();
	}
	
}
