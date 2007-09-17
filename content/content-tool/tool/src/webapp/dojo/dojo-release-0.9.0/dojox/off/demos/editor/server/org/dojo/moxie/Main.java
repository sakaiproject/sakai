package org.dojo.moxie;

import javax.servlet.*;
import javax.servlet.http.*;
 
import org.mortbay.jetty.*;
import org.mortbay.jetty.handler.*;
import org.mortbay.jetty.servlet.*;

/**
	A quick starter class that enables users to run the Moxie
	server-side extremely quickly. We use the embedded Derby
	database that comes bundled with Java, coupled with an
	embedded version of the tiny Jetty embedded web-server.
	
	@author Brad Neuberg, bkn3@columbia.edu
*/
public class Main{
	public static void main(String args[]){
		try{
			// start our embedded web server, Jetty
			Server server = new Server(8000);   
			
			// serve up our Moxie/Dojo files
			ResourceHandler resourceHandler = new ResourceHandler();
			resourceHandler.setResourceBase("../../../../..");
			server.addHandler(resourceHandler);
			
			// add the Moxie servlet
			Context moxieRoot = new Context(server, "/moxie", Context.SESSIONS);
			Servlet moxieServlet = new MoxieServlet("jdbc:derby:moxie;create=true", null, null,
													"org.apache.derby.jdbc.EmbeddedDriver");
			moxieRoot.addServlet(new ServletHolder(moxieServlet), "/*");
			
			System.out.println("Starting web server on port 8000...");
			server.start();
		
			System.out.println("Moxie ready to try on port 8000.");
			System.out.println("Open a web browser and go to:");
			System.out.println("http://localhost:8000/dojox/off/demos/editor/editor.html");
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
}