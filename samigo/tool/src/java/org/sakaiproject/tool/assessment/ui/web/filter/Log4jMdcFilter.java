/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
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
