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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;


public class UploadFilter implements Filter {
   private int sizeThreshold = -1;
   private long sizeMax = -1;
   private String repositoryPath;

   public void init(FilterConfig config) throws ServletException {
      repositoryPath = config.getInitParameter(
         "com.corejsf.UploadFilter.repositoryPath");
      try {
         String paramValue = config.getInitParameter(
            "com.corejsf.UploadFilter.sizeThreshold");
         if (paramValue != null)
            sizeThreshold = Integer.parseInt(paramValue);
         paramValue = config.getInitParameter(
            "com.corejsf.UploadFilter.sizeMax");
         if (paramValue != null)
            sizeMax = Long.parseLong(paramValue);
      }
      catch (NumberFormatException ex) {
         ServletException servletEx = new ServletException();
         servletEx.initCause(ex);
         throw servletEx;
      }
      //System.out.println("*** repositoryPath="+repositoryPath);
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
            String str = item.getString();
            if (item.isFormField()){
              map.put(item.getFieldName(), new String[] {str});
              //System.out.println("item is form field, field name= "+item.getFieldName()+":"+str);
            }
            else{
              httpRequest.setAttribute(item.getFieldName(), item);
              //System.out.println("item is NOT form field, fieldname="+item.getFieldName()
                                 // +"name="+item.getName()+":"+item);
            }
         }

         chain.doFilter(new
            HttpServletRequestWrapper(httpRequest) {
               public Map getParameterMap() {
                  return map;
               }
               // busywork follows ... should have been part of the wrapper
               public String[] getParameterValues(String name) {
                  //System.out.println("**#1."+name);
                  Map map = getParameterMap();
                  return (String[]) map.get(name);
               }
               public String getParameter(String name) {
                 //System.out.println("**#2."+name);
                  String[] params = getParameterValues(name);
                  if (params == null) return null;
                  return params[0];
               }
               public Enumeration getParameterNames() {
                  //System.out.println("**#3. keyset");
                  Map map = getParameterMap();
                  return Collections.enumeration(map.keySet());
               }
            }, response);
      } catch (FileUploadException ex) {
         System.out.println(ex.getMessage());
         ServletException servletEx = new ServletException();
         servletEx.initCause(ex);
         throw servletEx;
      }
   }
}
