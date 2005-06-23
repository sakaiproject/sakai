/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
 * Licencesed from The Apache Software Foundation
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package org.sakaiproject.tool.assessment.ui.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Example filter that unconditionally sets the character encoding to be used
 * in parsing the incoming request to a value specified by the
 * <strong>encoding</string> filter initialization parameter in the web app
 * deployment descriptor (</code>/WEB-INF/web.xml</code>).  This filter could
 * easily be extended to be more intelligent about what character encoding to
 * set, based on characteristics of the incoming request (such as the values
 * of the <code>Accept-Language</code> and <code>User-Agent</code> headers,
 * or a value stashed in the current user's session).
 *
 * @author Craig McClanahan
 * @version $Id$
 */
public class SetCharacterEncodingFilter
  implements Filter
{
  private static final org.apache.log4j.Logger LOG =
    org.apache.log4j.Logger.getLogger(SetCharacterEncodingFilter.class);

  // ----------------------------------------------------- Instance Variables

  /**
   * The default character encoding to set for requests that pass through
   * this filter.
   */
  protected String encoding = null;

  /**
   * The filter configuration object we are associated with.  If this value
   * is null, this filter instance is not currently configured.
   */
  protected FilterConfig filterConfig = null;

  // --------------------------------------------------------- Public Methods

  /**
   * Take this filter out of service.
   */
  public void destroy()
  {
    this.encoding = null;
    this.filterConfig = null;
  }

  /**
   * Select and set (if specified) the character encoding to be used to
   * interpret request parameters for this request.
   *
   * @param request The servlet request we are processing
   * @param result The servlet response we are creating
   * @param chain The filter chain we are processing
   *
   * @exception IOException if an input/output error occurs
   * @exception ServletException if a servlet error occurs
   */
  public void doFilter(
    ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug(
        "doFilter(ServletRequest request, ServletResponse response, FilterChain chain)");
    }

    // Select and set (if needed) the character encoding to be used
    request.setCharacterEncoding(selectEncoding(request));

    // Pass control on to the next filter
    chain.doFilter(request, response);

    // filter Response
    ; // nada
  }

  /**
   * Place this filter into service.
   *
   * @param filterConfig The filter configuration object
   */
  public void init(FilterConfig filterConfig)
    throws ServletException
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("init(FilterConfig filterConfig)");
    }

    this.filterConfig = filterConfig;
    this.encoding = filterConfig.getInitParameter("encoding");
    if(LOG.isDebugEnabled())
    {
      LOG.debug("encoding=" + encoding);
    }
  }

  // ------------------------------------------------------ Protected Methods

  /**
   * Select an appropriate character encoding to be used, based on the
   * characteristics of the current request and/or filter initialization
   * parameters.  If no character encoding should be set, return
   * <code>null</code>.
   * <p>
   * The default implementation unconditionally returns the value configured
   * by the <strong>encoding</strong> initialization parameter for this
   * filter.
   *
   * @param request The servlet request we are processing
   */
  protected String selectEncoding(ServletRequest request)
  {
    return this.encoding;
  }
}
