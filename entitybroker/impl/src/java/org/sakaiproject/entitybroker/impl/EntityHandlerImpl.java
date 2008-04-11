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
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.EntityUrlCustomizable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputDefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
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
@SuppressWarnings("deprecation")
public class EntityHandlerImpl implements EntityRequestHandler {

   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private HttpServletAccessProviderManager accessProviderManager;
   public void setAccessProviderManager(HttpServletAccessProviderManager accessProviderManager) {
      this.accessProviderManager = accessProviderManager;
   }

   private EntityViewAccessProviderManager entityViewAccessProviderManager;
   public void setEntityViewAccessProviderManager(
         EntityViewAccessProviderManager entityViewAccessProviderManager) {
      this.entityViewAccessProviderManager = entityViewAccessProviderManager;
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
    * @param reference an entity reference object
    * @return true if entity exists, false otherwise
    */
   public boolean entityExists(EntityReference ref) {
      boolean exists = false;
      if (ref != null) {
         EntityProvider provider = entityProviderManager.getProviderByPrefix(ref.getPrefix());
         if (provider == null) {
            // no provider found so no entity can't exist
            exists = false;
         } else if (!(provider instanceof CoreEntityProvider)) {
            // no core provider so assume it does exist
            exists = true;
         } else {
            if (ref.getId() == null) {
               // currently we assume exists if it is only a prefix
               exists = true;
            } else {
               exists = ((CoreEntityProvider) provider).entityExists( ref.getId() );
            }
         }
      }
      return exists;
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
    * @return the full URL to a specific entity or space
    */
   public String getEntityURL(String reference) {
      // ensure this is a valid reference first
      EntityReference ref = parseReference(reference);
      EntityView view = new EntityView();
      EntityUrlCustomizable custom = (EntityUrlCustomizable) entityProviderManager
            .getProviderByPrefixAndCapability(ref.getPrefix(), EntityUrlCustomizable.class);
      if (custom == null) {
         view.setEntityReference(ref);
      } else {
         // use the custom parsing templates
         view.loadParseTemplates( custom.getParseTemplates() );
      }
      view.setEntityReference(ref);
      String url = serverConfigurationService.getServerUrl() + "/direct" + view.toString();
      return url;
   }

   /**
    * Parses an entity reference into the appropriate reference form
    * 
    * @param reference a unique entity reference
    * @return the entity reference object or 
    * null if there is no provider found for the prefix parsed out
    * @throws IllegalArgumentException if there is a failure during parsing
    */
   public EntityReference parseReference(String reference) {
      String prefix = EntityReference.getPrefix(reference);
      EntityReference ref = null;
      ReferenceParseable provider = (ReferenceParseable) 
            entityProviderManager.getProviderByPrefixAndCapability(prefix, ReferenceParseable.class);
      if (provider == null) {
         ref = null;
      } else if (provider instanceof BlankReferenceParseable) {
         ref = new EntityReference(reference);
      } else {
         EntityReference exemplar = provider.getParsedExemplar();
         if (exemplar.getClass() == EntityReference.class) {
            ref = new EntityReference(reference);
         } else {
            // construct the custom class and then return it
            try {
               Constructor<? extends Object> m = exemplar.getClass().getConstructor(String.class);
               ref = (EntityReference) m.newInstance(reference);
            } catch (Exception e) {
               throw new RuntimeException("Failed to invoke a constructor which takes a single string "
               		+ "(reference="+reference+") for class: " + exemplar.getClass(), e);
            }
         }
      }
      return ref;
   }

   /**
    * Parses an entity URL into an entity view object,
    * handles custom parsing templates
    * 
    * @param entityURL an entity URL
    * @return the entity view object representing this URL or 
    * null if there is no provider found for the prefix parsed out
    * @throws IllegalArgumentException if there is a failure during parsing
    */
   public EntityView parseEntityUrl(String entityURL) {
      EntityView view = null;
      // first get the prefix
      String prefix = EntityReference.getPrefix(entityURL);
      // get the basic provider to see if this prefix is valid
      EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
      if (provider != null) {
         // this prefix is valid so check for custom entity templates
         EntityUrlCustomizable custom = (EntityUrlCustomizable) entityProviderManager
               .getProviderByPrefixAndCapability(prefix, EntityUrlCustomizable.class);
         if (custom == null) {
            view = new EntityView(entityURL);
         } else {
            // use the custom parsing templates to build the object
            view = new EntityView();
            view.loadParseTemplates( custom.getParseTemplates() );
            view.parseEntityUrl(entityURL);
         }
      }
      return view;
   }

   /**
    * Get an entity object of some kind for this reference if it has an id,
    * will simply return null if no id is available in this reference
    * 
    * @param reference a unique string representing an entity
    * @return the entity object for this reference or null if none can be retrieved
    */
   public Object getEntityObject(EntityReference ref) {
      Object entity = null;
      EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Resolvable.class);
      if (provider != null) {
         entity = ((Resolvable)provider).getEntity(ref);
      }
      return entity;
   }



   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityRequestHandler#handleEntityAccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   public String handleEntityAccess(HttpServletRequest req, HttpServletResponse res, String path) {
      // get the path info if not set
      if (path == null) {
         path = req.getPathInfo();
      }

      EntityView view;
      try {
         view = parseEntityUrl(path);
      } catch (IllegalArgumentException e) {
         // indicates we could not parse the reference
         throw new EntityException("Could not parse entity path ("+path+"): " + e.getMessage(), path, HttpServletResponse.SC_BAD_REQUEST);
      }

      if (view == null) {
         // no provider for this entity prefix
         throw new EntityException( "No entity provider could be found to handle the prefix in this path: " + path, 
               path, HttpServletResponse.SC_NOT_IMPLEMENTED );
      } else if (! entityExists(view.getEntityReference()) ) {
         // reference parsing failure
         String message = "Attempted to access an entity URL path (" + path + ") for an entity ("
            + view.getEntityReference().toString() + ") that does not exist";
         throw new EntityException( message, view.toString(), HttpServletResponse.SC_NOT_FOUND );
      } else {
         String prefix = view.getEntityReference().getPrefix();
         // reference successfully parsed
         res.setStatus(HttpServletResponse.SC_OK); // other things can switch this later on

         // store the current request and response
         ((RequestGetterImpl) requestGetter).setRequest(req);
         ((RequestGetterImpl) requestGetter).setResponse(res);

         // check for extensions
         if (view.getExtension() == null) {
            view.setExtension(Outputable.HTML); // update the view
         }
         req.setAttribute("extension", view.getExtension());

         // handle the before interceptor
         RequestInterceptor interceptor = (RequestInterceptor) entityProviderManager.getProviderByPrefixAndCapability(prefix, RequestInterceptor.class);
         if (interceptor != null) {
            interceptor.before(view, req, res);
         }

         // now handle the extensions
         Outputable output = (Outputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
         if (output == null) {
            // default output handling, send to the access provider if there is one
            handleAccessProvider(view, req, res);
         } else {
            OutputDefineable def = (OutputDefineable) entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputDefineable.class);
            if (def == null) {
               // internally handle this request if allowed
               if ( contains(output.getHandledExtensions(), view.getExtension()) ) {
                  if (Outputable.HTML.equals(view.getExtension())) {
                     // Special handling for HTML: send to access provider
                     handleAccessProvider(view, req, res);
                  } else {
                     // use internal processor
                     encodeToResponse(req, res, view);
                  }
               } else {
                  throw new EntityException( "Will not handle internal access to  "+view.getExtension()+" for this path (" 
                        + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference().toString() + ")", 
                        view.getEntityReference().toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
               }
            } else {
               if ( contains(output.getHandledExtensions(), view.getExtension()) ) {
                  handleClassLoaderAccess(def, req, res, view);
               } else {
                  throw new EntityException( "Will not handle defined access to  "+view.getExtension()+" for this path (" 
                        + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference().toString() + ")", 
                        view.getEntityReference().toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
               }
            }
         }

         // handle the after interceptor
         if (interceptor != null) {
            interceptor.after(view, req, res);
         }
      }

      return view.toString();
   }

   /**
    * Will choose whichever access provider is currently available to handle the request
    * @throws EntityException if there is none
    */
   @SuppressWarnings("deprecation")
   private void handleAccessProvider(EntityView view, HttpServletRequest req, HttpServletResponse res) {
      // no special handling so send on to the standard access provider if one can be found
      EntityViewAccessProvider evAccessProvider = entityViewAccessProviderManager.getProvider(view.getEntityReference().getPrefix());
      if (evAccessProvider == null) {
         // try the old type access provider then
         HttpServletAccessProvider httpAccessProvider = accessProviderManager.getProvider(view.getEntityReference().getPrefix());
         if (httpAccessProvider == null) {
            String message = "Attempted to access an entity URL path ("
                        + view.toString() + ") for an entity (" + view.toString() + ") when there is no " 
                        + "access provider to handle the request for prefix (" + view.getEntityReference().getPrefix() + ")";
            throw new EntityException( message, view.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
         } else {
            handleClassLoaderAccess(httpAccessProvider, req, res, view.getEntityReference());
         }
      } else {
         handleClassLoaderAccess(evAccessProvider, req, res, view);
      }
   }


   /**
    * Wrap this in an appropriate classloader before handling the request to ensure we
    * do not get ugly classloader failures
    */
   @SuppressWarnings("deprecation")
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
    * Wrap this in an appropriate classloader before handling the request to ensure we
    * do not get ugly classloader failures
    */
   private void handleClassLoaderAccess(EntityViewAccessProvider accessProvider,
         HttpServletRequest req, HttpServletResponse res, EntityView view) {
      ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      try {
         ClassLoader newClassLoader = accessProvider.getClass().getClassLoader();
         // check to see if this access provider reports the correct classloader
         if (accessProvider instanceof ClassLoaderReporter) {
            newClassLoader = ((ClassLoaderReporter) accessProvider).getSuitableClassLoader();
         }
         Thread.currentThread().setContextClassLoader(newClassLoader);
         // send request to the access provider which will route it on to the correct entity world
         accessProvider.handleAccess(view, req, res);
      } finally {
         Thread.currentThread().setContextClassLoader(currentClassLoader);
      }
   }


   /**
    * stores the various xstream processors for handling the different types of data
    */
   private Map<String, XStream> xstreams = new HashMap<String, XStream>();
   /**
    * Internal method for processing an entity or collection into an output format,
    * Currently only handles JSON and XML correctly
    */
   public void encodeToResponse(HttpServletRequest req, HttpServletResponse res, EntityView ev) {
      String extension = ev.getExtension();
      if (extension == null) { extension = Outputable.HTML; }
      EntityReference ref = ev.getEntityReference();

      Object toEncode = null;
      boolean collection = false;
      if (ref.getId() == null) {
         collection = true;
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, CollectionResolvable.class);
         if (provider != null) {
            Search search = makeSearchFromRequest(req);
            toEncode = ((CollectionResolvable)provider).getEntities(ref, search);
         }         
      } else {
         toEncode = getEntityObjectOrBasic(ref);
      }
      if (toEncode == null) {
         throw new RuntimeException("Failed to encode data for entity (" + ref.toString() + "), entity object to encode could not be found");
      } else {
         String encoding = "text/plain";
         XStream encoder = null;
         if (Outputable.JSON.equals(extension)) {
            if (! xstreams.containsKey(extension)) {
               XStream x = new XStream(new JsonHierarchicalStreamDriver());
               x.registerConverter(new MapConverter(), 3);
               xstreams.put( extension, x );
            }
            encoder = xstreams.get(extension);
            encoding = "text/javascript";
         } else if (Outputable.XML.equals(extension)) {
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
    * 
    * @param reference a unique string representing an entity
    * @return the entity object for this reference or null if none can be retrieved
    */
   public Object getEntityObjectOrBasic(EntityReference ref) {
      Object entity = getEntityObject(ref);
      if (entity == null) {
         // return basic entity information
         entity = new BasicEntity(ref);
      }
      return entity;
   }

   /**
    * Checks to see if an array contains a value,
    * will return false if a null value is supplied
    * 
    * @param <T>
    * @param array any array of objects
    * @param value the value to check for
    * @return true if the value is found, false otherwise
    */
   public static <T> boolean contains(T[] array, T value) {
      boolean foundValue = false;
      if (value != null) {
         for (int i = 0; i < array.length; i++) {
            if (value.equals(array[i])) {
               foundValue = true;
               break;
            }
         }
      }
      return foundValue;
   }

}
