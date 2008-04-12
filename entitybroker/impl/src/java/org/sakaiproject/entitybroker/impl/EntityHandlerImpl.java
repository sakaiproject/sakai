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
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.EntityViewUrlCustomizable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.InputTranslatable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputFormattable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestHandler;
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
    * @param viewKey the specific view type to get the URL for,
    * can be null to determine the key automatically
    * @param extension the optional extension to add to the end,
    * can be null to use no extension
    * @return the full URL to a specific entity or space
    */
   public String getEntityURL(String reference, String viewKey, String extension) {
      // ensure this is a valid reference first
      EntityReference ref = parseReference(reference);
      EntityView view = new EntityView();
      EntityViewUrlCustomizable custom = (EntityViewUrlCustomizable) entityProviderManager
            .getProviderByPrefixAndCapability(ref.getPrefix(), EntityViewUrlCustomizable.class);
      if (custom == null) {
         view.setEntityReference(ref);
      } else {
         // use the custom parsing templates
         view.loadParseTemplates( custom.getParseTemplates() );
      }
      view.setEntityReference(ref);
      if (viewKey != null) {
         view.setViewKey(viewKey);
      }
      if (extension != null) {
         view.setExtension(extension);
      }
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
   public EntityView parseEntityURL(String entityURL) {
      EntityView view = null;
      // first get the prefix
      String prefix = EntityReference.getPrefix(entityURL);
      // get the basic provider to see if this prefix is valid
      EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
      if (provider != null) {
         // this prefix is valid so check for custom entity templates
         EntityViewUrlCustomizable custom = (EntityViewUrlCustomizable) entityProviderManager
               .getProviderByPrefixAndCapability(prefix, EntityViewUrlCustomizable.class);
         if (custom == null) {
            view = new EntityView(entityURL);
         } else {
            // use the custom parsing templates to build the object
            view = new EntityView();
            view.loadParseTemplates( custom.getParseTemplates() );
            view.parseEntityURL(entityURL);
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
         view = parseEntityURL(path);
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

         // check for provider handling of this request
         RequestHandler handler = (RequestHandler) entityProviderManager.getProviderByPrefixAndCapability(prefix, RequestHandler.class);
         if (handler != null) {
            // provider is handling this request
            handleClassLoaderAccess(handler, req, res, view);
         } else {
            // handle the request internally if possible

            // identify the type of request (input or output) and the action (will be encoded in the viewKey)
            boolean output = false;
            String method = req.getMethod() == null ? "GET" : req.getMethod().toUpperCase().trim();
            if ("GET".equals(method)) {
               output = true;
            } else {
               // identify the action based on the method type or "_method" attribute
               if ("DELETE".equals(method)) {
                  view.setViewKey(EntityView.VIEW_DELETE);
               } else if ("PUT".equals(method)) {
                  view.setViewKey(EntityView.VIEW_EDIT);
               } else if ("POST".equals(method)) {
                  String _method = req.getParameter("_method");
                  if (_method == null) {
                     // this better be a create request
                     view.setViewKey(EntityView.VIEW_NEW);
                  } else {
                     _method = _method.toUpperCase().trim();
                     if ("DELETE".equals(_method)) {
                        view.setViewKey(EntityView.VIEW_DELETE);
                     } else if ("PUT".equals(_method)) {
                        view.setViewKey(EntityView.VIEW_EDIT);
                     } else {
                        throw new EntityException("Unable to handle POST request with _method, unknown method (only PUT/DELETE allowed): " + _method, 
                              view.toString(), HttpServletResponse.SC_BAD_REQUEST);                        
                     }
                  }
               } else {
                  throw new EntityException("Unable to handle request method, unknown method (only GET/POST/PUT/DELETE allowed): " + method, 
                        view.toString(), HttpServletResponse.SC_BAD_REQUEST);
               }

               // check that the request is valid (edit and delete require an entity id)
               if ( (EntityView.VIEW_EDIT.equals(view.getViewKey()) || EntityView.VIEW_DELETE.equals(view.getViewKey()) )
                     && view.getEntityReference().getId() == null) {
                  throw new EntityException("Unable to handle entity ("+prefix+") edit or delete request without entity id", 
                        view.toString(), HttpServletResponse.SC_BAD_REQUEST);                     
               }
            }

            boolean handled = false;
            if (output) {
               // output request
               Outputable outputable = (Outputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
               if (outputable != null) {
                  if ( contains(outputable.getHandledOutputFormats(), view.getExtension()) ) {
                     // we are handling this type of format for this entity
                     res.setCharacterEncoding("UTF-8");
                     String encoding = null;
                     if (Outputable.XML.equals(view.getExtension())) {
                        encoding = Outputable.XML_MIME_TYPE;
                     } else if (Outputable.HTML.equals(view.getExtension())) {
                        encoding = Outputable.HTML_MIME_TYPE;
                     } else if (Outputable.JSON.equals(view.getExtension())) {
                        encoding = Outputable.JSON_MIME_TYPE;
                     } else if (Outputable.RSS.equals(view.getExtension())) {
                        encoding = Outputable.RSS_MIME_TYPE;                        
                     } else if (Outputable.ATOM.equals(view.getExtension())) {
                        encoding = Outputable.ATOM_MIME_TYPE;                        
                     } else {
                        encoding = Outputable.TXT_MIME_TYPE;
                     }
                     res.setContentType(encoding);

                     OutputFormattable formattable = (OutputFormattable) entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
                     if (formattable == null) {
                        // handle internally or fail
                        encodeToResponse(view, req, res);
                     } else {
                        // use provider's formatter
                        try {
                           formattable.formatOutput(view.getEntityReference(), view.getExtension(), res.getOutputStream());
                        } catch (IOException e) {
                           throw new RuntimeException("Failed to get outputstream from response: " + view.toString() + ":" + e.getMessage(), e);
                        }
                     }
                     res.setStatus(HttpServletResponse.SC_OK);
                     handled = true;
                  } else {
                     // will not handle this format type
                     throw new EntityException( "Will not handle output request for format  "+view.getExtension()+" for this path (" 
                           + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference().toString() + ")", 
                           view.getEntityReference().toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
                  }
               }
            } else {
               // input request
               if (EntityView.VIEW_DELETE.equals(view.getViewKey())) {
                  // delete request
                  Deleteable deleteable = (Deleteable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Deleteable.class);
                  if (deleteable != null) {
                     deleteable.deleteEntity(view.getEntityReference());
                     res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                     handled = true;
                  }
               } else {
                  // save request
                  Inputable inputable = (Inputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class);
                  if (inputable != null) {
                     if ( contains(inputable.getHandledInputFormats(), view.getExtension()) ) {
                        // we are handling this type of format for this entity
                        InputTranslatable translatable = (InputTranslatable) entityProviderManager.getProviderByPrefixAndCapability(prefix, InputTranslatable.class);
                        Object entity = null;
                        if (translatable == null) {
                           // use internal translators or fail
                           // TODO add internal translators
                           throw new UnsupportedOperationException("Not implemented yet");
                        } else {
                           // use provider's translator
                           try {
                              entity = translatable.translateFormattedData(view.getEntityReference(), 
                                    view.getExtension(), req.getInputStream());
                           } catch (IOException e) {
                              throw new RuntimeException("Failed to get inputstream from request: " + view.toString() + ":" + e.getMessage(), e);
                           }
                        }

                        if (entity == null) {
                           throw new EntityException("Unable to save entity ("+view.getEntityReference()+"), entity object was null", 
                                 view.toString(), HttpServletResponse.SC_BAD_REQUEST);
                        } else {
                           if (EntityView.VIEW_NEW.equals(view.getViewKey())) {
                              String createdId = inputable.createEntity(view.getEntityReference(), entity);
                              view.setEntityReference( new EntityReference(prefix, createdId) ); // update the entity view
                              res.setStatus(HttpServletResponse.SC_CREATED);
                           } else if (EntityView.VIEW_EDIT.equals(view.getViewKey())) {
                              inputable.updateEntity(view.getEntityReference(), entity);
                              res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                           } else {
                              throw new EntityException("Unable to handle entity input ("+view.getEntityReference()+"), " +
                              		"action was not understood: " + view.getViewKey(), 
                                    view.toString(), HttpServletResponse.SC_BAD_REQUEST);
                           }
                           // return the location of this updated or created entity (without any extension)
                           res.setHeader("Location", view.getEntityURL(EntityView.VIEW_SHOW, null));
                           handled = true;
                        }
                     } else {
                        // will not handle this format type
                        throw new EntityException( "Will not handle input request for format  "+view.getExtension()+" for this path (" 
                              + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference().toString() + ")", 
                              view.getEntityReference().toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
                     }
                  }
               }
            }

            if (! handled) {
               // default handling, send to the access provider if there is one
               handleAccessProvider(view, req, res);
            } else {
               // cannot handle this request
               throw new EntityException( "Will not handle request for format  "+view.getExtension()+" for this path (" 
                     + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference().toString() + ")", 
                     view.getEntityReference().toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
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
   public void encodeToResponse(EntityView ev, HttpServletRequest req, HttpServletResponse res) {
      String extension = ev.getExtension();
      if (extension == null) { extension = Outputable.HTML; }
      EntityReference ref = ev.getEntityReference();

      Object toEncode = null;
      boolean collection = false;
      if (EntityView.VIEW_LIST.equals(ev.getViewKey())) {
         collection = true;
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.prefix, CollectionResolvable.class);
         if (provider != null) {
            Search search = makeSearchFromRequest(req);
            toEncode = ((CollectionResolvable)provider).getEntities(ref, search);
         }         
      } else {
         toEncode = getEntityObject(ref);
         if (toEncode == null) {
            // return basic entity information
            toEncode = new BasicEntity(ref);
         }
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
            encoding = "application/json";
         } else if (Outputable.XML.equals(extension)) {
            if (! xstreams.containsKey(extension)) {
               XStream x = new XStream(new DomDriver());
               x.registerConverter(new MapConverter(), 3);
               xstreams.put( extension, x );
            }
            encoder = xstreams.get(extension);
            encoding = "application/xml";
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

//   public class InternalOutputTranslator implements 

}
