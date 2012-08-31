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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.api.PortalRenderEngine;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.webapp.api.WebappResourceManager;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jul 13, 2007
 * Time: 12:06:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class XsltRenderEngine implements PortalRenderEngine {

   private static final String XSLT_CONTEXT = "xsltCharon";

   private static final Log log = LogFactory.getLog(XsltRenderEngine.class);

   /** injected **/
   private PortalService portalService;
   private String defaultTransformerPath;

   private Templates defaultTemplates;
   private URIResolver libraryServletResolver;
   private URIResolver servletResolver;

   private Map<String, Templates> templates = new Hashtable<String, Templates>();
   private Map<String, String> transformerPaths;
   private WebappResourceManager libraryWebappResourceManager;
   private WebappResourceManager portalWebappResourceManager;
   private boolean cacheTemplates = true;
   private static final String XSLT_PORTAL_CACHE_TEMPLATES = "xslt-portal.cacheTemplates";

    public XsltRenderEngine() {
   }

   public XsltRenderEngine(PortalService portalService) {
      this.portalService = portalService;
   }

   /**
    * Initialise the render engine
    *
    * @throws Exception
    */
   public void init() throws Exception {
   }

   public void springInit() {
      if (ServerConfigurationService.getString(XSLT_PORTAL_CACHE_TEMPLATES) != null){
          try {
              cacheTemplates = Boolean.parseBoolean(ServerConfigurationService.getString(XSLT_PORTAL_CACHE_TEMPLATES, "true"));
          } catch (Exception e) {
              log.error("can't parse " + XSLT_PORTAL_CACHE_TEMPLATES + " into a boolean", e);
          }
      }

      getPortalService().addRenderEngine(XSLT_CONTEXT, this);
      try {
         setDefaultTemplates(createTemplate());
         setupTemplates();
      } catch (MalformedURLException e) {
         log.error("unable to init portal transformation", e);
      } catch (TransformerConfigurationException e) {
         log.error("unable to init portal transformation", e);
      } catch (IOException e) {
         log.error("unable to init portal transformation", e);
      }

      setServletResolver(new ServletResourceUriResolver(getPortalWebappResourceManager()));
      //setLibraryServletResolver(new ServletResourceUriResolver(getLibraryWebappResourceManager()));
   }

   protected void setupTemplates() throws IOException, TransformerConfigurationException {
      for (Iterator<Map.Entry<String, String>> i=getTransformerPaths().entrySet().iterator();
           i.hasNext();) {
         Map.Entry<String, String> entry = i.next();
         getTemplates().put(entry.getKey(), createTemplate(entry.getValue()));
      }
   }

   public void destroy() {
      getPortalService().removeRenderEngine(XSLT_CONTEXT, this);
   }

   /**
    * generate a non thread safe render context for the current
    * request/thread/operation
    *
    * @param request
    * @return new render context
    */
   public PortalRenderContext newRenderContext(HttpServletRequest request) {
      PortalRenderContext base =
         getPortalService().getRenderEngine(Portal.DEFAULT_PORTAL_CONTEXT, request).newRenderContext(request);

      return new XsltRenderContext(this, base, request);
   }

   /**
    * Render a PortalRenderContext against a template. The real template may be
    * based on a skining name, out output will be send to the Writer
    *
    * @param template
    * @param rcontext
    * @param out
    * @throws Exception
    */
   public void render(String template, PortalRenderContext rcontext, Writer out) throws Exception {
      XsltRenderContext xrc = (XsltRenderContext) rcontext;

      if (log.isTraceEnabled()) {
     	 log.trace("Portal trace is on, dumping PortalRenderContext to log:\n" + xrc.dump());
      }

      if (template.equals("site")) {
         Document doc = xrc.produceDocument();
         writeDocument(doc, out, xrc);
      }
      else {
         xrc.getBaseContext().getRenderEngine().render(template, xrc.getBaseContext(), out);
      }
   }

   protected void writeDocument(Document doc, Writer out, XsltRenderContext xrc) {
      try {
         StreamResult outputTarget = new StreamResult(out);

         Transformer transformer = getTransformer(xrc);
         transformer.transform(new DOMSource(doc), outputTarget);
      }
      catch (TransformerException e) {
         throw new RuntimeException(e);
      }

   }

   public Transformer getTransformer(XsltRenderContext xrc) {
      try {
         Templates templates = null;
         boolean skin = false;
         
         if (xrc.getAlternateTemplate() != null) {
            templates = getTemplates().get(xrc.getAlternateTemplate());
         }
         
         // test seperately in case the param wasnt' correct
         if (templates == null) {
            templates = getSkinTemplates(xrc);
            skin = true;
         }

         if (templates == null) {
            templates = getDefaultTemplates();
            skin = false;
         }
         
         Transformer trans = templates.newTransformer();
         trans.setURIResolver(getServletResolver(skin));
         return trans;
      }
      catch (TransformerConfigurationException e) {
         throw new RuntimeException(e);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   protected Templates getSkinTemplates(XsltRenderContext xrc) throws IOException, TransformerConfigurationException {
      String skin = (String) xrc.get("pageSkin");
      Templates returned = null;
      
      if (cacheTemplates) {
         returned = getTemplates().get("skin." + skin);
         if (returned != null) {
            return returned;
         }
      }
      
      InputStream skinStream = getLibraryWebappResourceManager().getResourceAsStream(
         "/skin/" + skin + "/portal.xslt");
      
      if (skinStream == null) {
         // check the default one
			String defaultSkin = ServerConfigurationService.getString("skin.default");
         skinStream = getLibraryWebappResourceManager().getResourceAsStream(
            "/skin/" + defaultSkin + "/portal.xslt");
         if (skinStream == null) {
            return null;
         }
      }
      
      returned = createTemplate(skinStream);
      
      if (cacheTemplates) {
         getTemplates().put("skin." + skin, returned);
      }
      
      return returned;
   }

   protected URIResolver getServletResolver(boolean skin) {
      if (skin) {
         return getLibraryServletResolver();
      }
      else {
         return getServletResolver();
      }
   }

   public Templates getDefaultTemplates() {
      return defaultTemplates;
   }

   public void setDefaultTemplates(Templates defaultTemplates) {
      this.defaultTemplates = defaultTemplates;
   }

   public URIResolver getServletResolver() {
      return servletResolver;
   }

   public void setServletResolver(URIResolver servletResolver) {
      this.servletResolver = servletResolver;
   }

   /**
    * prepare for a forward operation in the render engine, this might include
    * modifying the request attributes.
    *
    * @param req
    * @param res
    * @param p
    * @param skin
    */
   public void setupForward(HttpServletRequest req, HttpServletResponse res, Placement p, String skin) {
      getPortalService().getRenderEngine(Portal.DEFAULT_PORTAL_CONTEXT, req).setupForward(req, res, p, skin);
   }

   public PortalService getPortalService() {
      return portalService;
   }

   public void setPortalService(PortalService portalService) {
      this.portalService = portalService;
   }

   protected Templates createTemplate()
      throws IOException, TransformerConfigurationException {
	   // TODO Update default trasformer path to use neo transformer when available
      String transformerPath = getDefaultTransformerPath();
      return createTemplate(transformerPath);
   }

   protected Templates createTemplate(String transformerPath) throws IOException, TransformerConfigurationException {
      return createTemplate(getPortalWebappResourceManager().getResourceAsStream(transformerPath));
   }
   
   protected Templates createTemplate(URL url)
      throws IOException, TransformerConfigurationException {
      
      InputStream stream = url.openStream();
      String urlPath = url.toString();
      String systemId = urlPath.substring(0, urlPath.lastIndexOf('/') + 1);
      Templates templates = TransformerFactory.newInstance().newTemplates(
                     new StreamSource(stream, systemId));
      return templates;
   }

   protected Templates createTemplate(InputStream stream)
      throws IOException, TransformerConfigurationException {
      Templates templates = TransformerFactory.newInstance().newTemplates(
                     new StreamSource(stream));
      return templates;
   }

   public String getDefaultTransformerPath() {
      return defaultTransformerPath;
   }

   public void setDefaultTransformerPath(String defaultTransformerPath) {
      this.defaultTransformerPath = defaultTransformerPath;
   }

   public Map<String, Templates> getTemplates() {
      return templates;
   }

   public void setTemplates(Map<String, Templates> templates) {
      this.templates = templates;
   }

   public Map<String, String> getTransformerPaths() {
      return transformerPaths;
   }

   public void setTransformerPaths(Map<String, String> transformerPaths) {
      this.transformerPaths = transformerPaths;
   }

   public WebappResourceManager getLibraryWebappResourceManager() {
      if (libraryWebappResourceManager == null) {
         libraryWebappResourceManager = 
            (WebappResourceManager) ComponentManager.get("org.sakaiproject.webapp.api.WebappResourceManager.library");
      }
      return libraryWebappResourceManager;
   }

   public void setLibraryWebappResourceManager(WebappResourceManager libraryWebappResourceManager) {
      this.libraryWebappResourceManager = libraryWebappResourceManager;
   }

   public WebappResourceManager getPortalWebappResourceManager() {
      return portalWebappResourceManager;
   }

   public void setPortalWebappResourceManager(WebappResourceManager portalWebappResourceManager) {
      this.portalWebappResourceManager = portalWebappResourceManager;
   }

   public boolean isCacheTemplates() {
      return cacheTemplates;
   }

   public void setCacheTemplates(boolean cacheTemplates) {
      this.cacheTemplates = cacheTemplates;
   }

   public URIResolver getLibraryServletResolver() {
      if (libraryServletResolver == null) {
         libraryServletResolver = new ServletResourceUriResolver(getLibraryWebappResourceManager());
      }
      return libraryServletResolver;
   }

   public void setLibraryServletResolver(URIResolver libraryServletResolver) {
      this.libraryServletResolver = libraryServletResolver;
   }
}
