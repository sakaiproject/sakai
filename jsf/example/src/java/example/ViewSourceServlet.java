/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

package example;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * Serves up the source of a JSP/JSF page.  Should be mapped to "*.source"
 * in web.xml.  Looks at the URL its mapping from, strips off ".source", 
 * and looks for a corresponding JSP/JSF file.  Use like:
 * http://localhost:8080/myservlet/thefile.jsp.source
 */
public class ViewSourceServlet extends HttpServlet 
{
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException
    {
        String webPage = req.getServletPath();
        
        // remove the '*.source' suffix that maps to this servlet
        int chopPoint = webPage.indexOf(".source");
        
        webPage = webPage.substring(0, chopPoint - 1);
        webPage += "p"; // replace jsf with jsp
        
        // get the actual file location of the requested resource
        String realPath = getServletConfig().getServletContext().getRealPath(webPage);
        System.out.println("realPath: " + realPath);

        // output an HTML page
        res.setContentType("text/plain");

        // print some html
        ServletOutputStream out = res.getOutputStream();

        // print the file
        InputStream in = null;
        try 
        {
            in = new BufferedInputStream(new FileInputStream(realPath));
            int ch;
            while ((ch = in.read()) !=-1) 
            {
                out.print((char)ch);
            }
        }
        finally {
            if (in != null) in.close();  // very important
        }
    }
}



