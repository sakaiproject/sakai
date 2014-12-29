/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.syllabus;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;


public class FileUploadFilter implements Filter 
{
  protected int sizeThreshold = 2;
  protected long sizeMax = 2048;
  private String repositoryPath = "/fileUpload";
  private FileWriter fw;
  
  public void init(FilterConfig config) throws ServletException 
  {
    repositoryPath = config.getInitParameter("org.sakaiproject.tool.syllabus.FileUploadFilter.repositoryPath");
    
    try 
    {
      String paramValue = config.getInitParameter("org.sakaiproject.tool.syllabus.FileUploadFilter.sizeThreshold");

      if (paramValue != null) 
        sizeThreshold = Integer.parseInt(paramValue);
      paramValue = config.getInitParameter("org.sakaiproject.tool.syllabus.FileUploadFilter.sizeMax");
      if (paramValue != null) 
        sizeMax = Long.parseLong(paramValue);
    }
    catch (NumberFormatException ex) 
    {
      ServletException servletEx = new ServletException();
      servletEx.initCause(ex);
      throw servletEx;
    }
  }
  
  public void destroy() 
  {
  }
  
  public void doFilter(ServletRequest request, 
          ServletResponse response, FilterChain chain) 
  				throws IOException, ServletException 
  {
    if (!(request instanceof HttpServletRequest)) 
    {
      chain.doFilter(request, response);
      return;
    }
      
    HttpServletRequest httpRequest = (HttpServletRequest) request;
      
    boolean isMultipartContent = FileUpload.isMultipartContent(httpRequest);
    if (!isMultipartContent) 
    {
      chain.doFilter(request, response);
      return;
    }
      
    DiskFileUpload upload = new DiskFileUpload();
    if (repositoryPath != null) 
      upload.setRepositoryPath(repositoryPath);
      
    try 
    {
      List list = upload.parseRequest(httpRequest);
      final Map map = new HashMap();
      for (int i = 0; i < list.size(); i++) 
      {
        FileItem item = (FileItem) list.get(i);
        String str = item.getString();
        if (item.isFormField())
          map.put(item.getFieldName(), new String[] { str });
        else
          httpRequest.setAttribute(item.getFieldName(), item);
      }
          
      chain.doFilter(new 
                  HttpServletRequestWrapper(httpRequest) {
              public Map getParameterMap() {
                  return map;
              }                   
              public String[] getParameterValues(String name) {
                  Map map = getParameterMap();
                  return (String[]) map.get(name);
              }
              public String getParameter(String name) {
                  String[] params = getParameterValues(name);
                  if (params == null) return null;
                  return params[0];
              }
              public Enumeration getParameterNames() {
                  Map map = getParameterMap();
                  return Collections.enumeration(map.keySet());
              }
          }, response);
    } 
    catch (FileUploadException ex) 
    {
      ServletException servletEx = new ServletException();
      servletEx.initCause(ex);
      throw servletEx;
    }      
  }   
}



