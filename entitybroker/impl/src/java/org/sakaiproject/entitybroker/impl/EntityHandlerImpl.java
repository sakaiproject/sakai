/**
 * $Id$
 * $URL$
 * EntityHandler.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.HTMLable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.HTMLdefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.JSONable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.JSONdefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestInterceptor;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.XMLable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.XMLdefineable;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestGetterImpl;
import org.sakaiproject.entitybroker.util.ClassLoaderReporter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Common implementation of the handler for the EntityBroker system. This should be used in
 * preference to the EntityBroker directly by implementation classes part of the EntityBroker
 * scheme, rather than the user-facing EntityBroker directly.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class EntityHandlerImpl implements EntityRequestHandler {

   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private HttpServletAccessProviderManager accessProviderManager;
   public void setAccessProviderManager(HttpServletAccessProviderManager accessProviderManager) {
      this.accessProviderManager = accessProviderManager;
   }

   private RequestGetter requestGetter;
   public void setRequestGetter(RequestGetter requestGetter) {
      this.requestGetter = requestGetter;
   }

   private ServerConfigurationService serverConfigurationService;
   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
      this.serverConfigurationService = serverConfigurationService;
   }


   /**
    * Determines if an entity exists based on the reference
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optionaly the local id
    * @return true if entity exists, false otherwise
    */
   public boolean entityExists(String reference) {
      String prefix = IdEntityReference.getPrefix(reference);
      EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
      if (provider == null) {
         // no provider found so no entity can exist
         return false;
      } else if (!(provider instanceof CoreEntityProvider)) {
         return true;
      }
      return ((CoreEntityProvider) provider).entityExists(IdEntityReference.getID(reference));
   }

   /**
    * Creates the full URL to an entity using the sakai {@link ServerConfigurationService}, 
    * (e.g. http://server:8080/direct/entity/123/)<br/>
    * <br/>
    * <b>Note:</b> the webapp name (relative URL path) of the direct servlet, of "/direct" 
    * is hardcoded into this method, and the
    * {@link org.sakaiproject.entitybroker.servlet.DirectServlet} must be deployed there on this
    * server.
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optionally the local id
    * @return the full URL to a specific entity
    */
   public String getEntityURL(String reference) {
      // try to get the prefix to ensure this is at least a valid formatted reference, should this
      // make sure the entity exists?
      EntityReference.getPrefix(reference);
      String togo = serverConfigurationService.getServerUrl() + "/direct" + reference;
      return togo;
   }

   /**
    * Returns the provider, if any, responsible for handling a reference,
    * this will be the core provider only
    */
   public EntityProvider getProvider(String reference) {
      String prefix = EntityReference.getPrefix(reference);
      EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
      return provider;
   }

   /**
    * Parses an entity reference into the appropriate reference form
    * 
    * @param reference a unique entity reference
    * @return null if there is no provider found for the prefix parsed out
    * @throws IllegalArgumentException if there is a failure during parsing
    */
   public EntityReference parseReference(String reference) {
      String prefix = EntityReference.getPrefix(reference);
      ReferenceParseable provider = (ReferenceParseable) entityProviderManager
            .getProviderByPrefixAndCapability(prefix, ReferenceParseable.class);
      if (provider == null) {
         return null;
      }
      else if (provider instanceof BlankReferenceParseable) {
         return parseDefaultReference(prefix, reference);
      }
      else {
         Object exemplar = provider.getParsedExemplar();
         if (exemplar.getClass() == EntityReference.class) {
            return new EntityReference(provider.getEntityPrefix());
         }
         else {
            // cannot test this in a meaningful way so the tests are designed to not get here -AZ
            throw new UnsupportedOperationException(
                  "Support for custom EntityReference classes is not yet supported");
         }
      }
   }

   /**
    * Standard way to parse the reference, attempts to get the id and
    * if fails then just uses the prefix only
    * 
    * @param prefix only pass valid prefixes to this method
    * @param reference a unique entity reference
    * @return an {@link EntityReference}
    */
   protected EntityReference parseDefaultReference(String prefix, String reference) {
      EntityReference ref = null;
      try {
         ref = new IdEntityReference(reference);
      }
      catch (IllegalArgumentException e) {
         // fall back to the simplest reference type
         ref = new EntityReference(prefix);
      }
      return ref;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityRequestHandler#handleEntityAccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   public String handleEntityAccess(HttpServletRequest req, HttpServletResponse res, String path) {
      // get the path info if not set
      if (path == null) {
         path = req.getPathInfo();
      }

      EntityReference ref;
      try {
         ref = parseReference(path);
      } catch (IllegalArgumentException e) {
         // indicates we could not parse the reference
         throw new EntityException("Could not parse entity path ("+path+"): " + e.getMessage(), path, HttpServletResponse.SC_BAD_REQUEST);
      }

      if (ref == null) {
         // no provider for this entity prefix
         throw new EntityException( "No entity provider could be found to handle the prefix in this path: " + path, 
               path, HttpServletResponse.SC_NOT_IMPLEMENTED );
      } else if (! entityExists(ref.toString())) {
         // reference parsing failure
         String message = "Attempted to access an entity URL path (" + path + ") for an entity ("
            + ref.toString() + ") that does not exist";
         throw new EntityException( message, ref.toString(), HttpServletResponse.SC_NOT_FOUND );
      } else {
         // reference successfully parsed
         res.setStatus(HttpServletResponse.SC_OK); // other things can switch this later on

         // store the current request and response
         ((RequestGetterImpl) requestGetter).setRequest(req);
         ((RequestGetterImpl) requestGetter).setResponse(res);

         // check for extensions
         String extension = getExtension(path);
         if (extension == null) {
            extension = HTMLable.EXTENSION;
         }
         req.setAttribute("extension", extension);

         // handle the before interceptor
         RequestInterceptor interceptor = (RequestInterceptor) entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, RequestInterceptor.class);
         if (interceptor != null) {
            interceptor.before(req, res, ref);
         }

         // now handle the extensions
         if (JSONable.EXTENSION.equals(extension)) {
            EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, JSONable.class);
            if (provider != null) {
               JSONdefineable def = (JSONdefineable) entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, JSONdefineable.class);
               if (def != null) {
                  handleClassLoaderAccess(def, req, res, ref);
               } else {
                  // internally handle this request
                  makeJSONData(req, res, ref);
               }
            } else {
               throw new EntityException( "Cannot access JSON for this path (" + path + ") for prefix ("
                  + ref.prefix + ") for entity (" + ref.toString() + ")", ref.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
            }
         } else if (XMLable.EXTENSION.equals(extension)) {
            EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, XMLable.class);
            if (provider != null) {
               XMLdefineable def = (XMLdefineable) entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, XMLdefineable.class);
               if (def != null) {
                  handleClassLoaderAccess(def, req, res, ref);
               } else {
                  // internally handle this request
                  makeXMLData(req, res, ref);
               }
            } else {
               throw new EntityException( "Cannot access XML for this path (" + path + ") for prefix ("
                  + ref.prefix + ") for entity (" + ref.toString() + ")", ref.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
            }            
         } else if (HTMLable.EXTENSION.equals(extension)) {
            // currently we assume everyone handles HTML, maybe we shouldn't?
            HTMLdefineable def = (HTMLdefineable) entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, HTMLdefineable.class);
            if (def != null) {
               handleClassLoaderAccess(def, req, res, ref);
            } else {
               // no special handling so send on to the standard access provider if one can be found
               HttpServletAccessProvider accessProvider = accessProviderManager.getProvider(ref.prefix);
               if (accessProvider == null) {
                  String message = "Attempted to access an entity URL path ("
                              + path + ") for an entity (" + ref.toString()
                              + ") when there is no HttpServletAccessProvider to handle the request for prefix ("
                              + ref.prefix + ")";
                  throw new EntityException( message, ref.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
               } else {
                  handleClassLoaderAccess(accessProvider, req, res, ref);
               }
            }
         }

         // handle the after interceptor
         if (interceptor != null) {
            interceptor.after(req, res, ref);
         }
      }

      return ref.toString();
   }



   /**
    * Wrap this in appropriate classloader before handling the request to ensure we
    * do not get ugly classloader failures
    * 
    * @param accessProvider
    * @param req
    * @param res
    * @param ref
    */
   private void handleClassLoaderAccess(HttpServletAccessProvider accessProvider,
         HttpServletRequest req, HttpServletResponse res, EntityReference ref) {
      ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      try {
         ClassLoader newClassLoader = accessProvider.getClass().getClassLoader();
         // check to see if this access provider reports the correct classloader
         if (accessProvider instanceof ClassLoaderReporter) {
            newClassLoader = ((ClassLoaderReporter) accessProvider).getSuitableClassLoader();
         }
         Thread.currentThread().setContextClassLoader(newClassLoader);
         // send request to the access provider which will route it on to the correct entity world
         accessProvider.handleAccess(req, res, ref);
      } finally {
         Thread.currentThread().setContextClassLoader(currentClassLoader);
      }
   }


   /**
    * @param path
    * @return the extension or null if none can be found
    */
   public String getExtension(String path) {
      String extension = null;
      int extensionLoc = path.lastIndexOf('.', path.length());
      if (extensionLoc > 0 
            && extensionLoc < path.length()-1) {
         extension = path.substring(extensionLoc + 1);
      }
      return extension;
   }

   private XStream xstreamJSON = null;
   /**
    * @param req
    * @param res
    * @param ref
    * @return the entity that was encoded
    * @throws RuntimeException if this fails for any reason
    */
   public Object makeJSONData(HttpServletRequest req, HttpServletResponse res, EntityReference ref) {
      if (xstreamJSON == null) {
         xstreamJSON = new XStream(new JsonHierarchicalStreamDriver());
      }
      Object entity = getEntityObject(ref);
      if (entity == null) {
         throw new RuntimeException("Failed to encode JSON data for entity (" + ref + "), could not locate entity data");
      } else {
         xstreamJSON.alias(ref.prefix, entity.getClass()); // add alias for the current entity prefix
         try {
            byte[] b = xstreamJSON.toXML(entity).getBytes("UTF-8");
            res.setContentType("text/javascript");
            res.setCharacterEncoding("UTF-8");
            res.setContentLength(b.length);
            res.getOutputStream().write(b);
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode UTF-8 JSON: " + ref, e);
         } catch (IOException e) {
            throw new RuntimeException("Failed to encode JSON into response: " + ref, e);
         }         
      }
      return entity;
   }

   private XStream xstreamXML = null;
   /**
    * @param req
    * @param res
    * @param ref
    * @return the entity that was encoded
    * @throws RuntimeException if this fails for any reason
    */
   public Object makeXMLData(HttpServletRequest req, HttpServletResponse res, EntityReference ref) {
      if (xstreamXML == null) {
         xstreamXML = new XStream(new DomDriver());
      }
      Object entity = getEntityObject(ref);
      if (entity == null) {
         throw new RuntimeException("Failed to encode XML data for entity (" + ref + "), could not locate entity data");
      } else {
         xstreamXML.alias(ref.prefix, entity.getClass()); // add alias for the current entity prefix
         try {
            byte[] b = xstreamXML.toXML(entity).getBytes("UTF-8");
            res.setContentType("text/xml");
            res.setCharacterEncoding("UTF-8");
            res.setContentLength(b.length);
            res.getOutputStream().write(b);
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode UTF-8 XML: " + ref, e);
         } catch (IOException e) {
            throw new RuntimeException("Failed to encode XML into response: " + ref, e);
         }         
      }
      return entity;
   }


   /**
    * Get an entity object of some kind for this reference,
    * will create a {@link BasicEntity} if this entity type is not resolveable
    * @param ref an entity reference
    * @return the entity object for this reference or null if none can be retrieved
    */
   public Object getEntityObject(EntityReference ref) {
      Object entity = null;
      EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, Resolvable.class);
      if (provider != null) {
         // TODO - what about the case of multiple entities to return?
         entity = ((Resolvable)provider).getEntity(ref);
      } else {
         entity = new BasicEntity(ref);
      }
      return entity;
   }

   /**
    * Very basic entity object, used when there is no Resolveable entity
    * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
    */
   public class BasicEntity {
      public String reference;
      public String prefix;
      public String id;
      public boolean exists = true;
      public boolean resolved = false;
      public String message = "Could not resolve entity object, returning known metadata";

      public BasicEntity(EntityReference ref) {
         this.reference = ref.toString();
         this.prefix = ref.prefix;
         if (ref instanceof IdEntityReference) {
            this.id = ((IdEntityReference)ref).id;
         }
      }
      
      public String getReference() {
         return reference;
      }
      
      public String getPrefix() {
         return prefix;
      }
      
      public String getId() {
         return id;
      }
      
      public boolean isExists() {
         return exists;
      }
   }

}
