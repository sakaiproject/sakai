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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
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
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestGetterImpl;
import org.sakaiproject.entitybroker.impl.util.EntityXStream;
import org.sakaiproject.entitybroker.impl.util.ReflectUtil;
import org.sakaiproject.entitybroker.util.ClassLoaderReporter;

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

   private static Log log = LogFactory.getLog(EntityHandlerImpl.class);

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

   public static final String UTF_8 = Formats.UTF_8;
   private static final String ENTITY_PREFIX = "entityPrefix";
   private static final String COLLECTION = "-collection";

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
      EntityView view = makeEntityView(ref, viewKey, extension);
      String url = makeFullURL(view.toString());
      return url;
   }

   /**
    * Reduce code duplication and ensure custom templates are used
    */
   private EntityView makeEntityView(EntityReference ref, String viewKey, String extension) {
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
      return view;
   }

   /**
    * Make a full URL (http://....) from just a path URL (/prefix/id.xml)
    */
   private String makeFullURL(String pathURL) {
      String url = serverConfigurationService.getServerUrl() + "/direct" + pathURL;
      return url;
   }

   /**
    * @param ev an entity view
    * @return a copy of this entityView including the internal templates and parsed template cache
    */
   private EntityView makeEVfromEV(EntityView ev) {
      EntityView togo = new EntityView();
      EntityReference ref = ev.getEntityReference();
      togo.setEntityReference( new EntityReference(ref.getPrefix(), ref.getId() == null ? "" : ref.getId()) );
      togo.preloadParseTemplates( ev.getAnazlyzedTemplates() );
      togo.setExtension( ev.getExtension() );
      togo.setViewKey( ev.getViewKey() );
      return togo;
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
    * This looks at search parameters and returns anything it finds in the
    * request parameters that can be put into the search
    * 
    * @param req a servlet request
    * @return a search filter object
    */
   @SuppressWarnings("unchecked")
   public Search makeSearchFromRequest(HttpServletRequest req) {
      Search search = new Search();
      try {
         if (req != null) {
            Map<String, String[]> params = req.getParameterMap();
            if (params != null) {
               for (String key : params.keySet()) {
                  if ("_method".equals(key)) {
                     // skip the method
                     continue;
                  }
                  Object value = null;
                  String[] values = req.getParameterValues(key);
                  if (values == null) {
                     // in theory this should not happen
                     continue;
                  } else if (values.length > 1) {
                     value = values;
                  } else if (values.length == 1) {
                     value = values[0];
                  }
                  search.addRestriction( new Restriction(key, value) );
               }
            }
         }
      } catch (Exception e) {
         // failed to translate the request to a search, not really much to do here
         log.warn("Could not translate entity request into search params: " + e.getMessage(), e);
      }
      return search;
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

   /**
    * Get the list of entities based on a reference and supplied search,
    * passes through to the EP methods if available
    * 
    * @param ref an entity reference
    * @param search an optional search
    * @return the list of entities if they can be retrieved or null these entities cannot be resolved
    */
   @SuppressWarnings("unchecked")
   protected List<?> fetchEntityList(EntityReference ref, Search search) {
      List entities = null;
      if (ref.getId() == null) {
         // encoding a collection of entities
         EntityProvider provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), CollectionResolvable.class);
         if (provider != null) {
            entities = new ArrayList( ((CollectionResolvable)provider).getEntities(ref, search) );
         }
      } else {
         // encoding a single entity
         Object entity = getEntityObject(ref);
         if (entity == null) {
            throw new EntityException("Failed to retrieve entity (" + ref + "), entity object could not be found",
                  ref.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
         }
         entities = new ArrayList();
         entities.add(entity);
      }
      return entities;
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
                  if ( ReflectUtil.contains(outputable.getHandledOutputFormats(), view.getExtension()) ) {
                     // we are handling this type of format for this entity
                     res.setCharacterEncoding(UTF_8);
                     String encoding = null;
                     if (Formats.XML.equals(view.getExtension())) {
                        encoding = Formats.XML_MIME_TYPE;
                     } else if (Formats.HTML.equals(view.getExtension())) {
                        encoding = Formats.HTML_MIME_TYPE;
                     } else if (Formats.JSON.equals(view.getExtension())) {
                        encoding = Formats.JSON_MIME_TYPE;
                     } else if (Formats.RSS.equals(view.getExtension())) {
                        encoding = Formats.RSS_MIME_TYPE;                        
                     } else if (Formats.ATOM.equals(view.getExtension())) {
                        encoding = Formats.ATOM_MIME_TYPE;                        
                     } else {
                        encoding = Formats.TXT_MIME_TYPE;
                     }
                     res.setContentType(encoding);

                     OutputFormattable formattable = (OutputFormattable) entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
                     if (formattable == null) {
                        // handle internally or fail
                        Search search = makeSearchFromRequest(req);
                        List<?> entities = fetchEntityList(view.getEntityReference(), search);
                        try {
                           internalOutputFormatter(view.getEntityReference(), view.getExtension(), entities, res.getOutputStream(), view);
                        } catch (IOException e) {
                           throw new RuntimeException("Failed to get output stream from response: " + view.getEntityReference(), e);
                        }
                     } else {
                        // use provider's formatter
                        try {
                           formattable.formatOutput(view.getEntityReference(), view.getExtension(), null, res.getOutputStream());
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
                     if ( ReflectUtil.contains(inputable.getHandledInputFormats(), view.getExtension()) ) {
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
    * Format entities for output based on the reference into a format,
    * use the provided list or get the entities
    * 
    * @param ref the entity reference for this, 
    * if this is a reference to a collection then this will be rendered as a collection of entities,
    * if a reference to a single entity then only the matching one from the collection will be used
    * @param format the format to use for the output data
    * @param entities (optional) if this is null then the entities will be fetched
    * @param output the outputstream to place the encoded data into
    * @param view (optional) 
    */
   public void internalOutputFormatter(EntityReference ref, String format, List<?> entities, OutputStream output, EntityView view) {
      if (format == null) { format = Outputable.HTML; }
      if (view == null) {
         view = makeEntityView(ref, null, null);
      }
      // create a scrap view from the current view, this should be more efficient
      EntityView workingView = makeEVfromEV(view);

      // get the entities if not supplied
      if (entities == null) {
         entities = fetchEntityList(ref, new Search());
      }

      // get the encoder to use
      EntityXStream encoder = getEncoderForExtension(format);

      String encoded = null;
      if (entities.isEmpty()) {
         // nothing to do here but warn
         log.warn("No entities to format ("+format+") and output for: " + ref);
      } else if (EntityView.VIEW_LIST.equals(view.getViewKey()) 
            || ref.getId() == null) {
         // encoding a collection of entities
         if (encoder != null) {
            Class<?> entityClass = ReflectUtil.getClassFromCollection((Collection<?>)entities);
            encoder.alias(ref.prefix, entityClass);
            StringBuilder sb = new StringBuilder();
            // make header
            if (Formats.JSON.equals(format)) {
               sb.append("{\""+ENTITY_PREFIX+"\": "+ref.getPrefix() + ", \"" + ref.getPrefix() + COLLECTION + "\": [\n");
            } else { // assume XML
               sb.append("<" + ref.getPrefix() + COLLECTION + " " + ENTITY_PREFIX + "=\"" + ref.getPrefix() + "\">\n");
            }
            // loop through and encode items
            int encodedEntities = 0;
            for (Object entity : entities) {
               String encode = encodeEntity(ref, workingView, entity, encoder);
               if (encode.length() > 3) {
                  if (Formats.JSON.equals(format)) {
                     if (encodedEntities > 0) {
                        sb.append(",\n");
                     }
                     // special JSON cleanup (strips off the {"stuff": ... })
                     encode = encode.substring(encode.indexOf(':')+1, encode.length()-1);
                  } else {
                     if (encodedEntities > 0) {
                        sb.append("\n");
                     }
                  }
                  sb.append(encode);                     
                  encodedEntities++;
               }
            }
            // make footer
            if (Formats.JSON.equals(format)) {
               sb.append("\n]}");
            } else { // assume XML
               sb.append("\n</" + ref.getPrefix() + COLLECTION + ">");
            }
            encoded = sb.toString();
         } else {
            // just dump the whole thing to a string
            encoded = encodeEntity(ref, workingView, entities, null);
         }
      } else {
         // encoding a single entity
         Object toEncode = entities.get(0);
         if (toEncode == null) {
            throw new RuntimeException("Failed to encode data for entity (" + ref.toString() + "), entity object to encode could not be found");
         } else {
            Class<?> encodeClass = toEncode.getClass();
            if (encoder != null) {
               encoder.alias(ref.getPrefix(), encodeClass); // add alias for the current entity prefix
            }
            encoded = encodeEntity(ref, workingView, toEncode, encoder);
         }
      }
      // put the encoded data into the OS
      try {
         byte[] b = encoded.getBytes(UTF_8);
         output.write(b);
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException("Failed to encode UTF-8: " + ref.toString(), e);
      } catch (IOException e) {
         throw new RuntimeException("Failed to encode into output stream: " + ref.toString(), e);
      }
   }


   /**
    * stores the various xstream processors for handling the different types of data
    */
   private Map<String, EntityXStream> xstreams = new HashMap<String, EntityXStream>();
   /**
    * @param format
    * @return the appropriate encoder for the format
    */
   private EntityXStream getEncoderForExtension(String format) {
      EntityXStream encoder = null;
      if (Formats.JSON.equals(format)) {
         if (! xstreams.containsKey(format)) {
            xstreams.put( format, new EntityXStream(new JsonHierarchicalStreamDriver()) );
         }
         encoder = xstreams.get(format);
      } else if (Formats.XML.equals(format)) {
         if (! xstreams.containsKey(format)) {
            xstreams.put( format, new EntityXStream(new DomDriver()) );
         }
         encoder = xstreams.get(format);
      } else {
         encoder = null; // do a toString dump
      }
      return encoder;
   }

   /**
    * @param ref the entity reference
    * @param workingView this is a working view which can be changed around as needed to generate URLs,
    * always go to the EntityReference for original data
    * @param toEncode entity to encode
    * @param encoder enhanced xstream encoder or null if no encoder available
    * @return the encoded entity or "" if encoding fails
    */
   private String encodeEntity(EntityReference ref, EntityView workingView, Object toEncode, EntityXStream encoder) {
      String encoded = "";
         if (encoder != null) {
            // generate entity meta data
            Class<?> entityClass = toEncode.getClass();
            Map<String, Object> entityData = null;
            if (! BasicEntity.class.equals(entityClass)) {
               entityData = new HashMap<String, Object>();
               entityData.put(EntityXStream.EXTRA_DATA_CLASS, entityClass);
               String entityId = ref.getId();
               if (entityId == null) {
                  // try to get it from the toEncode object
                  entityId = ReflectUtil.getFieldValueAsString(toEncode, "id", EntityId.class);
               }
               if (entityId != null) {
                  entityData.put("ID", entityId);
                  workingView.setEntityReference( new EntityReference(ref.getPrefix(), entityId) );
                  String url = makeFullURL( workingView.getEntityURL(EntityView.VIEW_SHOW, null) );
                  entityData.put("URL", url);
               } else {
                  String url = makeFullURL( workingView.getEntityURL(EntityView.VIEW_LIST, null) );
                  entityData.put("URL", url);               
               }
            }

            // encode the object
            encoded = encoder.toXML(toEncode, entityData);
         } else {
            // just to string this and dump it out
            encoded = "<b>" + ref.getPrefix() + "</b>: " + toEncode.toString();
         }
      return encoded;
   }

   /**
    * Handled the internal encoding of data into an entity object
    * 
    * @param ev
    * @param req
    * @param res
    * @return
    * @throws EntityException if there is a failure in translation
    */
   public Object internalInputTranslator(EntityView ev, HttpServletRequest req, HttpServletResponse res) {
      Object entity = null;
      return entity;
   }

}
