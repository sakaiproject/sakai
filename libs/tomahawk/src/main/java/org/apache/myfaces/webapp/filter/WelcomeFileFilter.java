/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.webapp.filter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 *
 * Due to the manner in which the JSP / servlet lifecycle
 * functions, it is not currently possible to specify default
 * welcome files for a web application and map them to the
 * MyFacesServlet.  Normally they will be mapped to the
 * default servlet for the JSP container.  To offset this
 * shortcoming, we utilize a servlet Filter which examines
 * the URI of all incoming requests.
 *
 * @author Robert J. Lebowitz (latest modification by $Author: skitching $)
 * @author Anton Koinov
 * @since February 18th, 2003
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (Thu, 03 Jul 2008) $
 */
public class WelcomeFileFilter
implements Filter
{
    //~ Instance fields --------------------------------------------------------

    private FilterConfig   _config;
    private ServletContext _context;
    private String[]       _welcomeFiles = new String[0];

    //~ Methods ----------------------------------------------------------------

    /**
     * @see jakarta.servlet.Filter#destroy()
     */
    public void destroy()
    {
        _config           = null;
        _context          = null;
        _welcomeFiles     = null;
    }

    /**
     *
     * If the URI indicates a context, or a subdirectory within a particular
     * context, but does not specify a filename, the request is redirected to
     * one of the default welcome files, assuming they are provided in the web.xml file.
     * If no welcome files are specified, or if none of the welcome files
     * actually exists, then the request is redirected to a file named "index.jsp" for
     * that context or subdirectory with the current context.  If the index.jsp file
     * does not exist, the servlet will return a File Not Found Error 404 message.
     *
     * A well configured servlet should provide a means of handling this type of
     * error, along with a link to an appropriate help page.
     *
     * A URI is thought to represent a context and/or subdirectory(s) if
     * it lacks a suffix following the pattern <b>.suffix</b>.
     *
     */
    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException
    {
        if (_config == null)
        {
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String             uri = httpRequest.getRequestURI();

        // if the uri does not contain a suffix, we consider 
        // it to represent a directory / context, not a file.
        // file has suffix.  No need to search for welcome file
        if (uri.lastIndexOf('.') > uri.lastIndexOf('/'))
        {
            chain.doFilter(request, response);

            return;
        }

        String       contextPath = httpRequest.getContextPath();
        String       welcomeFile = null;
        StringBuffer sb          = new StringBuffer(uri);

        if (!uri.endsWith("/"))
        {
            sb.append('/');
        }

        String baseURI = sb.delete(
                0,
                contextPath.length()).toString();

        // REVISIT: we probably can check for existence once at startup 
        //          and know the exact welcome file by now. Of course, that 
        //          would not work if the files change at runtime, but does it matter?
        for (int i = 0; i < _welcomeFiles.length; i++)
        {
            sb.setLength(0);
            sb.append(baseURI).append(_welcomeFiles[i]);

            File file = new File(_context.getRealPath(sb.toString()));

            //                        context.log("Welcome File: " + file.getAbsolutePath());
            if (file.exists())
            {
                // REVISIT: This will force all "welcome" JSPs through MyFaces. 
                //           Shouldn't we allow the user to enter *.jsf and check for *.jsp for existence, instead? 
                if (_welcomeFiles[i].endsWith(".jsp"))
                {
                    // alter the name of the file we are requesting to
                    // force it through the MyFacesServlet
                    sb.replace(
                        sb.lastIndexOf(".jsp"),
                        sb.length(),
                        ".jsf");
                    welcomeFile = sb.toString();
                }

                // we have discovered a filename;
                // stop the loop
                break;
            }
        }

        if (welcomeFile == null)
        {
            sb.setLength(0);
            sb.append(baseURI);
            sb.append("index.jsf");
            welcomeFile = sb.toString();
        }

        RequestDispatcher rd = httpRequest.getRequestDispatcher(welcomeFile);
        rd.forward(request, response);

        return;
    }

    /**
     * During the init method, we have to get any predefined welcome files
     * for the current ServletContext.
     * @param config The filter configuration data
     * @throws ServletException
     */
    public void init(FilterConfig config)
    throws ServletException
    {
        if (config == null)
        {
            return;
        }

        this._config      = config;
        this._context     = config.getServletContext();

        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            SAXParser          parser  = factory.newSAXParser();
            WelcomeFileHandler handler = new WelcomeFileHandler();
            InputStream        is      =
                _context.getResourceAsStream("WEB-INF/web.xml");

            if (is == null)
            {
                _context.log("Unable to get inputstream for web.xml");
            }

            parser.parse(is, handler);
            _welcomeFiles = handler.getWelcomeFiles();
            _context.log("Number of welcome files: " + _welcomeFiles.length);
        }
        catch (Exception ex)
        {
            throw new ServletException(ex);
        }
    }
}
