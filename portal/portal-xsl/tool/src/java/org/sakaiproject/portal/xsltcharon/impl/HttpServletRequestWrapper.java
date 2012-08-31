/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.portal.xsltcharon.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletInputStream;
import javax.servlet.RequestDispatcher;
import java.util.Enumeration;
import java.util.Map;
import java.util.Locale;
import java.security.Principal;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.BufferedReader;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jul 23, 2007
 * Time: 9:49:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class HttpServletRequestWrapper implements HttpServletRequest {

   private HttpServletRequest base;
   private String servletPath = null;

   public HttpServletRequestWrapper(HttpServletRequest base, String servletPath) {
      this.base = base;
      this.servletPath = servletPath;
   }

   public String getAuthType() {
      return base.getAuthType();
   }

   public Cookie[] getCookies() {
      return base.getCookies();
   }

   public long getDateHeader(String s) {
      return base.getDateHeader(s);
   }

   public String getHeader(String s) {
      return base.getHeader(s);
   }

   public Enumeration getHeaders(String s) {
      return base.getHeaders(s);
   }

   public Enumeration getHeaderNames() {
      return base.getHeaderNames();
   }

   public int getIntHeader(String s) {
      return base.getIntHeader(s);
   }

   public String getMethod() {
      return base.getMethod();
   }

   public String getPathInfo() {
      return base.getPathInfo();
   }

   public String getPathTranslated() {
      return base.getPathTranslated();
   }

   public String getContextPath() {
      return base.getContextPath();
   }

   public String getQueryString() {
      return base.getQueryString();
   }

   public String getRemoteUser() {
      return base.getRemoteUser();
   }

   public boolean isUserInRole(String s) {
      return base.isUserInRole(s);
   }

   public Principal getUserPrincipal() {
      return base.getUserPrincipal();
   }

   public String getRequestedSessionId() {
      return base.getRequestedSessionId();
   }

   public String getRequestURI() {
      return base.getRequestURI();
   }

   public StringBuffer getRequestURL() {
      return base.getRequestURL();
   }

   public String getServletPath() {
      if (servletPath == null) {
         return base.getServletPath();
      }
      else {
         return servletPath;
      }
   }

   public HttpSession getSession(boolean b) {
      return base.getSession(b);
   }

   public HttpSession getSession() {
      return base.getSession();
   }

   public boolean isRequestedSessionIdValid() {
      return base.isRequestedSessionIdValid();
   }

   public boolean isRequestedSessionIdFromCookie() {
      return base.isRequestedSessionIdFromCookie();
   }

   public boolean isRequestedSessionIdFromURL() {
      return base.isRequestedSessionIdFromURL();
   }

   public boolean isRequestedSessionIdFromUrl() {
      return base.isRequestedSessionIdFromUrl();
   }

   public Object getAttribute(String s) {
      return base.getAttribute(s);
   }

   public Enumeration getAttributeNames() {
      return base.getAttributeNames();
   }

   public String getCharacterEncoding() {
      return base.getCharacterEncoding();
   }

   public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
      base.setCharacterEncoding(s);
   }

   public int getContentLength() {
      return base.getContentLength();
   }

   public String getContentType() {
      return base.getContentType();
   }

   public ServletInputStream getInputStream() throws IOException {
      return base.getInputStream();
   }

   public String getParameter(String s) {
      return base.getParameter(s);
   }

   public Enumeration getParameterNames() {
      return base.getParameterNames();
   }

   public String[] getParameterValues(String s) {
      return base.getParameterValues(s);
   }

   public Map getParameterMap() {
      return base.getParameterMap();
   }

   public String getProtocol() {
      return base.getProtocol();
   }

   public String getScheme() {
      return base.getScheme();
   }

   public String getServerName() {
      return base.getServerName();
   }

   public int getServerPort() {
      return base.getServerPort();
   }

   public BufferedReader getReader() throws IOException {
      return base.getReader();
   }

   public String getRemoteAddr() {
      return base.getRemoteAddr();
   }

   public String getRemoteHost() {
      return base.getRemoteHost();
   }

   public void setAttribute(String s, Object o) {
      base.setAttribute(s, o);
   }

   public void removeAttribute(String s) {
      base.removeAttribute(s);
   }

   public Locale getLocale() {
      return base.getLocale();
   }

   public Enumeration getLocales() {
      return base.getLocales();
   }

   public boolean isSecure() {
      return base.isSecure();
   }

   public RequestDispatcher getRequestDispatcher(String s) {
      return base.getRequestDispatcher(s);
   }

   public String getRealPath(String s) {
      return base.getRealPath(s);
   }

   public int getLocalPort() {
      return base.getLocalPort();
   }

   public int getRemotePort() {
      return base.getRemotePort();
   }

   public String getLocalName() {
      return base.getLocalName();
   }

   public String getLocalAddr() {
      return base.getLocalAddr();
   }
}
