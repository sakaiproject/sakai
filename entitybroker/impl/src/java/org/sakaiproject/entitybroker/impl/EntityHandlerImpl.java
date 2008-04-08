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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputHTMLable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputHTMLdefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputJSONable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputJSONdefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputXMLable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputXMLdefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestInterceptor;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.BasicEntity;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestGetterImpl;
import org.sakaiproject.entitybroker.impl.util.MapConverter;
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
            extension = OutputHTMLable.EXTENSION;
         }
         req.setAttribute("extension", extension);

         // handle the before interceptor
         RequestInterceptor interceptor = (RequestInterceptor) entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, RequestInterceptor.class);
         if (interceptor != null) {
            interceptor.before(req, res, ref);
         }

         // now handle the extensions
         if (OutputJSONable.EXTENSION.equals(extension)) {
            EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, OutputJSONable.class);
            if (provider != null) {
               OutputJSONdefineable def = (OutputJSONdefineable) entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, OutputJSONdefineable.class);
               if (def != null) {
                  handleClassLoaderAccess(def, req, res, ref);
               } else {
                  // internally handle this request
                  encodeToResponse(req, res, ref, OutputJSONable.EXTENSION);
               }
            } else {
               throw new EntityException( "Cannot access JSON for this path (" + path + ") for prefix ("
                  + ref.prefix + ") for entity (" + ref.toString() + ")", ref.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
            }
         } else if (OutputXMLable.EXTENSION.equals(extension)) {
            EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, OutputXMLable.class);
            if (provider != null) {
               OutputXMLdefineable def = (OutputXMLdefineable) entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, OutputXMLdefineable.class);
               if (def != null) {
                  handleClassLoaderAccess(def, req, res, ref);
               } else {
                  // internally handle this request
                  encodeToResponse(req, res, ref, OutputXMLable.EXTENSION);
               }
            } else {
               throw new EntityException( "Cannot access XML for this path (" + path + ") for prefix ("
                  + ref.prefix + ") for entity (" + ref.toString() + ")", ref.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
            }            
         } else if (OutputHTMLable.EXTENSION.equals(extension)) {
            // currently we assume everyone handles HTML, maybe we shouldn't?
            OutputHTMLdefineable def = (OutputHTMLdefineable) entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, OutputHTMLdefineable.class);
            if (def != null) {
               handleClassLoaderAccess(def, req, res, ref);
            } else {
               // no special handling so send on to the standard access provider if one can be found
               HttpServletAccessProvider accessProvider = accessProviderManager.getProvider(ref.prefix);
               if (accessProvider == null) {
                  String message = "Attempted to access an entity URL path ("
                              + path + ") for an entity (" + ref.toString() + ") when there is no " 
                              + "HttpServletAccessProvider to handle the request for prefix (" + ref.prefix + ")";
                  throw new EntityException( message, ref.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
               } else {
                  handleClassLoaderAccess(accessProvider, req, res, ref);
               }
            }
         } else {
            String message = "Attempted to access an entity URL path ("
               + path + ") for an entity (" + ref.toString()
               + ") with extension (" + extension + ") when extension is of unknown type";
            throw new EntityException( message, ref.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );            
         }

         // handle the after interceptor
         if (interceptor != null) {
            interceptor.after(req, res, ref);
         }
      }

      return ref.toString();
   }


   /**
    * Wrap this in an appropriate classloader before handling the request to ensure we
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


   /**
    * stores the various xstream processors for handling the different types of data
    */
   private Map<String, XStream> xstreams = new HashMap<String, XStream>();
   /**
    * Internal method for processing an entity or collection into an output format,
    * Currently only handles JSON and XML correctly
    */
   public void encodeToResponse(HttpServletRequest req, HttpServletResponse res, EntityReference ref, String extension) {
      boolean collection = false;
      Object toEncode = getEntityObject(ref);
      if (toEncode == null) {
         collection = true;
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, CollectionResolvable.class);
         if (provider != null) {
            Search search = makeSearchFromRequest(req);
            toEncode = ((CollectionResolvable)provider).getEntities(ref, search);
         }
      }
      if (toEncode == null) {
         throw new RuntimeException("Failed to encode data for entity (" + ref.toString() + "), entity object to encode could not be found");
      } else {
         String encoding = "text/plain";
         XStream encoder = null;
         if (OutputJSONable.EXTENSION.equals(extension)) {
            if (! xstreams.containsKey(extension)) {
               XStream x = new XStream(new JsonHierarchicalStreamDriver());
               x.registerConverter(new MapConverter(), 3);
               xstreams.put( extension, x );
            }
            encoder = xstreams.get(extension);
            encoding = "text/javascript";
         } else if (OutputXMLable.EXTENSION.equals(extension)) {
            if (! xstreams.containsKey(extension)) {
               XStream x = new XStream(new DomDriver());
               x.registerConverter(new MapConverter(), 3);
               xstreams.put( extension, x );
            }
            encoder = xstreams.get(extension);
            encoding = "text/xml";
         } else {
            encoder = null;
         }

         if (encoder != null) {
            if (collection) {
               // this is a collection of some kind so handle it specially
               Class<?> encodeClass = toEncode.getClass();
               encoder.alias("entities", encodeClass);
               boolean isCollection = Collection.class.isAssignableFrom(encodeClass);
               if (isCollection) {
                  Class<?> entityClass = getClassFromCollection((Collection<?>)toEncode);
                  encoder.alias(ref.prefix, entityClass);
               }
            } else {
               Class<?> encodeClass = toEncode.getClass();
               encoder.alias(ref.prefix, encodeClass); // add alias for the current entity prefix
            }
         }
         try {
            byte[] b = null;
            if (encoder != null) {
               b = encoder.toXML(toEncode).getBytes("UTF-8");
            } else {
               // just to string this and dump it out
               String s = "<b>" + ref.prefix + "</b>: " + toEncode.toString();
               b = s.getBytes("UTF-8");
            }
            res.setContentType(encoding);
            res.setCharacterEncoding("UTF-8");
            res.setContentLength(b.length);
            res.getOutputStream().write(b);
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode UTF-8: " + ref.toString(), e);
         } catch (IOException e) {
            throw new RuntimeException("Failed to encode into response: " + ref.toString(), e);
         }
      }
   }

   /**
    * This looks at search parameters and returns anything it finds in the
    * request parameters that can be put into the search
    * 
    * @param req a servlet request
    * @return a search filter object
    */
   @SuppressWarnings("unchecked")
   public Search makeSearchFromRequest(HttpServletRequest req) {
      Search search = new Search();
      if (req != null) {
         Map<String, String[]> params = req.getParameterMap();
         if (params != null) {
            for (String key : params.keySet()) {
               search.addRestriction( new Restriction("key", req.getParameter(key)) );
            }
         }
      }
      return search;
   }

   /**
    * Finds a class type that is in the containing collection,
    * will always return something (failsafe to Object.class)
    * @param collection
    * @return the class type contained in this collecion
    */
   @SuppressWarnings("unchecked")
   public Class<?> getClassFromCollection(Collection collection) {
      // try to get the type of entities out of this collection
      Class<?> c = Object.class;
      if (collection != null) {
         if (! collection.isEmpty()) {
            c = collection.iterator().next().getClass();
         } else {
            // this always gets Object.class -AZ
            c = collection.toArray().getClass().getComponentType();                     
         }
      }
      return c;
   }


   /**
    * Get an entity object of some kind for this reference if it has an id,
    * will create a {@link BasicEntity} if this entity type is not resolveable,
    * will simply return null if no id is available in this reference
    * @param ref an entity reference
    * @return the entity object for this reference or null if none can be retrieved
    */
   public Object getEntityObject(EntityReference ref) {
      Object entity = null;
      if (ref instanceof IdEntityReference 
            && ((IdEntityReference)ref).id != null) {
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, Resolvable.class);
         if (provider != null) {
            entity = ((Resolvable)provider).getEntity(ref);
         } else {
            // return basic entity information
            entity = new BasicEntity(ref);
         }
      }
      return entity;
   }

}
