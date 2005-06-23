/**********************************************************************************
* $HeaderURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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


package org.sakaiproject.tool.assessment.ui.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 */
public class Log4jMdcFilter
  implements Filter
{
  private static final String REMOTE_ADDR = "REMOTE_ADDR";
  private static final String SERVLET_PATH = "SERVLET_PATH";
  private static final String UNIQUE_ID = "UNIQUE_ID";
  private static final String NULL_STRING = "";

  private FilterConfig filterConfig;

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config)
    throws ServletException
  {
    this.filterConfig = config;
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(
    ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    // process request
    org.apache.log4j.MDC.put(REMOTE_ADDR, request.getRemoteAddr());

    if(request instanceof HttpServletRequest)
    {
      org.apache.log4j.MDC.put(SERVLET_PATH, ((HttpServletRequest) request).getServletPath());
    }

    // next filter in chain
    chain.doFilter(request, response);

    // filter response

    // clean up log4j MDC
    org.apache.log4j.MDC.put(REMOTE_ADDR, NULL_STRING);
    org.apache.log4j.MDC.put(UNIQUE_ID, NULL_STRING);
    org.apache.log4j.MDC.put(SERVLET_PATH, NULL_STRING);
  }

  /**
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy()
  {
    ;
  }
}
