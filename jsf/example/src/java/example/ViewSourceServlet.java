/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Sakai Foundation.
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



