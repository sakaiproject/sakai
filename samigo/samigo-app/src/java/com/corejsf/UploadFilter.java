/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
* Copyright (c) 2004 Sun Microsystems from the Java Series, Core Java ServerFaces
* source freely distributable.
* see http://www.sun.com/books/java_series.html
 ***********************************************************************************
* Modifications Copyright (c) 2005, 2006 Sakai Foundation
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
package com.corejsf;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.sakaiproject.component.cover.ServerConfigurationService;

@Slf4j
public class UploadFilter implements Filter {

   private int sizeThreshold = -1;
   private long sizeMax = -1;
   private String repositoryPath;
   private String saveMediaToDb = "false";

   public void init(FilterConfig config) throws ServletException {
       repositoryPath = ServerConfigurationService.getString(
	 "samigo.answerUploadRepositoryPath", "${sakai.home}/samigo/answerUploadRepositoryPath/");

      try {
         String paramValue = ServerConfigurationService.getString(
            "samigo.sizeThreshold", "1024");
         if (paramValue != null)
            sizeThreshold = Integer.parseInt(paramValue);

         paramValue = ServerConfigurationService.getString(
            "samigo.sizeMax", "40960");
         if (paramValue != null)
            sizeMax = Long.parseLong(paramValue);

         paramValue = ServerConfigurationService.getString(
            "samigo.saveMediaToDb", "true");
         if (paramValue != null)
            saveMediaToDb = paramValue;

         log.debug("**** repositoryPath="+repositoryPath);
         log.debug("**** saveMediaToDb="+saveMediaToDb);
         log.debug("**** sizeThreshold="+sizeThreshold);
         log.debug("**** sizeMax="+sizeMax);

  }
      catch (NumberFormatException ex) {
         ServletException servletEx = new ServletException();
         servletEx.initCause(ex);
         throw servletEx;
      }
      ServletContext context = config.getServletContext();
      context.setAttribute("FILEUPLOAD_REPOSITORY_PATH",repositoryPath);
      context.setAttribute("FILEUPLOAD_SIZE_THRESHOLD", Integer.valueOf(sizeThreshold));
      context.setAttribute("FILEUPLOAD_SIZE_MAX", Long.valueOf(sizeMax));
      context.setAttribute("FILEUPLOAD_SAVE_MEDIA_TO_DB", saveMediaToDb);
   }

   public void destroy() {
   }

   public void doFilter(ServletRequest request,
      ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

      if (!(request instanceof HttpServletRequest)) {
         chain.doFilter(request, response);
         return;
      }

      HttpServletRequest httpRequest = (HttpServletRequest) request;
      boolean isMultipartContent = FileUpload.isMultipartContent(httpRequest);
      if (!isMultipartContent) {
         chain.doFilter(request, response);
         return;
      }

      DiskFileUpload upload = new DiskFileUpload();
      if (repositoryPath != null)
         upload.setRepositoryPath(repositoryPath);

      try {
         List list = upload.parseRequest(httpRequest);
         final Map map = new HashMap();
         for (int i = 0; i < list.size(); i ++) {
            FileItem item = (FileItem) list.get(i);
            if (item.isFormField()){
              String str = item.getString("UTF-8");
              map.put(item.getFieldName(), new String[] {str});
            }
            else{
              httpRequest.setAttribute(item.getFieldName(), item);
            }
         }

         chain.doFilter(new
            HttpServletRequestWrapper(httpRequest) {
               public Map getParameterMap() {
                  return map;
               }
               // busywork follows ... should have been part of the wrapper
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
      } catch (FileUploadException ex) {
         log.error(ex.getMessage());
         ServletException servletEx = new ServletException();
         servletEx.initCause(ex);
         throw servletEx;
      }
   }
}
