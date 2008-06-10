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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
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
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;
import org.sakaiproject.entitybroker.entityprovider.extension.BasicEntity;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EncodingException;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestGetterImpl;
import org.sakaiproject.entitybroker.impl.util.EntityXStream;
import org.sakaiproject.entitybroker.util.ClassLoaderReporter;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.reflect.ReflectUtil;

import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.XppDomDriver;

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

   private static final String POST_METHOD = "_method";

   private static Log log = LogFactory.getLog(EntityHandlerImpl.class);

   // must be the same as the string in DirectServlet with the same name
   private static final String ORIGINAL_METHOD = "_originalMethod";
   
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

   private ReflectUtil reflectUtil = new ReflectUtil();
   public ReflectUtil getReflectUtil() {
      return reflectUtil;
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
      if (entityProviderManager.getProviderByPrefix(prefix) != null) {
         ReferenceParseable provider = entityProviderManager.getProviderByPrefixAndCapability(prefix, ReferenceParseable.class);
         if (provider == null) {
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
                  if (POST_METHOD.equals(key) 
                        || ORIGINAL_METHOD.equals(key)) {
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

      String handledReference = null;

      // special handling for describe or empty path
      if (path == null || "".equals(path) || "/".equals(path) 
            || path.startsWith(EntityReference.SEPARATOR + DESCRIBE)) {
         String format = TemplateParseUtil.findExtension(path)[2];
         String output = makeDescribeAll(format);
         try {
            res.getWriter().write(output);
         } catch (IOException e) {
            throw new RuntimeException("Failed to put output into the response writer: " + e.getMessage(), e);
         }
         res.setStatus(HttpServletResponse.SC_OK);
         handledReference = EntityView.SEPARATOR+"";
      } else {

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
         } else if ( DESCRIBE.equals(view.getEntityReference().getId()) ) {
            // Special handling for describe URLs
            String format = TemplateParseUtil.findExtension(path)[2];
            String entityId = req.getParameter("_id");
            if (entityId == null || "".equals(entityId)) {
               entityId = FAKE_ID;
            }
            String output = makeDescribeEntity(view.getEntityReference().getPrefix(), entityId, format, true, null);
            try {
               res.getWriter().write(output);
            } catch (IOException e) {
               throw new RuntimeException("Failed to put output into the response writer: " + e.getMessage(), e);
            }
            res.setStatus(HttpServletResponse.SC_OK);
            handledReference = view.getEntityReference().getSpaceReference() + EntityView.SEPARATOR + DESCRIBE;
         } else if (! entityExists(view.getEntityReference()) ) {
            // invalid entity reference (entity does not exist)
            throw new EntityException( "Attempted to access an entity URL path (" + path + ") for an entity ("
                  + view.getEntityReference() + ") that does not exist", 
                  view.getEntityReference()+"", HttpServletResponse.SC_NOT_FOUND );
         } else {
            // reference successfully parsed
            String prefix = view.getEntityReference().getPrefix();
            res.setStatus(HttpServletResponse.SC_OK); // other things can switch this later on

            // check for extensions
            if (view.getExtension() == null) {
               view.setExtension(Formats.HTML); // update the view
            }
            req.setAttribute("extension", view.getExtension());

            // store the current request and response
            ((RequestGetterImpl) requestGetter).setRequest(req);
            ((RequestGetterImpl) requestGetter).setResponse(res);

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
               // this fails because the original post gets lost therefore we are giving up on this for now
//               // check to see if the original method value was set
//               if (req.getAttribute(ORIGINAL_METHOD) != null) {
//                  method = (String) req.getAttribute(ORIGINAL_METHOD);
//               }
               if ("GET".equals(method)) {
                  output = true;
               } else {
                  // identify the action based on the method type or "_method" attribute
                  if ("DELETE".equals(method)) {
                     view.setViewKey(EntityView.VIEW_DELETE);
                  } else if ("PUT".equals(method)) {
                     view.setViewKey(EntityView.VIEW_EDIT);
                  } else if ("POST".equals(method)) {
                     String _method = req.getParameter(POST_METHOD);
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
                                 view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);                        
                        }
                     }
                  } else {
                     throw new EntityException("Unable to handle request method, unknown method (only GET/POST/PUT/DELETE allowed): " + method, 
                           view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);
                  }

                  // check that the request is valid (edit and delete require an entity id)
                  if ( (EntityView.VIEW_EDIT.equals(view.getViewKey()) || EntityView.VIEW_DELETE.equals(view.getViewKey()) )
                        && view.getEntityReference().getId() == null) {
                     throw new EntityException("Unable to handle entity ("+prefix+") edit or delete request without entity id, url=" 
                           + view.getOriginalEntityUrl(), 
                           view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);
                  }
               }

               boolean handled = false;
               try {
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

                           // get the entities to output
                           Search search = makeSearchFromRequest(req);
                           List<?> entities = fetchEntityList(view.getEntityReference(), search);
                           OutputStream outputStream = null;
                           try {
                              outputStream = res.getOutputStream();
                           } catch (IOException e) {
                              throw new RuntimeException("Failed to get output stream from response: " + view.getEntityReference(), e);
                           }

                           OutputFormattable formattable = (OutputFormattable) entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
                           if (formattable == null) {
                              // handle internally or fail
                              internalOutputFormatter(view.getEntityReference(), view.getExtension(), entities, outputStream, view);
                           } else {
                              // use provider's formatter
                              formattable.formatOutput(view.getEntityReference(), view.getExtension(), entities, outputStream);
                           }
                           res.setStatus(HttpServletResponse.SC_OK);
                           handled = true;
                        } else {
                           // will not handle this format type
                           throw new EntityException( "Will not handle output request for format  "+view.getExtension()+" for this path (" 
                                 + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference() + ")", 
                                 view.getEntityReference()+"", HttpServletResponse.SC_METHOD_NOT_ALLOWED );
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
                              Object entity = null;
                              InputStream inputStream = null;
                              try {
                                 inputStream = req.getInputStream();
                              } catch (IOException e) {
                                 throw new RuntimeException("Failed to get output stream from response: " + view.getEntityReference(), e);
                              }

                              InputTranslatable translatable = (InputTranslatable) entityProviderManager.getProviderByPrefixAndCapability(prefix, InputTranslatable.class);
                              if (translatable == null) {
                                 // use internal translators or fail
                                 entity = internalInputTranslator(view.getEntityReference(), 
                                       view.getExtension(), inputStream, req);
                              } else {
                                 // use provider's translator
                                 entity = translatable.translateFormattedData(view.getEntityReference(), 
                                       view.getExtension(), inputStream);
                              }

                              if (entity == null) {
                                 throw new EntityException("Unable to save entity ("+view.getEntityReference()+"), entity object was null", 
                                       view.toString(), HttpServletResponse.SC_BAD_REQUEST);
                              } else {
                                 if (EntityView.VIEW_NEW.equals(view.getViewKey())) {
                                    String createdId = inputable.createEntity(view.getEntityReference(), entity);
                                    view.setEntityReference( new EntityReference(prefix, createdId) ); // update the entity view
                                    res.setHeader(EntityRequestHandler.HEADER_ENTITY_ID, createdId);
                                    res.setStatus(HttpServletResponse.SC_CREATED);
                                 } else if (EntityView.VIEW_EDIT.equals(view.getViewKey())) {
                                    inputable.updateEntity(view.getEntityReference(), entity);
                                    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                 } else {
                                    throw new EntityException("Unable to handle entity input ("+view.getEntityReference()+"), " +
                                          "action was not understood: " + view.getViewKey(), 
                                          view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);
                                 }
                                 // return the location of this updated or created entity (without any extension)
                                 res.setHeader(EntityRequestHandler.HEADER_ENTITY_URL, view.getEntityURL(EntityView.VIEW_SHOW, null));
                                 res.setHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE, view.getEntityReference().toString());
                                 handled = true;
                              }
                           } else {
                              // will not handle this format type
                              throw new EntityException( "Will not handle input request for format  "+view.getExtension()+" for this path (" 
                                    + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference().toString() + ")", 
                                    view.getEntityReference()+"", HttpServletResponse.SC_METHOD_NOT_ALLOWED );
                           }
                        }
                     }
                  }
               } catch (IllegalArgumentException e) {
                  // translate IAE into EE - bad request
                  throw new EntityException("IllegalArgumentException: Unable to handle " + (output ? "output" : "input") + " request url ("
                        + view.getOriginalEntityUrl()+"), " + e.getMessage(),
                        view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);        
               } catch (IllegalStateException e) {
                  // translate ISE into EE - internal server error
                  throw new EntityException("IllegalStateException: Unable to handle " + (output ? "output" : "input") + " request url ("
                        + view.getOriginalEntityUrl()+"), " + e.getMessage(),
                        view.getEntityReference()+"", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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

            // clear the request getter
            ((RequestGetterImpl) requestGetter).setRequest(null);
            ((RequestGetterImpl) requestGetter).setResponse(null);

            handledReference = view.getEntityReference().toString();
         }
      }
      return handledReference;
   }

   /**
    * Generate a description of all entities in the system,
    * this is only available as XML and XHTML
    * 
    * @param format XML or HTML (default is HTML)
    * @return the description string for all known entities
    */
   protected String makeDescribeAll(String format) {
      Map<String, List<Class<? extends EntityProvider>>> map = entityProviderManager.getRegisteredEntityCapabilities();
      String describeURL = makeFullURL("") + EntityView.SEPARATOR + DESCRIBE;
      String output = "";
      if (Formats.XML.equals(format)) {
         // XML available in case someone wants to parse this in javascript or whatever
         StringBuilder sb = new StringBuilder();
         sb.append("<describe>\n");
         sb.append("  <describeURL>" + describeURL + "</describeURL>\n");
         sb.append("  <prefixes>\n");
         ArrayList<String> prefixes = new ArrayList<String>(map.keySet());
         Collections.sort(prefixes);
         for (int i = 0; i < prefixes.size(); i++) {
            String prefix = prefixes.get(i);
            sb.append( makeDescribeEntity(prefix, FAKE_ID, format, false, map.get(prefix)) );
         }
         sb.append("  </prefixes>\n");
         sb.append("</describe>\n");
         output = sb.toString();
      } else {
         // just do HTML if not one of the handled ones
         StringBuilder sb = new StringBuilder();
         sb.append("<h1><a href='"+ describeURL +"'>Describe all</a> registered entities"
               + makeFormatUrlHtml(describeURL, Formats.XML) +"</h1>\n");
         sb.append("  <i>RESTful URLs: <a href='http://microformats.org/wiki/rest/urls'>http://microformats.org/wiki/rest/urls</a></i><br/>\n");
         sb.append("  <h2>Registered prefixes (entity types): "+map.size()+"</h2>\n");
         ArrayList<String> prefixes = new ArrayList<String>(map.keySet());
         Collections.sort(prefixes);
         for (int i = 0; i < prefixes.size(); i++) {
            String prefix = prefixes.get(i);
            sb.append( makeDescribeEntity(prefix, FAKE_ID, format, false, map.get(prefix)) );
         }
         output = sb.toString();
      }
      return output;
   }

   /**
    * Generate a description of an entity type
    * 
    * @param prefix an entity prefix
    * @param id the entity id to use for generating URLs
    * @param format a format to output, HTML and XML supported
    * @param extra if true then include URLs and extra data, if false then include basic info only
    * @param caps (optional) a list of capabilities, this will be looked up if this is null
    * @return the description string
    */
   protected String makeDescribeEntity(String prefix, String id, String format, boolean extra, List<Class<? extends EntityProvider>> caps) {
      if (caps == null) {
         caps = entityProviderManager.getPrefixCapabilities(prefix);
      }
      String directUrl = makeFullURL("");
      StringBuilder sb = new StringBuilder();
      if (Formats.XML.equals(format)) {
         // XML available in case someone wants to parse this in javascript or whatever
         String describePrefixUrl = directUrl + "/" + prefix + "/" + DESCRIBE;
         sb.append("    <prefix>\n");
         sb.append("      <prefix>" + prefix + "</prefix>\n");
         sb.append("      <describeURL>" + describePrefixUrl + "</describeURL>\n");
         if (extra) {
            // URLs
            EntityView ev = makeEntityView(new EntityReference(prefix, id), null, null);
            if (caps.contains(CollectionResolvable.class)) {
               sb.append("      <collectionURL>" + ev.getEntityURL(EntityView.VIEW_LIST, null) + "</collectionURL>\n");
            }
            if (caps.contains(Createable.class)) {
               sb.append("      <createURL>" + ev.getEntityURL(EntityView.VIEW_NEW, null) + "</createURL>\n");
            }
            sb.append("      <showURL>" + ev.getEntityURL(EntityView.VIEW_SHOW, null) + "</showURL>\n");
            if (caps.contains(Updateable.class)) {
               sb.append("      <updateURL>" + ev.getEntityURL(EntityView.VIEW_EDIT, null) + "</updateURL>\n");
            }
            if (caps.contains(Deleteable.class)) {
               sb.append("      <deleteURL>" + ev.getEntityURL(EntityView.VIEW_DELETE, null) + "</deleteURL>\n");
            }
            // Formats
            String[] outputFormats = getFormats(prefix, true);
            sb.append("      <outputFormats>\n");
            for (int i = 0; i < outputFormats.length; i++) {
               sb.append("        <format>"+outputFormats[i]+"</format>\n");               
            }
            sb.append("      </outputFormats>\n");
            String[] inputFormats = getFormats(prefix, false);
            sb.append("      <inputFormats>\n");
            for (int i = 0; i < inputFormats.length; i++) {
               sb.append("        <format>"+inputFormats[i]+"</format>\n");               
            }
            sb.append("      </inputFormats>\n");
            // Resolvable Entity Info
            Object entity = getSampleEntityObject(prefix);
            if (entity != null) {
               sb.append("      <entityClass>\n");
               sb.append("        <class>"+ entity.getClass().getName() +"</class>\n");
               Map<String, Class<?>> entityTypes = reflectUtil.getFieldTypes(entity.getClass());
               ArrayList<String> keys = new ArrayList<String>(entityTypes.keySet());
               Collections.sort(keys);
               for (String key : keys) {
                  Class<?> type = entityTypes.get(key);
                  sb.append("        <"+ key +">"+ type.getName() +"</"+key+">\n");
               }
               sb.append("      </entityClass>\n");
            }
         }
         sb.append("      <capabilities>\n");
         for (Class<? extends EntityProvider> class1 : caps) {
            sb.append("        <capability>"+class1.getName()+"</capability>\n");
         }
         sb.append("      </capabilities>\n");
         sb.append("    </prefix>\n");
      } else {
         // just do HTML if not one of the handled ones
         String describePrefixUrl = directUrl + "/" + prefix + "/" + DESCRIBE;
         sb.append("    <h3><a href='"+describePrefixUrl+"'>"+prefix+"</a>"
               + makeFormatUrlHtml(describePrefixUrl, Formats.XML) +"</h3>\n");
         if (extra) {
            sb.append("      <i>RESTful URLs: <a href='http://microformats.org/wiki/rest/urls'>http://microformats.org/wiki/rest/urls</a></i><br/>\n");
            String[] outputFormats = getFormats(prefix, true);
            // URLs
            EntityView ev = makeEntityView(new EntityReference(prefix, id), null, null);
            String url = "";
            sb.append("      <h4>Sample Entity URLs (_id='"+id+"') [may not be valid]:</h4>\n");
            sb.append("        <ul>\n");
            if (caps.contains(CollectionResolvable.class)) {
               url = ev.getEntityURL(EntityView.VIEW_LIST, null);
               sb.append("          <li>Entity Collection URL: <a href='"+ directUrl+url +"'>"+url+"<a/>"
                     + makeFormatsUrlHtml(directUrl+url, outputFormats) +"</li>\n");
            }
            if (caps.contains(Createable.class)) {
               url = ev.getEntityURL(EntityView.VIEW_NEW, null);
               sb.append("          <li>Create Entity URL: <a href='"+ directUrl+url +"'>"+url+"<a/></li>\n");
            }
            url = ev.getEntityURL(EntityView.VIEW_SHOW, null);
            sb.append("          <li>Show Entity URL: <a href='"+ directUrl+url +"'>"+url+"<a/>"
                     + makeFormatsUrlHtml(directUrl+url, outputFormats) +"</li>\n");
            if (caps.contains(Updateable.class)) {
               url = ev.getEntityURL(EntityView.VIEW_EDIT, null);
               sb.append("          <li>Update Entity URL: <a href='"+ directUrl+url +"'>"+url+"<a/></li>\n");
            }
            if (caps.contains(Deleteable.class)) {
               url = ev.getEntityURL(EntityView.VIEW_DELETE, null);
               sb.append("          <li>Delete Entity URL: <a href='"+ directUrl+url +"'>"+url+"<a/></li>\n");
            }
            sb.append("        </ul>\n");
            // Formats
            sb.append("      <h4>Output formats : "+ makeFormatsString(outputFormats) +"</h4>\n");
            String[] inputFormats = getFormats(prefix, false);
            sb.append("      <h4>Input formats : "+ makeFormatsString(inputFormats) +"</h4>\n");
            // Resolvable Entity Info
            Object entity = getSampleEntityObject(prefix);
            if (entity != null) {
               sb.append("      <h4>Entity class : "+ entity.getClass().getName() +"</h4>\n");
               sb.append("        <ul>\n");
               Map<String, Class<?>> entityTypes = reflectUtil.getFieldTypes(entity.getClass());
               ArrayList<String> keys = new ArrayList<String>(entityTypes.keySet());
               Collections.sort(keys);
               for (String key : keys) {
                  Class<?> type = entityTypes.get(key);
                  sb.append("          <li>"+ key +" : "+ type.getName() +"</li>\n");                  
               }
               sb.append("        </ul>\n");
            }
         }
         sb.append("      <h4 style='font-style: italic;'>Capabilities: "+caps.size()+"</h4>\n");
         sb.append("        <ol>\n");
         for (Class<? extends EntityProvider> class1 : caps) {
            sb.append("          <li>"+class1.getName()+"</li>\n");
         }
         sb.append("        </ol>\n");
      }
      return sb.toString();
   }

   // DESCRIBE formatting utilities

   private String[] getFormats(String prefix, boolean output) {
      String[] formats;
      try {
         if (output) {
            formats = entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class).getHandledOutputFormats();
         } else {
            formats = entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class).getHandledInputFormats();
         }
      } catch (NullPointerException e) {
         formats = new String[] {};
      }
      if (formats == null) {
         formats = new String[] {};
      }
      return formats;
   }

   protected String makeFormatsUrlHtml(String url, String[] formats) {
      StringBuilder sb = new StringBuilder();
      if (formats != null) {
         for (String format : formats) {
            sb.append( makeFormatUrlHtml(url, format) );
         }
      }
      return sb.toString();
   }

   protected String makeFormatsString(String[] formats) {
      String s = ReflectUtil.arrayToString( formats );
      if ("".equals(s)) {
         s = "<i>NONE</i>";
      }
      return s;
   }

   protected String makeFormatUrlHtml(String url, String format) {
      return " (<a href='"+url+"."+format+"'>"+format+"</a>)";
   }

   /**
    * Safely get the sample entity object for descriptions
    */
   protected Object getSampleEntityObject(String prefix) {
      Object entity = null;
      try {
         Resolvable resolvable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Resolvable.class);
         if (resolvable != null) {
            entity = resolvable.getEntity(new EntityReference(prefix, ""));
         }
      } catch (RuntimeException e) {
         entity = null;
      }
      if (entity == null) {
         try {
            Createable createable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Createable.class);
            if (createable != null) {
               entity = createable.getSampleEntity();
            }
         } catch (RuntimeException e) {
            entity = null;
         }
      }
      return entity;
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
            // classloader protection START
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
               Object thing = httpAccessProvider;
               ClassLoader newClassLoader = thing.getClass().getClassLoader();
               // check to see if this access provider reports the correct classloader
               if (thing instanceof ClassLoaderReporter) {
                  newClassLoader = ((ClassLoaderReporter) thing).getSuitableClassLoader();
               }
               Thread.currentThread().setContextClassLoader(newClassLoader);
               // send request to the access provider which will route it on to the correct entity world
               httpAccessProvider.handleAccess(req, res, view.getEntityReference());
            } finally {
               Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
            // classloader protection END
         }
      } else {
         handleClassLoaderAccess(evAccessProvider, req, res, view);
      }
   }

   /**
    * Wrap this in an appropriate classloader before handling the request to ensure we
    * do not get ugly classloader failures
    */
   private void handleClassLoaderAccess(EntityViewAccessProvider accessProvider,
         HttpServletRequest req, HttpServletResponse res, EntityView view) {
      // START classloader protection
      ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      try {
         Object classloaderIndicator = accessProvider;
         ClassLoader newClassLoader = classloaderIndicator.getClass().getClassLoader();
         // check to see if this access provider reports the correct classloader
         if (classloaderIndicator instanceof ClassLoaderReporter) {
            newClassLoader = ((ClassLoaderReporter) classloaderIndicator).getSuitableClassLoader();
         }
         Thread.currentThread().setContextClassLoader(newClassLoader);
         // START run in classloader
         accessProvider.handleAccess(view, req, res);
         // END run in classloader
      } finally {
         Thread.currentThread().setContextClassLoader(currentClassLoader);
      }
      // END classloader protection
   }


   /**
    * Handled the internal encoding of data into an entity object
    * 
    * @param ref the entity reference
    * @param format the format which the input is encoded in
    * @param input the data being input
    * @return the entity object based on the data
    * @throws EntityException if there is a failure in translation
    */
   @SuppressWarnings("unchecked")
   public Object internalInputTranslator(EntityReference ref, String format, InputStream input, HttpServletRequest req) {
      Object entity = null;

      // get the encoder to use
      EntityXStream encoder = getEncoderForFormat(format, false);

      Inputable inputable = (Inputable) entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Inputable.class);
      if (inputable != null) {
         // get a the current entity object or a sample
         Object current = null;
         if (ref.getId() == null) {
            // get a sample
            current = inputable.getSampleEntity();
         } else {
            // get the current entity
            current = inputable.getEntity(ref);
         }

         if (current != null) {
            if (Formats.HTML.equals(format) || format == null || "".equals(format)) {
               // html req handled specially
               if (req != null) {
                  Map<String, String[]> params = req.getParameterMap();
                  if (params != null && params.size() > 0) {
                     entity = current;
                     try {
                        reflectUtil.populateFromParams(entity, params);
                     } catch (Exception e) {
                        throw new IllegalArgumentException("Unable to populate bean for ref ("+ref+") from request: " + e.getMessage(), e);
                     }
                  } else {
                     // no request params, bad request
                     throw new EntityException("No request params for html input request (there must be at least one) for reference: " + ref, 
                           ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
                  }
               }
            } else if (encoder != null) {
               if (input == null) {
                  // no request params, bad request
                  throw new EntityException("No input for input translation (input cannot be null) for reference: " + ref, 
                        ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
               } else {
                  encoder.alias(ref.getPrefix(), current.getClass());
                  // START classloader protection
                  ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                  try {
                     Object classloaderIndicator = current;
                     ClassLoader newClassLoader = classloaderIndicator.getClass().getClassLoader();
                     encoder.setClassLoader(newClassLoader);
                     // translate using the encoder
                     entity = encoder.fromXML(input, current);
                     // END run in classloader
                  } catch (RuntimeException e) {
                     throw new EncodingException("Failure during internal input encoding of entity: " + ref, ref.toString(), e);
                  } finally {
                     encoder.setClassLoader(currentClassLoader);
                  }
                  // END classloader protection
               }
            }
         }
      } else {
         throw new IllegalArgumentException("This entity ("+ref+") does not allow input translation");
      }

      if (entity == null) {
         throw new EntityException("Unable to encode entity from input for reference: " + ref, ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
      }
      return entity;
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
      if (entities.isEmpty()) {
         // just log this for now
         log.info("No entities to format ("+format+") and output for ref (" + ref + ")");
      }

      // get the encoder to use
      EntityXStream encoder = getEncoderForFormat(format, true);

      String encoded = null;
      if (EntityView.VIEW_LIST.equals(view.getViewKey()) 
            || ref.getId() == null) {
         // encoding a collection of entities
         if (encoder != null) {
            Class<?> entityClass = ReflectUtil.getClassFromCollection((Collection<?>)entities);
            encoder.alias(ref.prefix, entityClass);
            StringBuilder sb = new StringBuilder();
            // make header
            if (Formats.JSON.equals(format)) {
               sb.append("{\""+ENTITY_PREFIX+"\": \""+ref.getPrefix() + "\", \"" + ref.getPrefix() + COLLECTION + "\": [\n");
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
            throw new EncodingException("Failed to encode data for entity (" + ref 
                  + "), entity object to encode could not be found", ref.toString());
         } else {
            Class<?> encodeClass = toEncode.getClass();
            if (encoder != null) {
               encoder.alias(ref.getPrefix(), encodeClass); // add alias for the current entity prefix
            }
            try {
               encoded = encodeEntity(ref, workingView, toEncode, encoder);
            } catch (RuntimeException e) {
               throw new EncodingException("Failure during internal output encoding of entity: " + ref, ref.toString(), e);
            }
         }
      }
      // put the encoded data into the OS
      try {
         byte[] b = encoded.getBytes(UTF_8);
         output.write(b);
      } catch (UnsupportedEncodingException e) {
         throw new EncodingException("Failed to encode UTF-8: " + ref, ref.toString(), e);
      } catch (IOException e) {
         throw new EncodingException("Failed to encode into output stream: " + ref, ref.toString(), e);
      }
   }


   /**
    * stores the various xstream processors for handling the different types of data
    */
   private Map<String, EntityXStream> xstreams = new HashMap<String, EntityXStream>();
   /**
    * @param format
    * @param output if true then get the encode for output, if false then for input
    * @return the appropriate encoder for the format
    */
   private EntityXStream getEncoderForFormat(String format, boolean output) {
      EntityXStream encoder = null;
      if (Formats.JSON.equals(format)) {
         // http://jira.sakaiproject.org/jira/browse/SAK-13681
//       if (output) {
//          if (! xstreams.containsKey(format)) {
//             xstreams.put( format, new EntityXStream(new JsonHierarchicalStreamDriver()) );
//          }
//       } else {
//          format += "-IN";
//          if (! xstreams.containsKey(format)) {
//             xstreams.put( format, new EntityXStream(new JettisonMappedXmlDriver()) );
//          }
//       }
       if (! xstreams.containsKey(format)) {
          xstreams.put( format, new EntityXStream(new JettisonMappedXmlDriver()) );
       }
       encoder = xstreams.get(format);
      } else if (Formats.XML.equals(format)) {
         if (! xstreams.containsKey(format)) {
            xstreams.put( format, new EntityXStream(new XppDomDriver()) );
         }
         encoder = xstreams.get(format);
      } else if (Formats.TXT.equals(format)) {
         // TODO Add in plaintext encoder/decoder
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
               entityId = reflectUtil.getFieldValueAsString(toEncode, "id", EntityId.class);
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

}
