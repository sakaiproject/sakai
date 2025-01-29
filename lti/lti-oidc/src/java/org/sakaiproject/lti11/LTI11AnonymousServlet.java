/**
 * Copyright (c) 2019- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Author: Charles Severance <csev@umich.edu>
 */
package org.sakaiproject.lti11;

/* This does not use the request filter because it needs full control of response headers.
       But this also means that no work that should be in a session should be done in this servlet.
       In particular, never use ThreadLocal in this servlet.
 */
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.util.ResourceLoader;

/**
 * This is a servlet that does LTI 1.1 things where the user does not need to be logged in
 */
@SuppressWarnings("deprecation")
@Slf4j
public class LTI11AnonymousServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static ResourceLoader rb = new ResourceLoader("lti11");

    private final String returnHTML =
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
        "   \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
        "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n" +
        "<body>\n" +
        "<script language=\"javascript\">\n" +
        "$message = '<div align=\"center\" style=\"text-align:left;width:80%;margin-top:5px;margin-left:auto;margin-right:auto;border-width:1px 1px 1px 1px;border-style:solid;border-color: gray;padding:.5em;font-family:Verdana,Arial,Helvetica,sans-serif;font-size:.8em\"><p>MESSAGE</p>';\n" +
        "$closeText = '<p>CLOSETEXT</p>'\n" +
        "document.write($message);\n" +
        "if(self.location==top.location) {\n" +
        "  document.write($closeText);\n" +
        "}\n" +
        "</script>\n" +
        "</div></body>\n" +
        "</html>\n";



	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String rpi = request.getPathInfo();
        if ( rpi.startsWith("/return-url") ) {
            handleReturnUrl(request, response);
            return;
        }
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

    /* launch_presentation_return_url=http://lmsng.school.edu/portal/123/page/988/

        The TP may add a parameter called lti_errormsg that includes some detail as to
        the nature of the error.  The lti_errormsg value should make sense if displayed
        to the user.  If the tool has displayed a message to the end user and only wants
        to give the TC a message to log, use the parameter lti_errorlog instead of
        lti_errormsg. If the tool is terminating normally, and wants a message displayed
        to the user it can include a text message as the lti_msg parameter to the
        return URL. If the tool is terminating normally and wants to give the TC a
        message to log, use the parameter lti_log.

        http://localhost:8080/imsoidc/lti11/return-url/site/12345
        http://localhost:8080/imsoidc/lti11/return-url/pda/12345
    */
    protected void handleReturnUrl(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        String lti_errorlog = request.getParameter("lti_errorlog");
        if ( lti_errorlog != null ) log.error(lti_errorlog);
        String lti_errormsg = request.getParameter("lti_errormsg");
        if ( lti_errormsg != null ) log.error(lti_errormsg);
        String lti_log = request.getParameter("lti_log");
        if ( lti_log != null ) log.info(lti_log);
        String lti_msg = request.getParameter("lti_msg");
        if ( lti_msg != null ) log.info(lti_msg);

        String message = rb.getString("outcome.tool.finished");
        if ( lti_msg != null ) {
            message = rb.getString("outcome.tool.lti_msg") + " " + lti_msg;
        } else if ( lti_errormsg != null ) {
            message = rb.getString("outcome.tool.lti_errormsg") + " " + lti_errormsg;
        }

        String output = returnHTML;
        output = output.replace("MESSAGE",message);
        output = output.replace("CLOSETEXT",rb.getString("outcome.tool.close.window"));
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(output);
    }


}
